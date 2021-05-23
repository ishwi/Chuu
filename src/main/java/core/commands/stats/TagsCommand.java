package core.commands.stats;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ServiceView;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.text.WordUtils;

import javax.validation.constraints.NotNull;
import java.util.List;

public class TagsCommand extends ConcurrentCommand<ArtistParameters> {

    private final Spotify spotify;
    private final DiscogsApi discogsApi;

    public TagsCommand(ServiceView dao) {
        super(dao);
        this.spotify = SpotifySingleton.getInstance();
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }


    @Override
    public String getDescription() {
        return "List all the tags of one artist ";
    }

    @Override
    public List<String> getAliases() {
        return List.of("tags", "genres");
    }

    @Override
    public String getName() {
        return "Tags";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistParameters params) throws LastFmException, InstanceNotFoundException {
        ArtistParameters parse = this.parser.parse(e);

        String artist = parse.getArtist();

        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify);


        String correctedArtist = CommandUtil.escapeMarkdown(scrobbledArtist.getArtist());
        List<String> artistTags = db.getArtistTag(scrobbledArtist.getArtistId())
                .stream().map(x -> String.format(". **[%s](%s)**%n",
                        WordUtils.capitalizeFully(x)
                        , LinkUtils.getLastFmTagUrl(x))).toList();
        if (artistTags.isEmpty()) {
            sendMessageQueue(e, correctedArtist + " doesn't have any tags.");
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < artistTags.size(); i++) {
            a.append(i + 1).append(artistTags.get(i));
        }


        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setDescription(a)
                .setAuthor(correctedArtist + "'s tags", LinkUtils.getLastFmArtistUrl(scrobbledArtist.getArtist()), scrobbledArtist.getUrl());
        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(artistTags, message1, embedBuilder));
    }

}
