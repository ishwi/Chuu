package core.commands.stats;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.ui.UserCommandMarker;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.imagerenderer.ProfileMaker;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.Optionals;
import core.services.UserInfoService;
import core.util.ServiceView;
import dao.entities.CommandStats;
import dao.entities.ProfileEntity;
import dao.entities.UserInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class ProfileInfoCommand extends ConcurrentCommand<ChuuDataParams> implements UserCommandMarker {

    public ProfileInfoCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        OnlyUsernameParser parser = new OnlyUsernameParser(db, Optionals.LIST.opt);
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
    public void onCommand(Context e, @Nonnull ChuuDataParams params) throws LastFmException {

        String lastFmName = params.getLastFMData().getName();
        UserInfo userInfo;
        long albumCount;

        long discordId = params.getLastFMData().getDiscordId();

        var topArtist = db.getAllUserArtist(discordId, 1).stream().findFirst().orElse(null);
        var topAlbum = db.getUserAlbums(lastFmName, 1).stream().findFirst().orElse(null);
        if (topAlbum != null && StringUtils.isBlank(topAlbum.getAlbum())) {
            String s = CommandUtil.albumUrl(db, lastFM, topAlbum.getArtistId(), topAlbum.getArtist(), topAlbum.getAlbum());
            topAlbum.setUrl(s);
        }
        CommandStats commandStats = db.getCommandStats(discordId);
        userInfo = new UserInfoService(db).maybeRefresh(params.getLastFMData());
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

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(entity.lastmId() + "'s profile", PrivacyUtils.getLastFmUser(entity.lastmId()), entity.imageUrl())
                .setDescription(sb)
                .setFooter("Account created on " + entity.date());

        e.sendMessage(embedBuilder.build()).queue();
    }

    @Override
    public String getName() {
        return "Profile";
    }

}
