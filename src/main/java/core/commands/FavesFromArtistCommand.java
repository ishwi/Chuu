package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.ArtistTimeFrameParser;
import core.parsers.Parser;
import core.parsers.params.ArtistTimeFrameParameters;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;
import dao.entities.Track;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
        return new ArtistTimeFrameParser(getService(), lastFM);
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
    public void onCommand(MessageReceivedEvent e, @NotNull ArtistTimeFrameParameters params) throws LastFmException, InstanceNotFoundException {

        long userId = params.getLastFMData().getDiscordId();
        TimeFrameEnum timeframew = params.getTimeFrame();
        String artist = params.getArtist();
        ScrobbledArtist who = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(getService(), who, lastFM, discogs, spotify);
        List<Track> ai;
        String lastFmName = params.getLastFMData().getName();

        ai = lastFM.getTopArtistTracks(lastFmName, who.getArtist(), timeframew.toApiFormat(), artist);

        final String userString = getUserString(e, userId, lastFmName);
        if (ai.isEmpty()) {
            sendMessageQueue(e, ("Couldn't find your fav tracks in your top 5k songs (or you don't have any track with more than 3 plays) of " + CommandUtil.cleanMarkdownCharacter(who.getArtist()) + timeframew.getDisplayString() + "!"));
            return;
        }

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 10 && i < ai.size(); i++) {
            Track g = ai.get(i);
            s.append(i + 1).append(". **").append(CommandUtil.cleanMarkdownCharacter(g.getName())).append("** - ").append(g.getPlays()).append(" plays")
                    .append("\n");
        }
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setDescription(s)
                .setColor(CommandUtil.randomColor())
                .setTitle(String.format("%s's top %s tracks%s", userString, who.getArtist(), timeframew.getDisplayString()), CommandUtil.getLastFmUser(lastFmName))
                .setThumbnail(CommandUtil.noImageUrl(who.getUrl()));

        e.getChannel().sendMessage(new MessageBuilder().setEmbed(embedBuilder.build()).build()).queue();
    }
}
