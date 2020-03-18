package core.commands;

import core.exceptions.DuplicateInstanceException;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.TwoArtistsParser;
import dao.ChuuService;
import dao.entities.ArtistSummary;
import dao.entities.LastFMData;
import dao.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class AliasCommand extends ConcurrentCommand {

    public AliasCommand(ChuuService dao) {
        super(dao);
        this.parser = new TwoArtistsParser();
    }

    @Override
    public String getDescription() {
        return "Let's you alias an artist to another";
    }

    @Override
    public List<String> getAliases() {
        return List.of("alias");
    }

    @Override
    public String getName() {
        return "Alias";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] message = parser.parse(e);
        if (message == null) {
            return;
        }
        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = getService().findLastFMData(idLong);
        String alias = message[0];
        String to = message[1];
        long artistId;
        String corrected = getService().findCorrection(alias);
        if (corrected != null) {
            sendMessageQueue(e, "The alias: " + CommandUtil.cleanMarkdownCharacter(alias) + " already exists on the bot");
            return;
        }
        try {
            artistId = getService().getArtistId(to);
        } catch (InstanceNotFoundException ex) {
            sendMessageQueue(e, "The artist: " + CommandUtil.cleanMarkdownCharacter(to) + " doesn't exist in the bot");
            return;
        }
        try {
            getService().getArtistId(alias);
            sendMessageQueue(e, "The alias: " + CommandUtil.cleanMarkdownCharacter(alias) + " points to an existing artist within the bot!");
            return;
        } catch (InstanceNotFoundException ex) {
            try {
                ArtistSummary artistSummary = lastFM.getArtistSummary(alias, lastFMData.getName());
                if (artistSummary.getListeners() > 1000) {
                    sendMessageQueue(e, "The alias: " + CommandUtil.cleanMarkdownCharacter(alias) + " is an existing artist in last.fm!");
                    return;
                }
            } catch (LastFmEntityNotFoundException ignored) {
            }
        }

        if (!lastFMData.getRole().equals(Role.ADMIN)) {
            getService().enqueAlias(alias, artistId, idLong);
            sendMessageQueue(e, "Your alias will be added to the review queue, only bot admins can add an alias");

        } else {
            try {
                getService().addAlias(alias, artistId);
                sendMessageQueue(e, "Succesfully aliased " + alias + " to " + to);
            } catch (DuplicateInstanceException ex) {
                sendMessageQueue(e, "The alias: " + alias + " is an already existing alias within the bot");
            }
        }


    }
}
