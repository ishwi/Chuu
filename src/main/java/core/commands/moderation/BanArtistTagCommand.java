package core.commands.moderation;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BanArtistTagCommand extends ConcurrentCommand<CommandParameters> {
    public BanArtistTagCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Bans a tag from an artist in the bot system";
    }

    @Override
    public List<String> getAliases() {
        return List.of("banartisttag", "bantaga");
    }

    @Override
    public String getName() {
        return "Ban Artist tag";
    }

    @Override
    protected void onCommand(Context e, @Nonnull CommandParameters params) throws LastFmException, InstanceNotFoundException {

        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = db.findLastFMData(idLong);
        if (lastFMData.getRole() != Role.ADMIN) {
            sendMessageQueue(e, "Only bot admins can delete tags");
            return;
        }
        Pattern regex = Pattern.compile("tag:([\\s\\S]+)artist:(.*)");

        String[] subMessage = parser.getSubMessage(e);
        String joined = String.join(" ", subMessage).trim();
        Matcher matcher = regex.matcher(joined);
        if (!matcher.matches()) {
            parser.sendError("Pattern must follow the following format:\n" +
                             "**tag:** your tag **artist:** your artist", e);
            return;
        }
        String tag = matcher.group(1).trim().replaceAll(" +", " ");
        String artist = matcher.group(2).trim().replaceAll(" +", " ");
        if (tag.isBlank() || artist.isBlank()) {
            sendMessageQueue(e, "Bruh.");
            return;
        }
        ScrobbledArtist scrobbledArtist = new ArtistValidator(db, lastFM, e).validate(artist, false, true);
        db.addArtistBannedTag(tag, scrobbledArtist.getArtistId(), e.getAuthor().getIdLong());
        sendMessageQueue(e, String.format("Deleted tag %s for artist %s.", tag, artist));


    }
}
