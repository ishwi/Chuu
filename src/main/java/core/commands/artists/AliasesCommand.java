package core.commands.artists;

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
import core.util.ServiceView;
import dao.entities.ScrobbledArtist;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

public class AliasesCommand extends ConcurrentCommand<ArtistParameters> {


    public AliasesCommand(ServiceView dao) {
        super(dao);
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
        return "List all the aliases or corrections of one artist ";
    }

    @Override
    public List<String> getAliases() {
        return List.of("aliases", "corrections");
    }

    @Override
    public String getName() {
        return "Aliases";
    }

    @Override
    public void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException {
        String artist = params.getArtist();
        char prefix = CommandUtil.getMessagePrefix(e);

        ScrobbledArtist scrobbledArtist = new ArtistValidator(db, lastFM, e)
                .validate(artist, false, !params.isNoredirect());

        String correctedArtist = CommandUtil.escapeMarkdown(scrobbledArtist.getArtist());
        List<String> artistAliases = db.getArtistAliases(scrobbledArtist.getArtistId());

        if (artistAliases.isEmpty()) {
            sendMessageQueue(e, correctedArtist + " doesn't have any correction:");
            return;
        }

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(correctedArtist + "'s aliases", LinkUtils.getLastFmArtistUrl(correctedArtist), scrobbledArtist.getUrl())
                .setFooter("You can submit an alias using " + prefix + "alias", null);

        Function<String, String> mapper = x -> ". **" + CommandUtil.escapeMarkdown(x) + "**\n";


        new PaginatorBuilder<>(e, embedBuilder, artistAliases).mapper(mapper).build().queue();
    }

}
