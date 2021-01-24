package core.commands.stats;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.ProfileMaker;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
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
        OnlyUsernameParser parser = new OnlyUsernameParser(db, new OptionalEntity("list", "display in list format"));
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
    protected void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) throws LastFmException {

        String lastFmName = params.getLastFMData().getName();
        UserInfo userInfo;
        long albumCount;


        long guildId = e.getGuild().getIdLong();
        int guildCrownThreshold = db.getGuildCrownThreshold(guildId);
        CompletableFuture<UniqueWrapper<ArtistPlays>> completablecrowns = CompletableFuture.supplyAsync(() -> db.getCrowns(lastFmName, guildId, guildCrownThreshold));
        CompletableFuture<UniqueWrapper<ArtistPlays>> completableUnique = CompletableFuture.supplyAsync(() -> db.getUniqueArtist(guildId, lastFmName));
        CommandStats commandStats = db.getCommandStats(params.getLastFMData().getDiscordId());
        userInfo = lastFM.getUserInfo(Collections.singletonList(params.getLastFMData().getName()), params.getLastFMData()).get(0);
        albumCount = db.getUserAlbumCount(params.getLastFMData().getDiscordId());
        int totalArtist = db.getUserArtistCount(lastFmName, 0);
        int randomCount = db.randomCount(params.getLastFMData().getDiscordId());

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
            case IMAGE -> doImage(e, params.getLastFMData().getDiscordId(), lastFmName, userInfo, albumCount, crowns, unique, totalUnique, totalCrowns, totalArtist, crownRepresentative, uniqueRepresentative, date, commandStats, randomCount);
            case LIST, PIE -> list(e, lastFmName, userInfo, albumCount, unique, totalUnique, totalCrowns, totalArtist, crownRepresentative, uniqueRepresentative, date, commandStats, randomCount);
        }
    }

    private void doImage(MessageReceivedEvent e, long discordId, String lastFmName, UserInfo userInfo, long albumCount, UniqueWrapper<ArtistPlays> crowns, UniqueWrapper<ArtistPlays> unique, int totalUnique, int totalCrowns, int totalArtist, String crownRepresentative, String uniqueRepresentative, String date, CommandStats commandStats, int randomCount) throws LastFmException {

        String crownImage = !crowns.getUniqueData().isEmpty() ?
                CommandUtil
                        .getArtistImageUrl(db, crownRepresentative, lastFM, discogsApi, spotify)
                : null;

        String uniqueImage = !unique.getUniqueData().isEmpty() ? CommandUtil
                .getArtistImageUrl(db, uniqueRepresentative, lastFM, discogsApi, spotify) : null;

        String lastFmId = Chuu.getLastFmId(lastFmName);
        if (lastFmId.equals(Chuu.DEFAULT_LASTFM_ID)) {
            lastFmId = getUserString(e, discordId);
        }
        ProfileEntity entity = new ProfileEntity(lastFmId, "", crownRepresentative, uniqueRepresentative, uniqueImage, crownImage, userInfo
                .getImage(), "", userInfo
                .getPlayCount(), Math.toIntExact(albumCount), totalArtist, totalCrowns, totalUnique, randomCount, date, commandStats);
        sendImage(ProfileMaker.makeProfile(entity), e);
    }

    private void list(MessageReceivedEvent e, String lastFmName, UserInfo userInfo, long albumCount, UniqueWrapper<ArtistPlays> unique, int totalUnique, int totalCrowns, int totalArtist, String crownRepresentative, String uniqueRepresentative, String date, CommandStats commandStats, int randomurlCount) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("# of scrobbles: ").append(userInfo.getPlayCount()).append("\n")
                .append("# of albums: ").append(albumCount).append("\n")
                .append("# of artists: ").append(totalArtist).append("\n")
                .append("# of crowns: ").append(totalCrowns).append("\n")
                .append("Top crown: ").append(CommandUtil.cleanMarkdownCharacter(crownRepresentative)).append("\n")
                .append("Number of unique artist: ").append(totalUnique).append("\n")
                .append("Top unique: ").append(CommandUtil.cleanMarkdownCharacter(uniqueRepresentative)).append("\n")
                .append("# of commands executed: ").append(commandStats.commandCount()).append("\n")
                .append("# of images submitted: ").append(commandStats.imageCount()).append("\n")
                .append("# of random url submitted ").append(randomurlCount).append("\n");

        String name = getUserString(e, unique.getDiscordId());

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(name + "'s profile", CommandUtil.getLastFmUser(lastFmName))
                .setColor(CommandUtil.randomColor())
                .setThumbnail(userInfo.getImage().isEmpty() ? null : userInfo.getImage())
                .setDescription(stringBuilder)
                .setFooter("Account created on " + date);

        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }

    @Override
    public String getName() {
        return "Profile";
    }

}
