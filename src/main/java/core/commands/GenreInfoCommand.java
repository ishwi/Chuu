package core.commands;

import core.exceptions.LastFmException;
import core.parsers.GenreParser;
import core.parsers.Parser;
import core.parsers.params.GenreParameters;
import dao.ChuuService;
import dao.entities.GenreInfo;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class GenreInfoCommand extends ConcurrentCommand<GenreParameters> {
    public GenreInfoCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public Parser<GenreParameters> initParser() {
        return new GenreParser(getService(), lastFM);
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        GenreParameters gp = parser.parse(e);
        if (gp == null) {
            return;
        }
        String genre = gp.getGenre();
        GenreInfo genreInfo = lastFM.getGenreInfo(genre);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String substring = genreInfo.getString() != null && !genreInfo.getString().isBlank() ? genreInfo.getString().substring(0, Math.min(1024, genreInfo.getString().length())) : "";
        embedBuilder.setTitle("Information about " + genreInfo.getName())
                .addField("Usage of the genre:", String.valueOf(genreInfo.getTotal()), false)
                .addField("Listeners", String.valueOf(genreInfo.getReach()), false)
                .addField("Info", substring, false);

        if (gp.isAutoDetected()) {
            NowPlayingArtist np = gp.getNp();
            embedBuilder.setFooter("This genre was obtained from " + String.format("%s - %s | %s", np.getArtistName(), np.getSongName(), np.getAlbumName()));
        }
        e.getChannel().sendMessage(new MessageBuilder().setEmbed(embedBuilder.build()).build()).queue();
    }
}
