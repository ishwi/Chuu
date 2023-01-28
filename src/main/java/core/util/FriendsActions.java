package core.util;

import core.commands.whoknows.LocalWhoKnowsAlbumCommand;
import core.commands.whoknows.LocalWhoKnowsSongCommand;
import core.commands.whoknows.WhoKnowsCommand;
import core.parsers.*;
import core.parsers.utils.OptionalEntity;
import core.parsers.utils.Optionals;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public enum FriendsActions implements Subcommand, Aliasable, Descriptible {
    ADD((deps) -> new OnlyUsernameParser(deps.db(), true), "Sends a friend request to a given user"),
    PENDING((deps) -> NoOpParser.INSTANCE, "Lists the friend requests you have sent that are yet to be accepted"),
    INCOMING((deps) -> NoOpParser.INSTANCE, "Lists all your incoming requests"),
    REMOVE((deps) -> new OnlyUsernameParser(deps.db(), true), "Removes the specified friend from your friend list", "delete", "erase"),
    LIST((deps) -> new OnlyUsernameParser(deps.db()), "Lists all your friends"),
    NP((deps) -> new OnlyUsernameParser(deps.db())
            .addOptional(new OptionalEntity("recent", "the last scrobble instead of the current scrobble")),
            "What your friends are playing right now"
            , "fm"),
    WK((deps) -> new ArtistParser(deps.db(), deps.lastFM()), "Who knows an artist in your friend list", WhoKnowsCommand.WK_ALIASES),
    WKT((deps) -> new ArtistSongParser(deps.db(), deps.lastFM()), "Who knows a song in your friend list", LocalWhoKnowsSongCommand.WKT_ALIASES),
    WKA((deps) -> new ArtistAlbumParser(deps.db(), deps.lastFM()), "Who knows an album in your friend list", LocalWhoKnowsAlbumCommand.WKA_ALIASES),
    TOP((deps) -> new OnlyChartSizeParser(deps.db())
            .replaceOptional("plays", Optionals.NOPLAYS.opt)
            .addOptional(Optionals.PLAYS.opt.withBlockedBy("noplays")), "Chart compose of all your friends favourite artists"),
    CHART((deps) -> new OnlyChartSizeParser(deps.db())
            .replaceOptional("plays", Optionals.NOPLAYS.opt)
            .addOptional(Optionals.PLAYS.opt.withBlockedBy("noplays")), "Chart compose of all your friends favourite albums", "c"),
    TOPTRACKS((deps -> new OnlyChartSizeParser(deps.db())
            .replaceOptional("plays", Optionals.NOPLAYS.opt)
            .replaceOptional("list", Optionals.IMAGE.opt)
            .addOptional(Optionals.LIST.opt.withBlockedBy("image", "pie", "aside"))
            .addOptional(Optionals.PLAYS.opt.withBlockedBy("noplays"))), "List of top tracks for your friends", "tt"),
    FAVS((deps) -> new ArtistParser(deps.db(), deps.lastFM())
            .addOptional(Optionals.LIST.opt)
            .addOptional(Optionals.PIE.opt)
            , "Favourite tracks for your friend list", "favourites", "favorites", "artist-songs"),
    ARTIST((deps) -> new ArtistParser(deps.db(), deps.lastFM())
            .addOptional(Optionals.PIE.opt), "Favourite albums for an artist in your friend list", "a");

    private final SubcommandEx<?> subcommandEx;
    private final Set<String> aliases;
    private final String description;


    FriendsActions(SubcommandEx<?> subcommandEx, String description) {
        this(subcommandEx, description, Collections.emptySet());
    }

    FriendsActions(SubcommandEx<?> subcommandEx, String description, Collection<String> aliases) {
        this.subcommandEx = subcommandEx;
        this.aliases = Set.copyOf(aliases);
        this.description = description;
    }


    FriendsActions(SubcommandEx<?> subcommandEx, String description, String... aliases) {
        this(subcommandEx, description, Set.of(aliases));
    }


    @Override
    public SubcommandEx<?> getSubcommandEx() {
        return subcommandEx;
    }


    @Override
    public Set<String> aliases() {
        return aliases;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
