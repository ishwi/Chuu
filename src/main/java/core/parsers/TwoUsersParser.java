package core.parsers;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class TwoUsersParser extends DaoParser {
    public TwoUsersParser(ChuuService dao) {
        super(dao);
    }

    public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        String[] message = getSubMessage(e.getMessage());
        if (!e.isFromGuild()) {
            sendError("Can't get two different users on DM's", e);
            return null;
        }
        List<User> list = e.getMessage().getMentionedUsers();
        if (message.length == 0) {
            sendError(getErrorMessage(5), e);
            return null;
        }

        LastFMData[] datas = new LastFMData[]{null, null};
        if (message.length == 1 && list.size() == 1) {
            datas[1] = dao.findLastFMData(list.get(0).getIdLong());
            datas[0] = dao.findLastFMData(e.getAuthor().getIdLong());
        } else if (message.length == 1 && list.size() == 0) {
            long discordIdFromLastfm = dao.getDiscordIdFromLastfm(message[0], e.getGuild().getIdLong());
            datas[1] = new LastFMData(message[0], discordIdFromLastfm, Role.USER);
            datas[0] = dao.findLastFMData(e.getAuthor().getIdLong());
        } else if (message.length == 2 & list.size() == 1) {
            User sample = list.get(0);
            if (message[0].equals(sample.getAsMention()) || message[0].equals("<@!" + sample.getAsMention().substring(2))) {
                datas[0] = dao.findLastFMData(sample.getIdLong());
                long discordIdFromLastfm = dao.getDiscordIdFromLastfm(message[1], e.getGuild().getIdLong());
                datas[1] = new LastFMData(message[1], discordIdFromLastfm, Role.USER);
            } else {
                long discordIdFromLastfm = dao.getDiscordIdFromLastfm(message[0], e.getGuild().getIdLong());
                datas[0] = new LastFMData(message[0], discordIdFromLastfm, Role.USER);
                datas[1] = dao.findLastFMData(sample.getIdLong());
            }

        } else if (message.length == 2 & list.size() == 2) {
            datas[0] = dao.findLastFMData(list.get(0).getIdLong());
            datas[1] = dao.findLastFMData(list.get(1).getIdLong());
        } else if (message.length == 2 & list.size() == 0) {
            long discordIdFromLastfm = dao.getDiscordIdFromLastfm(message[0], e.getGuild().getIdLong());
            datas[0] = new LastFMData(message[0], discordIdFromLastfm, Role.USER);
            long discordIdFromLastfm2 = dao.getDiscordIdFromLastfm(message[1], e.getGuild().getIdLong());
            datas[1] = new LastFMData(message[1], discordIdFromLastfm2, Role.USER);
        } else {
            sendError("Coudn't get two usernames from the input", e);
            return null;
        }
        return new String[]{String.valueOf(datas[0].getDiscordId()), datas[0].getName(), String.valueOf(datas[1].getDiscordId()), datas[1].getName()};
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *userName* *userName***\n" +
               "\tIf the second user is missing it gets replaced by the owner of the message\n";

    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(5, "Need at least one username");
        errorMessages.put(-1, "Mentioned user is not registered");


    }
}
