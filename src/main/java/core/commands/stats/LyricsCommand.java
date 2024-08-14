package core.commands.stats;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import core.Chuu;
import core.apis.lyrics.EvanLyrics;
import core.apis.lyrics.Lyrics;
import core.apis.lyrics.TextSplitter;
import core.commands.Context;
import core.commands.abstracts.MusicCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.music.MusicManager;
import core.music.utils.YoutubeSearchManagerSingleton;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.validators.ArtistValidator;
import core.util.ServiceView;
import dao.entities.ScrobbledArtist;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LyricsCommand extends MusicCommand<ArtistAlbumParameters> {
    private final EvanLyrics evanLyrics;
    private final YoutubeAudioSourceManager audioSourceManager;


    public LyricsCommand(ServiceView dao) {
        super(dao);
        evanLyrics = new EvanLyrics();
        requireManager = false;
        audioSourceManager = YoutubeSearchManagerSingleton.getInstance();

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
    public void onCommand(Context e, @NotNull ArtistAlbumParameters params) throws LastFmException {
        MusicManager manager = Chuu.playerRegistry.get(e.getGuild());
        Optional<Lyrics> lyrics = Optional.empty();
        if (manager != null && e.getMember().getVoiceState() != null && e.getGuild().getSelfMember().getVoiceState() != null) {
            lyrics = manager.getLyrics();
        }
        String song = params.getAlbum();
        String artist = params.getArtist();
        String url = null;
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        try {
            scrobbledArtist = new ArtistValidator(db, lastFM, e).validate(artist, !params.isNoredirect());
            url = scrobbledArtist.getUrl();
        } catch (LastFmEntityNotFoundException exception) {
            //Ignored
        }
        String correctedArtist = scrobbledArtist.getArtist();
        Optional<Lyrics> or = lyrics
                .or(() -> evanLyrics.getLyrics(correctedArtist, song)).
                or(() -> evanLyrics.getTopResult(correctedArtist + " " + song))
                .or(() -> {
                    AudioItem audioItem = audioSourceManager.loadItem(null, new AudioReference("ytsearch:" + artist + " " + song, null));
                    if (audioItem instanceof BasicAudioPlaylist playlist) {
                        return playlist.getTracks().stream().limit(5).map(t -> {
                            String identifier = t.getInfo().identifier;
                            String foundLyrics = audioSourceManager.getLyricsForVideo(identifier);
                            if (foundLyrics != null) {
                                return new Lyrics(foundLyrics, t.getInfo().author, t.getInfo().title);
                            }
                            return null;
                        }).filter(Objects::nonNull).findFirst();
                    }
                    return Optional.empty();
                });
        if (or.isEmpty()) {
            sendMessageQueue(e, String.format("Couldn't find any lyrics for %s - %s", correctedArtist, song));
            return;
        }
        Lyrics lyr = or.get();
        List<String> pages = TextSplitter.split(lyr.getLyrics(), 3000);
        if (pages.isEmpty()) {
            sendMessageQueue(e, String.format("Couldn't find any lyrics for %s - %s", correctedArtist, song));
            return;
        }


        String urlImage = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId()).urlImage();
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s - %s", correctedArtist, song), LinkUtils.getLastFMArtistTrack(correctedArtist, song), urlImage)
                .setFooter(String.format("Lyrics found for %s - %s", correctedArtist, song))
                .setThumbnail(lyr.getImageUrl() == null ? url : lyr.getImageUrl());
        new PaginatorBuilder<>(e, embedBuilder, pages).pageSize(1).unnumered().withIndicator().seconds(120).build().queue();

    }
}
