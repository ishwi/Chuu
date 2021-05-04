package core.commands.stats;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.lyrics.EvanLyrics;
import core.apis.lyrics.Lyrics;
import core.apis.lyrics.TextSplitter;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public class LyricsCommand extends ConcurrentCommand<ArtistAlbumParameters> {
    private final EvanLyrics evanLyrics;
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;


    public LyricsCommand(ChuuService dao) {
        super(dao);
        evanLyrics = new EvanLyrics();
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();

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
        return "Gets lyrics from a given song";
    }

    @Override
    public List<String> getAliases() {
        return List.of("lyrics");
    }

    @Override
    public String getName() {
        return "Lyrics";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistAlbumParameters params) throws LastFmException {
        String song = params.getAlbum();
        String artist = params.getArtist();
        String url = null;
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        try {
            CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotifyApi);
            url = scrobbledArtist.getUrl();
        } catch (LastFmEntityNotFoundException exception) {
            //Ignored
        }
        String correctedArtist = scrobbledArtist.getArtist();
        Optional<Lyrics> or = evanLyrics.getLyrics(correctedArtist, song).or(() -> evanLyrics.getTopResult(correctedArtist + " " + song));
        if (or.isEmpty()) {
            sendMessageQueue(e, String.format("Couldn't find any lyrics for %s - %s", correctedArtist, song));
            return;
        }
        Lyrics lyrics = or.get();
        List<String> pages = TextSplitter.split(lyrics.getLyrics(), 1000);
        if (pages.isEmpty()) {
            sendMessageQueue(e, String.format("Couldn't find any lyrics for %s - %s", correctedArtist, song));
            return;
        }

        String desc = pages.get(0);
        if (pages.size() != 1) {
            desc += "\n1" + "/" + pages.size();
        }
        String urlImage = CommandUtil.getUserInfoNotStripped(e, params.getLastFMData().getDiscordId()).getUrlImage();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(desc)
                .setColor(ColorService.computeColor(e))
                .setAuthor(String.format("%s - %s", correctedArtist, song), LinkUtils.getLastFMArtistTrack(correctedArtist, song), urlImage)
                .setFooter(String.format("Lyrics found for %s - %s", correctedArtist, song))
                .setThumbnail(lyrics.getImageUrl() == null ? url : lyrics.getImageUrl());
        e.sendMessage(new MessageBuilder().setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(pages, message1, 1, embedBuilder, false, true, 120));
    }
}
