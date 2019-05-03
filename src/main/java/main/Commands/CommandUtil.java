package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.WrapperReturnNowPlaying;
import main.ImageRenderer.NPMaker;
import main.last.ConcurrentLastFM;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.stream.Collectors;

public class CommandUtil {
	static String noImageUrl(String artist) {
		return artist == null || artist.isEmpty() ? "https://lastfm-img2.akamaized.net/i/u/174s/4128a6eb29f94943c9d206c08e625904" : artist;
	}

	public static void a(String who, DaoImplementation dao, MessageReceivedEvent event, Boolean isImage, ConcurrentLastFM lastFM) {
		MessageBuilder messageBuilder = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		WrapperReturnNowPlaying wrapperReturnNowPlaying = dao.whoKnows(who, event.getGuild().getIdLong());

		if (wrapperReturnNowPlaying.getRows() == 0) {
			String repeated = lastFM.getCorrection(who);
			wrapperReturnNowPlaying = dao.whoKnows(repeated, event.getGuild().getIdLong());
			//With db cache?? Extra
			if (wrapperReturnNowPlaying.getRows() == 0) {
				messageBuilder.setContent("No nibba listens to " + who).sendTo(event.getChannel()).queue();
				return;
			}
		}

		try {

			if (!isImage) {
				StringBuilder builder = new StringBuilder();
				int counter = 1;
				for (ReturnNowPlaying returnNowPlaying : wrapperReturnNowPlaying.getReturnNowPlayings()) {
					Member member = event.getGuild().getMemberById(returnNowPlaying.getDiscordId());

					String userName = member == null ? returnNowPlaying.getLastFMId() : member.getEffectiveName();

					builder.append(counter++)
							.append(". ")
							.append("[").append(userName).append("]")
							.append("(https://www.last.fm/user/").append(returnNowPlaying.getLastFMId())
							.append("/library/music/").append(wrapperReturnNowPlaying.getArtist().replaceAll(" ", "+").replaceAll("[)]", "%29")).append(") - ")
							.append(returnNowPlaying.getPlaynumber()).append(" plays\n");
				}

				embedBuilder.setTitle("Who knows " + who + " in " + event.getGuild().getName() + "?").
						setThumbnail(noImageUrl(wrapperReturnNowPlaying.getUrl())).setDescription(builder)
						.setColor(randomColor());
				//.setFooter("Command invoked by " + event.getMember().getUser().getDiscriminator() + " · " + LocalDateTime.now().format(DateTimeFormatter.ISO_WEEK_DATE).toString(), );
				messageBuilder.setEmbed(embedBuilder.build()).sendTo(event.getChannel()).queue();
				return;
			}


			wrapperReturnNowPlaying.setReturnNowPlayings(wrapperReturnNowPlaying.getReturnNowPlayings().stream().map(element -> {
				Member member = event.getGuild().getMemberById(element.getDiscordId());
				String userName = member == null ? element.getLastFMId() : member.getEffectiveName();

				element.setDiscordName(userName);

				return element;
			}).collect(Collectors.toList()));

			BufferedImage image = NPMaker.generateTasteImage(wrapperReturnNowPlaying, event.getGuild().getName());
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ImageIO.write(image, "png", b);
			byte[] img = b.toByteArray();
			if (img.length < 8388608)
				messageBuilder.sendTo(event.getChannel()).addFile(img, "cat.png").queue();
			else
				messageBuilder.setContent("Boot to big").sendTo(event.getChannel()).queue();


		} catch (IOException e2) {
			messageBuilder.setContent("Unknown error happened").sendTo(event.getChannel()).queue();
			//messageBuilder.setContent("No nibba listens to " + who).sendTo(event.getChannel()).queue();
		}


	}

	static Color randomColor() {
		Random rand = new Random();
		double r = rand.nextFloat() / 2f + 0.5;
		double g = rand.nextFloat() / 2f + 0.5;
		double b = rand.nextFloat() / 2f + 0.5;
		return new Color((float) r, (float) g, (float) b);
	}
}