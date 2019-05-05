package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.WrapperReturnNowPlaying;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Exceptions.ParseException;
import main.ImageRenderer.NPMaker;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class WhoKnowsCommand extends ConcurrentCommand {

	public WhoKnowsCommand(DaoImplementation dao) {
		super(dao);
	}

	void whoKnowsLogic(String who, Boolean isImage) {
		MessageBuilder messageBuilder = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		WrapperReturnNowPlaying wrapperReturnNowPlaying = this.getDao().whoKnows(who, e.getGuild().getIdLong());

		if (wrapperReturnNowPlaying.getRows() == 0) {
			String repeated = null;
			try {
				repeated = lastFM.getCorrection(who);
			} catch (LastFmEntityNotFoundException ex) {
				messageBuilder.setContent("No nibba listens to " + who).sendTo(e.getChannel()).queue();
				return;
			} catch (LastFmException ex2) {
				errorMessage(e, 100, "");
				return;
			}

			wrapperReturnNowPlaying = this.getDao().whoKnows(repeated, e.getGuild().getIdLong());
			//With db cache?? Extra
			if (wrapperReturnNowPlaying.getRows() == 0) {
				messageBuilder.setContent("No nibba listens to " + who).sendTo(e.getChannel()).queue();
				return;
			}
		}

		try {

			if (!isImage) {
				StringBuilder builder = new StringBuilder();
				int counter = 1;
				for (ReturnNowPlaying returnNowPlaying : wrapperReturnNowPlaying.getReturnNowPlayings()) {
					Member member = e.getGuild().getMemberById(returnNowPlaying.getDiscordId());

					String userName = member == null ? returnNowPlaying.getLastFMId() : member.getEffectiveName();

					builder.append(counter++)
							.append(". ")
							.append("[").append(userName).append("]")
							.append("(https://www.last.fm/user/").append(returnNowPlaying.getLastFMId())
							.append("/library/music/").append(wrapperReturnNowPlaying.getArtist().replaceAll(" ", "+").replaceAll("[)]", "%29")).append(") - ")
							.append(returnNowPlaying.getPlaynumber()).append(" plays\n");
				}

				embedBuilder.setTitle("Who knows " + who + " in " + e.getGuild().getName() + "?").
						setThumbnail(CommandUtil.noImageUrl(wrapperReturnNowPlaying.getUrl())).setDescription(builder)
						.setColor(CommandUtil.randomColor());
				//.setFooter("Command invoked by " + event.getMember().getUser().getDiscriminator() + " · " + LocalDateTime.now().format(DateTimeFormatter.ISO_WEEK_DATE).toString(), );
				messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
				return;
			}


			wrapperReturnNowPlaying.setReturnNowPlayings(wrapperReturnNowPlaying.getReturnNowPlayings()
					.stream().peek(element -> {
						Member member = e.getGuild().getMemberById(element.getDiscordId());
						String userName = member == null ? element.getLastFMId() : member.getEffectiveName();

						element.setDiscordName(userName);

					}).collect(Collectors.toList()));
			BufferedImage logo = null;
			try {
				logo = ImageIO.read(getDao().findLogo(e.getGuild().getIdLong()));
			} catch (InstanceNotFoundException ignored) {
				System.out.println(e.getGuild().getName() + "Guild has no logo");
			}
			BufferedImage image = NPMaker.generateTasteImage(wrapperReturnNowPlaying, e.getGuild().getName(), logo);
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			assert image != null;
			ImageIO.write(image, "png", b);
			byte[] img = b.toByteArray();
			if (img.length < 8388608)
				messageBuilder.sendTo(e.getChannel()).addFile(img, "cat.png").queue();
			else
				messageBuilder.setContent("Boot to big").sendTo(e.getChannel()).queue();


		} catch (IOException e2) {
			messageBuilder.setContent("Unknown error happened").sendTo(e.getChannel()).queue();
			//messageBuilder.setContent("No nibba listens to " + who).sendTo(event.getChannel()).queue();
		}
	}


	@Override
	public void threadableCode() {
		String[] returned;
		try {
			returned = parse(e);
			whoKnowsLogic(returned[0], Boolean.valueOf(returned[1]));
		} catch (ParseException e1) {
			errorMessage(e, 0, e1.getMessage());
		}

	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!whoknows");
	}

	@Override
	public String getDescription() {
		return "Returns List Of Users Who Know the inputed Artist";
	}

	@Override
	public String getName() {
		return "Who Knows";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!whoknows artist\n\t --image for Image format\n\n"
		);
	}


	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {

		String[] message = getSubMessage(e.getMessage());


		boolean flag = false;
		String[] message1 = Arrays.stream(message).filter(s -> !s.equals("--image")).toArray(String[]::new);
		if (message1.length != message.length) {
			message = message1;
			flag = true;
		}
		if (message.length == 0) {
			//No Commands Inputed
			throw new ParseException("Input");
		}
		String artist;
		if (message.length > 1) {
			StringBuilder a = new StringBuilder();
			for (String s : message) {
				a.append(s).append(" ");
			}
			artist = a.toString().trim();
		} else {
			artist = message[0];
		}
		return new String[]{artist, Boolean.toString(flag)};

	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request: ";
		if (code == 0) {
			sendMessage(e, base + " You need to specify the Artist!");
			return;
		}
		sendMessage(e, base + "Internal Server Error, Try Again later ");
	}
}
