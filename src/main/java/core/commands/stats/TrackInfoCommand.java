package core.commands.stats;

import core.apis.last.entities.TrackExtended;
import core.commands.Context;
import core.commands.albums.AlbumPlaysCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.ColorService;
import core.services.TrackTagService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.TrackInfo;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class TrackInfoCommand extends AlbumPlaysCommand {
    public TrackInfoCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistSongParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Information about a track";
    }

    @Override
    public List<String> getAliases() {
        return List.of("trackinfo", "ti");
    }

    @Override
    public String getName() {
        return "Track Info";
    }

    protected void doSomethingWithAlbumArtist(ScrobbledArtist artist, String song, Context e, long who, ArtistAlbumParameters params) throws LastFmException {
        LastFMData lastFMData = params.getLastFMData();
        TrackExtended trackInfo = this.lastFM.getTrackInfoExtended(lastFMData, artist.getArtist(), song);


        String username = getUserString(e, who, lastFMData.getName());
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder();
        String tagsField = trackInfo.getTags().isEmpty()
                ? ""
                : trackInfo.getTags().stream()
                .map(tag -> String.format("[%s](%s)", CommandUtil.cleanMarkdownCharacter(tag), LinkUtils.getLastFmTagUrl(tag)))
                .collect(Collectors.joining(" - "));

        embedBuilder.setTitle(trackInfo.getName(), LinkUtils.getLastFMArtistTrack(trackInfo.getArtist(), trackInfo.getName()))
                .addField("Artist:", String.format("[%s](%s)", CommandUtil.cleanMarkdownCharacter(trackInfo.getArtist()),
                        LinkUtils.getLastFmArtistUrl(trackInfo.getArtist())), false);
        if (trackInfo.getAlbumName() != null) {
            embedBuilder.
                    addField("Album:",
                            String.format("[%s](%s)", CommandUtil.cleanMarkdownCharacter(trackInfo.getAlbumName()), LinkUtils.getLastFmArtistAlbumUrl(trackInfo.getArtist(), trackInfo.getAlbumName())),
                            false);
        }
        embedBuilder
                .addField(username + "'s plays:", String.valueOf(trackInfo.getPlays()), true)
                .addField("Loved?", trackInfo.isLoved() ? ":heart:" : ":black_heart: ", true)

                .addField("Listeners:", String.valueOf(trackInfo.getListeners()), true)
                .addField("Scrobbles:", String.valueOf(trackInfo.getTotalPlayCount()), true)
                .addField("Tags:", tagsField, false)

                .addField("Duration:",
                        (String.format("%02d:%02d minutes", trackInfo.getDuration() / 60, trackInfo.getDuration() % 60))
                        , true);

        embedBuilder.setImage(trackInfo.getImageUrl() == null || trackInfo.getImageUrl().isBlank() ? null : trackInfo.getImageUrl())
                .setColor(ColorService.computeColor(e))
                .setThumbnail(artist.getUrl());
        e.sendMessage(embedBuilder.build()).

                queue();
        if (!trackInfo.getTags().isEmpty()) {
            executor.submit(new TrackTagService(db, lastFM, trackInfo.getTags(), new TrackInfo(trackInfo.getArtist(), null, trackInfo.getName(), null)));
        }
    }
}
