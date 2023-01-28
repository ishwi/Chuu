package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.explanation.AlbumExplanation;
import core.parsers.explanation.YearExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.AlbumYearParameters;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlbumYearParser extends DaoParser<AlbumYearParameters> {
    private final Pattern a = Pattern.compile(".*(?:year|y):(\\d{4}).*");

    public AlbumYearParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    protected AlbumYearParameters parseLogic(Context e, String[] words) {
        // Check if it exists
        String message = String.join(" ", words);
        Matcher matcher = a.matcher(message);
        if (!matcher.matches()) {
            sendError(String.format("Invalid format. You must provide the artist name and then the year with the following format: artist - album year:%s", Year.now()), e);
            return null;
        }
        String year = matcher.group(1);
        Year parse;
        try {
            parse = Year.parse(year);
            if (parse.isAfter(Year.now())) {
                sendError(year + " is a future year and cannot be used.", e);
                return null;
            }
        } catch (DateTimeParseException exception) {
            sendError(String.format("Invalid format. You must provide the artist name and then the year with the following format: artist - album year:%s", Year.now()), e);
            return null;
        }
        message = message.replaceFirst("(y|year):" + year, "");
        String regex = "(?<!\\\\)" + ("\\s*-\\s*");
        String[] content = message.split(regex);

        if (content.length != 2) {
            sendError(String.format("Invalid format. You must provide the artist name and then the year with the following format: artist - album year:%s", Year.now()), e);
            return null;
        }
        String artist = content[0].trim().replaceAll("\\\\-", "-");
        String album = content[1].trim().replaceAll("\\\\-", "-");

        AlbumInfo ai = new AlbumInfo(album, artist);
        return new AlbumYearParameters(e, parse, ai);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(InteractionAux.required(new AlbumExplanation().artist()),
                InteractionAux.required(new AlbumExplanation().album()), InteractionAux.required(new YearExplanation()));
    }

    @Override
    public AlbumYearParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) {
        InteractionAux.ArtistAlbum artistAlbum = InteractionAux.parseAlbum(ctx.e(), () -> {
        });
        Year year = InteractionAux.parseYear(ctx.e(), () -> sendError("Inputted year is a future year and cannot be used.", ctx));
        assert artistAlbum != null;
        return new AlbumYearParameters(ctx, year, new AlbumInfo(artistAlbum.album(), artistAlbum.artist()));

    }
}
