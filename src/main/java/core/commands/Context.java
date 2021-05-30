package core.commands;

import core.imagerenderer.ChartQuality;
import dao.exceptions.ChuuServiceException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public sealed interface Context permits ContextMessageReceived, ContextSlashReceived {

    User getAuthor();

    GenericEvent getEvent();

    JDA getJDA();

    Member getMember();

    MessageChannel getChannel();

    Guild getGuild();

    boolean isFromGuild();

    char getPrefix();

    String toLog();

    long getId();

    @CheckReturnValue
    RestAction<Message> sendEmbed(EmbedBuilder embedBuilder);

    List<User> getMentionedUsers();

    @CheckReturnValue
    RestAction<Message> sendMessage(String content);


    @CheckReturnValue
    RestAction<Message> sendMessage(MessageEmbed embed);

    RestAction<Message> sendMessage(MessageEmbed embed, List<ActionRow> rows);

    default void sendMessageQueue(String content) {
        sendMessage(content).queue();
    }


    default void sendImage(BufferedImage image) {
        sendImage(image, ChartQuality.PNG_BIG, null);
    }

    default void sendImage(BufferedImage image, ChartQuality chartQuality) {
        sendImage(image, chartQuality, null);
    }

    RestAction<Message> sendMessage(Message message, User toMention);

    void doSendImage(byte[] img, String format, @Nullable EmbedBuilder embedBuilder);

    default void sendImage(BufferedImage image, ChartQuality chartQuality, EmbedBuilder embedBuilder) {
        if (image == null) {
            sendMessageQueue("Something went wrong generating the image");
            return;
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try {
            String format = "png";
            if (chartQuality == ChartQuality.JPEG_SMALL || chartQuality == ChartQuality.JPEG_BIG)
                format = "jpg";
            ImageIO.write(image, format, b);

            byte[] img = b.toByteArray();
            long maxSize = isFromGuild() ? getGuild().getMaxFileSize() : Message.MAX_FILE_SIZE;
            if (img.length < maxSize) {
                doSendImage(img, format, embedBuilder);
            } else {
                sendMessageQueue("File was real big");
            }
        } catch (
                IOException ex) {
            if (ex.getMessage().equals("Maximum supported image dimension is 65500 pixels")) {
                sendMessageQueue("Programming language won't allow images with more than 65500 pixels in one dimension");
            } else {
                throw new ChuuServiceException(ex);
            }
        }
    }

    RestAction<Message> sendFile(InputStream inputStream, String s, String title);


}
