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
import core.parsers.ArtistTimeFrameParser;
import core.parsers.Parser;
import core.parsers.params.ArtistTimeFrameParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;
import dao.entities.Track;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

public class FavesFromArtistCommand extends ConcurrentCommand<ArtistTimeFrameParameters> {

    private final DiscogsApi discogs;
    private final Spotify spotify;

    public FavesFromArtistCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = true;
        this.discogs = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistTimeFrameParameters> initParser() {
        return new ArtistTimeFrameParser(db, lastFM, true);
    }

    @Override
    public String getDescription() {
        return "Your favourite tracks from an artist";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("favs", "favourites", "favorites");
    }

    @Override
    public String getName() {

        return "Fav tracks";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistTimeFrameParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();
        TimeFrameEnum timeframew = params.getTimeFrame();
        String artist = params.getArtist();
        ScrobbledArtist who = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(db, who, lastFM, discogs, spotify, true, !params.isNoredirect());
        List<Track> ai;
        String lastFmName = params.getLastFMData().getName();
        if (timeframew.equals(TimeFrameEnum.ALL)) {
            ai = db.getTopArtistTracks(lastFmName, who.getArtistId(), Integer.MAX_VALUE);
            if (ai.isEmpty()) {
                sendMessageQueue(e, ("Couldn't find your fav tracks of " + CommandUtil.cleanMarkdownCharacter(who.getArtist()) + timeframew.getDisplayString()));
                return;
            }
        } else {
            ai = lastFM.getTopArtistTracks(params.getLastFMData(), who.getArtist(), timeframew, artist);
            if (ai.isEmpty()) {
                sendMessageQueue(e, ("Couldn't find your fav tracks in your top 5k songs (or you don't have any track with more than 3 plays) of " + CommandUtil.cleanMarkdownCharacter(who.getArtist()) + timeframew.getDisplayString() + "!"));
                return;
            }
        }
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoNotStripped(e, userId);
        String userString = uInfo.getUsername();

        StringBuilder a = new StringBuilder();
        List<String> s = ai.stream().map(g -> ". **[" + CommandUtil.cleanMarkdownCharacter(g.getName()) + "](" + LinkUtils.getLastFMArtistTrack(g.getArtist(), g.getName()) + ")** - " + g.getPlays() + " plays" +
                                              "\n").toList();
        for (int i = 0; i < ai.size() && i < 10; i++) {
            String sb = s.get(i);
            a.append(i + 1).append(sb);
        }
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setDescription(a)
                .setAuthor(String.format("%s's top %s tracks%s", userString, who.getArtist(), timeframew.getDisplayString()), PrivacyUtils.getLastFmArtistUserUrl(who.getArtist(), lastFmName), uInfo.getUrlImage())
                .setColor(ColorService.computeColor(e))
                .setThumbnail(CommandUtil.noImageUrl(who.getUrl()));
        if (ai.size() > 10) {
            embedBuilder.setFooter(userString + " has listened to " + s.size() + " different " + who.getArtist() + " songs!");
            e.sendMessage(embedBuilder.build()).queue(mes ->
                    new Reactionary<>(s, mes, embedBuilder));
        } else {
            e.sendMessage(embedBuilder.build()).queue();
        }
    }
}
