package core.commands;

import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.ArtistSummary;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

public class ArtistPlaysCommand extends ConcurrentCommand<ArtistParameters> {
    public ArtistPlaysCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
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
    public void onCommand(MessageReceivedEvent e, @NotNull ArtistParameters params) throws LastFmException, InstanceNotFoundException {

        ScrobbledArtist scrobbledArtist = CommandUtil.onlyCorrection(getService(), params.getArtist(), lastFM, !params.isNoredirect());
        long whom = params.getLastFMData().getDiscordId();
        int a;
        LastFMData data = params.getLastFMData();
        ArtistSummary artistSummary = lastFM.getArtistSummary(scrobbledArtist.getArtist(), data.getName());
        a = artistSummary.getUserPlayCount();
        String usernameString = getUserString(e, whom, data.getName());
        String ending = a != 1 ? "times" : "time";
        sendMessageQueue(e, "**" + usernameString + "** has scrobbled **" + CommandUtil.cleanMarkdownCharacter(artistSummary.getArtistname()) + " " + a + "** " + ending);

    }
}
