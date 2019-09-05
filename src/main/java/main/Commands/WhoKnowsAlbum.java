package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.AlbumUserPlays;
import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.UsersWrapper;
import DAO.Entities.WrapperReturnNowPlaying;
import main.Chuu;
import main.Exceptions.LastFmException;
import main.ImageRenderer.WhoKnowsMaker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

public class WhoKnowsAlbum extends AlbumPlaysCommand {


	public WhoKnowsAlbum(DaoImplementation dao) {
		super(dao);
		this.respondInPrivate = false;
	}

	@Override
	public void doSomethingWithAlbumArtist(String artist, String album, MessageReceivedEvent e, long who) {

		long id = e.getGuild().getIdLong();
		//Gets list of users registered in guild
		List<UsersWrapper> userList = getDao().getAll(id);

		//Gets play number for each registered artist
		AlbumUserPlays urlContainter = new AlbumUserPlays("", "");
		Map<UsersWrapper, Integer> userMapPlays = fillPlayCounter(userList, artist, album, urlContainter);

		//Manipulate data in order to pass it to the image Maker
		BufferedImage logo = CommandUtil.getLogo(getDao(), e);
		List<Map.Entry<UsersWrapper, Integer>> list = new ArrayList<>(userMapPlays.entrySet());
		list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
		List<ReturnNowPlaying> list2 = list.stream().map(t -> {
					long id2 = t.getKey().getDiscordID();
					ReturnNowPlaying np = new ReturnNowPlaying(id2, t.getKey().getLastFMName(), artist, t.getValue());
					np.setDiscordName(getUserString(id2, e, t.getKey().getLastFMName()));
					return np;
				}
		).filter(x -> x.getPlayNumber() > 0).collect(Collectors.toList());
		WrapperReturnNowPlaying a = new WrapperReturnNowPlaying(list2, 0, urlContainter
				.getAlbum_url(), artist + " - " + album);

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
				userMapPlays.put(u, albumUserPlays.getPlays());
			} catch (LastFmException ex) {
				Chuu.getLogger().warn(ex.getMessage(), ex);
			}
		});
		return userMapPlays;
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
		return Collections.singletonList("!wkalbum");
	}


}
