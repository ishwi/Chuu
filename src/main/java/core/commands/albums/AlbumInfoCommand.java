package core.commands.albums;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.params.ArtistAlbumParameters;
import core.services.ColorService;
import core.services.TagAlbumService;
import dao.ChuuService;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class AlbumInfoCommand extends AlbumPlaysCommand {
    private final MusicBrainzService mb;

    public AlbumInfoCommand(ChuuService dao) {
        super(dao);
        mb = MusicBrainzServiceSingleton.getInstance();
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public String getDescription() {
        return "Information about an album";
    }

    @Override
    public List<String> getAliases() {
        return List.of("albuminfo", "abi");
    }

    @Override
    public String getName() {
        return "Album Info";
    }

    @Override
    protected void doSomethingWithAlbumArtist(ScrobbledArtist artist, String album, Context e, long who, ArtistAlbumParameters params) throws LastFmException {
        LastFMData lastFMData = params.getLastFMData();
        FullAlbumEntityExtended albumSummary = lastFM.getAlbumSummary(lastFMData, artist.getArtist(), album);
        String username = getUserString(e, who, lastFMData.getName());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        String tagsField = albumSummary.getTagList().isEmpty()
                ? ""
                : albumSummary.getTagList().stream()
                .map(tag -> "[" + CommandUtil.cleanMarkdownCharacter(tag) + "](" + LinkUtils.getLastFmTagUrl(tag) + ")")
                .collect(Collectors.joining(" - "));
        StringBuilder trackList = new StringBuilder();

        MusicbrainzFullAlbumEntity albumInfo = mb.getAlbumInfo(albumSummary);
        albumSummary.getTrackList().forEach(x ->
                trackList.append(x.getPosition()).append(". ")
                        .append(CommandUtil.cleanMarkdownCharacter(x.getName()))
                        .append(". ").append(
                        String
                                .format("%02d:%02d", x.getDuration() / 60, x.getDuration() % 60))
                        .append("\n"));
        embedBuilder.setTitle(CommandUtil.cleanMarkdownCharacter(albumSummary.getAlbum()), LinkUtils.getLastFmArtistAlbumUrl(albumSummary.getArtist(), albumSummary.getAlbum()))
                .addField("Artist:", "[" + CommandUtil.cleanMarkdownCharacter(albumSummary.getArtist()) + "](" + LinkUtils.getLastFmArtistUrl(albumSummary.getArtist()) + ")", false)
                .addField(username + "'s plays:", String.valueOf(albumSummary.getTotalPlayNumber()), true)
                .addField("Listeners:", String.valueOf(albumSummary.getListeners()), true)
                .addField("Scrobbles:", String.valueOf(albumSummary.getTotalscrobbles()), true)
                .addField("Tags:", tagsField, false);
        if (!albumInfo.getTags().isEmpty()) {
            String tagLine = albumInfo.getTags().stream().limit(5)
                    .filter(x -> x != null && x.length() > 0)
                    .map(tag -> "[" + CommandUtil.cleanMarkdownCharacter(tag) + "](" + LinkUtils.getMusicbrainzTagUrl(tag) + ")")
                    .collect(Collectors.joining(" - "));
            embedBuilder.addField("MusicBrainz Tags: ", tagLine, false);
        }
        if (albumInfo.getYear() != null) {
            embedBuilder.addField("Year:", String.valueOf(albumInfo.getYear()), false);
        }

        if (!albumSummary.getTrackList().isEmpty()) {
            embedBuilder.addField("Track List:", trackList.substring(0, Math.min(trackList.length(), 1000)), false)
                    .addField("Total Duration:",
                            (String.format("%02d:%02d minutes", albumSummary.getTotalDuration() / 60, albumSummary.getTotalDuration() % 60))
                            , true);
        }
        embedBuilder.setImage(
                Chuu.getCoverService().getCover(albumSummary.getArtist(), albumSummary.getAlbum(), albumSummary.getAlbumUrl(), e))
                .setColor(ColorService.computeColor(e))
                .setThumbnail(artist.getUrl());
        e.sendMessage(embedBuilder.build()).queue();
        if (!albumSummary.getTagList().isEmpty()) {
            executor.submit(new TagAlbumService(db, lastFM, albumSummary.getTagList(), new AlbumInfo(albumSummary.getMbid(), albumSummary.getAlbum(), albumSummary.getArtist())));

        }
    }
}
