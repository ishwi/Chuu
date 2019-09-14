package main.commands;

import dao.DaoImplementation;
import dao.entities.AlbumUserPlays;
import dao.entities.ReturnNowPlaying;
import dao.entities.UsersWrapper;
import dao.entities.WrapperReturnNowPlaying;
import main.Chuu;
import main.exceptions.LastFmException;
import main.imagerenderer.WhoKnowsMaker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

public class WhoKnowsAlbumCommand extends AlbumPlaysCommand {


	public WhoKnowsAlbumCommand(DaoImplementation dao) {
		super(dao);
		this.respondInPrivate = false;
	}

	@Override
	public void doSomethingWithAlbumArtist(String artist, String album, MessageReceivedEvent e, long who) {

		long id = e.getGuild().getIdLong();
		//Gets list of users registered in guild
		List<UsersWrapper> userList = getDao().getAll(id);
		if (userList.isEmpty()) {
			sendMessageQueue(e, "No users are registered on this server");
			return;
		}

		//Gets play number for each registered artist
		AlbumUserPlays urlContainter = new AlbumUserPlays("", "");
		Map<UsersWrapper, Integer> userMapPlays = fillPlayCounter(userList, artist, album, urlContainter);
		String corrected_album = urlContainter.getAlbum() == null || urlContainter.getAlbum()
				.isEmpty() ? album : urlContainter.getAlbum();
		String corrected_artist = urlContainter.getArtist() == null || urlContainter.getArtist()
				.isEmpty() ? artist : urlContainter.getArtist();
		//Manipulate data in order to pass it to the image Maker
		BufferedImage logo = CommandUtil.getLogo(getDao(), e);
		List<Map.Entry<UsersWrapper, Integer>> list = new ArrayList<>(userMapPlays.entrySet());
		list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
		List<ReturnNowPlaying> list2 = list.stream().map(t -> {
					long id2 = t.getKey().getDiscordID();
			ReturnNowPlaying np = new ReturnNowPlaying(id2, t.getKey().getLastFMName(), corrected_artist, t.getValue());
					np.setDiscordName(getUserString(id2, e, t.getKey().getLastFMName()));
					return np;
				}
		).filter(x -> x.getPlayNumber() > 0).collect(Collectors.toList());
		if (list2.isEmpty()) {
			sendMessageQueue(e, " No nibba knows " + artist + " - " + album);
			return;
		}

		doExtraThings(list2, id, corrected_artist, corrected_album);

		WrapperReturnNowPlaying a = new WrapperReturnNowPlaying(list2, 0, urlContainter
				.getAlbum_url(), corrected_artist + " - " + corrected_album);

		BufferedImage sender = WhoKnowsMaker.generateWhoKnows(a, e.getGuild().getName(), logo);

		sendImage(sender, e);


	}


	Map<UsersWrapper, Integer> fillPlayCounter(List<UsersWrapper> userList, String artist, String album, AlbumUserPlays fillWithUrl) {
		Map<UsersWrapper, Integer> userMapPlays = new LinkedHashMap<>();
		userList.forEach(u -> {
			try {

				AlbumUserPlays albumUserPlays = lastFM.getPlaysAlbum_Artist(u.getLastFMName(), artist, album);
				if (fillWithUrl.getAlbum_url().isEmpty())
					fillWithUrl.setAlbum_url(albumUserPlays.getAlbum_url());
				if (fillWithUrl.getAlbum().isEmpty())
					fillWithUrl.setAlbum(albumUserPlays.getAlbum());
				if (fillWithUrl.getArtist() == null || fillWithUrl.getArtist().isEmpty())
					fillWithUrl.setArtist(albumUserPlays.getArtist());
				userMapPlays.put(u, albumUserPlays.getPlays());
			} catch (LastFmException ex) {
				Chuu.getLogger().warn(ex.getMessage(), ex);
			}
		});
		return userMapPlays;
	}

	void doExtraThings(List<ReturnNowPlaying> list2, long id, String artist, String album) {
		ReturnNowPlaying crownUser = list2.get(0);
		getDao().insertAlbumCrown(artist, album, crownUser.getDiscordId(), id, crownUser.getPlayNumber());
	}


	@Override
	public String getDescription() {
		return ("How many times the guild has heard an album!");
	}

	@Override
	public String getName() {
		return "Get guild Album plays";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("wkalbum", "wka", "whoknowsalbum");
	}


}
