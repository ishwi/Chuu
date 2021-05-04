package core.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.abstracts.MusicCommand;
import core.commands.utils.CommandUtil;
import core.music.MusicManager;
import core.music.sources.MetadataTrack;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.Metadata;
import dao.entities.TriFunction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class MetadataCommand extends MusicCommand<CommandParameters> {
    protected static final TriFunction<String, String, String, String> mapper = (artist, album, song) -> {
        String s = album == null || album.isBlank() ? "" : "\nAlbum: **" + album + "**";
        return String
                .format("Artist: **%s**\nSong: **%s**%s", artist, song, s);
    };
    private final Pattern metadataExpr = Pattern.compile("(?<!\\\\)\\s*-\\s*");

    public MetadataCommand(ChuuService dao) {
        super(dao);
        requirePlayingTrack = true;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Changes the metadata of the current playing track for scrobbling porpouses";
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
    protected void onCommand(Context e, @NotNull CommandParameters params) {
        String[] message = parser.getSubMessage(e);
        MusicManager manager = getManager(e);
        AudioTrack track = manager.getCurrentTrack();
        String identifier = track.getInfo().uri;
        String album = null;
        String url = null;
        if (track instanceof MetadataTrack spo) {
            album = spo.getAlbum();
            url = spo.getImage();
        }
        String cleanUri = CommandUtil.cleanMarkdownCharacter(identifier);
        String words = String.join(" ", message);
        if (words.length() == 0) {
            Optional<Metadata> metadata = Optional.ofNullable(manager.getMetadata());
            String finalUrl = url;
            String finalAlbum = album;
            metadata.ifPresentOrElse(meta -> e.sendMessage(new EmbedBuilder().setColor(ColorService.computeColor(e)).setThumbnail(meta.image()).setTitle("Current song metadata", identifier)
                    .setDescription(mapper.apply(meta.artist(), meta.album(), meta.song())).build()).
                    queue(), () ->
                    e.sendMessage(new EmbedBuilder().setColor(ColorService.computeColor(e)).setThumbnail(finalUrl)
                            .setTitle("Current song metadata", identifier)
                            .setThumbnail(finalUrl)
                            .setDescription(mapper.apply(track.getInfo().author, finalAlbum, track.getInfo().title))
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
        manager.setMetadata(metadata);
        db.storeMetadata(identifier, metadata);
        e.sendMessage(new EmbedBuilder().setColor(ColorService.computeColor(e)).setThumbnail(image)
                .setTitle("Current song metadata", identifier)
                .setDescription(mapper.apply(artist, matchedAlbum, song))
                .build()).
                queue();


    }
}
