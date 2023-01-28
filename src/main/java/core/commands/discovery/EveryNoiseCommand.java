package core.commands.discovery;

import core.apis.spotify.SpotifyUtils;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.Parser;
import core.parsers.UserStringParser;
import core.parsers.params.UserStringParameters;
import core.util.ServiceView;
import dao.everynoise.NoiseGenre;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EveryNoiseCommand extends ConcurrentCommand<UserStringParameters> {
    public EveryNoiseCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<UserStringParameters> initParser() {
        return new UserStringParser(db, true);
    }

    @Override
    public String getDescription() {
        return "List all of the genres supplied in everynoise.com or find one by name";
    }

    @Override
    public List<String> getAliases() {
        return List.of("everynoise", "findgenre", "noise", "every", "en", "new");
    }

    @Override
    public String getName() {
        return "Everynoise";
    }

    @Override
    public void onCommand(Context e, @NotNull UserStringParameters params) {
        String input = params.getInput();
        List<NoiseGenre> genres;
        boolean isSearchResult = !input.isBlank();
        if (!isSearchResult) {
            genres = db.listAllGenres();
            assert !genres.isEmpty();
        } else {
            genres = db.findMatchingGenre(input);
            if (genres.isEmpty()) {
                sendMessageQueue(e, "Couldn't find any genre searching by `%s`".formatted(input));
                return;
            }
        }
        List<String> strings = genres.stream().map(z -> "**[%s](%s)**\n".formatted(z.name(), SpotifyUtils.getPlaylistLink(z.uri()))).toList();
        String title = "Everynoise genres";
        String footer = "";
        if (isSearchResult) {
            title += " matching by " + input;
            footer = "Found %d genres".formatted(strings.size());
        } else if (strings.size() > 20) {
            footer = "Have %d genres".formatted(strings.size());
        }
        EmbedBuilder eb = new ChuuEmbedBuilder(e)
                .setAuthor(title, "https://everynoise.com/", "https://pbs.twimg.com/profile_images/3736544396/e0d7d0c8f2781c40b5f870df441e670c_400x400.png")
                .setFooter("You can click the genre for a playlist!\n" + footer, null);

        new PaginatorBuilder<>(e, eb, strings).unnumered().pageSize(20).build().queue();


    }
}
