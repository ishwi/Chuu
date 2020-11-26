package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.CustomTimeFrame;
import core.services.UpdaterHoarder;
import core.services.UpdaterService;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class UpdateCommand extends ConcurrentCommand<ChuuDataParams> {
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;


    public UpdateCommand(ChuuService dao) {
        super(dao);
        parser = new OnlyUsernameParser(dao, new OptionalEntity("force", "Does a full heavy update"), new OptionalEntity("beta", "Does a beta update"));

        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STARTING;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(getService(), new OptionalEntity("force", "Does a full heavy update"));
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
    public void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {

        LastFMData lastFMData = params.getLastFMData();
        String lastFmName = lastFMData.getName();
        long discordID = lastFMData.getDiscordId();
        if (lastFMData.isPrivateUpdate() && e.getAuthor().getIdLong() != discordID) {
            sendMessageQueue(e, "This user cannot be updated by other users");
            return;
        }
        getService().findLastFMData(discordID);
        boolean force = params.hasOptional("force");
        boolean beta = params.hasOptional("beta");
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
        boolean removeFlag = true;
        try {
            if (!UpdaterService.lockAndContinue(lastFmName)) {
                removeFlag = false;
                sendMessageQueue(e, "You were already being updated. Wait a few seconds");
                return;
            }

            if (beta) {
                if (!e.isFromGuild() || e.getGuild().getIdLong() != 693124899220226178L) {
                    sendMessageQueue(e, "This feature is in beta and is not available yet");
                    return;
                }
                List<ScrobbledArtist> artistData = lastFM.getAllArtists(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                getService().insertArtistDataList(artistData, lastFmName);

                List<ScrobbledTrack> trackData = lastFM.getAllTracks(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                getService().trackUpdate(trackData, artistData, lastFmName);
                sendMessageQueue(e, "S");
                sendMessageQueue(e, "Successfully updated " + userString + " info!");

            } else if (force) {
                e.getChannel().sendTyping().queue();
                List<ScrobbledArtist> artistData = lastFM.getAllArtists(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                getService().insertArtistDataList(artistData, lastFmName);
                e.getChannel().sendTyping().queue();
                //sendMessageQueue(e, "Finished updating your artist, now the album process will start");
                List<ScrobbledAlbum> albumData = lastFM.getAllAlbums(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                e.getChannel().sendTyping().queue();
                getService().albumUpdate(albumData, artistData, lastFmName);
            } else {
                UpdaterUserWrapper userUpdateStatus = getService().getUserUpdateStatus(lastFMData.getDiscordId());
                try {
                    UpdaterHoarder updaterHoarder = new UpdaterHoarder(userUpdateStatus, getService(), lastFM);
                    updaterHoarder.updateUser();
                    //getService().incrementalUpdate(artistDataLinkedList, userUpdateStatus.getLastFMName());
                } catch (LastFMNoPlaysException ex) {
                    getService().updateUserTimeStamp(userUpdateStatus.getLastFMName(), userUpdateStatus.getTimestamp(),
                            (int) (Instant.now().getEpochSecond() + 4000));
                    sendMessageQueue(e, "You were already up to date! If you consider you are not really up to date run this command again with **`--force`**");
                    return;
                }

                sendMessageQueue(e, "Successfully updated " + userString + " info!");


            }
        } finally {
            if (removeFlag) {
                UpdaterService.remove(lastFmName);
            }
        }

    }

    @Override
    public String getName() {
        return "Update";
    }


}
