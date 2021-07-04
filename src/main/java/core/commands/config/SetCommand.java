package core.commands.config;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ChartableParser;
import core.parsers.Parser;
import core.parsers.SetParser;
import core.parsers.params.WordParameter;
import core.parsers.utils.CustomTimeFrame;
import dao.ServiceView;
import dao.entities.*;
import dao.exceptions.DuplicateInstanceException;
import dao.exceptions.InstanceNotFoundException;

import javax.validation.constraints.NotNull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

public class SetCommand extends ConcurrentCommand<WordParameter> {
    public SetCommand(ServiceView dao) {
        super(dao);
        this.respondInPrivate = false;

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STARTING;
    }

    @Override
    public Parser<WordParameter> initParser() {
        return new SetParser();
    }

    @Override
    public String getDescription() {
        return "Adds you to the system";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("set");
    }

    @Override
    protected void onCommand(Context e, @NotNull WordParameter params) throws LastFmException, InstanceNotFoundException {


        String lastFmName = params.getWord();
        long guildID = e.getGuild().getIdLong();
        long userId = e.getAuthor().getIdLong();
        //Gets all users in this server

        try {
            try {
                // Just a dao check to avoid one api call
                LastFMData byLastfmName = db.findByLastfmName(lastFmName);
            } catch (InstanceNotFoundException ex) {
                String encode = URLEncoder.encode(lastFmName, StandardCharsets.UTF_8);
                if (!encode.equals(lastFmName)) {
                    sendMessageQueue(e, "The provided username doesn't exist on last.fm, choose another one");
                    return;
                }
                lastFM.getUserInfo(List.of(lastFmName), null);
            }
        } catch (LastFmEntityNotFoundException | IllegalArgumentException ex) {
            sendMessageQueue(e, "The provided username doesn't exist on last.fm, choose another one");
            return;
        }
        List<UsersWrapper> guildlist = db.getAll(guildID);
        if (guildlist.isEmpty()) {
            db.createGuild(guildID);
        }

        String repeatedMessage = "That username is already registered. If you own the account pls use: **" + e.getPrefix() + "login**\n" +
                                 "Any doubt you might have please contact the bot developers on the support server";
        boolean repeated;
        try {
            LastFMData byLastfmName = db.findByLastfmName(lastFmName);
            repeated = byLastfmName.getDiscordId() != userId;
        } catch (InstanceNotFoundException ex) {
            repeated = false;
        }

        if (repeated) {
            sendMessageQueue(e, repeatedMessage);
            return;
        }
        Optional<UsersWrapper> name = (guildlist.stream().filter(user -> user.getLastFMName().equals(lastFmName)).findFirst());
        //If name is already registered in this server
        if (name.isPresent()) {
            if (name.get().getDiscordID() != userId)
                sendMessageQueue(e, repeatedMessage);
            else
                sendMessageQueue(e, String.format("%s, you are good to go!", CommandUtil.escapeMarkdown(e.getAuthor().getName())));
            return;
        }

        Optional<UsersWrapper> u = (guildlist.stream().filter(user -> user.getDiscordID() == userId).findFirst());
        //User was already registered in this guild
        if (u.isPresent()) {
            //Registered with different username
            if (!u.get().getLastFMName().equalsIgnoreCase(lastFmName)) {
                sendMessageQueue(e, "Changing your username, might take a while");
                try {
                    db.changeLastFMName(userId, lastFmName);
                } catch (DuplicateInstanceException ex) {
                    sendMessageQueue(e, repeatedMessage);
                    return;
                }
            } else {
                sendMessageQueue(e, String.format("%s, you are good to go!", CommandUtil.escapeMarkdown(e.getAuthor().getName())));
                return;
            }
            //First Time on the guild
        } else {
            //If it was registered in at least other  guild theres no need to update
            if (db.getGuildList(userId).stream().anyMatch(guild -> guild != guildID)) {
                //Adds the user to the guild
                if (db.isUserServerBanned(userId, guildID)) {
                    sendMessageQueue(e, String.format("%s, you have been not allowed to appear on the server leaderboards as a choice of this server admins. Rest of commands should work fine.", CommandUtil.escapeMarkdown(e.getAuthor().getName())));
                    return;
                }
                db.addGuildUser(userId, guildID);
                sendMessageQueue(e, String.format("%s, you are good to go!", CommandUtil.escapeMarkdown(e.getAuthor().getName())));
                return;
            }
        }

        //Never registered before
        String sb = "**" + CommandUtil.escapeMarkdown(e.getAuthor()
                        .

                getName()) + "** has set their last FM name \n Updating your library, wait a moment";
        e.sendMessage(sb).
                queue(t -> e.getChannel().
                        sendTyping().
                        queue());
        LastFMData lastFMData = new LastFMData(lastFmName, userId, Role.USER, false, true, WhoKnowsMode.IMAGE, ChartMode.IMAGE, RemainingImagesMode.IMAGE, ChartableParser.DEFAULT_X, ChartableParser.DEFAULT_Y, PrivacyMode.NORMAL, true, false, true, TimeZone.getDefault(), null, null, true, EmbedColor.defaultColor(), false, 0, ChartOptions.defaultMode());
        lastFMData.setGuildID(guildID);

        db.insertNewUser(lastFMData);

        setProcess(e, lastFmName, userId, lastFMData, e.getAuthor().getName());

    }

    public void setProcess(Context e, String lastFmID, long userId, LastFMData lastFMData, String name) {
        try {


            List<ScrobbledArtist> artistData = lastFM.getAllArtists(lastFMData, new CustomTimeFrame(TimeFrameEnum.ALL));
            db.insertArtistDataList(artistData, lastFmID);
            e.sendMessage("Finished updating your artist, now the album/track process will start").queue();
            e.getChannel().sendTyping().queue();
            List<ScrobbledAlbum> albumData = lastFM.getAllAlbums(lastFMData, new CustomTimeFrame(TimeFrameEnum.ALL));
            db.albumUpdate(albumData, artistData, lastFmID);
            List<ScrobbledTrack> trackData = lastFM.getAllTracks(lastFMData, CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
            db.trackUpdate(trackData, artistData, lastFmID);
            e.sendMessage("Successfully updated " + lastFmID + " info!").queue();
            int lastScrobbleUTS = lastFM.getLastScrobbleUTS(lastFMData);
            db.updateUserTimeStamp(lastFmID, lastScrobbleUTS, null);
            //  e.getGuild().loadMembers((Chuu::caching));
        } catch (
                LastFMNoPlaysException ex) {
            db.updateUserTimeStamp(lastFmID, Math.toIntExact(Instant.now().getEpochSecond()), null);
            e.sendMessage("Finished updating " + CommandUtil.escapeMarkdown(name) + "'s library, you are good to go!").queue();
        } catch (
                LastFmEntityNotFoundException ex) {
            db.removeUserCompletely(userId);
            Chuu.getLogger().warn(ex.getMessage(), ex);
            e.sendMessage("The provided username doesn't exist anymore on last.fm, please re-set your account").queue();
        } catch (
                Throwable ex) {

            Chuu.getLogger().warn("Error while updating an user: " + lastFmID, ex);
            db.updateUserTimeStamp(lastFmID, 954682307, null);
            e.sendMessage("Error downloading your library, try to run !update, try again later or contact bot admins if the error persists").queue();
        }
    }


    @Override
    public String getName() {
        return "Set";
    }


}
