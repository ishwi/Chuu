package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class TotalArtistNumberCommand extends ConcurrentCommand {
    public TotalArtistNumberCommand(ChuuService dao) {
        super(dao);
        this.parser = new OnlyUsernameParser(dao);
    }

    @Override
    public String getDescription() {
        return ("Artists count of user ");
    }

    @Override
	public List<String> getAliases() {
		return Collections.singletonList("scrobbled");
	}

	@Override
	protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned = parser.parse(e);
        String lastFmName = returned[0];
        long discordID = Long.parseLong(returned[1]);
        String username = getUserString(e, discordID, lastFmName);

        int plays = getService().getUserArtistCount(lastFmName);
        sendMessageQueue(e, "**" + username + "** has scrobbled **" + plays + "** different artists");

    }

	@Override
	public String getName() {
		return "Artist count ";
	}
}
