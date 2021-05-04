package core.commands.artists;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.AlbumUserPlays;
import dao.entities.ScrobbledArtist;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

public class GlobalFavesFromArtistCommand extends ConcurrentCommand<ArtistParameters> {

    private final DiscogsApi discogs;
    private final Spotify spotify;

    public GlobalFavesFromArtistCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = true;
        this.discogs = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Favourite tracks from an artist on the bot";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("globalfavs", "globalfavourites", "globalfavorites", "gfavs");
    }

    @Override
    public String getName() {

        return "Favs in the bot";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();
        String artist = params.getArtist();
        ScrobbledArtist who = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(db, who, lastFM, discogs, spotify);
        String lastFmName = params.getLastFMData().getName();
        List<AlbumUserPlays> songs = db.getGlboalTopArtistTracks(who.getArtistId(), Integer.MAX_VALUE);
        if (songs.isEmpty()) {
            sendMessageQueue(e, ("Couldn't find any tracks of " + CommandUtil.cleanMarkdownCharacter(who.getArtist()) + " in the bot!"));
            return;
        }
        String userString = e.getJDA().getSelfUser().getName();
        StringBuilder a = new StringBuilder();
        List<String> s = songs.stream().map(g -> ". **[" + CommandUtil.cleanMarkdownCharacter(g.getAlbum()) + "](" + LinkUtils.getLastFMArtistTrack(g.getArtist(), g.getAlbum()) + ")** - " + g.getPlays() + " plays" +
                                                 "\n").toList();
        for (int i = 0; i < s.size() && i < 10; i++) {
            String sb = s.get(i);
            a.append(i + 1).append(sb);
        }
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setDescription(a)
                .setFooter(userString + " users have listened to " + s.size() + " different " + who.getArtist() + " songs!")
                .setAuthor(String.format("%s's top %s tracks", userString, who.getArtist()), PrivacyUtils.getLastFmArtistUserUrl(who.getArtist(), lastFmName), e.getJDA().getSelfUser().getAvatarUrl())
                .setColor(ColorService.computeColor(e))
                .setThumbnail(CommandUtil.noImageUrl(who.getUrl()));
        e.sendMessage(embedBuilder.build()).queue(mes ->
                new Reactionary<>(s, mes, embedBuilder));
    }
}
