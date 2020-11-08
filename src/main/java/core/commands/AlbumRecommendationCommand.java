package core.commands;

import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.Parser;
import core.parsers.RecommendationParser;
import core.parsers.params.RecommendationsParams;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class AlbumRecommendationCommand extends ConcurrentCommand<RecommendationsParams> {
    private final MusicBrainzService mb;
    private final Spotify spotify;

    public AlbumRecommendationCommand(ChuuService dao) {
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
        return new RecommendationParser(getService(), 1);
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
    void onCommand(MessageReceivedEvent e, @NotNull RecommendationsParams params) throws LastFmException, InstanceNotFoundException {

        long firstDiscordID;
        long secondDiscordID;
        String firstLastFMId;
        if (params.isNoUser()) {
            LastFMData lastFMData = getService().findLastFMData(e.getAuthor().getIdLong());
            firstLastFMId = lastFMData.getName();
            List<Affinity> serverAffinity = getService().getServerAffinity(firstLastFMId, e.getGuild().getIdLong(), AffinityCommand.DEFAULT_THRESHOLD);
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
            firstLastFMId = params.getFirstUser().getName();

        }

        List<ScrobbledArtist> recs = getService().getRecommendation(secondDiscordID, firstDiscordID, params.isShowRepeated(), Integer.MAX_VALUE);

        List<AlbumInfo> albumInfos = lastFM.getTopAlbums(firstLastFMId, TimeFrameEnum.ALL.toApiFormat(), 500).stream().filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                .collect(Collectors.toList());

        Map<Genre, Integer> map = mb.genreCount(albumInfos).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().size()))
                .entrySet().stream()
                .sorted(Comparator.comparingInt((ToIntFunction<Map.Entry<Genre, Integer>>) Map.Entry::getValue).reversed())
                .limit(15).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<AlbumGenre> albumRecs = mb.getAlbumRecommendationsByGenre(map, recs);
        Map<String, Long> m = recs.stream().collect(Collectors.groupingBy(ScrobbledArtist::getArtist, Collectors.summingLong(ScrobbledArtist::getCount)));
        albumRecs = albumRecs.stream().sorted(Comparator.comparingLong((AlbumGenre x) -> map.get(new Genre(x.getGenre(), null)) * m.get(x.getArtist())).reversed()).collect(Collectors.toList());

        String receiver = "you";
        if (firstDiscordID != e.getAuthor().getIdLong()) {
            receiver = getUserString(e, firstDiscordID);
        }
        DiscordUserDisplay giverUI = CommandUtil.getUserInfoConsideringGuildOrNot(e, secondDiscordID);
        String giver = giverUI.getUsername();

        if (albumRecs.isEmpty()) {
            sendMessageQueue(e, String.format("Couldn't get %s any album recommendation from %s", receiver, giver));
            return;
        }
        if (params.getRecCount() == 1 || recs.size() == 1) {
            String appendable = "";
            String albumLink = spotify.getAlbumLink(albumRecs.get(0).getArtist(), albumRecs.get(0).getAlbum());
            if (albumLink != null)
                appendable += "\n" + albumLink;
            sendMessageQueue(e, String.format("**%s** has recommended %s to listen to **%s - %s**%s",
                    giver, receiver, albumRecs.get(0).getArtist(), albumRecs.get(0).getAlbum(), appendable));
            return;
        }

        List<String> stringedList = albumRecs.stream().map((t) ->
        {
            String link;
            String albumLink = spotify.getAlbumLink(t.getArtist(), t.getAlbum());
            link = Objects.requireNonNullElseGet(albumLink, () -> LinkUtils.getLastFmArtistAlbumUrl(t.getArtist(), t.getAlbum()));
            return String.format(". **[%s - %s](%s)**\n", CommandUtil.cleanMarkdownCharacter(t.getArtist()), t.getAlbum(), link);
        }).limit(params.getRecCount())
                .collect(Collectors.toList());

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < stringedList.size(); i++) {
            a.append(i + 1).append(stringedList.get(i));
        }


        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(String.format("%s album recommendations for %s", giver, receiver))
                .setThumbnail(giverUI.getUrlImage())
                .setColor(CommandUtil.randomColor())
                .setDescription(a);
        MessageBuilder messageBuilder = new MessageBuilder();
        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build()).queue(mes ->
                new Reactionary<>(stringedList, mes, 10, embedBuilder));
    }
}
