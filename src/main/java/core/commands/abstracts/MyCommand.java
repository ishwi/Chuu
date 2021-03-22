package core.commands.abstracts;

import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.last.TokenExceptionHandler;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.*;
import core.imagerenderer.ChartQuality;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import core.services.HeavyCommandRateLimiter;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public abstract class MyCommand<T extends CommandParameters> implements EventListener {
    public final ConcurrentLastFM lastFM;
    protected final ChuuService db;
    private final CommandCategory category;
    public boolean respondInPrivate = true;
    protected boolean isLongRunningCommand = false;
    protected final Parser<T> parser;


    protected MyCommand(ChuuService db) {
        this.db = db;
        lastFM = LastFMFactory.getNewInstance();
        this.parser = initParser();
        this.category = initCategory();
    }

    private static void logCommand(ChuuService service, MessageReceivedEvent e, MyCommand<?> command, long exectTime) {
        service.logCommand(e.getAuthor().getIdLong(), e.isFromGuild() ? e.getGuild().getIdLong() : null, command.getName(), exectTime, Instant.now());

    }

    protected abstract CommandCategory initCategory();

    public abstract Parser<T> initParser();

    public Parser<T> getParser() {
        return parser;
    }


    public abstract String getDescription();

    public String getUsageInstructions() {
        return parser.getUsage(getAliases().get(0));
    }

    public abstract List<String> getAliases();

    @Override
    public void onEvent(@org.jetbrains.annotations.NotNull GenericEvent event) {
        onMessageReceived((MessageReceivedEvent) event);
    }

    /**
     * @param e Because we are using the {@link core.commands.CustomInterfacedEventManager CustomInterfacedEventManager} we know that this is the only OnMessageReceived event handled so we can skip the cheks
     */
    public void onMessageReceived(MessageReceivedEvent e) {

        e.getChannel().sendTyping().queue(unused -> {
        }, throwable -> {
        });
        System.out.println("We received a message from " +
                e.getAuthor().getName() + "; " + e.getMessage().getContentDisplay());
        if (!e.isFromGuild() && !respondInPrivate) {
            sendMessageQueue(e, "This command only works in a server");
            return;
        }
        if (isLongRunningCommand) {
            HeavyCommandRateLimiter.RateLimited rateLimited = HeavyCommandRateLimiter.checkRateLimit(e);
            switch (rateLimited) {
                case SERVER -> {
                    sendMessageQueue(e, "This command takes a while to execute so it cannot be executed in this server more than 4 times per 10 minutes.\n" + "Usable again in: " + rateLimited.remainingTime(e));
                    return;
                }
                case GLOBAL -> {
                    sendMessageQueue(e, "This command takes a while to execute so it cannot be executed more than 12 times per 10 minutes.\n" + "Usable again in: " + rateLimited.remainingTime(e));
                    return;
                }

            }
        }
        measureTime(e);
    }

    public CommandCategory getCategory() {
        return category;
    }

    public abstract String getName();

    protected void measureTime(MessageReceivedEvent e) {
        long startTime = System.nanoTime();
        handleCommand(e);
        long timeElapsed = System.nanoTime() - startTime;
        System.out.printf("Execution time in milliseconds %s: %d%n", getName(), timeElapsed / 1000);
        logCommand(db, e, this, timeElapsed);
    }


    protected void handleCommand(MessageReceivedEvent e) {
        try {
            T params = parser.parse(e);
            if (params != null) {
                onCommand(e, params);
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
            s = s.replaceFirst("prefix_to_be_used_yep_yep", Matcher.quoteReplacement(String.valueOf(e.getMessage().getContentStripped().charAt(0))));

            e.getChannel().sendMessage(new EmbedBuilder()
                    .setColor(ColorService.computeColor(e))
                    .setDescription(s).build()).reference(e.getMessage()).queue();
        } catch (LastFMConnectionException ex) {
            parser.sendError("Last.fm is not working well or the bot might be overloaded :(", e);
        } catch (
                Exception ex) {
            if (ex instanceof LastFMServiceException && ex.getMessage().equals("500")) {
                parser.sendError("Last.fm is not working well atm :(", e);
                return;
            }
            parser.sendError("Internal Chuu Error", e);
            Chuu.getLogger().warn(ex.getMessage(), ex);
        }
        if (e.isFromGuild())
            deleteMessage(e, e.getGuild().getIdLong());
    }

    private void deleteMessage(MessageReceivedEvent e, long guildId) {
        if (Chuu.getMessageDeletionService().isMarked(guildId) && e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            e.getMessage().delete().queueAfter(5, TimeUnit.SECONDS, (vo) -> {
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

    protected abstract void onCommand(MessageReceivedEvent e, @NotNull T params) throws LastFmException, InstanceNotFoundException;


    public void sendMessageQueue(MessageReceivedEvent e, String message) {
        sendMessageQueue(e, new MessageBuilder().append(message).build());
    }

    private void sendMessageQueue(MessageReceivedEvent e, Message message) {
        e.getChannel().sendMessage(message).queue();
    }

    public String getUserString(MessageReceivedEvent e, long discordId) {
        return getUserString(e, discordId, "Unknown");
    }

    public String getUserString(MessageReceivedEvent e, long discordId, String replacement) {
        try {
            return CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId).getUsername();
        } catch (Exception ex) {
            return replacement != null ? replacement : "Unknown";
        }

    }


    protected MessageAction sendMessage(MessageReceivedEvent e, String message) {
        return sendMessage(e, new MessageBuilder().append(message).build());
    }

    private MessageAction sendMessage(MessageReceivedEvent e, Message message) {
        if (e.isFromType(ChannelType.PRIVATE))
            return e.getPrivateChannel().sendMessage(message);
        else
            return e.getTextChannel().sendMessage(message);
    }

    protected static String[] commandArgs(Message message) {
        return commandArgs(message.getContentDisplay());
    }

    private static String[] commandArgs(String string) {
        return string.split("\\s+");
    }

    public void sendImage(BufferedImage image, MessageReceivedEvent e) {
        sendImage(image, e, ChartQuality.PNG_BIG, null);
    }

    protected void sendImage(BufferedImage image, MessageReceivedEvent e, ChartQuality chartQuality) {
        sendImage(image, e, chartQuality, null);
    }

    protected void sendImage(BufferedImage image, MessageReceivedEvent e, ChartQuality chartQuality, EmbedBuilder
            embedBuilder) {
        if (image == null) {
            sendMessageQueue(e, "Something went wrong generating the image");
            return;
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();

        try {
            String format = "png";
            if (chartQuality == ChartQuality.JPEG_SMALL || chartQuality == ChartQuality.JPEG_BIG)
                format = "jpg";
            ImageIO.write(image, format, b);

            byte[] img = b.toByteArray();
            long maxSize = e.isFromGuild() ? e.getGuild().getMaxFileSize() : Message.MAX_FILE_SIZE;
            if (img.length < maxSize) {
                if (embedBuilder != null) {
                    //embedBuilder.setImage("attachment://cat." + format);
                    e.getChannel().sendFile(img, "cat." + format).embed(embedBuilder.build()).queue();
                } else {
                    e.getChannel().sendFile(img, "cat." + format).queue();
                }
            } else
                e.getChannel().sendMessage("File was real big").queue();

        } catch (
                IOException ex) {
            if (ex.getMessage().equals("Maximum supported image dimension is 65500 pixels")) {
                sendMessageQueue(e, "Programming language won't allow images with more than 65500 pixels in one dimension");

            } else {
                sendMessageQueue(e, "Ish Pc Bad");
                Chuu.getLogger().warn(ex.getMessage(), ex);
            }
        }


    }


}
