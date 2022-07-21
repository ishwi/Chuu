package core.commands.friends;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.artists.BandInfoCommand;
import core.commands.artists.GlobalFavesFromArtistCommand;
import core.commands.charts.GuildTopAlbumsCommand;
import core.commands.charts.GuildTopCommand;
import core.commands.charts.GuildTopTracksCommand;
import core.commands.stats.PlayingCommand;
import core.commands.utils.*;
import core.commands.whoknows.LocalWhoKnowsAlbumCommand;
import core.commands.whoknows.LocalWhoKnowsSongCommand;
import core.commands.whoknows.WhoKnowsCommand;
import core.exceptions.LastFmException;
import core.imagerenderer.util.pie.DefaultList;
import core.imagerenderer.util.pie.IPieableList;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ParentParser;
import core.parsers.Parser;
import core.parsers.params.*;
import core.services.validators.ArtistValidator;
import core.util.Deps;
import core.util.FriendsActions;
import core.util.ServiceView;
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
    private final GuildTopCommand topArtistsCommands;
    private final GuildTopAlbumsCommand topAlbumsCommand;
    private final GuildTopTracksCommand topTracksCommand;
    private final BandInfoCommand bandInfoCommand;
    private final IPieableList<AlbumUserPlays, ArtistParameters> pie = DefaultList.fillPie(AlbumUserPlays::getAlbum, AlbumUserPlays::getPlays);

    private final LoadingCache<Long, LocalDateTime> controlAccess;
    private final LoadingCache<Long, LocalDateTime> serverControlAccess;


    public FriendsCommand(ServiceView dao) {
        super(dao);
        FriendCommandLoader loader = new FriendCommandLoader(dao);
        whoKnowsCommand = loader.whoKnowsCommand();
        whoKnowsTrackCommand = loader.localWhoKnowsSongCommand();
        whoKnowsAlbumCommand = loader.localWhoKnowsAlbumCommand();
        topArtistsCommands = loader.guildTopCommand();
        topAlbumsCommand = loader.guildTopAlbumsCommand();
        topTracksCommand = loader.guildTopTracksCommand();
        bandInfoCommand = loader.bandInfoCommand();
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
                .setFooter("Do `" + e.getPrefix() + "friend add` to add a friend to your friend list!");
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
            case PENDING -> doShowPending(e);
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
                topArtistsCommands.onCommand(e, chartParameters);

            }
            case CHART -> {
                ChartSizeParameters chartParameters = action.parse(e, deps, args);
                topAlbumsCommand.onCommand(e, chartParameters);
            }
            case TOPTRACKS -> {
                ChartSizeParameters chartParameters = action.parse(e, deps, args);
                topTracksCommand.onCommand(e, chartParameters);
            }
            case FAVS -> {
                ArtistParameters ap = action.parse(e, deps, args);
                ScrobbledArtist who = new ArtistValidator(db, lastFM, e).validate(ap.getArtist(), !ap.isNoredirect());
                LastFMData data = ap.getLastFMData();
                long discordId = data.getDiscordId();
                String lastFmName = data.getName();
                String validArtist = who.getArtist();
                List<AlbumUserPlays> songs = db.friendsTopArtistSongs(discordId, who.getArtistId(), Integer.MAX_VALUE);
                DiscordUserDisplay ui = CommandUtil.getUserInfoEscaped(e, discordId);
                GlobalFavesFromArtistCommand.sendArtistFaves(e, who, validArtist, lastFmName, songs, "%s friends".formatted(ui.username()), "%s friends'".formatted(ui.username()), "in your friend list!", ui.urlImage(), ap, pie, (b) -> sendImage(b, e));
            }
            case ARTIST -> {
                ArtistParameters ap = action.parse(e, deps, args);
                bandInfoCommand.onCommand(e, ap);
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

    private void doShowPending(Context e) {
        long author = e.getAuthor().getIdLong();
        List<Friend> userPendingFriends = db.getFriendPendingRequests(author);
        if (userPendingFriends.isEmpty()) {
            sendMessageQueue(e, "There are no pending friend requests");
            return;
        }

        EmbedBuilder eb = new ChuuEmbedBuilder(e).setTitle("Your pending friend requests");
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
            sendMessageQueue(e, "%s doesn't have any friends :(".formatted(ui.username()));
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
            format(e, cooldown, "This command has a 5 minute cooldown between uses.");
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
            sendMessageQueue(e, "No one is playing anything on your friend list!");
            return;
        }
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle(
                        showFresh ? "What is being played now in your friend list" : "What was being played in your friend list ");

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
            parser.sendError("You can't add yourself!", e);
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
                if ((usersSorted.first() == author && status == Friend.FriendStatus.PENDING_SECOND)
                        || (usersSorted.second() == author && status == Friend.FriendStatus.PENDING_FIRST)) {
                    sendMessageQueue(e, "%s is yet to accept your friend request".formatted(userInfoEscaped.username()));
                } else {
                    if (db.acceptRequest(usersSorted.first(), usersSorted.second())) {
                        sendMessageQueue(e, "Accepted %s's friend request!".formatted(userInfoEscaped.username()));
                    } else {
                        sendMessageQueue(e, "Something went wrong accepting the request. The request may have become invalid.");
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
                        .setDescription(("**Account:** %s (%s)%n" +
                                "**Last.fm:** [%s](%s)%n".formatted(authorData.getName(), PrivacyUtils.getLastFmUser(authorData.getName())) +
                                "%s").formatted(e.getAuthor().getAsMention(), e.getAuthor().getAsTag(), finalArtists));
                MessageBuilder messageBuilder = new MessageBuilder(eb.build()).setActionRows(
                        ActionRow.of(ButtonUtils.declineFriendRequest(author), ButtonUtils.acceptFriendRequest(author)));
                return privateChannel.sendMessage(messageBuilder.build());
            }).queue(message -> sendMessageQueue(e, "Sent a friend request to %s".formatted(userInfoEscaped.username())),
                    throwable -> e.sendMessage("An error occurred while trying to send a DM to %s!".formatted(userInfoEscaped.username())).queue());
        }
    }


}

