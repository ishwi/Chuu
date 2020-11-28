package core.commands;

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
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateCommand extends ConcurrentCommand<ChuuDataParams> {


    public AtomicInteger maxConcurrency = new AtomicInteger(5);

    public UpdateCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STARTING;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(getService(), new OptionalEntity("force", "Does a full heavy update"), new OptionalEntity("beta", "Does a beta update"));
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
                synchronized (this) {
                    if (maxConcurrency.decrementAndGet() == 0) {
                        sendMessageQueue(e, "There are a lot of people executing this type of update, try again later :(");
                        maxConcurrency.incrementAndGet();
                    }
                }
                try {
                    List<ScrobbledArtist> artistData = lastFM.getAllArtists(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                    getService().insertArtistDataList(artistData, lastFmName);
                    List<ScrobbledAlbum> albumData = lastFM.getAllAlbums(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                    e.getChannel().sendTyping().queue();
                    getService().albumUpdate(albumData, artistData, lastFmName);
                    List<ScrobbledTrack> trackData = lastFM.getAllTracks(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                    getService().trackUpdate(trackData, artistData, lastFmName);
                    sendMessageQueue(e, "Successfully updated " + userString + " info!");
                } finally {
                    synchronized (this) {
                        maxConcurrency.incrementAndGet();
                    }
                }
            } else if (force) {
                e.getChannel().sendTyping().queue();
                List<ScrobbledArtist> artistData = lastFM.getAllArtists(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                getService().insertArtistDataList(artistData, lastFmName);
                e.getChannel().sendTyping().queue();
                //sendMessageQueue(e, "Finished updating your artist, now the album process will start");
                List<ScrobbledAlbum> albumData = lastFM.getAllAlbums(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                e.getChannel().sendTyping().queue();
                getService().albumUpdate(albumData, artistData, lastFmName);
                sendMessageQueue(e, "Successfully updated " + userString + " info!");

            } else {
                UpdaterUserWrapper userUpdateStatus = getService().getUserUpdateStatus(lastFMData.getDiscordId());
                List<ScrobbledTrack> topTracks = getService().getTopTracks(userUpdateStatus.getLastFMName());
                if (topTracks.size() < getService().getUserArtistCount(lastFmName, 0)) {
                    if (maxConcurrency.decrementAndGet() == 0) {
                        maxConcurrency.incrementAndGet();
                        UpdaterHoarder updaterHoarder = new UpdaterHoarder(userUpdateStatus, getService(), lastFM);
                        updaterHoarder.updateUser();

                    } else {
                        try {
                            sendMessageQueue(e, "Since you haven't indexed your tracks yet, all your tracks will be indexed on the bot. This will take a while.");
                            e.getChannel().sendTyping().queue();
                            List<ScrobbledArtist> artistData = lastFM.getAllArtists(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                            getService().insertArtistDataList(artistData, lastFmName);
                            e.getChannel().sendTyping().queue();
                            //sendMessageQueue(e, "Finished updating your artist, now the album process will start");
                            List<ScrobbledAlbum> albumData = lastFM.getAllAlbums(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                            e.getChannel().sendTyping().queue();
                            getService().albumUpdate(albumData, artistData, lastFmName);
                            List<ScrobbledTrack> trackData = lastFM.getAllTracks(lastFMData.getName(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                            getService().trackUpdate(trackData, artistData, lastFmName);
                            sendMessageQueue(e, "Successfully indexed " + userString + " tracks");
                        } finally {
                            synchronized (this) {
                                maxConcurrency.incrementAndGet();
                            }
                        }
                        return;
                    }
                } else {
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
