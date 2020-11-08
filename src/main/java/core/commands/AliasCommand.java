package core.commands;

import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.TwoArtistsParser;
import core.parsers.params.TwoArtistParams;
import dao.ChuuService;
import dao.entities.ArtistSummary;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.DuplicateInstanceException;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

public class AliasCommand extends ConcurrentCommand<TwoArtistParams> {

    public AliasCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<TwoArtistParams> initParser() {
        return new TwoArtistsParser();
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
    void onCommand(MessageReceivedEvent e, @NotNull TwoArtistParams params) throws LastFmException, InstanceNotFoundException {

        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = getService().findLastFMData(idLong);
        String alias = params.getFirstArtist();
        String to = params.getSecondArtist();
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
                //We know it doesnt exists on last
            }
        }

        if (!lastFMData.getRole().equals(Role.ADMIN)) {
            getService().enqueAlias(alias, artistId, idLong);
            sendMessageQueue(e, "Your alias will be added to the review queue, only bot admins can add an alias");

        } else {
            try {
                getService().addAlias(alias, artistId);
                sendMessageQueue(e, "Successfully aliased " + alias + " to " + to);
            } catch (DuplicateInstanceException ex) {
                sendMessageQueue(e, "The alias: " + alias + " is an already existing alias within the bot");
            }
        }


    }
}
