package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.text.WordUtils;

import javax.annotation.Nonnull;
import java.util.List;

public class TagsCommand extends ConcurrentCommand<ArtistParameters> {


    public TagsCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.GENRES;
    }

    @Override
    public String slashName() {
        return "artist";
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
    public void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException, InstanceNotFoundException {
        ArtistParameters parse = this.parser.parse(e);

        String artist = parse.getArtist();

        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(artist, !params.isNoredirect());


        String correctedArtist = CommandUtil.escapeMarkdown(sA.getArtist());
        List<String> artistTags = db.getArtistTag(sA.getArtistId())
                .stream().map(x -> String.format(". **[%s](%s)**%n",
                        WordUtils.capitalizeFully(x)
                        , LinkUtils.getLastFmTagUrl(x))).toList();
        if (artistTags.isEmpty()) {
            sendMessageQueue(e, correctedArtist + " doesn't have any tags.");
            return;
        }

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(correctedArtist + "'s tags", LinkUtils.getLastFmArtistUrl(sA.getArtist()), sA.getUrl());

        new PaginatorBuilder<>(e, embedBuilder, artistTags).build().queue();
    }

}
