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
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;
import dao.entities.TimestampWrapper;
import dao.entities.UpdaterUserWrapper;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class UpdateCommand extends ConcurrentCommand {
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;


    public UpdateCommand(ChuuService dao) {
        super(dao);
        parser = new OnlyUsernameParser(dao, new OptionalEntity("--force", "Does a full heavy update"));
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    public String getDescription() {
        return "Keeps you up to date ";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("update");
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned = parser.parse(e);
        String lastFmName = returned[0];
        long discordID = Long.parseLong(returned[1]);
        boolean force = Boolean.parseBoolean(returned[2]);
        String userString = getUserString(e, discordID, lastFmName);
        if (e.isFromGuild()) {
            if (getService().getAll(e.getGuild().getIdLong()).stream()
                    .noneMatch(s -> s.getLastFMName().equals(lastFmName))) {
                sendMessageQueue(e, userString + " is not registered in this guild");
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
                sendMessageQueue(e, "You were already up to date!");
                return;
            }
        }
        sendMessageQueue(e, "Successfully updated " + userString + " info !");


    }

    @Override
    public String getName() {
        return "Update";
    }


}
