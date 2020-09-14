package core.parsers;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.UsersWrapper;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.concurrent.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ParserAux {
    private String[] message;
    private static final Pattern user = Pattern.compile("(.{2,32})#(\\d{4})");
    private final boolean doExpensiveSearch;

    public ParserAux(String[] message) {
        this(message, false);
    }

    public ParserAux(String[] message, boolean doExpensiveSearch) {
        this.message = message;
        this.doExpensiveSearch = doExpensiveSearch;
    }

    User getOneUserPermissive(MessageReceivedEvent e) {
        User sample;
        String join = String.join(" ", message);
        if (e.isFromGuild()) {
            List<Member> members = e.getMessage().getMentionedMembers();
            if (!members.isEmpty()) {
                sample = members.get(0).getUser();
                message = Arrays.stream(message).filter(s -> !s.equals(sample.getAsMention()) && !s.equals("<@!" + sample.getAsMention().substring(2))).toArray(String[]::new);
            } else {
                if (join.isBlank()) {
                    return e.getAuthor();
                }


                Member memberByTag = null;
                if (user.matcher(join).matches()) {
                    memberByTag = e.getGuild().getMemberByTag(join);
                }
                if (memberByTag == null) {
                    List<Member> membersByEffectiveName = e.getGuild().getMembersByEffectiveName(join, true);
                    if (membersByEffectiveName.isEmpty()) {
                        List<Member> membersByName = e.getGuild().getMembersByName(join, true);
                        if (membersByName.isEmpty()) {
                            List<Member> membersByNickname = e.getGuild().getMembersByNickname(join, true);
                            if (membersByNickname.isEmpty()) {
                                return e.getAuthor();
                            }
                            return membersByEffectiveName.get(0).getUser();
                        }
                        return membersByName.get(0).getUser();
                    }
                    return membersByEffectiveName.get(0).getUser();
                }
                return memberByTag.getUser();
            }
        } else
            sample = e.getAuthor();
        return sample;
    }

    User getOneUser(MessageReceivedEvent e) {
        User sample = null;
        List<String> tempArray = new ArrayList<>();
        boolean hasMatched = false;
        for (String s : message) {
            if (!hasMatched && user.matcher(s).matches()) {
                User userByTag = e.getJDA().getUserByTag(s);
                if (userByTag != null) {
                    hasMatched = true;
                    sample = userByTag;
                }
            } else {
                tempArray.add(s);
            }
        }
        this.message = tempArray.toArray(String[]::new);
        if (sample != null) {
            return sample;
        }
        if (e.isFromGuild()) {
            List<Member> members = e.getMessage().getMentionedMembers();
            if (!members.isEmpty()) {
                if (members.size() != 1) {
                    return null;
                }
                sample = members.get(0).getUser();
                User finalSample = sample;
                this.message = Arrays.stream(this.message).filter(s -> !s.equals(finalSample.getAsMention()) && !s.equals("<@!" + finalSample.getAsMention().substring(2))).toArray(String[]::new);
            } else {
                sample = e.getAuthor();
            }
        } else
            sample = e.getAuthor();
        return sample;
    }


    private LastFMData findUsername(MessageReceivedEvent event, User user, ChuuService dao) throws InstanceNotFoundException {
        if (event.isFromGuild() && this.doExpensiveSearch) {
            return dao.computeLastFmData(user.getIdLong(), event.getGuild().getIdLong());
        } else {
            return dao.findLastFMData(user.getIdLong());
        }
    }

    public LastFMData[] getTwoUsers(ChuuService dao, String[] message, MessageReceivedEvent e) throws InstanceNotFoundException {
        LastFMData[] result = null;
        boolean finished = false;
        if (message.length != 0) {
            LastFMData[] datas = new LastFMData[]{null, null};
            if (message.length > 2) {
                String join = String.join(" ", message);
                datas[0] = findUsername(e, e.getAuthor(), dao);
                datas[1] = handleRawMessage(join, e, dao);
            } else {
                List<User> list = e.getMessage().getMentionedUsers();
                if (message.length == 1 && list.size() == 1) {
                    datas[1] = findUsername(e, list.get(0), dao);
                    datas[0] = findUsername(e, e.getAuthor(), dao);
                } else if (message.length == 1 && list.isEmpty()) {
                    datas[1] = handleRawMessage(message[0], e, dao);
                    datas[0] = findUsername(e, e.getAuthor(), dao);
                } else if (message.length == 2 && list.size() == 1) {
                    User sample = list.get(0);
                    if (message[0].equals(sample.getAsMention()) || message[0].equals("<@!" + sample.getAsMention().substring(2))) {
                        datas[0] = findUsername(e, sample, dao);
                        datas[1] = handleRawMessage(message[1], e, dao);
                    } else {
                        datas[0] = handleRawMessage(message[0], e, dao);
                        datas[1] = findUsername(e, sample, dao);
                    }
                } else if (message.length == 2 && list.size() == 2) {
                    datas[0] = findUsername(e, list.get(0), dao);
                    datas[1] = findUsername(e, list.get(1), dao);
                } else if (message.length == 2 && list.isEmpty()) {
                    try {
                        datas[1] = handleRawMessage(String.join(" ", message), e, dao);
                        datas[0] = findUsername(e, e.getAuthor(), dao);
                    } catch (InstanceNotFoundException ex) {
                        datas[0] = handleRawMessage(message[0], e, dao);
                        datas[1] = handleRawMessage(message[1], e, dao);
                    }

                } else {
                    finished = true;
                }
            }
            if (!finished) {
                result = datas;
            }
        }
        return result;
    }

    private LastFMData getLastFMData(Optional<User> opt, String message, ChuuService dao, MessageReceivedEvent e) throws InstanceNotFoundException {
        if (opt.isEmpty()) {
            throw new InstanceNotFoundException(message);
        } else {
            return findUsername(e, opt.get(), dao);
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
            return getLastFMData(Optional.ofNullable(memberByTag).map(Member::getUser), message, dao, e);
        }
        List<Member> membersByEffectiveName = e.getGuild().getMembersByEffectiveName(message, true);
        if (!membersByEffectiveName.isEmpty()) {
            Optional<Member> first = membersByEffectiveName.stream().findFirst();
            if (!biPredicate.test(first.get())) {
                throw new InstanceNotFoundException(first.get().getIdLong());
            }
            return getLastFMData(first.map(Member::getUser), message, dao, e);

        }
        List<Member> membersByName = e.getGuild().getMembersByName(message, true);
        if (!membersByName.isEmpty()) {
            Optional<Member> user = membersByName.stream().findFirst();
            if (!biPredicate.test(user.get())) {
                throw new InstanceNotFoundException(user.get().getIdLong());
            }
            return getLastFMData(Optional.of(user.get().getUser()), message, dao, e);
        }
        long discordIdFromLastfm = dao.getDiscordIdFromLastfm(message, e.getGuild().getIdLong());
        return getLastFMData(Optional.ofNullable(e.getGuild().getJDA().getUserById(discordIdFromLastfm)), message, dao, e);
    }

    public String[] getMessage() {
        return message;
    }
}
