package core.commands.abstracts;

import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.last.TokenExceptionHandler;
import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.ContextSlashReceived;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.*;
import core.imagerenderer.ChartQuality;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.HeavyCommandRateLimiter;
import dao.ChuuService;
import dao.ServiceView;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public abstract class MyCommand<T extends CommandParameters> implements EventListener {
    public final ConcurrentLastFM lastFM;
    protected final ChuuService db;
    protected final Parser<T> parser;
    private final CommandCategory category;
    private final boolean isLongRunningCommand;
    public boolean respondInPrivate = true;
    public boolean ephemeral = false;
    public boolean canAnswerFast = false;
    public int order = Integer.MAX_VALUE;

    protected MyCommand(ServiceView serviceview, boolean isLongRunningCommand) {
        this.isLongRunningCommand = isLongRunningCommand;
        lastFM = LastFMFactory.getNewInstance();
        this.db = serviceview.getView(isLongRunningCommand);
        this.parser = initParser();
        this.category = initCategory();
    }

    protected MyCommand(ServiceView serviceview) {
        this(serviceview, false);

    }

    private static void logCommand(ChuuService service, Context e, MyCommand<?> command, long exectTime, boolean success, boolean isNormalCommand) {
        service.logCommand(
                e.getAuthor().getIdLong(),
                e.isFromGuild() ? e.getGuild().getIdLong() : null,
                command.getName(),
                exectTime,
                Instant.now(),
                success, isNormalCommand);
    }

    protected abstract CommandCategory initCategory();

    public abstract Parser<T> initParser();

    public final Parser<T> getParser() {
        return parser;
    }

    public abstract String getDescription();

    public String getUsageInstructions() {
        return parser.getUsage(getAliases().get(0));
    }

    public abstract List<String> getAliases();

    @Override
    public void onEvent(@org.jetbrains.annotations.NotNull GenericEvent event) {
        onMessageReceived(((MessageReceivedEvent) event));
    }

    public void onSlashCommandReceived(@NotNull SlashCommandEvent event) {
        if (!event.isFromGuild() && !respondInPrivate) {
            event.reply("This command only works in a server").queue();
            return;
        }
        if (!canAnswerFast) {
            event.deferReply(ephemeral).queue();
        }
        ContextSlashReceived ctx = new ContextSlashReceived(event);
        doCommand(ctx);

    }

    /**
     * @param e Because we are using the {@link core.commands.CustomInterfacedEventManager CustomInterfacedEventManager} we know that this is the only OnMessageReceived event handled so we can skip the cheks
     */
    public void onMessageReceived(MessageReceivedEvent e) {
        ContextMessageReceived ctx = new ContextMessageReceived(e);
        e.getChannel().sendTyping().queue(unused -> {
        }, throwable -> {
        });
        if (!e.isFromGuild() && !respondInPrivate) {
            sendMessageQueue(ctx, "This command only works in a server");
            return;
        }
        doCommand(ctx);
    }

    private void doCommand(Context ctx) {
        if (isLongRunningCommand) {
            HeavyCommandRateLimiter.RateLimited rateLimited = HeavyCommandRateLimiter.checkRateLimit(ctx);
            switch (rateLimited) {
                case SERVER -> {
                    sendMessageQueue(ctx, "This command takes a while to execute so it cannot be executed in this server more than 4 times per 10 minutes.\n" + "Usable again in: " + rateLimited.remainingTime(ctx));
                    return;
                }
                case GLOBAL -> {
                    sendMessageQueue(ctx, "This command takes a while to execute so it cannot be executed more than 12 times per 10 minutes.\n" + "Usable again in: " + rateLimited.remainingTime(ctx));
                    return;
                }

            }
        }
        measureTime(ctx);
    }

    public final CommandCategory getCategory() {
        return category;
    }

    public abstract String getName();

    protected void measureTime(Context e) {
        long startTime = System.nanoTime();
        boolean sucess = handleCommand(e);
        long timeElapsed = System.nanoTime() - startTime;
        System.out.printf("Execution time in milliseconds %s: %d%n", getName(), timeElapsed / 1000);
        logCommand(db, e, this, timeElapsed, sucess, e instanceof ContextMessageReceived);
    }


    protected boolean handleCommand(Context e) {
        boolean success = false;
        try {
            T params = parser.parse(e);
            if (params != null) {
                onCommand(e, params);
                success = true;
            }
        } catch (LastFMNoPlaysException ex) {
            String username = ex.getUsername();
            if (e.isFromGuild()) {
                long idLong = e.getGuild().getIdLong();
                try {
                    long discordIdFromLastfm = db.getDiscordIdFromLastfm(ex.getUsername(), idLong);
                    username = getUserString(e, discordIdFromLastfm, username);
                } catch (InstanceNotFoundException ignored) {
                    // We left the inital value
                }
            } else {
                username = CommandUtil.cleanMarkdownCharacter(e.getAuthor().getName());
            }

            String init = "hasn't played anything" + ex.getTimeFrameEnum().toLowerCase();

            parser.sendError(username + " " + init, e);
        } catch (LastFmEntityNotFoundException ex) {
            parser.sendError(ex.toMessage(), e);
        } catch (UnknownLastFmException ex) {
            parser.sendError(new TokenExceptionHandler(ex, db).handle(), e);
            Chuu.getLogger().warn(ex.getMessage(), ex);
            Chuu.getLogger().warn(String.valueOf(ex.getCode()));
        } catch (InstanceNotFoundException ex) {

            String instanceNotFoundTemplate = InstanceNotFoundException.getInstanceNotFoundTemplate();

            String s = instanceNotFoundTemplate
                    .replaceFirst("user_to_be_used_yep_yep", Matcher.quoteReplacement(getUserString(e, ex.getDiscordId(), ex
                            .getLastFMName())));
            s = s.replaceFirst("prefix_to_be_used_yep_yep", Matcher.quoteReplacement(String.valueOf(CommandUtil.getMessagePrefix(e))));

            MessageEmbed build = new ChuuEmbedBuilder(e)
                    .setDescription(s).build();
            if (e instanceof ContextMessageReceived mes) {
                mes.e().getChannel().sendMessage(build).reference(mes.e().getMessage()).queue();

            } else {
                e.sendMessage(build).queue();
            }

        } catch (LastFMConnectionException ex) {
            parser.sendError("Last.fm is not working well or the bot might be overloaded :(", e);
        } catch (
                Exception ex) {
            if (ex instanceof LastFMServiceException && ex.getMessage().equals("500")) {
                parser.sendError("Last.fm is not working well atm :(", e);
                return false;
            }
            parser.sendError("Internal Chuu Error", e);
            Chuu.getLogger().warn(ex.getMessage(), ex);
        }
        if (e.isFromGuild())
            deleteMessage(e, e.getGuild().getIdLong());
        return success;
    }

    private void deleteMessage(Context e, long guildId) {
        if (e instanceof ContextMessageReceived mes && Chuu.getMessageDeletionService().isMarked(guildId) && e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            mes.e().getMessage().delete().queueAfter(5, TimeUnit.SECONDS, (vo) -> {
            }, (throwable) -> {
                if (throwable instanceof ErrorResponseException) {
                    ErrorResponse errorResponse = ((ErrorResponseException) throwable).getErrorResponse();
                    if (errorResponse.equals(ErrorResponse.MISSING_PERMISSIONS)) {
                        Chuu.getMessageDeletionService().removeServerToDelete(guildId);
                        sendMessageQueue(e, "Can't delete messages anymore so from now one won't delete any more message");
                    }
                }
            });
        }
    }

    protected abstract void onCommand(Context e, @NotNull T params) throws LastFmException, InstanceNotFoundException;

    protected final String getUserString(Context e, long discordId) {
        return getUserString(e, discordId, "Unknown");
    }

    protected String getUserString(Context e, long discordId, String replacement) {
        try {
            return CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId).getUsername();
        } catch (Exception ex) {
            return replacement != null ? replacement : "Unknown";
        }

    }

    protected void sendMessageQueue(Context e, String message) {
        e.sendMessageQueue(message);
    }

    @CheckReturnValue
    protected RestAction<Message> sendMessage(Context e, String message) {
        return e.sendMessage(message);
    }

    protected final void sendImage(BufferedImage image, Context e) {
        e.sendImage(image);
    }

    protected final void sendImage(BufferedImage image, Context e, ChartQuality chartQuality) {
        e.sendImage(image, chartQuality);
    }

    protected final void sendImage(BufferedImage image, Context e, ChartQuality chartQuality, EmbedBuilder
            embedBuilder) {
        e.sendImage(image, chartQuality, embedBuilder);
    }

}
