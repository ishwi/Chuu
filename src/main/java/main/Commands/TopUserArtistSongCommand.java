package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.TimeFrameEnum;
import DAO.Entities.Track;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;

public class TopUserArtistSongCommand extends WhoKnowsCommand {
	public TopUserArtistSongCommand(DaoImplementation dao) {
		super(dao);
		respondInPrivate = true;
	}

	@Override
	void whoKnowsLogic(ArtistData who, Boolean isList, MessageReceivedEvent e, long userId) {
		List<Track> ai;
		String lastFmName;
		try {
			lastFmName = getDao().findLastFMData(userId).getName();
		} catch (InstanceNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(1), e);
			return;
		}

		try {
			ai = lastFM.getTopArtistTracks(lastFmName, who.getArtist(), TimeFrameEnum.ALL.toApiFormat());
		} catch (LastFMNoPlaysException ex) {
			parser.sendError("No plays at all bro", e);
			return;
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
			return;
		}
		final String userString = getUserStringConsideringGuildOrNot(e, userId, lastFmName);
		MessageBuilder mes = new MessageBuilder();
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < 10 && i < ai.size(); i++) {
			Track g = ai.get(i);
			s.append(i + 1).append(": ").append(g.getName()).append(" - ").append(g.getPlays()).append(" plays")
					.append("\n");
		}
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setDescription(s);
		embedBuilder.setColor(CommandUtil.randomColor());

		embedBuilder
				.setTitle(userString + "'s top " + who.getArtist() + " tracks", CommandUtil.getLastFmUser(lastFmName));
		embedBuilder.setThumbnail(CommandUtil.noImageUrl(who.getUrl()));

		e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue();
	}

	@Override
	public String getDescription() {
		return
				"Fav  tracks from an artist";
	}

	@Override
	public String getName() {

		return "Fav tracks";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!favs");
	}
}
