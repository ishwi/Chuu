package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ArtistPlaysCommand extends ConcurrentCommand<ArtistParameters> {
    public ArtistPlaysCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistParameters> getParser() {
        return new ArtistParser(getService(), lastFM);
    }

    @Override
    public String getDescription() {
        return "Gets the amount of times an user has played an specific artist";
    }

    @Override
    public List<String> getAliases() {
        return List.of("plays", "p");
    }

    @Override
    public String getName() {
        return "Plays on a specific artist";
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ArtistParameters returned = parser.parse(e);
        if (returned == null)
            return;
        ScrobbledArtist scrobbledArtist = CommandUtil.onlyCorrection(getService(), returned.getArtist(), lastFM, !returned.isNoredirect());
        long whom = returned.getLastFMData().getDiscordId();
        int a;
        LastFMData data = returned.getLastFMData();
        a = lastFM.getArtistSummary(scrobbledArtist.getArtist(), data.getName()).getUserPlayCount();
        String usernameString = getUserString(e, whom, data.getName());
        String ending = a != 1 ? "times" : "time";
        sendMessageQueue(e, "**" + usernameString + "** has scrobbled **" + CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist()) + " " + a + "** " + ending);

    }
}
