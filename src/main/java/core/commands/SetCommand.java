package core.commands;

import core.Chuu;
import core.exceptions.*;
import core.parsers.Parser;
import core.parsers.SetParser;
import core.parsers.params.WordParameter;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SetCommand extends ConcurrentCommand<WordParameter> {
    public SetCommand(ChuuService dao) {
        super(dao);
        parser = new SetParser();
        this.respondInPrivate = false;

    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.STARTING;
    }

    @Override
    public Parser<WordParameter> getParser() {
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
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        WordParameter returned = parser.parse(e);
        if (returned == null)
            return;

        MessageBuilder mes = new MessageBuilder();
        String lastFmID = returned.getWord();
        long guildID = e.getGuild().getIdLong();
        long userId = e.getAuthor().getIdLong();
        //Gets all users in this server

        try {
            lastFM.getUserInfo(List.of(lastFmID));
        } catch (LastFmEntityNotFoundException | IllegalArgumentException ex) {
            sendMessageQueue(e, "The provided username doesn't exist on last.fm, choose another one");
            return;
        }
        List<UsersWrapper> guildlist = getService().getAll(guildID);
        if (guildlist.isEmpty()) {
            getService().createGuild(guildID);
        }

        List<UsersWrapper> list = getService().getAllALL();
        Optional<UsersWrapper> globalName = (list.stream().filter(user -> user.getLastFMName().equals(lastFmID)).findFirst());
        String repeatedMessage = "That username is already registered, if you think this is a mistake, please contact the bot developers";
        if (globalName.isPresent() && (globalName.get().getDiscordID() != userId)) {
            sendMessageQueue(e, repeatedMessage);
            return;
        }

        Optional<UsersWrapper> name = (guildlist.stream().filter(user -> user.getLastFMName().equals(lastFmID)).findFirst());
        //If name is already registered in this server
        if (name.isPresent()) {
            if (name.get().getDiscordID() != userId)
                sendMessageQueue(e, repeatedMessage);
            else
                sendMessageQueue(e, String.format("%s, you are good to go!", CommandUtil.cleanMarkdownCharacter(e.getAuthor().getName())));
            return;
        }

        Optional<UsersWrapper> u = (guildlist.stream().filter(user -> user.getDiscordID() == userId).findFirst());
        //User was already registered in this guild
        if (u.isPresent()) {
            //Registered with different username
            if (!u.get().getLastFMName().equalsIgnoreCase(lastFmID)) {
                sendMessageQueue(e, "Changing your username, might take a while");
                try {
                    getService().changeLastFMName(userId, lastFmID);
                } catch (DuplicateInstanceException ex) {
                    sendMessageQueue(e, repeatedMessage);
                    return;
                }
            } else {
                sendMessageQueue(e, String.format("%s, you are goo d to go!", CommandUtil.cleanMarkdownCharacter(e.getAuthor().getName())));
                return;
            }
            //First Time on the guild
        } else {
            //If it was registered in at least other  guild theres no need to update
            if (getService().getGuildList(userId).stream().anyMatch(guild -> guild != guildID)) {
                //Adds the user to the guild
                getService().addGuildUser(userId, guildID);
                sendMessageQueue(e, String.format("%s, you are good to go!", CommandUtil.cleanMarkdownCharacter(e.getAuthor().getName())));
                return;
            }
        }


        //Never registered before
        mes.setContent("**" + CommandUtil.cleanMarkdownCharacter(e.getAuthor()
                .

                        getName()) + "** has set their last FM name \n Updating your library, wait a moment");
        mes.sendTo(e.getChannel()).

                queue(t -> e.getChannel().

                        sendTyping().

                        queue());
        LastFMData lastFMData = new LastFMData(lastFmID, userId, Role.USER, false, true, false);
        lastFMData.setGuildID(guildID);

        getService().

                insertNewUser(lastFMData);

        try {

            List<ScrobbledArtist> allArtists = lastFM.getAllArtists(lastFmID, TimeFrameEnum.ALL.toApiFormat());
            getService().insertArtistDataList(allArtists, lastFmID);
            sendMessageQueue(e, "Finished updating " + CommandUtil.cleanMarkdownCharacter(e.getAuthor().getName()) + " library, you are good to go!");
        } catch (
                LastFMNoPlaysException ex) {
            sendMessageQueue(e, "Finished updating " + CommandUtil.cleanMarkdownCharacter(e.getAuthor().getName()) + "'s library, you are good to go!");
        } catch (
                LastFmEntityNotFoundException ex) {
            getService().removeUserCompletely(userId);
            Chuu.getLogger().warn(ex.getMessage(), ex);
            sendMessageQueue(e, "The provided username doesn't exist anymore on last.fm, please re-set your account");
        } catch (
                Throwable ex) {
            System.out.println("Error while updating " + lastFmID + LocalDateTime.now()
                    .format(DateTimeFormatter.ISO_DATE));
            Chuu.getLogger().warn(ex.getMessage(), ex);
            getService().updateUserTimeStamp(lastFmID, 0, null);
            sendMessageQueue(e, String.format("Error downloading %s's library, try to run !update, try again later or contact bot admins if the error persists", CommandUtil.cleanMarkdownCharacter(e.getAuthor()
                    .getName())));
        }

    }

    @Override
    public String getName() {
        return "Set";
    }


}
