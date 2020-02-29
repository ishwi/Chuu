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

public class ProfileInfoCommand extends ConcurrentCommand {
    private final Spotify spotify;
    private final DiscogsApi discogsApi;

    public ProfileInfoCommand(ChuuService dao) {
        super(dao);
        this.parser = new OnlyUsernameParser(dao, new OptionalEntity("--image", "display in list format"));
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
        this.respondInPrivate = false;
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
        String[] returned = parser.parse(e);
        String lastFmName = returned[0];
        //long discordID = Long.parseLong(returned[1]);
        boolean isList = !Boolean.parseBoolean(returned[2]);
        UserInfo userInfo;
        int albumCount;

        userInfo = lastFM.getUserInfo(Collections.singletonList(lastFmName)).get(0);
        albumCount = lastFM.getTotalAlbumCount(lastFmName);

        UniqueWrapper<ArtistPlays> crowns = getService().getCrowns(lastFmName, e.getGuild().getIdLong());
        UniqueWrapper<ArtistPlays> unique = getService().getUniqueArtist(e.getGuild().getIdLong(), lastFmName);

        int totalUnique = unique.getRows();
        int totalCrowns = crowns.getRows();
        int totalArtist = getService().getUserArtistCount(lastFmName);
        String crownRepresentative = !crowns.getUniqueData().isEmpty() ? crowns.getUniqueData().get(0)
                .getArtistName() : "no crowns";
        String UniqueRepresentative = !unique.getUniqueData().isEmpty() ? unique.getUniqueData().get(0)
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
                    .append("Top crown:").append(crownRepresentative).append("\n")
                    .append("Total number of unique artist: ").append(totalUnique).append("\n")
                    .append("Top unique:").append(UniqueRepresentative).append("\n");

            String name = getUserString(unique.getDiscordId(), e, lastFmName);

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
                    .getArtistImageUrl(getService(), UniqueRepresentative, lastFM, discogsApi, spotify) : null;

            ProfileEntity entity = new ProfileEntity(lastFmName, "", crownRepresentative, UniqueRepresentative, uniqueImage, crownImage, userInfo
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
