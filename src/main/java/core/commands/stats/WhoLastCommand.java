package core.commands.stats;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
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
import dao.entities.Memoized;
import dao.entities.ScrobbledArtist;
import dao.entities.UserListened;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WhoLastCommand extends ConcurrentCommand<ArtistParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public WhoLastCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }

    public static void handleUserListened(MessageReceivedEvent e, ArtistParameters params, List<UserListened> firsts, boolean isFirst) {
        Function<UserListened, String> toMemoize = (userListened) -> {
            String whem;
            if (userListened.moment().isEmpty()) {
                whem = "**Never**";
            } else {
                OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(userListened.moment().get(), userListened.timeZone().toZoneId());
                whem = "**" + CommandUtil.getAmericanizedDate(offsetDateTime) + "**";
            }
            return ". [" + CommandUtil.getUserInfoConsideringGuildOrNot(e, userListened.discordId()).getUsername() + "](" + PrivacyUtils.getLastFmUser(userListened.lastfmId()) + "): " + whem + "\n";
        };

        List<Memoized<UserListened, String>> strings = firsts.stream().map(t -> new Memoized<>(t, toMemoize)).collect(Collectors.toList());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        StringBuilder builder = new StringBuilder();


        int counter = 1;

        for (var b : strings) {
            builder.append(counter++)
                    .append(b.toString());
            if (counter == 11)
                break;
        }
        String usable = CommandUtil.cleanMarkdownCharacter(e.getGuild().getName());
        embedBuilder.setTitle("Who listened " + (isFirst ? "first" : "last") + " to" + params.getScrobbledArtist().getArtist() + "in" + usable).
                setThumbnail(CommandUtil.noImageUrl(params.getScrobbledArtist().getUrl())).setDescription(builder)
                .setFooter(strings.size() + CommandUtil.singlePlural(strings.size(), " listener", " listeners"))
                .setColor(ColorService.computeColor(e));
        e.getChannel().sendMessage(embedBuilder.build())
                .queue(message1 ->
                        new Reactionary<>(strings, message1, embedBuilder));
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Who listened last to an artist on a server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("wholast", "wl", "wlast", "whol");
    }

    @Override
    public String getName() {
        return "Who listened last";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull ArtistParameters params) throws LastFmException {

        String artist = params.getArtist();

        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify);
        params.setScrobbledArtist(scrobbledArtist);
        List<UserListened> lasts = db.getServerLastScrobbledArtist(scrobbledArtist.getArtistId(), e.getGuild().getIdLong());
        if (lasts.isEmpty()) {
            sendMessageQueue(e, "Couldn't get the last time this server scrobbled **" + artist + "**");
            return;
        }

        handleUserListened(e, params, lasts, false);

    }

}
