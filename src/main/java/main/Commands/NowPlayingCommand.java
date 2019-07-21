package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class NowPlayingCommand extends NpCommand {
	public NowPlayingCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void doSomethingWithArtist(NowPlayingArtist nowPlayingArtist, MessageReceivedEvent e) {
		StringBuilder a = new StringBuilder();

		String username = nowPlayingArtist.getUsername();
		a.append("**[").append(username).append("'s Profile](").append("https://www.last.fm/user/").append(username)
				.append(")**\n\n")
				.append(nowPlayingArtist.isNowPlaying() ? "Current" : "Last")
				.append(":\n")
				.append(nowPlayingArtist.getArtistName())
				.append(" - ").append(nowPlayingArtist.getSongName())
				.append(" | ").append(nowPlayingArtist.getAlbumName());

		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
				.setThumbnail(CommandUtil.noImageUrl(nowPlayingArtist.getUrl()))
				.setTitle("Now Playing:")
				.setDescription(a);

		MessageBuilder messageBuilder = new MessageBuilder();
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
	}

	@Override
	public String getDescription() {
		return "Returns your current playing song";
	}

	@Override
	public String getName() {
		return "Now Playing";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!np");
	}


}
