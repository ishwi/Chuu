package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.WrapperReturnNowPlaying;
import main.ImageRenderer.NPMaker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

public class CommandUtil {

	public static void a(String who, DaoImplementation dao, MessageReceivedEvent event, Boolean isImage) {
		MessageBuilder messageBuilder = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		WrapperReturnNowPlaying wrapperReturnNowPlaying = dao.whoKnows(who, event.getGuild().getIdLong());
		try {

			if (!isImage) {
				StringBuilder builder = new StringBuilder();
				int counter = 1;
				for (ReturnNowPlaying returnNowPlaying : wrapperReturnNowPlaying.getReturnNowPlayings()) {
					String userName = event.getGuild().getMemberById(returnNowPlaying.getDiscordId()).getEffectiveName();
					builder.append(counter++)
							.append(". ")
							.append("[").append(userName).append("]")
							.append("(https://www.last.fm/user/").append(returnNowPlaying.getLastFMId()).append(") - ")
							.append(returnNowPlaying.getPlaynumber()).append(" plays\n");
				}

				embedBuilder.setTitle("Who knows " + wrapperReturnNowPlaying.getArtist() + " in " + event.getGuild().getName() + "?").
						setThumbnail(wrapperReturnNowPlaying.getUrl()).setDescription(builder)
						.setColor(randomColor());
				//.setFooter("Command invoked by " + event.getMember().getUser().getDiscriminator() + " · " + LocalDateTime.now().format(DateTimeFormatter.ISO_WEEK_DATE).toString(), );
				messageBuilder.setEmbed(embedBuilder.build()).sendTo(event.getChannel()).queue();
				return;
			}
			BufferedImage image = NPMaker.generateNP(wrapperReturnNowPlaying, event.getGuild().getName());
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", b);
			byte[] img = b.toByteArray();
			if (img.length < 8388608)
				messageBuilder.sendTo(event.getChannel()).addFile(img, "cat.png").queue();


		} catch (IOException | IllegalArgumentException e2) {
			messageBuilder.setContent("No nibba listens to " + who).sendTo(event.getChannel()).queue();
		}
	}

	public static Color randomColor() {
		Random rand = new Random();
		double r = rand.nextFloat() / 2f + 0.5;
		double g = rand.nextFloat() / 2f + 0.5;
		double b = rand.nextFloat() / 2f + 0.5;
		return new Color((float) r, (float) g, (float) b);
	}
}
