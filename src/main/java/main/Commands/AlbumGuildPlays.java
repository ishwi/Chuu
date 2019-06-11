package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.AlbumInfo;
import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.UsersWrapper;
import DAO.Entities.WrapperReturnNowPlaying;
import main.Exceptions.LastFmException;
import main.ImageRenderer.NPMaker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

public class AlbumGuildPlays extends AlbumSongPlaysCommand {
	public AlbumGuildPlays(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void doSomethingWithAlbumArtist(String artist, String album, MessageReceivedEvent e, String who) {

		long id = e.getGuild().getIdLong();
		//Gets list of users registered in guild
		List<UsersWrapper> userList = getDao().getAll(id);
		Map<UsersWrapper, Integer> userMapPlays = new LinkedHashMap<>();
		AlbumInfo container = new AlbumInfo("", "");
		//Gets play number for each registered artist
		userList.forEach(u -> {
			try {


				AlbumInfo albumInfo = lastFM.getPlaysAlbum_Artist(u.getLastFMName(), artist, album);
				//Need to get the album art url from someone
				if (container.getAlbum_url().isEmpty())
					container.setAlbum_url(albumInfo.getAlbum_url());
				userMapPlays.put(u, albumInfo.getPlays());
			} catch (LastFmException ex) {
				ex.printStackTrace();
			}
		});

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
		).filter(x -> x.getPlaynumber() > 0).collect(Collectors.toList());
		WrapperReturnNowPlaying a = new WrapperReturnNowPlaying(list2, 0, container.getAlbum_url(), artist + " - " + album);

		BufferedImage sender = NPMaker.generateTasteImage(a, e.getGuild().getName(), logo);
		sendImage(sender, e);

	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!wkalbum");
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
	public List<String> getUsageInstructions() {
		return Collections.singletonList
				("!wkalbum artist-album \n" +
						"if no artist is provided default to now playing artist\n\n");

	}
}
