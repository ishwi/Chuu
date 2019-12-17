package core.commands;

import dao.DaoImplementation;
import dao.entities.AlbumUserPlays;
import dao.entities.ArtistAlbums;
import dao.entities.ArtistData;
import dao.entities.WrapperReturnNowPlaying;
import core.Chuu;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.BandRendered;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BandInfoCommand extends WhoKnowsCommand {

	public BandInfoCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public String getDescription() {
		return "Band info";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("artist", "a");
	}

	@Override
	void whoKnowsLogic(ArtistData who, Boolean isList, MessageReceivedEvent e, long userId) throws InstanceNotFoundException, LastFmException {
		ArtistAlbums ai;
		String lastFmName;
		lastFmName = getDao().findLastFMData(userId).getName();

		ai = lastFM.getAlbumsFromArtist(who.getArtist(), 14);

		String artist = ai.getArtist();
		final String username = lastFmName;
		List<AlbumUserPlays> list = ai.getAlbumList();

		int plays = getDao().getArtistPlays(artist, username);
		if (plays == 0) {
			parser.sendError("You still haven't listened to " + artist, e);
			return;
		}

		list =
				list.stream().peek(albumInfo -> {
					try {
						albumInfo.setPlays(lastFM.getPlaysAlbum_Artist(username, artist, albumInfo.getAlbum())
								.getPlays());

					} catch (LastFmException ex) {
						Chuu.getLogger().warn(ex.getMessage(), ex);
					}
				})
						.filter(a -> a.getPlays() > 0)
						.collect(Collectors.toList());

		list.sort(Comparator.comparing(AlbumUserPlays::getPlays).reversed());
		ai.setAlbumList(list);
		WrapperReturnNowPlaying np = getDao().whoKnows(artist, e.getGuild().getIdLong(), 5);
		np.getReturnNowPlayings().forEach(element ->
				element.setDiscordName(getUserString(element.getDiscordId(), e, element.getLastFMId()))
		);

		BufferedImage logo = CommandUtil.getLogo(getDao(), e);
		BufferedImage returnedImage = BandRendered
				.makeBandImage(np, ai, plays, logo, getUserString(userId, e, username));
		sendImage(returnedImage, e);
	}

	@Override
	public String getName() {
		return "Band";
	}


}
