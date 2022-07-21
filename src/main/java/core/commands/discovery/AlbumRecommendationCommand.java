package core.commands.discovery;

import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.stats.AffinityCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.Parser;
import core.parsers.RecommendationParser;
import core.parsers.params.RecommendationsParams;
import core.parsers.utils.CustomTimeFrame;
import core.util.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class AlbumRecommendationCommand extends ConcurrentCommand<RecommendationsParams> {
    private final MusicBrainzService mb;
    private final Spotify spotify;

    public AlbumRecommendationCommand(ServiceView dao) {
        super(dao);
        this.mb = MusicBrainzServiceSingleton.getInstance();
        spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<RecommendationsParams> initParser() {
        return new RecommendationParser(db, 1);
    }

    @Override
    public String getDescription() {
        return "Album Recommendations based on affinity and top listened genres";
    }

    @Override
    public List<String> getAliases() {
        return List.of("albumrecommendation", "arec", "albrec", "albumrec");
    }

    @Override
    public String getName() {
        return "Album Recommendation";
    }

    @Override
    public void onCommand(Context e, @Nonnull RecommendationsParams params) throws LastFmException, InstanceNotFoundException {

        long firstDiscordID;
        long secondDiscordID;
        LastFMData firstLastFMId;
        if (params.isNoUser()) {
            LastFMData lastFMData = db.findLastFMData(e.getAuthor().getIdLong());
            firstLastFMId = lastFMData;
            List<Affinity> serverAffinity = db.getServerAffinity(lastFMData.getName(), e.getGuild().getIdLong(), AffinityCommand.DEFAULT_THRESHOLD);
            if (serverAffinity.isEmpty()) {
                sendMessageQueue(e, "Couldn't get you any recommendation :(");
                return;

            }
            TreeMap<Float, Affinity> integerAffinityTreeMap = new TreeMap<>();
            float counter = 1;
            for (Affinity affinity : serverAffinity) {
                integerAffinityTreeMap.put(counter, affinity);
                counter += affinity.getAffinity() + 0.001f;
            }
            int numberOfTries = 2;
            Map.Entry<Float, Affinity> floatAffinityEntry = null;
            while (numberOfTries-- != 0 && floatAffinityEntry == null) {
                double v = CommandUtil.rand.nextDouble();
                floatAffinityEntry = integerAffinityTreeMap.floorEntry((float) (v * counter));
            }

            if (floatAffinityEntry == null) {
                sendMessageQueue(e, "Couldn't get you any recommendation :(");
                return;
            }
            Affinity affinity = floatAffinityEntry.getValue();

            firstDiscordID = e.getAuthor().getIdLong();
            secondDiscordID = affinity.getDiscordId();
        } else {
            firstDiscordID = params.getFirstUser().getDiscordId();
            secondDiscordID = params.getSecondUser().getDiscordId();
            firstLastFMId = params.getFirstUser();

        }

        List<ScrobbledArtist> recs = db.getRecommendation(secondDiscordID, firstDiscordID, params.isShowRepeated(), Integer.MAX_VALUE);

        List<AlbumInfo> albumInfos = lastFM.getTopAlbums(firstLastFMId, new CustomTimeFrame(TimeFrameEnum.ALL), 500).stream().filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                .toList();

        Map<Genre, Integer> map = mb.genreCount(albumInfos).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().size()))
                .entrySet().stream()
                .sorted(Comparator.comparingInt((ToIntFunction<Map.Entry<Genre, Integer>>) Map.Entry::getValue).reversed())
                .limit(15).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<AlbumGenre> albumRecs = mb.getAlbumRecommendationsByGenre(map, recs);
        Map<String, Long> m = recs.stream().collect(Collectors.groupingBy(ScrobbledArtist::getArtist, Collectors.summingLong(ScrobbledArtist::getCount)));
        albumRecs = albumRecs.stream().sorted(Comparator.comparingLong((AlbumGenre x) -> map.get(new Genre(x.genre())) * m.get(x.artist())).reversed()).toList();

        String receiver = "you";
        if (firstDiscordID != e.getAuthor().getIdLong()) {
            receiver = getUserString(e, firstDiscordID);
        }
        DiscordUserDisplay giverUI = CommandUtil.getUserInfoEscaped(e, secondDiscordID);
        String giver = giverUI.username();

        if (albumRecs.isEmpty()) {
            sendMessageQueue(e, String.format("Couldn't get %s any album recommendation from %s", receiver, giver));
            return;
        }
        if (params.getRecCount() == 1 || recs.size() == 1) {
            String appendable = "";
            String albumLink = spotify.getAlbumLink(albumRecs.get(0).artist(), albumRecs.get(0).album());
            if (albumLink != null)
                appendable += "\n" + albumLink;
            sendMessageQueue(e, String.format("**%s** has recommended %s to listen to **%s - %s**%s",
                    giver, receiver, albumRecs.get(0).artist(), albumRecs.get(0).album(), appendable));
            return;
        }

        List<String> stringedList = albumRecs.stream().map((t) ->
                {
                    String link;
                    String albumLink = spotify.getAlbumLink(t.artist(), t.album());
                    link = Objects.requireNonNullElseGet(albumLink, () -> LinkUtils.getLastFmArtistAlbumUrl(t.artist(), t.album()));
                    return String.format(". **[%s - %s](%s)**\n", CommandUtil.escapeMarkdown(t.artist()), t.album(), link);
                }).limit(params.getRecCount())
                .toList();


        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);
        embedBuilder.setTitle(String.format("%s album recommendations for %s", giver, receiver))
                .setThumbnail(giverUI.urlImage());
        new PaginatorBuilder<>(e, embedBuilder, stringedList).pageSize(10).build().queue();
    }
}
