package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ArtistPlaysCommand extends ConcurrentCommand {
    public ArtistPlaysCommand(ChuuService dao) {
        super(dao);
        parser = new ArtistParser(dao, lastFM);
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
        String[] returned;
        returned = parser.parse(e);
        if (returned == null)
            return;
        String artist = returned[0];
        ScrobbledArtist scrobbledArtist = CommandUtil.onlyCorrection(getService(), artist, lastFM);
        long whom = Long.parseLong(returned[1]);
        int a;
        LastFMData data = getService().findLastFMData(whom);

        a = getService().getArtistPlays(scrobbledArtist.getArtistId(), data.getName());
        String usernameString = getUserStringConsideringGuildOrNot(e, whom, data.getName());
        String ending = a != 1 ? "times" : "time";
        sendMessageQueue(e, "**" + usernameString + "** has scrobbled **" + scrobbledArtist.getArtist() + " " + a + "** " + ending);

    }
}
