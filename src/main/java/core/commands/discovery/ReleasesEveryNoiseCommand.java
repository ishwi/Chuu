package core.commands.discovery;

import core.apis.spotify.SpotifyUtils;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.Confirmator;
import core.otherlisteners.Reactionary;
import core.otherlisteners.util.ConfirmatorItem;
import core.parsers.Parser;
import core.parsers.UserStringParser;
import core.parsers.params.UserStringParameters;
import dao.ChuuService;
import dao.everynoise.NoiseGenre;
import dao.everynoise.NoiseGenreReleases;
import dao.everynoise.Release;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.commons.text.WordUtils;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReleasesEveryNoiseCommand extends ConcurrentCommand<UserStringParameters> {
    private static final String genreUrl = "https://everynoise.com/new_releases_by_genre.cgi?genre=%s&region=GB&albumsonly=true&hidedupes=on";

    public ReleasesEveryNoiseCommand(ChuuService dao) {
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
        return List.of("releases", "newralbums", "releasegenre", "releasenoise", "everynoiserelease");
    }

    @Override
    public String getName() {
        return "Everynoise releases";
    }

    @Override
    protected void onCommand(Context e, @NotNull UserStringParameters params) throws LastFmException, InstanceNotFoundException {
        String input = params.getInput();
        boolean isSearchResult = !input.isBlank();
        if (!isSearchResult) {
            handleList(e);
        } else {
            handleSearch(e, input);
        }

    }


    private void handleList(Context e) {
        Map<NoiseGenreReleases, Integer> counts = db.releasesByCount();
        String tab = EmbedBuilder.ZERO_WIDTH_SPACE + "\t\t";


        List<String> strings = counts.entrySet().stream().map(z -> {
            NoiseGenreReleases key = z.getKey();
            Integer count = z.getValue();
            String releases = z.getKey().releases().stream().limit(2).map(l -> "%s[%s - %s](%s)".formatted(tab, l.artist(), l.release(), SpotifyUtils.getAlbumLink(l.uri()))).collect(Collectors.joining("\n"));
            if (releases.isBlank()) {
                releases += tab + "-";
            }
            if (key.releases().size() > 2) {
                releases += "\n" + tab + "...";
            }
            return "**[%s](%s)**: %d %s\n%s\n\n".formatted(key.name(),
                    genreUrl.formatted(URLEncoder.encode(key.name(), StandardCharsets.UTF_8)),
                    count,
                    CommandUtil.singlePlural(count, "release", "releases"), releases);
        }).toList();
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 5 && i < strings.size(); i++) {
            a.append(strings.get(i));
        }
        ChuuEmbedBuilder eb = new ChuuEmbedBuilder();
        String title = "Genres with releases";
        eb.setDescription(a)
                .setAuthor(title, "https://everynoise.com/", "https://pbs.twimg.com/profile_images/3736544396/e0d7d0c8f2781c40b5f870df441e670c_400x400.png")
                .setColor(CommandUtil.pastelColor())
                .setFooter("There are  " + strings.size() + " unique genres with releases");

        e.sendMessage(eb.build()).queue(finalMessage ->
                new Reactionary<>(strings, finalMessage, 5, eb, false));
    }

    private void handleSearch(Context e, String input) {
        Optional<NoiseGenre> exactMatch = db.findExactMatch(input);
        NoiseGenre theOne;
        if (exactMatch.isEmpty()) {
            List<NoiseGenre> matchingGenre = db.findMatchingGenre(input).stream().limit(5).toList();
            if (matchingGenre.isEmpty()) {
                sendMessage(e, "Couldn't find any genre searching by " + input);
                return;
            } else if (matchingGenre.size() > 1) {
                int counter = 1;
                List<ConfirmatorItem> reacts = new ArrayList<>();
                StringBuilder description = new StringBuilder();
                var eb = new ChuuEmbedBuilder().setTitle("Multiple genres found").setFooter("Please disambiguate choosing the appropiate emote");
                for (NoiseGenre noiseGenre : matchingGenre) {
                    //  48 is 0x0030 -> which is 0 || 0x0031 is 1 ...
                    String s = new String(new int[]{48 + counter++}, 0, 1);
                    String emote = s + "\ufe0f\u20e3";

                    ConfirmatorItem confirmatorItem = new ConfirmatorItem(emote, (z) -> z, (z) ->
                            buildEmbed(e, z, db.releasesOfGenre(noiseGenre), noiseGenre.name()));
                    reacts.add(confirmatorItem);
                    description.append(emote).append(" \u279C ").append(noiseGenre.name()).append("\n");
                }
                e.sendMessage(eb.setDescription(description).build())
                        .queue(message -> new Confirmator(eb, message, e.getAuthor().getIdLong(), reacts, (z) -> z.clear().setDescription("You didn't select any genre!"), false, 50));
                return;
            } else
                theOne = matchingGenre.get(0);
        } else {
            theOne = exactMatch.get();
        }
        buildEmbed(e, null, db.releasesOfGenre(theOne), theOne.name());
    }

    private void buildEmbed(Context e, @Nullable Message message, List<Release> releases, String input) {
        if (releases.isEmpty()) {
            if (message == null) {
                sendMessageQueue(e, "Couldn't find any release searching by `%s`".formatted(input));
            } else {
                message.editMessage(new ChuuEmbedBuilder().setTitle("Didn't find any new release by " + input).build()).queue();
            }
            return;
        }
        List<String> strings = releases.stream().map(z -> "**[%s - %s](%s)**\n".formatted(z.artist(), z.release(), SpotifyUtils.getAlbumLink(z.uri()))).toList();
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < strings.size(); i++) {
            a.append(strings.get(i));
        }
        ChuuEmbedBuilder eb = new ChuuEmbedBuilder();
        String title = WordUtils.capitalize(input) + " releases";
        String footer = "";
        if (releases.size() > 10) {
            eb.setFooter("Found " + releases.size() + " " + input + " releases ");
        }
        eb.setDescription(a)
                .setAuthor(title, "https://everynoise.com/", "https://pbs.twimg.com/profile_images/3736544396/e0d7d0c8f2781c40b5f870df441e670c_400x400.png")
                .setColor(CommandUtil.pastelColor());
        RestAction<Message> messageRestAction;
        if (message == null) {
            messageRestAction = e.sendMessage(eb.build());
        } else {
            messageRestAction = message.editMessage(eb.build());
        }
        messageRestAction.queue(finalMessage ->
                new Reactionary<>(strings, finalMessage, eb, false));

    }
}
