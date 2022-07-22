package core.commands.config;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.CustomTimeFrame;
import core.parsers.utils.OptionalEntity;
import core.services.UpdaterHoarder;
import core.services.UpdaterService;
import core.util.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class UpdateCommand extends ConcurrentCommand<ChuuDataParams> {


    public final AtomicInteger maxConcurrency = new AtomicInteger(5);
    private final ReentrantLock reentrantLock = new ReentrantLock();

    public UpdateCommand(ServiceView dao) {
        super(dao, false);
        ephemeral = true;
        order = 6;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STARTING;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db, new OptionalEntity("force", "reset all your scrobble data in the bot"));
    }

    @Override
    public String getDescription() {
        return "Keeps you up to date";
    }

    @Override
    public List<String> getAliases() {
        return List.of("update", "u");
    }

    @Override
    public void onCommand(Context e, @Nonnull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {

        LastFMData lastFMData = params.getLastFMData();
        String lastFmName = lastFMData.getName();
        long discordID = lastFMData.getDiscordId();
        if (lastFMData.isPrivateUpdate() && e.getAuthor().getIdLong() != discordID) {
            sendMessageQueue(e, "This user cannot be updated by other users");
            return;
        }
        db.findLastFMData(discordID);
        boolean force = params.hasOptional("force");
        String userString = getUserString(e, discordID, lastFmName);
        boolean removeFlag = true;
        try {
            if (!UpdaterService.lockAndContinue(lastFmName)) {
                removeFlag = false;
                sendMessageQueue(e, "You were already being updated. Wait a few seconds");
                return;
            }
            if (force) {
                reentrantLock.lock();
                try {
                    if (maxConcurrency.decrementAndGet() == 0) {
                        sendMessageQueue(e, "There are a lot of people executing this type of update, try again later :(");
                        maxConcurrency.incrementAndGet();
                    }
                } finally {
                    reentrantLock.unlock();
                }
                try {
                    List<ScrobbledArtist> artistData = lastFM.getAllArtists(lastFMData, CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                    db.insertArtistDataList(artistData, lastFmName);
                    List<ScrobbledAlbum> albumData = lastFM.getAllAlbums(lastFMData, CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                    e.getChannel().sendTyping().queue();
                    db.albumUpdate(albumData, artistData, lastFmName);
                    List<ScrobbledTrack> trackData = lastFM.getAllTracks(lastFMData, CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                    db.trackUpdate(trackData, artistData, lastFmName);
                    db.updateUserTimeStamp(lastFmName, lastFM.getLastScrobbleUTS(lastFMData), null);
                    sendMessageQueue(e, "Successfully force updated %s info!".formatted(userString));
                } finally {
                    maxConcurrency.incrementAndGet();
                }
            } else {
                UpdaterUserWrapper userUpdateStatus = db.getUserUpdateStatus(lastFMData.getDiscordId());
                try {
                    UpdaterHoarder updaterHoarder = new UpdaterHoarder(userUpdateStatus, db, lastFM, lastFMData);
                    int i = updaterHoarder.updateUser();
                    sendMessageQueue(e, "Successfully updated %s info with %d new %s".formatted(userString, i, CommandUtil.singlePlural(i, "scrobble", "scrobbles")));

                    //dao.incrementalUpdate(artistDataLinkedList, userUpdateStatus.getLastFMName());
                } catch (LastFMNoPlaysException ex) {
                    db.updateUserTimeStamp(userUpdateStatus.getLastFMName(), userUpdateStatus.getTimestamp(),
                            (int) (Instant.now().getEpochSecond() + 4000));
                    int timestamp = userUpdateStatus.getTimestamp();
                    long epochSecond = Instant.now().getEpochSecond();
                    String s = Duration.ofSeconds(epochSecond - timestamp).toString()
                            .substring(2)
                            .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                            .replaceAll("\\.\\d+", "")
                            .toLowerCase();
                    sendMessageQueue(e, "You were already up to date! Last scrobble was " + s + " ago");
                }
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
