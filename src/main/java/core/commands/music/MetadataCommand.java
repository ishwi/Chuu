package core.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.abstracts.MusicCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandUtil;
import core.music.MusicManager;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.Metadata;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class MetadataCommand extends MusicCommand<CommandParameters> {

    private final Pattern metadataExpr = Pattern.compile("(?<!\\\\)\\s*-\\s*");

    public MetadataCommand(ServiceView dao) {
        super(dao);
        requirePlayingTrack = true;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Changes the metadata of the current playing track for scrobbling purposes.";
    }

    @Override
    public List<String> getAliases() {
        return List.of("meta", "metadata");
    }

    @Override
    public String getName() {
        return "Metadata";
    }

    @Override
    public void onCommand(Context e, @Nonnull CommandParameters params) {
        String[] message = parser.getSubMessage(e);
        MusicManager manager = getManager(e);
        AudioTrack track = manager.getCurrentTrack();
        String identifier = track.getInfo().uri;

        String cleanUri = CommandUtil.escapeMarkdown(identifier);
        String words = String.join(" ", message);
        if (words.length() == 0) {
            manager.getScrobble().thenAccept(scrobble ->
                    e.sendMessage(new ChuuEmbedBuilder(e).setThumbnail(scrobble.image())
                            .setTitle("Current song metadata", identifier)
                            .setThumbnail(scrobble.image())
                            .setFooter(scrobble.linelessReversed())
                            .setDescription(scrobble.toLines())
                            .build()).queue());
            return;
        }
        String[] split = metadataExpr.split(words);
        if (split.length != 2) {
            parser.sendError("Invalid metadata format: Use **ARTIST - SONG | ALBUM?**\nAlso dont forget to escape the character **-** if it appears on an artist/album/song name '", e);
            return;
        }

        String artist = split[0].trim().replaceAll("\\\\-", "-");
        String song = split[1].trim().replaceAll("\\\\-", "-");
        String[] songSplitted = song.split("\\|");
        song = songSplitted[0];
        String matchedAlbum = songSplitted.length > 1 ? String.join(" ", Arrays.copyOfRange(songSplitted, 1, songSplitted.length)) : null;
        String image = null;
        if (e instanceof ContextMessageReceived mes) {
            image = mes.e().getMessage().getAttachments().stream().filter(Message.Attachment::isImage).findFirst().map(Message.Attachment::getUrl).orElse(null);
        }
        Metadata metadata = new Metadata(artist, song, matchedAlbum, image);
        db.storeMetadata(identifier, metadata);
        String finalImage = image;
        manager.setMetadata(metadata).thenCompose(v -> manager.getScrobble()).thenAccept(z ->
                e.sendMessage(new ChuuEmbedBuilder(e).setThumbnail(finalImage)
                                .setAuthor("Metadata changed", identifier)
                                .setDescription(z.toLines())
                                .build()).
                        queue());
    }
}
