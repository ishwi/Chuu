package core.util;

import core.commands.whoknows.LocalWhoKnowsAlbumCommand;
import core.commands.whoknows.LocalWhoKnowsSongCommand;
import core.commands.whoknows.WhoKnowsCommand;
import core.parsers.*;
import core.parsers.utils.OptionalEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public enum FriendsActions implements Subcommand, Aliasable, Descriptible {
    ADD((deps) -> new OnlyUsernameParser(deps.db(), true), "Sends a request to a given user"),
    PENDING((deps) -> NoOpParser.INSTANCE, "Sees the request that you have sent that are yet to be accepted"),
    INCOMING((deps) -> NoOpParser.INSTANCE, "Lists all your incoming petitions"),
    REMOVE((deps) -> new OnlyUsernameParser(deps.db(), true), "Deletes a certain friend from your friend list", "delete", "erase"),
    LIST((deps) -> new OnlyUsernameParser(deps.db()), "Lists all your friends"),
    NP((deps) -> new OnlyUsernameParser(deps.db())
            .addOptional(new OptionalEntity("recent", "the last scrobble instead of the current scrobble")),
            "What your friends are playing right now"
            , "fm"),
    WK((deps) -> new ArtistParser(deps.db(), deps.lastFM()), "Who knows an artist in your friendlist", WhoKnowsCommand.WK_ALIASES),
    WKT((deps) -> new ArtistSongParser(deps.db(), deps.lastFM()), "Who knows an song in your friendlist", LocalWhoKnowsSongCommand.WKT_ALIASES),
    WKA((deps) -> new ArtistAlbumParser(deps.db(), deps.lastFM()), "Who knows an album in your friendlist", LocalWhoKnowsAlbumCommand.WKA_ALIASES);

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
