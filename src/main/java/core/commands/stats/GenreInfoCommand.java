package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.GenreParser;
import core.parsers.Parser;
import core.parsers.params.GenreParameters;
import dao.ServiceView;
import dao.entities.GenreInfo;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.List;

public class GenreInfoCommand extends ConcurrentCommand<GenreParameters> {
    public GenreInfoCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.GENRES;
    }

    @Override
    public Parser<GenreParameters> initParser() {
        return new GenreParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Information about a Genre";
    }

    @Override
    public List<String> getAliases() {
        return List.of("genreinfo", "gi");
    }

    @Override
    public String getName() {
        return "Genre Information";
    }

    @Override
    protected void onCommand(Context e, @Nonnull GenreParameters params) throws LastFmException {


        String genre = params.getGenre();
        GenreInfo genreInfo = lastFM.getGenreInfo(genre);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);
        String substring = genreInfo.string() != null && !genreInfo.string().isBlank() ? genreInfo.string().substring(0, Math.min(1024, genreInfo.string().length())) : "";
        embedBuilder.setTitle("Information about " + genreInfo.name())
                .addField("Usage of the genre:", String.valueOf(genreInfo.total()), false)
                .addField("Listeners", String.valueOf(genreInfo.reach()), false)
                .addField("Info", substring, false);

        if (params.isAutoDetected()) {
            NowPlayingArtist np = params.getNp();
            embedBuilder.setFooter("This genre was obtained from " + String.format("%s - %s | %s", np.artistName(), np.songName(), np.albumName()));
        }
        e.sendMessage(embedBuilder.build()).queue();
    }
}
