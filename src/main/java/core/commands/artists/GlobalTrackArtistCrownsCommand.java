package core.commands.artists;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistParser;
import core.parsers.NumberParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScrobbledArtist;
import dao.entities.TrackPlays;
import dao.entities.UniqueWrapper;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class GlobalTrackArtistCrownsCommand extends ConcurrentCommand<NumberParameters<ArtistParameters>> {
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;

    public GlobalTrackArtistCrownsCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotifyApi = SpotifySingleton.getInstance();
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public Parser<NumberParameters<ArtistParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce a number to vary the number of plays to award a crown, " +
                "defaults to whatever the guild has configured (0 if not configured)";
        NumberParser<ArtistParameters, ArtistParser> artistParametersArtistParserNumberParser = new NumberParser<>(new ArtistParser(getService(), lastFM),
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true);
        artistParametersArtistParserNumberParser.addOptional(new OptionalEntity("nobotted", "discard users that have been manually flagged as potentially botted accounts"));
        artistParametersArtistParserNumberParser.addOptional(new OptionalEntity("botted", "show botted accounts in case you have the config show-botted disabled"));

        return artistParametersArtistParserNumberParser;
    }

    public String getTitle(ScrobbledArtist scrobbledArtist) {
        return scrobbledArtist.getArtist() + "'s global track ";
    }


    public UniqueWrapper<TrackPlays> getList(NumberParameters<ArtistParameters> params) {
        Long threshold = params.getExtraParam();
        if (threshold == null) {
            if (params.getE().isFromGuild()) {
                long idLong = params.getE().getGuild().getIdLong();
                threshold = (long) getService().getGuildCrownThreshold(idLong);
            } else {
                threshold = 0L;
            }
        }

        return getService().getArtistGlobalTrackCrowns(params.getInnerParams().getLastFMData().getName(), params.getInnerParams().getScrobbledArtist().getArtistId(), Math.toIntExact(threshold), CommandUtil.showBottedAccounts(null, params, getService()));
    }

    @Override
    public String getDescription() {
        return ("List of artist you are the top listener within a server");
    }

    @Override
    public List<String> getAliases() {
        return List.of("globaltrackcrownsartist", "gtca", "gcta");
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<ArtistParameters> params) throws LastFmException {


        ArtistParameters innerParams = params.getInnerParams();
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(innerParams.getArtist(), 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotifyApi);
        innerParams.setScrobbledArtist(scrobbledArtist);
        UniqueWrapper<TrackPlays> uniqueDataUniqueWrapper = getList(params);
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, innerParams.getLastFMData().getDiscordId());
        String userName = userInformation.getUsername();
        String userUrl = userInformation.getUrlImage();
        List<TrackPlays> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();

        int rows = resultWrapper.size();
        if (rows == 0) {
            sendMessageQueue(e, userName + " doesn't have any " + getTitle(scrobbledArtist) + "crown :'(");
            return;
        }

        StringBuilder a = new StringBuilder();
        List<String> strings = resultWrapper.stream().map(x -> ". **[" +
                LinkUtils.cleanMarkdownCharacter(x.getTrack()) +
                "](" + LinkUtils.getLastFMArtistTrack(scrobbledArtist.getArtist(), x.getTrack()) +
                ")** - " + x.getCount() +
                " plays\n").collect(Collectors.toList());
        for (int i = 0; i < 10 && i < rows; i++) {
            a.append(i + 1).append(strings.get(i));
        }


        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setDescription(a)
                .setColor(CommandUtil.randomColor())
                .setAuthor(String.format("%s's %scrowns", userName, getTitle(scrobbledArtist)), CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.getLastFmId()), userInformation.getUrlImage())
                .setThumbnail(scrobbledArtist.getUrl())
                .setFooter(String.format("%s has %d %scrowns!!%n", CommandUtil.markdownLessUserString(userName, uniqueDataUniqueWrapper.getDiscordId(), e), resultWrapper.size(), getTitle(scrobbledArtist)), null);
        e.getChannel().sendMessage(new MessageBuilder()
                .setEmbed(embedBuilder.build()).build()).queue(message1 ->

                new Reactionary<>(strings, message1, embedBuilder));
    }

    @Override
    public String getName() {
        return "Artist track crowns";
    }
}
