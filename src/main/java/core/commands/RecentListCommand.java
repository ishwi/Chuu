package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class RecentListCommand extends ConcurrentCommand {
    private static final int LIMIT = 5;

    public RecentListCommand(ChuuService dao) {
        super(dao);
        this.parser = new OnlyUsernameParser(dao);
    }

    @Override
    public String getDescription() {
        return "Returns your most recent songs played";
    }

    @Override
	public List<String> getAliases() {
		return Collections.singletonList("recent");
	}

	@Override
	protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned = parser.parse(e);
		String lastFmName = returned[0];
		long discordID = Long.parseLong(returned[1]);
		String usable = getUserStringConsideringGuildOrNot(e, discordID, lastFmName);

		List<NowPlayingArtist> list = lastFM.getRecent(lastFmName, LIMIT);
			//Can't be empty because NoPLaysException
			NowPlayingArtist header = list.get(0);

			EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
					.setThumbnail(CommandUtil.noImageUrl(header.getUrl()))
					.setTitle("" + usable + "'s last " + LIMIT + " tracks",
							CommandUtil.getLastFmUser(lastFmName));

			int counter = 1;
			for (NowPlayingArtist nowPlayingArtist : list) {
				embedBuilder.addField("Track #" + counter++ + ":", "**" + nowPlayingArtist.getSongName() +
						"** - " + nowPlayingArtist.getArtistName() + " | " + nowPlayingArtist
						.getAlbumName() + "\n", false);
			}

			MessageBuilder messageBuilder = new MessageBuilder();
			messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();



	}

	@Override
	public String getName() {
		return "Recent";
	}


}
