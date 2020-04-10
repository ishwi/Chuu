package core.parsers;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.UsersWrapper;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ParserAux {
    private String[] message;
    private Pattern user = Pattern.compile("(.{2,32})#(\\d{4})");

    public ParserAux(String[] message) {
        this.message = message;
    }


    LastFMData[] getTwoUsers(ChuuService dao, String[] message, MessageReceivedEvent e) throws InstanceNotFoundException {
        if (message.length == 0 || message.length > 2) {
            return null;
        }
        List<User> list = e.getMessage().getMentionedUsers();
        LastFMData[] datas = new LastFMData[]{null, null};
        if (message.length == 1 && list.size() == 1) {
            datas[1] = dao.findLastFMData(list.get(0).getIdLong());
            datas[0] = dao.findLastFMData(e.getAuthor().getIdLong());
        } else if (message.length == 1 && list.isEmpty()) {
            datas[1] = handleRawMessage(message[0], e, dao);
            datas[0] = dao.findLastFMData(e.getAuthor().getIdLong());
        } else if (message.length == 2 && list.size() == 1) {
            User sample = list.get(0);
            if (message[0].equals(sample.getAsMention()) || message[0].equals("<@!" + sample.getAsMention().substring(2))) {
                datas[0] = dao.findLastFMData(sample.getIdLong());
                datas[1] = handleRawMessage(message[1], e, dao);
            } else {
                datas[0] = handleRawMessage(message[0], e, dao);
                datas[1] = dao.findLastFMData(sample.getIdLong());
            }
        } else if (message.length == 2 && list.size() == 2) {
            datas[0] = dao.findLastFMData(list.get(0).getIdLong());
            datas[1] = dao.findLastFMData(list.get(1).getIdLong());
        } else if (message.length == 2 && list.isEmpty()) {
            datas[0] = handleRawMessage(message[0], e, dao);
            datas[1] = handleRawMessage(message[1], e, dao);
        } else {
            return null;
        }
        return datas;
    }

    private LastFMData getLastFMData(Optional<User> opt, String message, ChuuService dao) throws InstanceNotFoundException {
        if (opt.isEmpty()) {
            throw new InstanceNotFoundException(message);
        } else {
            return dao.findLastFMData(opt.get().getIdLong());
        }
    }

    LastFMData handleRawMessage(String message, MessageReceivedEvent e, ChuuService dao) throws InstanceNotFoundException {

        List<UsersWrapper> all = dao.getAll(e.getGuild().getIdLong());
        Predicate<Member> biPredicate = member -> all.stream().anyMatch(x -> member != null && x.getDiscordID() == member.getIdLong());
        if (user.matcher(message).matches()) {
            Member memberByTag = e.getGuild().getMemberByTag(message);
            if (!biPredicate.test(memberByTag) && memberByTag != null) {
                throw new InstanceNotFoundException(memberByTag.getId());
            }
            return getLastFMData(Optional.ofNullable(memberByTag).map(Member::getUser), message, dao);
        }
        List<Member> membersByEffectiveName = e.getGuild().getMembersByEffectiveName(message, true);
        if (!membersByEffectiveName.isEmpty()) {
            Optional<Member> first = membersByEffectiveName.stream().findFirst();
            if (!biPredicate.test(first.get())) {
                throw new InstanceNotFoundException(first.get().getIdLong());
            }
            return getLastFMData(first.map(Member::getUser), message, dao);

        } else {
            List<Member> membersByName = e.getGuild().getMembersByName(message, true);
            if (!membersByName.isEmpty()) {
                Optional<Member> user = membersByName.stream().findFirst();
                if (!biPredicate.test(user.get())) {
                    throw new InstanceNotFoundException(user.get().getIdLong());
                }
                return getLastFMData(Optional.of(user.get().getUser()), message, dao);
            } else {
                long discordIdFromLastfm = dao.getDiscordIdFromLastfm(message, e.getGuild().getIdLong());
                return getLastFMData(Optional.ofNullable(e.getGuild().getJDA().getUserById(discordIdFromLastfm)), message, dao);
            }
        }
    }

    public String[] getMessage() {
        return message;
    }
}
