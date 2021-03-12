package core.parsers;

import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.UsersWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParserAux {
    private static final Pattern user = Pattern.compile("(.{2,32})#(\\d{4})");
    public static final Pattern discordId = Pattern.compile("\\d{17,}");
    private static final Pattern lfm = Pattern.compile("lfm:(\\S+)");
    private static final Pattern userRaw = Pattern.compile("u:(\\S+)");
    private static final Pattern idParser = Pattern.compile("^(?:<(?:@!?|@&|#)(?<sid>[0-9]{17,21})>|(?<id>[0-9]{17,21}))$");

    private final boolean doExpensiveSearch;
    private String[] message;

    public ParserAux(String[] message) {
        this(message, false);
    }

    public ParserAux(String[] message, boolean doExpensiveSearch) {
        this.message = message;
        this.doExpensiveSearch = doExpensiveSearch;
    }

    public static Optional<Snowflake> parseSnowflake(String words) {
        var match = idParser.matcher(words);

        if (match.matches()) {
            String sid = match.group("sid");
            if (sid == null) {
                sid = match.group("id");
            }
            Snowflake value = new Snowflake(Long.parseLong(sid));
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public record Snowflake(long id) {
    }

    @NotNull User getOneUserPermissive(MessageReceivedEvent e) {
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
                                try {
                                    long l = Long.parseLong(join);
                                    User userById = e.getJDA().getUserById(l);
                                    if (userById != null) {
                                        return userById;
                                    }
                                } catch (NumberFormatException ignored) {
                                    //
                                }
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

    @NotNull User getOneUser(MessageReceivedEvent e, ChuuService dao) throws InstanceNotFoundException {
        User sample = null;
        List<String> tempArray = new ArrayList<>();
        boolean hasMatched = false;
        for (String s : message) {
            if (!hasMatched) {
                Matcher matcher = userRaw.matcher(s);
                Matcher lfmM = lfm.matcher(s);
                if (matcher.matches()) {
                    String userName = matcher.group(1);
                    Optional<User> user = userString(userName, e, dao);
                    if (user.isPresent()) {
                        hasMatched = true;
                        sample = user.get();
                    }
                } else if (e.isFromGuild() && lfmM.matches()) {
                    String userName = lfmM.group(1);
                    long discordIdFromLastfm = dao.getDiscordIdFromLastfm(userName, e.getGuild().getIdLong());
                    Member memberById = e.getGuild().getMemberById(discordIdFromLastfm);
                    if (memberById != null) {
                        hasMatched = true;
                        sample = memberById.getUser();
                    }
                } else if (user.matcher(s).matches()) {
                    User userByTag = e.getJDA().getUserByTag(s);
                    if (userByTag != null) {
                        hasMatched = true;
                        sample = userByTag;
                    }
                } else if (discordId.matcher(s).matches()) {
                    try {
                        long l = Long.parseLong(s);
                        User userById = e.getJDA().getUserById(l);
                        if (userById != null) {
                            sample = userById;
                            hasMatched = true;
                        }
                    } catch (NumberFormatException ignored) {
                        //
                    }
                } else {
                    tempArray.add(s);
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

    LastFMData handleRawMessage(String message, MessageReceivedEvent e, ChuuService db) throws InstanceNotFoundException {

        Set<Long> all = db.getAll(e.getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
        Predicate<Member> isOnServer = member -> member != null && all.stream().anyMatch(x -> x == member.getIdLong());
        if (discordId.matcher(message).matches()) {
            Member memberById = e.getGuild().getMemberById(message);
            if (!isOnServer.test(memberById) && memberById != null) {
                throw new InstanceNotFoundException(memberById.getId());
            }
            return getLastFMData(Optional.ofNullable(memberById).map(Member::getUser), message, db, e);
        }
        if (user.matcher(message).matches()) {
            Member memberByTag = e.getGuild().getMemberByTag(message);
            if (!isOnServer.test(memberByTag) && memberByTag != null) {
                throw new InstanceNotFoundException(memberByTag.getId());
            }
            return getLastFMData(Optional.ofNullable(memberByTag).map(Member::getUser), message, db, e);
        }
        List<Member> membersByEffectiveName = e.getGuild().getMembersByEffectiveName(message, true);
        if (!membersByEffectiveName.isEmpty()) {
            Optional<Member> first = membersByEffectiveName.stream().filter(isOnServer).findFirst();
            if (first.isEmpty()) {
                throw new InstanceNotFoundException(membersByEffectiveName.get(0).getIdLong());
            }
            return getLastFMData(first.map(Member::getUser), message, db, e);

        }
        List<Member> membersByName = e.getGuild().getMembersByName(message, true);
        if (!membersByName.isEmpty()) {
            Optional<Member> user = membersByName.stream().filter(isOnServer).findFirst();
            if (user.isEmpty()) {
                throw new InstanceNotFoundException(membersByName.get(0).getIdLong());
            }
            return getLastFMData(Optional.of(user.get().getUser()), message, db, e);
        }
        long discordIdFromLastfm = db.getDiscordIdFromLastfm(message, e.getGuild().getIdLong());
        return getLastFMData(Optional.ofNullable(e.getGuild().getJDA().getUserById(discordIdFromLastfm)), message, db, e);
    }

    Optional<User> userString(String message, MessageReceivedEvent e, ChuuService db) throws InstanceNotFoundException {

        Set<Long> all = db.getAll(e.getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
        Predicate<Member> isOnServer = member -> member != null && all.stream().anyMatch(x -> x == member.getIdLong());

        if (discordId.matcher(message).matches()) {
            Member memberById = e.getGuild().getMemberById(message);
            if (!isOnServer.test(memberById) && memberById != null) {
                throw new InstanceNotFoundException(memberById.getId());
            }
            return Optional.ofNullable(memberById).map(Member::getUser);
        }

        if (user.matcher(message).matches()) {
            Member memberByTag = e.getGuild().getMemberByTag(message);
            if (!isOnServer.test(memberByTag) && memberByTag != null) {
                throw new InstanceNotFoundException(memberByTag.getId());
            }
            return Optional.ofNullable(memberByTag).map(Member::getUser);
        }

        List<Member> membersByEffectiveName = e.getGuild().getMembersByEffectiveName(message, true);
        if (!membersByEffectiveName.isEmpty()) {
            Optional<Member> first = membersByEffectiveName.stream().filter(isOnServer).findFirst();
            if (first.isEmpty()) {
                throw new InstanceNotFoundException(membersByEffectiveName.get(0).getIdLong());
            }
            return first.map(Member::getUser);
        }

        List<Member> membersByName = e.getGuild().getMembersByName(message, true);
        if (!membersByName.isEmpty()) {
            Optional<Member> user = membersByName.stream().filter(isOnServer).findFirst();
            if (user.isEmpty()) {
                throw new InstanceNotFoundException(membersByName.get(0).getIdLong());
            }
            return user.map(Member::getUser);
        }
        return Optional.empty();
    }


    public String[] getMessage() {
        return message;
    }
}
