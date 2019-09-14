package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class NowPlayingCommand extends NpCommand {
	public NowPlayingCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void doSomethingWithArtist(NowPlayingArtist nowPlayingArtist, MessageReceivedEvent e) {
		StringBuilder a = new StringBuilder();

		String username = nowPlayingArtist.getUsername();

		a.append("**").append(nowPlayingArtist.getSongName())
				.append("** - ").append(nowPlayingArtist.getArtistName())
				.append(" | ").append(nowPlayingArtist.getAlbumName());

		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
				.setThumbnail(CommandUtil.noImageUrl(nowPlayingArtist.getUrl()))
				.setTitle("Now Playing:", CommandUtil.getLastFmUser(username))
				.addField(nowPlayingArtist.isNowPlaying() ? "Current:" : "Last:", a.toString(), false);

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
		return Arrays.asList("np", "fm");
	}


}
