package core.commands;

import core.Chuu;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.SetParser;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;
import dao.entities.UsersWrapper;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SetCommand extends ConcurrentCommand {
    public SetCommand(ChuuService dao) {
        super(dao);
        parser = new SetParser();
        this.respondInPrivate = false;

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
        String[] returned;

        returned = parser.parse(e);
        if (returned == null)
            return;

        MessageBuilder mes = new MessageBuilder();
        String lastFmID = returned[0];
        long guildID = e.getGuild().getIdLong();
        long userId = e.getAuthor().getIdLong();
        List<UsersWrapper> list = getService().getAll(guildID);
        if (list.isEmpty()) {
            getService().createGuild(guildID);
        }
        Optional<UsersWrapper> name = (list.stream().filter(user -> user.getLastFMName().equals(lastFmID)).findFirst());
        if (name.isPresent()) {

            if (name.get().getDiscordID() == userId)
                sendMessageQueue(e, "You already have that username!");
            else
                sendMessageQueue(e, "That username is already registered in this server sorry");

            return;
        }

        Optional<UsersWrapper> u = (list.stream().filter(user -> user.getDiscordID() == userId).findFirst());
        //User was already registered in this guild
        if (u.isPresent()) {
            //Registered with different username
            if (!u.get().getLastFMName().equals(lastFmID)) {
                sendMessageQueue(e, "Changing your username, might take a while");
                //Remove but only from the guild if not guild removeUser all
                getService().removeUserFromOneGuildConsequent(userId, guildID);
            } else {
                sendMessageQueue(e, e.getAuthor().getName() + " , you are good to go!");
                return;
            }
            //First Time on the guild
        } else {
            //If it was registered in at least other  guild theres no need to update
            if (getService().getGuildList(userId).stream().anyMatch(user -> user != guildID)) {
                //Adds the user to the guild
                getService().addGuildUser(userId, guildID);
                sendMessageQueue(e, e.getAuthor().getName() + " , you are good to go!");
                return;

            }
        }

        //Never registered before
        mes.setContent("**" + e.getAuthor()
                .getName() + "** has set their last FM name \n Updating your library, wait a moment");
        mes.sendTo(e.getChannel()).queue(t -> e.getChannel().sendTyping().queue());


        try {
            List<ScrobbledArtist> scrobbledArtistLinkedList = lastFM.getAllArtists(lastFmID, TimeFrameEnum.ALL.toApiFormat());

            getService().insertArtistDataList(new LastFMData(lastFmID, userId, guildID));
            getService().insertArtistDataList(scrobbledArtistLinkedList, lastFmID);
            System.out.println("Updated Info Normally  of " + lastFmID + LocalDateTime
                    .now().format(DateTimeFormatter.ISO_DATE));

            System.out.println(" Number of rows updated :" + scrobbledArtistLinkedList.size());
            sendMessageQueue(e, "Finished updating " + e.getAuthor().getName() + " library, you are good to go!");
        } catch (
                LastFMNoPlaysException ex) {
            getService().updateUserTimeStamp(lastFmID, null, null);
            sendMessageQueue(e, "Finished updating " + e.getAuthor().getName() + "'s library, you are good to go!");

        } catch (LastFmEntityNotFoundException ex) {
            sendMessageQueue(e, "The provided username doesn't exist on last.fm, choose another one");

        } catch (Throwable ex) {
            System.out.println("Error while updating " + lastFmID + LocalDateTime.now()
                    .format(DateTimeFormatter.ISO_DATE));
            Chuu.getLogger().warn(ex.getMessage(), ex);
            getService().updateUserTimeStamp(lastFmID, 0, null);
            sendMessageQueue(e, "Error  updating " + e.getAuthor()
                    .getName() + "'s  library, try to use the !update command!");
        }


    }

    @Override
    public String getName() {
        return "Set";
    }


}
