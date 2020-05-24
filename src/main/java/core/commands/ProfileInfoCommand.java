package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.ProfileMaker;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> getParser() {
        return new OnlyUsernameParser(getService(), new OptionalEntity("--image", "display in list format"));
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
        boolean isList = !params.hasOptional("--image");
        UserInfo userInfo;
        int albumCount;

        userInfo = lastFM.getUserInfo(Collections.singletonList(lastFmName)).get(0);
        albumCount = lastFM.getTotalAlbumCount(lastFmName);

        long guildId = e.getGuild().getIdLong();
        int guildCrownThreshold = getService().getGuildCrownThreshold(guildId);
        UniqueWrapper<ArtistPlays> crowns = getService().getCrowns(lastFmName, guildId, guildCrownThreshold);
        UniqueWrapper<ArtistPlays> unique = getService().getUniqueArtist(guildId, lastFmName);

        int totalUnique = unique.getRows();
        int totalCrowns = crowns.getRows();
        int totalArtist = getService().getUserArtistCount(lastFmName, 0);
        String crownRepresentative = !crowns.getUniqueData().isEmpty() ? crowns.getUniqueData().get(0)
                .getArtistName() : "no crowns";
        String uniqueRepresentative = !unique.getUniqueData().isEmpty() ? unique.getUniqueData().get(0)
                .getArtistName() : "no unique artists";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String date = LocalDateTime.ofEpochSecond(userInfo.getUnixtimestamp(), 0, ZoneOffset.UTC)
                .format(formatter);
        if (isList) {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Total number of scrobbles: ").append(userInfo.getPlayCount()).append("\n")
                    .append("Total number of albums: ").append(albumCount).append("\n")
                    .append("Total number of artists: ").append(totalArtist).append("\n")
                    .append("Total number of crowns: ").append(totalCrowns).append("\n")
                    .append("Top crown: ").append(CommandUtil.cleanMarkdownCharacter(crownRepresentative)).append("\n")
                    .append("Total number of unique artist: ").append(totalUnique).append("\n")
                    .append("Top unique: ").append(CommandUtil.cleanMarkdownCharacter(uniqueRepresentative)).append("\n");

            String name = getUserString(e, unique.getDiscordId(), lastFmName);

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(name + "'s profile", CommandUtil.getLastFmUser(lastFmName))
                    .setColor(CommandUtil.randomColor())
                    .setThumbnail(userInfo.getImage().isEmpty() ? null : userInfo.getImage())
                    .setDescription(stringBuilder)
                    .setFooter("Account created on " + date);

            MessageBuilder mes = new MessageBuilder();
            mes.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();

        } else {
            ObscuritySummary summary = getService().getObscuritySummary(lastFmName);

            String crownImage = !crowns.getUniqueData().isEmpty() ?
                    CommandUtil
                            .getArtistImageUrl(getService(), crownRepresentative, lastFM, discogsApi, spotify)
                    : null;

            String uniqueImage = !unique.getUniqueData().isEmpty() ? CommandUtil
                    .getArtistImageUrl(getService(), uniqueRepresentative, lastFM, discogsApi, spotify) : null;

            ProfileEntity entity = new ProfileEntity(lastFmName, "", crownRepresentative, uniqueRepresentative, uniqueImage, crownImage, userInfo
                    .getImage(), "", userInfo
                    .getPlayCount(), albumCount, totalArtist, totalCrowns, totalUnique, summary.getTotal(), date);
            sendImage(ProfileMaker.makeProfile(entity), e);
        }
    }

    @Override
    public String getName() {
        return "Profile";
    }

}
