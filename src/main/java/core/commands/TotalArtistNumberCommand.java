package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class TotalArtistNumberCommand extends ConcurrentCommand<ChuuDataParams> {
    public TotalArtistNumberCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> getParser() {
        return new OnlyUsernameParser(getService());
    }

    @Override
    public String getDescription() {
        return ("Number of artists listened by an user");
    }

    @Override
    public List<String> getAliases() {
        return List.of("artists", "art");
    }

    @Override
    protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ChuuDataParams params = parser.parse(e);
        String lastFmName = params.getLastFMData().getName();
        long discordID = params.getLastFMData().getDiscordId();
        String username = getUserString(e, discordID, lastFmName);

        int plays = getService().getUserArtistCount(lastFmName);
        sendMessageQueue(e, String.format("**%s** has scrobbled **%d** different artists", username, plays));

    }

    @Override
    public String getName() {
        return "Artist count ";
    }
}
