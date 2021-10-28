package core.commands.friends;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.charts.GuildTopCommand;
import core.commands.stats.PlayingCommand;
import core.commands.utils.*;
import core.commands.whoknows.LocalWhoKnowsAlbumCommand;
import core.commands.whoknows.LocalWhoKnowsSongCommand;
import core.commands.whoknows.WhoKnowsCommand;
import core.exceptions.LastFmException;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ParentParser;
import core.parsers.Parser;
import core.parsers.params.*;
import core.util.Deps;
import core.util.FriendsActions;
import dao.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.apache.commons.text.WordUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static core.commands.stats.PlayingCommand.format;

public class FriendsCommand extends ParentCommmand<FriendsActions> {

    private final WhoKnowsCommand whoKnowsCommand;
    private final LocalWhoKnowsSongCommand whoKnowsTrackCommand;
    private final LocalWhoKnowsAlbumCommand whoKnowsAlbumCommand;
    private final GuildTopCommand chartCommand;

    private final LoadingCache<Long, LocalDateTime> controlAccess;
    private final LoadingCache<Long, LocalDateTime> serverControlAccess;


    public FriendsCommand(ServiceView dao) {
        super(dao);
        FriendCommandLoader loader = new FriendCommandLoader(dao);
        whoKnowsCommand = loader.whoKnowsCommand();
        whoKnowsTrackCommand = loader.localWhoKnowsSongCommand();
        whoKnowsAlbumCommand = loader.localWhoKnowsAlbumCommand();
        chartCommand = loader.guildTopCommand();
        controlAccess = CacheBuilder.newBuilder().concurrencyLevel(2).expireAfterWrite(12, TimeUnit.HOURS).build(
                new CacheLoader<>() {
                    public LocalDateTime load(@org.jetbrains.annotations.NotNull Long guild) {
                        return LocalDateTime.now().plus(12, ChronoUnit.HOURS);
                    }
                });
        serverControlAccess = CacheBuilder.newBuilder().concurrencyLevel(2).expireAfterWrite(5, TimeUnit.MINUTES).build(
                new CacheLoader<>() {
                    public LocalDateTime load(@org.jetbrains.annotations.NotNull Long guild) {
                        return LocalDateTime.now().plus(5, ChronoUnit.MINUTES);
                    }
                });
    }

    @Override
    public Class<FriendsActions> getClazz() {
        return FriendsActions.class;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.FRIENDS;
    }

    @Override
    public Parser<EnumParameters<FriendsActions>> initParser() {
        return new ParentParser<>(FriendsActions.class, new Deps(lastFM, db), true, true, true);
    }

    @Override
    protected void showHelp(Context e, EnumParameters<FriendsActions> params) {
        String str = EnumSet.allOf(FriendsActions.class).stream()
                .map(z -> "__**%s**__  ➜ %s".formatted(WordUtils.capitalizeFully(z.name()), z.getDescription())).collect(Collectors.joining("\n"));
        DiscordUserDisplay ui = CommandUtil.getUserInfoUnescaped(params.getE().getAuthor().getIdLong());
        EmbedBuilder eb = new ChuuEmbedBuilder(e).setDescription(str)
                .setAuthor("Friend subcommands", null, ui.urlImage())
                .setFooter("Do `" + e.getPrefix() + "friend add` to add a friend to your friendlist!");
        e.sendMessage(eb.build()).queue();
    }

    @Override
    public String getUsageInstructions() {
        return super.getUsageInstructions();
    }

    @Override
    public String getDescription() {
        return "Manage your friend list and do commands for your full friend list";
    }

    @Override
    public List<String> getAliases() {
        return List.of("friend", "friends", "f");
    }

    @Override
    public String getName() {
        return "Friends";
    }


    @Override
    public void doSubcommand(Context e, FriendsActions action, String args, EnumParameters<FriendsActions> params) throws LastFmException, InstanceNotFoundException {
        switch (action) {
            case ADD -> doAdd(e, action, args);
            case PENDING -> doShowPending(e, action, args);
            case INCOMING -> doShowIncoming(e, action, args);
            case REMOVE -> doRemove(e, action, args);
            case LIST -> doList(e, action, args);
            case NP -> doNp(e, action, args);
            case WK -> {
                ArtistParameters artistParameters = action.parse(e, deps, args);
                whoKnowsCommand.onCommand(e, artistParameters);
            }
            case WKA -> {
                ArtistAlbumParameters artistAlbumParameters = action.parse(e, deps, args);
                whoKnowsAlbumCommand.onCommand(e, artistAlbumParameters);
            }
            case WKT -> {
                ArtistAlbumParameters artistAlbumParameters = action.parse(e, deps, args);
                whoKnowsTrackCommand.onCommand(e, artistAlbumParameters);
            }
            case TOP -> {
                ChartSizeParameters chartParameters = action.parse(e, deps, args);
                chartCommand.onCommand(e, chartParameters);

            }
        }
    }

    private void doShowIncoming(Context e, FriendsActions action, String args) {
        long author = e.getAuthor().getIdLong();
        List<Friend> userPendingFriends = db.getIncomingFriendRequests(author);
        if (userPendingFriends.isEmpty()) {
            sendMessageQueue(e, "You have no incoming friend requests!");
            return;
        }
        EmbedBuilder eb = new ChuuEmbedBuilder(e).setTitle("Incoming friend requests");
        new PaginatorBuilder<>(e, eb, userPendingFriends)
                .pageSize(1)
                .unnumered()
                .mapper(friend -> {
                    SimpleUser other = friend.other(author);
                    DiscordUserDisplay ui = CommandUtil.getUserInfoEscaped(e, other.discordId());
                    return "%s: %s\n".formatted(ui.username(), CommandUtil.getDateTimestampt(friend.when(), TimeFormat.RELATIVE));
                })
                .creator((context, embedBuilder) -> {
                    Friend friend = userPendingFriends.get(0);
                    SimpleUser other = friend.other(author);
                    return e.sendMessage(embedBuilder.build(),
                            ActionRow.of(ButtonUtils.declineFriendRequest(other.discordId()), ButtonUtils.acceptFriendRequest(other.discordId())));
                })
                .extraRow((list) -> {
                    Friend friend = list.get(0);
                    SimpleUser other = friend.other(author);
                    return ActionRow.of(ButtonUtils.declineFriendRequest(other.discordId()), ButtonUtils.acceptFriendRequest(other.discordId()));
                }).build().queue();
    }

    private void doShowPending(Context e, FriendsActions action, String args) {
        long author = e.getAuthor().getIdLong();
        List<Friend> userPendingFriends = db.getFriendPendingRequests(author);
        if (userPendingFriends.isEmpty()) {
            sendMessageQueue(e, "There are no pending requests");
            return;
        }

        EmbedBuilder eb = new ChuuEmbedBuilder(e).setTitle("Your yet to be accepted requests");
        new PaginatorBuilder<>(e, eb, userPendingFriends)
                .pageSize(1)
                .unnumered()
                .mapper(friend -> {
                    SimpleUser other = friend.other(author);
                    DiscordUserDisplay ui = CommandUtil.getUserInfoEscaped(e, other.discordId());
                    return "%s: %s\n".formatted(ui.username(), CommandUtil.getDateTimestampt(friend.when(), TimeFormat.RELATIVE));
                }).build().queue();
    }

    private void doNp(Context e, FriendsActions action, String args) throws LastFmException, InstanceNotFoundException {
        CommandParameters chuuDataParams = action.parse(e, deps, args);

        long discordId = e.getAuthor().getIdLong();
        LastFMData lastFMData = db.findLastFMData(discordId);
        DiscordUserDisplay ui = CommandUtil.getUserInfoEscaped(e, discordId);

        List<Friend> userFriends = db.getUserFriends(discordId);
        if (userFriends.isEmpty()) {
            sendMessageQueue(e, "%s doesn't have any friend :(".formatted(ui.username()));
            return;
        }

        LocalDateTime cooldown;
        if (userFriends.size() > 15) {
            LocalDateTime ifPresent = controlAccess.getIfPresent(discordId);
            if (ifPresent != null) {
                format(e, ifPresent, "You have too many friends, so `friends np` can only be executed twice per day ");
                return;
            }
            controlAccess.refresh(discordId);
        } else if ((cooldown = serverControlAccess.getIfPresent(discordId)) != null) {
            format(e, cooldown, "This command has a 5 min cooldown between uses.");
            return;
        } else {
            serverControlAccess.refresh(discordId);
        }

        List<LastFMData> users = Stream.concat(userFriends.stream().map(z -> {
            SimpleUser other = z.other(discordId);
            return LastFMData.ofUser(other.lastfmId(), other.discordId());
        }), Stream.of(lastFMData)).toList();

        boolean showFresh = !chuuDataParams.hasOptional("recent");
        List<String> recent = PlayingCommand.obtainNps(lastFM, e, showFresh, users);

        if (recent.isEmpty()) {
            sendMessageQueue(e, "No one is playing anything on your friendlist!");
            return;
        }
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle(
                        showFresh ? "What is being played now in your friendlist" : "What was being played in your friendlist ");

        new PaginatorBuilder<>(e, embedBuilder, recent).pageSize(20).randomize().unnumered().withIndicator().build().queue();
    }

    private void doList(Context e, FriendsActions action, String args) throws LastFmException, InstanceNotFoundException {
        ChuuDataParams chuuDataParams = action.parse(e, deps, args);
        long discordId = chuuDataParams.getLastFMData().getDiscordId();
        List<Friend> userFriends = db.getUserFriends(discordId);
        DiscordUserDisplay ui = CommandUtil.getUserInfoUnescaped(e, discordId);

        if (userFriends.isEmpty()) {
            sendMessageQueue(e, "%s has no friends :(".formatted(ui.username()));
            return;
        }

        EmbedBuilder eb = new ChuuEmbedBuilder(e)
                .setAuthor("%s friends".formatted(ui.username()), PrivacyUtils.getLastFmUser(chuuDataParams.getLastFMData().getName()), ui.urlImage())
                .setFooter("%s has %d %s".formatted(ui.username(), userFriends.size(), CommandUtil.singlePlural(userFriends.size(), "friend", "friends")));

        new PaginatorBuilder<>(e, eb, userFriends).memoized(friend -> {
            SimpleUser other = friend.other(discordId);
            DiscordUserDisplay otherUserInfo = CommandUtil.getUserInfoEscaped(e, other.discordId());
            return "[%s](%s) %s%n".formatted(
                    otherUserInfo.username(),
                    PrivacyUtils.getLastFmUser(other.lastfmId()),
                    CommandUtil.getDateTimestampt(friend.when(), TimeFormat.RELATIVE));
        }).unnumered().build().queue();
    }

    private void doRemove(Context e, FriendsActions action, String args) throws LastFmException, InstanceNotFoundException {
        if (e instanceof ContextMessageReceived && (args == null || args.isBlank())) {
            parser.sendError("You need to introduce the user you want to add!", e);
            return;
        }
        ChuuDataParams chuuDataParams = action.parse(e, deps, args);
        long discordId = chuuDataParams.getLastFMData().getDiscordId();
        long author = e.getAuthor().getIdLong();

        if (discordId == author) {
            parser.sendError("Introduce an user to remove!", e);
            return;
        }
        db.findLastFMData(author);
        DiscordUserDisplay userInfoEscaped = CommandUtil.getUserInfoEscaped(e, discordId);
        Optional<Friend> opt = db.areFriends(discordId, author);
        opt.ifPresentOrElse(f -> {
            db.rejectRequest(f.first().discordId(), f.second().discordId());
            e.sendMessage("Removed %s from your friend list!".formatted(userInfoEscaped.username())).queue();
        }, () -> e.sendMessage("You were not friends with %s!".formatted(userInfoEscaped.username())).queue());
    }

    private void doAdd(Context e, FriendsActions action, String args) throws LastFmException, InstanceNotFoundException {
        if (e instanceof ContextMessageReceived && (args == null || args.isBlank())) {
            parser.sendError("You need to introduce the user you want to add!", e);
            return;
        }

        ChuuDataParams chuuDataParams = action.parse(e, deps, args);
        long discordId = chuuDataParams.getLastFMData().getDiscordId();
        long author = e.getAuthor().getIdLong();
        if (discordId == author) {
            parser.sendError("Couldn't read any user from your input. You might be able to have better results using" +
                    " u:[discordId|Tag#Discriminator|name] or lfm:[lastfm-name]", e);
            return;
        }
        LastFMData authorData = db.findLastFMData(author);
        DiscordUserDisplay userInfoEscaped = CommandUtil.getUserInfoEscaped(e, discordId);
        Optional<Friend> optFriend = db.areFriends(author, discordId);
        if (optFriend.isPresent()) {
            Friend friend = optFriend.get();
            Friend.UsersSorted usersSorted = new Friend.UsersSorted(friend.first().discordId(), friend.second().discordId());

            Friend.FriendStatus status = friend.friendStatus();
            if (status == Friend.FriendStatus.ACCEPTED) {
                sendMessageQueue(e, "You and %s were already friends!".formatted(userInfoEscaped.username()));
            } else {
                if ((usersSorted.first() == author && status == Friend.FriendStatus.PENDING_FIRST)
                        || (usersSorted.second() == author && status == Friend.FriendStatus.PENDING_SECOND)) {
                    sendMessageQueue(e, "%s is yet to accept your friend request".formatted(userInfoEscaped.username()));
                } else {
                    if (db.acceptRequest(usersSorted.first(), usersSorted.second())) {
                        sendMessageQueue(e, "Accepted %s's friend request!".formatted(userInfoEscaped.username()));
                    } else {
                        sendMessageQueue(e, "Something went wrong accepting the request. Probably the request has become invalid.");
                    }
                }
            }
        } else {
            DiscordUserDisplay yourself = CommandUtil.getUserInfoEscaped(e, author);
            String artists = db.getAllUserArtist(author, 5).stream().map(ScrobbledArtist::getArtist).collect(Collectors.joining(", "));

            String finalArtists;
            if (!artists.isBlank()) {
                finalArtists = "**Fav artists:** %s".formatted(artists);
            } else {
                finalArtists = "";
            }
            SelfUser su = e.getJDA().getSelfUser();
            if (discordId == su.getIdLong()) {
                sendMessageQueue(e, "%s is not accepting friend requests!".formatted(su.getName()));
                return;
            }
            e.getJDA().openPrivateChannelById(discordId).flatMap(privateChannel -> {
                db.createRequest(author, discordId);
                EmbedBuilder eb = new ChuuEmbedBuilder(e)
                        .setAuthor("Friend Request from %s".formatted(yourself.username()), PrivacyUtils.getLastFmUser(authorData.getName()), yourself.urlImage())
                        .setDescription(("**Account:** %s%n" +
                                "**Last.fm:** [%s](%s)%n".formatted(authorData.getName(), PrivacyUtils.getLastFmUser(authorData.getName())) +
                                "%s").formatted(e.getAuthor().getAsMention(), finalArtists));
                MessageBuilder messageBuilder = new MessageBuilder(eb.build()).setActionRows(
                        ActionRow.of(ButtonUtils.declineFriendRequest(author), ButtonUtils.acceptFriendRequest(author)));
                return privateChannel.sendMessage(messageBuilder.build());
            }).queue(message -> sendMessageQueue(e, "Sent a friend request to %s".formatted(userInfoEscaped.username())),
                    throwable -> e.sendMessage("An error ocurred sending a dm to %s!".formatted(userInfoEscaped.username())).queue());
        }
    }


}

