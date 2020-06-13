package core.commands;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class UpdateCommand extends ConcurrentCommand<ChuuDataParams> {
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.STARTING;
    }

    public UpdateCommand(ChuuService dao) {
        super(dao);
        parser = new OnlyUsernameParser(dao, new OptionalEntity("--force", "Does a full heavy update"));
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    public Parser<ChuuDataParams> getParser() {
        return new OnlyUsernameParser(getService(), new OptionalEntity("--force", "Does a full heavy update"));
    }

    @Override
    public String getDescription() {
        return "Keeps you up to date";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("update");
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ChuuDataParams params = parser.parse(e);
        LastFMData lastFMData = params.getLastFMData();
        String lastFmName = lastFMData.getName();
        long discordID = lastFMData.getDiscordId();
        if (lastFMData.isPrivateUpdate() && e.getAuthor().getIdLong() != discordID) {
            sendMessageQueue(e, "This user cannot be updated by other users");
            return;
        }
        getService().findLastFMData(discordID);
        boolean force = params.hasOptional("--force");
        String userString = getUserString(e, discordID, lastFmName);
        if (e.isFromGuild()) {
            if (getService().getAll(e.getGuild().getIdLong()).stream()
                    .noneMatch(s -> s.getLastFMName().equals(lastFmName))) {
                sendMessageQueue(e, userString + " is not registered in this server");
                return;
            }
        } else if (!getService().getMapGuildUsers().containsValue(e.getAuthor().getIdLong())) {
            sendMessageQueue(e, "You are not registered yet, go to any server and register there!");
            return;
        }

        if (force) {
            List<ScrobbledArtist> list = lastFM.getAllArtists(lastFmName, TimeFrameEnum.ALL.toApiFormat());
            getService().insertArtistDataList(list, lastFmName);
        } else {
            UpdaterUserWrapper userUpdateStatus = null;

            try {
                userUpdateStatus = getService().getUserUpdateStatus(discordID);

                TimestampWrapper<List<ScrobbledArtist>> artistDataLinkedList = lastFM
                        .getWhole(userUpdateStatus.getLastFMName(),
                                userUpdateStatus.getTimestamp());

                // Correction with current last fm implementation should return the same name so
                // no correction gives
                for (Iterator<ScrobbledArtist> iterator = artistDataLinkedList.getWrapped().iterator(); iterator.hasNext(); ) {
                    ScrobbledArtist datum = iterator.next();
                    try {
                        CommandUtil.validate(getService(), datum, lastFM, discogsApi, spotifyApi);
                    } catch (LastFmEntityNotFoundException ex) {
                        Chuu.getLogger().error("WTF ARTIST DELETED {} ", datum.getArtist());
                        iterator.remove();
                    }
                }

                getService().incrementalUpdate(artistDataLinkedList, userUpdateStatus.getLastFMName());
            } catch (LastFMNoPlaysException ex) {
                getService().updateUserTimeStamp(userUpdateStatus.getLastFMName(), userUpdateStatus.getTimestamp(),
                        (int) (Instant.now().getEpochSecond() + 4000));
                sendMessageQueue(e, "You were already up to date! If you consider you are not really up to date run this command again with **`--force`**");
                return;
            }
        }
        sendMessageQueue(e, "Successfully updated " + userString + " info!");


    }

    @Override
    public String getName() {
        return "Update";
    }


}
