package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ArtistTimeFrameParser;
import core.parsers.Parser;
import core.parsers.params.ArtistTimeFrameParameters;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;
import dao.entities.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
    public Parser<ArtistTimeFrameParameters> getParser() {
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
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ArtistTimeFrameParameters returned = parser.parse(e);
        if (returned == null)
            return;
        long userId = returned.getUser().getIdLong();
        TimeFrameEnum timeframew = returned.getTimeFrame();
        String artist = returned.getArtist();
        ScrobbledArtist who = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(getService(), who, lastFM, discogs, spotify);
        List<Track> ai;
        String lastFmName;
        lastFmName = getService().findLastFMData(userId).getName();

        ai = lastFM.getTopArtistTracks(lastFmName, who.getArtist(), timeframew.toApiFormat());

        final String userString = getUserString(e, userId, lastFmName);
        if (ai.isEmpty()) {
            sendMessageQueue(e, ("Couldn't find your fav tracks of " + CommandUtil.cleanMarkdownCharacter(who.getArtist()) + timeframew.getDisplayString() + "!"));
            return;
        }

        MessageBuilder mes = new MessageBuilder();
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < 10 && i < ai.size(); i++) {
            Track g = ai.get(i);
            s.append(i + 1).append(". **").append(CommandUtil.cleanMarkdownCharacter(g.getName())).append("** - ").append(g.getPlays()).append(" plays")
                    .append("\n");
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(s);
        embedBuilder.setColor(CommandUtil.randomColor());

        embedBuilder
                .setTitle(String.format("%s's Top %s Tracks %s", userString, who.getArtist(), timeframew.getDisplayString()), CommandUtil.getLastFmUser(lastFmName))
                .setThumbnail(CommandUtil.noImageUrl(who.getUrl()));

        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue();
    }
}
