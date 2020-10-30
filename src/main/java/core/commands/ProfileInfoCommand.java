package core.commands;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.imagerenderer.ProfileMaker;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.ProfileEntity;
import dao.entities.UniqueWrapper;
import dao.entities.UserInfo;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ProfileInfoCommand extends ConcurrentCommand<ChuuDataParams> {
    private final Spotify spotify;
    private final DiscogsApi discogsApi;

    public ProfileInfoCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        OnlyUsernameParser parser = new OnlyUsernameParser(getService(), new OptionalEntity("list", "display in list format"));
        parser.setExpensiveSearch(true);
        return parser;
    }

    @Override
    public String getDescription() {
        return "Brief description of an user profile";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("profile");
    }

    @Override
    protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ChuuDataParams params = parser.parse(e);
        String lastFmName = params.getLastFMData().getName();
        UserInfo userInfo;
        long albumCount;


        long guildId = e.getGuild().getIdLong();
        int guildCrownThreshold = getService().getGuildCrownThreshold(guildId);
        CompletableFuture<UniqueWrapper<ArtistPlays>> completablecrowns = CompletableFuture.supplyAsync(() -> getService().getCrowns(lastFmName, guildId, guildCrownThreshold));
        CompletableFuture<UniqueWrapper<ArtistPlays>> completableUnique = CompletableFuture.supplyAsync(() -> getService().getUniqueArtist(guildId, lastFmName));

        userInfo = lastFM.getUserInfo(Collections.singletonList(lastFmName)).get(0);
        albumCount = getService().getUserAlbumCount(params.getLastFMData().getDiscordId());
        int totalArtist = getService().getUserArtistCount(lastFmName, 0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String date = LocalDateTime.ofEpochSecond(userInfo.getUnixtimestamp(), 0, ZoneOffset.UTC)
                .format(formatter);

        UniqueWrapper<ArtistPlays> crowns;
        try {
            crowns = completablecrowns.get();
        } catch (InterruptedException | ExecutionException interruptedException) {
            interruptedException.printStackTrace();
            throw new ChuuServiceException(interruptedException);
        }
        int totalCrowns = crowns.getRows();
        String crownRepresentative = !crowns.getUniqueData().isEmpty() ? crowns.getUniqueData().get(0)
                .getArtistName() : "no crowns";

        UniqueWrapper<ArtistPlays> unique;
        try {
            unique = completableUnique.get();
        } catch (InterruptedException | ExecutionException interruptedException) {
            interruptedException.printStackTrace();
            throw new ChuuServiceException(interruptedException);
        }
        String uniqueRepresentative = !unique.getUniqueData().isEmpty() ? unique.getUniqueData().get(0)
                .getArtistName() : "no unique artists";
        int totalUnique = unique.getRows();


        switch (CommandUtil.getEffectiveMode(params.getLastFMData().getRemainingImagesMode(), params)) {
            case IMAGE:
                doImage(e, params.getLastFMData().getDiscordId(), lastFmName, userInfo, albumCount, crowns, unique, totalUnique, totalCrowns, totalArtist, crownRepresentative, uniqueRepresentative, date);
                break;
            case LIST:
            case PIE:
                list(e, lastFmName, userInfo, albumCount, unique, totalUnique, totalCrowns, totalArtist, crownRepresentative, uniqueRepresentative, date);
                break;
        }
    }

    private void doImage(MessageReceivedEvent e, long discordId, String lastFmName, UserInfo userInfo, long albumCount, UniqueWrapper<ArtistPlays> crowns, UniqueWrapper<ArtistPlays> unique, int totalUnique, int totalCrowns, int totalArtist, String crownRepresentative, String uniqueRepresentative, String date) throws LastFmException {
        int randomCount = getService().randomCount(discordId);

        String crownImage = !crowns.getUniqueData().isEmpty() ?
                CommandUtil
                        .getArtistImageUrl(getService(), crownRepresentative, lastFM, discogsApi, spotify)
                : null;

        String uniqueImage = !unique.getUniqueData().isEmpty() ? CommandUtil
                .getArtistImageUrl(getService(), uniqueRepresentative, lastFM, discogsApi, spotify) : null;

        String lastFmId = Chuu.getLastFmId(lastFmName);
        if (lastFmId.equals(Chuu.DEFAULT_LASTFM_ID)) {
            lastFmId = getUserString(e, discordId);
        }
        ProfileEntity entity = new ProfileEntity(lastFmId, "", crownRepresentative, uniqueRepresentative, uniqueImage, crownImage, userInfo
                .getImage(), "", userInfo
                .getPlayCount(), Math.toIntExact(albumCount), totalArtist, totalCrowns, totalUnique, randomCount, date);
        sendImage(ProfileMaker.makeProfile(entity), e);
    }

    private void list(MessageReceivedEvent e, String lastFmName, UserInfo userInfo, long albumCount, UniqueWrapper<ArtistPlays> unique, int totalUnique, int totalCrowns, int totalArtist, String crownRepresentative, String uniqueRepresentative, String date) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Total number of scrobbles: ").append(userInfo.getPlayCount()).append("\n")
                .append("Total number of albums: ").append(albumCount).append("\n")
                .append("Total number of artists: ").append(totalArtist).append("\n")
                .append("Total number of crowns: ").append(totalCrowns).append("\n")
                .append("Top crown: ").append(CommandUtil.cleanMarkdownCharacter(crownRepresentative)).append("\n")
                .append("Total number of unique artist: ").append(totalUnique).append("\n")
                .append("Top unique: ").append(CommandUtil.cleanMarkdownCharacter(uniqueRepresentative)).append("\n");

        String name = getUserString(e, unique.getDiscordId());

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(name + "'s profile", CommandUtil.getLastFmUser(lastFmName))
                .setColor(CommandUtil.randomColor())
                .setThumbnail(userInfo.getImage().isEmpty() ? null : userInfo.getImage())
                .setDescription(stringBuilder)
                .setFooter("Account created on " + date);

        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue();
    }

    @Override
    public String getName() {
        return "Profile";
    }

}
