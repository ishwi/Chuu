package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import DAO.Entities.UsersWrapper;
import main.last.ConcurrentLastFM;
import main.last.LastFMNoPlaysException;
import main.last.LastFMServiceException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AllPlayingCommand extends MyCommandDbAccess {
	public AllPlayingCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		String[] message;
		try {
			message = parse(e);
		} catch (ParseException e1) {
			errorMessage(e, 1000, e1.getMessage());
			return;
		}
		List<UsersWrapper> list = getDao().getAll(e.getGuild().getIdLong());
		MessageBuilder messageBuilder = new MessageBuilder();

		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor()).setThumbnail(e.getGuild().getIconUrl())
				.setTitle("What is being played now in " + e.getGuild().getName());
		StringBuilder a = new StringBuilder();
		e.getChannel().sendTyping().queue();


		for (UsersWrapper usersWrapper : list) {

			try {
				NowPlayingArtist nowPlayingArtist = ConcurrentLastFM.getNowPlayingInfo(usersWrapper.getLastFMName());
				if (Boolean.valueOf(message[0])) {
					if (!nowPlayingArtist.isNowPlaying()) {
						continue;
					}
				}

				String username = e.getGuild().getMemberById(usersWrapper.getDiscordID()).getEffectiveName();
				a.append("+ ").append("[")
						.append(username).append("](").append("https://www.last.fm/user/").append(usersWrapper.getLastFMName())
						.append("): ")
						.append("**").append(nowPlayingArtist.getSongName())
						.append("** - ").append(nowPlayingArtist.getAlbumName()).append(" | ")
						.append(nowPlayingArtist.getArtistName()).append("\n");
			} catch (LastFMServiceException ex) {
				errorMessage(e, 0, ex.getMessage());
			} catch (LastFMNoPlaysException e1) {
				errorMessage(e, 1, e1.getMessage());

			}


		}
		embedBuilder.setDescription(a);
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!playing");
	}

	@Override
	public String getDescription() {
		return ("Returns lists of all people playing music rn");
	}

	@Override
	public String getName() {
		return "Playing";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("**!playing ** \n" +
				"--recent for last track\n\n ");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		String[] subMessage = getSubMessage(e.getMessage());
		boolean noFlag = Arrays.stream(subMessage).noneMatch(s -> s.equals("--recent"));

		return new String[]{Boolean.toString(noFlag)};
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request: ";
		String message;

		switch (code) {

			case 0:
				message = "There was a problem with Last FM Api" + cause;
				break;
			case 1:
				message = "User hasnt played any song recently!" + cause;
				break;
			default:
				message = "An unknown error happned";


		}
		sendMessage(e, base + message);
	}
}
