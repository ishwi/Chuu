package core.commands;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.CustomTimeFrame;
import core.services.UpdaterService;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static core.scheduledtasks.UpdaterThread.groupAlbumsToArtist;

public class UpdateCommand extends ConcurrentCommand<ChuuDataParams> {
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;


    public UpdateCommand(ChuuService dao) {
        super(dao);
        parser = new OnlyUsernameParser(dao, new OptionalEntity("force", "Does a full heavy update"));
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


            if (force) {
                e.getChannel().sendTyping().queue();
                List<ScrobbledArtist> artistData = lastFM.getAllArtists(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                getService().insertArtistDataList(artistData, lastFmName);
                e.getChannel().sendTyping().queue();
                //sendMessageQueue(e, "Finished updating your artist, now the album process will start");
                List<ScrobbledAlbum> albumData = lastFM.getAllAlbums(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                e.getChannel().sendTyping().queue();
                getService().albumUpdate(albumData, artistData, lastFmName);
            } else {
                UpdaterUserWrapper userUpdateStatus = null;

                try {
                    userUpdateStatus = getService().getUserUpdateStatus(discordID);
                    TimestampWrapper<List<ScrobbledAlbum>> albumDataList = lastFM
                            .getNewWhole(lastFmName,
                                    userUpdateStatus.getTimestamp());


                    // Correction with current last fm implementation should return the same name so
                    // no correction gives
                    List<ScrobbledAlbum> albumData = albumDataList.getWrapped();
                    List<ScrobbledArtist> artistData = groupAlbumsToArtist(albumData);
                    albumData = albumData.stream().filter(x -> x != null && !x.getAlbum().isBlank()).collect(Collectors.toList());
                    Map<String, ScrobbledArtist> changedName = new HashMap<>();
                    for (Iterator<ScrobbledArtist> iterator = artistData.iterator(); iterator.hasNext(); ) {
                        ScrobbledArtist datum = iterator.next();
                        try {
                            String artist = datum.getArtist();
                            CommandUtil.validate(getService(), datum, lastFM, discogsApi, spotifyApi);
                            String newArtist = datum.getArtist();
                            if (!artist.equals(newArtist)) {
                                ScrobbledArtist scrobbledArtist = new ScrobbledArtist(newArtist, 0, null);
                                scrobbledArtist.setArtistId(datum.getArtistId());
                                changedName.put(artist, scrobbledArtist);
                            }
                        } catch (LastFmEntityNotFoundException ex) {
                            Chuu.getLogger().error("WTF ARTIST DELETED" + datum.getArtist());
                            iterator.remove();
                        }
                    }

                    albumData.forEach(x -> {
                        ScrobbledArtist scrobbledArtist = changedName.get(x.getArtist());
                        if (scrobbledArtist != null) {
                            x.setArtist(scrobbledArtist.getArtist());
                            x.setArtistId(scrobbledArtist.getArtistId());
                        }
                    });

                    getService().incrementalUpdate(new TimestampWrapper<>(artistData, albumDataList.getTimestamp()), lastFmName, albumData);                //getService().incrementalUpdate(artistDataLinkedList, userUpdateStatus.getLastFMName());
                } catch (LastFMNoPlaysException ex) {
                    getService().updateUserTimeStamp(userUpdateStatus.getLastFMName(), userUpdateStatus.getTimestamp(),
                            (int) (Instant.now().getEpochSecond() + 4000));
                    sendMessageQueue(e, "You were already up to date! If you consider you are not really up to date run this command again with **`--force`**");
                    return;
                }
            }
            sendMessageQueue(e, "Successfully updated " + userString + " info!");
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
