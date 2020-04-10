package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ArtistTimeFrameParser;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;
import dao.entities.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class FavesFromArtistCommand extends ConcurrentCommand {

    private final DiscogsApi discogs;
    private final Spotify spotify;

    public FavesFromArtistCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = true;
        this.parser = new ArtistTimeFrameParser(dao, lastFM);
        this.discogs = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
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
        String[] returned;
        returned = parser.parse(e);
        if (returned == null)
            return;
        long userId = Long.parseLong(returned[1]);
        String timeframew = returned[2];
        String artist = returned[0];
        ScrobbledArtist who = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(getService(), who, lastFM, discogs, spotify);
        List<Track> ai;
        String lastFmName;
        lastFmName = getService().findLastFMData(userId).getName();

        ai = lastFM.getTopArtistTracks(lastFmName, who.getArtist(), timeframew);

        final String userString = getUserString(e, userId, lastFmName);
        if (ai.isEmpty()) {
            sendMessageQueue(e, ("Coudnt't find your fav tracks of " + CommandUtil.cleanMarkdownCharacter(who.getArtist()) + (timeframew
                    .equals("overall") ? "" : " in the last " + TimeFrameEnum
                    .fromCompletePeriod(timeframew).toString().toLowerCase() + "!")));
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
                .setTitle(userString + "'s Top " + who.getArtist() + " Tracks in " + TimeFrameEnum
                        .fromCompletePeriod(timeframew).toString(), CommandUtil.getLastFmUser(lastFmName));
        embedBuilder.setThumbnail(CommandUtil.noImageUrl(who.getUrl()));

        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue();
    }
}
