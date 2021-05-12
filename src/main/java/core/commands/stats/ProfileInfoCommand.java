package core.commands.stats;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.imagerenderer.ProfileMaker;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.services.ColorService;
import core.services.UserInfoService;
import dao.ChuuService;
import dao.entities.CommandStats;
import dao.entities.ProfileEntity;
import dao.entities.UserInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

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
    protected void onCommand(Context e, @NotNull ChuuDataParams params) throws LastFmException {

        String lastFmName = params.getLastFMData().getName();
        UserInfo userInfo;
        long albumCount;


        long guildId = e.getGuild().getIdLong();
        Long discordId = params.getLastFMData().getDiscordId();

        var topArtist = db.getAllUserArtist(discordId, 1).stream().findFirst().orElse(null);
        var topAlbum = db.getUserAlbums(lastFmName, 1).stream().findFirst().orElse(null);
        if (topAlbum != null && StringUtils.isBlank(topAlbum.getAlbum())) {
            String s = CommandUtil.albumUrl(db, lastFM, topAlbum.getArtistId(), topAlbum.getArtist(), topAlbum.getAlbum());
            topAlbum.setUrl(s);
        }
        int guildCrownThreshold = db.getGuildCrownThreshold(guildId);
        CommandStats commandStats = db.getCommandStats(discordId);
        userInfo = new UserInfoService(db).getUserInfo(params.getLastFMData());
        albumCount = db.getUserAlbumCount(discordId);
        int totalArtist = db.getUserArtistCount(lastFmName, 0);
        int randomCount = db.randomCount(discordId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String date = LocalDateTime.ofEpochSecond(userInfo.getUnixtimestamp(), 0, ZoneOffset.UTC)
                .format(formatter);
        String lastFmId = Chuu.getLastFmId(lastFmName);
        if (lastFmId.equals(Chuu.DEFAULT_LASTFM_ID)) {
            lastFmId = getUserString(e, discordId);
        }

        ProfileEntity entity = new ProfileEntity(lastFmId, "", userInfo.getImage(), userInfo.getPlayCount(),
                Math.toIntExact(albumCount), totalArtist, randomCount, date, commandStats, topArtist, topAlbum);

        switch (CommandUtil.getEffectiveMode(params.getLastFMData().getRemainingImagesMode(), params)) {
            case IMAGE -> doImage(e, entity);
            case LIST, PIE -> list(e, entity);
        }
    }

    private void doImage(Context e, ProfileEntity entity) {
        sendImage(ProfileMaker.makeProfile(entity), e);
    }

    private void list(Context e, ProfileEntity entity) {

        StringBuilder sb = new StringBuilder();
        sb.append("# of scrobbles: ").append(entity.scrobbles()).append("\n")
                .append("# of albums: ").append(entity.albums()).append("\n")
                .append("# of artists: ").append(entity.artist()).append("\n")
                .append("# of commands executed: ").append(entity.commandStats().commandCount()).append("\n")
                .append("# of images submitted: ").append(entity.commandStats().imageCount()).append("\n")
                .append("# of random url submitted: ").append(entity.randomCount()).append("\n");

        if (entity.topArtist() != null) {
            sb.append("Favourite artist: ").append(entity.topArtist().getArtist()).append("\n");
        }
        if (entity.topAlbum() != null) {
            sb.append("Favourite album: ").append(entity.topAlbum().getAlbum()).append("\n");
        }

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder()
                .setAuthor(entity.lastmId() + "'s profile", PrivacyUtils.getLastFmUser(entity.lastmId()), entity.imageUrl())
                .setColor(ColorService.computeColor(e))
                .setDescription(sb)
                .setFooter("Account created on " + entity.date());

        e.sendMessage(embedBuilder.build()).queue();
    }

    @Override
    public String getName() {
        return "Profile";
    }

}
