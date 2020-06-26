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
import java.util.*;
import java.util.stream.Collectors;

import static core.scheduledtasks.UpdaterThread.groupAlbumsToArtist;

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
        long userAlbumCount = getService().getUserAlbumCount(discordID);
        int userArtistCount = getService().getUserArtistCount(lastFmName, 0);

        if (force || userAlbumCount < 0.86 * userArtistCount) {
            if (!force) {
                sendMessageQueue(e, "Will run a full update to index your albums. Takes a while :pensive:");
            }
            e.getChannel().sendTyping().queue();
            List<ScrobbledArtist> artistData = lastFM.getAllArtists(lastFMData.getName(), TimeFrameEnum.ALL.toApiFormat());
            getService().insertArtistDataList(artistData, lastFmName);
            e.getChannel().sendTyping().queue();
            //sendMessageQueue(e, "Finished updating your artist, now the album process will start");
            List<ScrobbledAlbum> albumData = lastFM.getALlAlbums(lastFMData.getName(), TimeFrameEnum.ALL.toApiFormat());
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


    }

    @Override
    public String getName() {
        return "Update";
    }


}
