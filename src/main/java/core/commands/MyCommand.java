package core.commands;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.commands.utils.MyCommandSerializer;
import core.exceptions.*;
import core.imagerenderer.ChartQuality;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@JsonSerialize(using = MyCommandSerializer.class)
public abstract class MyCommand<T extends CommandParameters> extends ListenerAdapter {
    final ConcurrentLastFM lastFM;
    private final ChuuService dao;
    private final CommandCategory category;
    boolean respondInPrivate = true;
    Parser<T> parser;

    MyCommand(ChuuService dao) {
        this.dao = dao;
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

    ChuuService getService() {
        return dao;
    }

    public abstract String getDescription();

    public String getUsageInstructions() {
        return parser.getUsage(getAliases().get(0));
    }

    public abstract List<String> getAliases();

    /**
     * @param e Because we are using the {@link core.commands.CustomInterfacedEventManager CustomInterfacedEventManager} we know that this is the only OnMessageReceived event handled so we can skip the cheks
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {

        e.getChannel().sendTyping().queue();
        System.out.println("We received a message from " +
                e.getAuthor().getName() + "; " + e.getMessage().getContentDisplay());
        if (!e.isFromGuild() && !respondInPrivate) {
            sendMessageQueue(e, "This command only works in a server");
            return;
        }
        measureTime(e);
    }

    public CommandCategory getCategory() {
        return category;
    }

    public abstract String getName();

    void measureTime(MessageReceivedEvent e) {
        long startTime = System.nanoTime();
        handleCommand(e);
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        System.out.printf("Execution time in milliseconds %s: %d%n", getName(), timeElapsed / 1000);
        logCommand(getService(), e, this, timeElapsed);
    }


    void handleCommand(MessageReceivedEvent e) {
        try {
            onCommand(e);
        } catch (LastFMNoPlaysException ex) {
            String username = ex.getUsername();
            if (e.isFromGuild()) {
                long idLong = e.getGuild().getIdLong();
                try {
                    long discordIdFromLastfm = dao.getDiscordIdFromLastfm(ex.getUsername(), idLong);
                    username = getUserString(e, discordIdFromLastfm, username);
                } catch (InstanceNotFoundException ignored) {
                    // We left the inital value
                }
            } else {
                username = CommandUtil.cleanMarkdownCharacter(e.getAuthor().getName());
            }

            String init = "hasn't played anything";
            if (!ex.getTimeFrameEnum().equals(TimeFrameEnum.ALL.toString()))
                init += " in the last " + ex.getTimeFrameEnum().toLowerCase();

            parser.sendError(username + " " + init, e);
        } catch (LastFmEntityNotFoundException ex) {
            parser.sendError(ex.toMessage(), e);
        } catch (UnknownLastFmException ex) {
            parser.sendError("Unknown last.fm exception found:\n" + ex.getSentMessage(), e);
            Chuu.getLogger().warn(ex.getMessage(), ex);
            Chuu.getLogger().warn(String.valueOf(ex.getCode()));
        } catch (InstanceNotFoundException ex) {
            String instanceNotFoundTemplate = InstanceNotFoundException.getInstanceNotFoundTemplate();

            String s = instanceNotFoundTemplate
                    .replaceFirst("user_to_be_used_yep_yep", Matcher.quoteReplacement(getUserString(e, ex.getDiscordId(), ex
                            .getLastFMName())));
            s = s.replaceFirst("prefix_to_be_used_yep_yep", Matcher.quoteReplacement(String.valueOf(e.getMessage().getContentStripped().charAt(0))));
            parser.sendError(s, e);
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

    abstract void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException;


    void sendMessageQueue(MessageReceivedEvent e, String message) {
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

    MessageAction sendMessage(MessageReceivedEvent e, String message) {
        return sendMessage(e, new MessageBuilder().append(message).build());
    }

    private MessageAction sendMessage(MessageReceivedEvent e, Message message) {
        if (e.isFromType(ChannelType.PRIVATE))
            return e.getPrivateChannel().sendMessage(message);
        else
            return e.getTextChannel().sendMessage(message);
    }

    String[] commandArgs(Message message) {
        return commandArgs(message.getContentDisplay());
    }

    private String[] commandArgs(String string) {
        return string.split("\\s+");
    }

    void sendImage(BufferedImage image, MessageReceivedEvent e) {
        sendImage(image, e, ChartQuality.PNG_BIG, null);
    }

    void sendImage(BufferedImage image, MessageReceivedEvent e, ChartQuality chartQuality) {
        sendImage(image, e, chartQuality, null);
    }

    void sendImage(BufferedImage image, MessageReceivedEvent e, ChartQuality chartQuality, EmbedBuilder
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
