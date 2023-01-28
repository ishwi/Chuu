package core.commands.crowns;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.params.NumberParameters;
import core.parsers.utils.Optionals;
import core.services.validators.ArtistValidator;
import core.util.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScrobbledArtist;
import dao.entities.TrackPlays;
import dao.entities.UniqueWrapper;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static core.parsers.NumberParser.generateThresholdParser;

public class GlobalTrackArtistCrownsCommand extends ConcurrentCommand<NumberParameters<ArtistParameters>> {

    public GlobalTrackArtistCrownsCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = false;
    }

    @Override
    public String slashName() {
        return "global-track-artist";
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public Parser<NumberParameters<ArtistParameters>> initParser() {
        Parser<NumberParameters<ArtistParameters>> parser = generateThresholdParser(new ArtistParser(db, lastFM));
        parser.addOptional(Optionals.NOBOTTED.opt);
        parser.addOptional(Optionals.BOTTED.opt);
        return parser;
    }

    public String getTitle(ScrobbledArtist scrobbledArtist) {
        return scrobbledArtist.getArtist() + "'s global track ";
    }


    public UniqueWrapper<TrackPlays> getList(NumberParameters<ArtistParameters> params) {
        Long threshold = params.getExtraParam();
        if (threshold == null) {
            if (params.getE().isFromGuild()) {
                long idLong = params.getE().getGuild().getIdLong();
                threshold = (long) db.getGuildCrownThreshold(idLong);
            } else {
                threshold = 0L;
            }
        }

        return db.getArtistGlobalTrackCrowns(params.getInnerParams().getLastFMData().getName(), params.getInnerParams().getScrobbledArtist().getArtistId(), Math.toIntExact(threshold), CommandUtil.showBottedAccounts(null, params, db));
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
    public void onCommand(Context e, @NotNull NumberParameters<ArtistParameters> params) throws LastFmException {


        ArtistParameters innerParams = params.getInnerParams();
        ScrobbledArtist scrobbledArtist = new ArtistValidator(db, lastFM, e)
                .validate(innerParams.getArtist(), innerParams.isNoredirect());

        innerParams.setScrobbledArtist(scrobbledArtist);
        UniqueWrapper<TrackPlays> uniqueDataUniqueWrapper = getList(params);
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoEscaped(e, innerParams.getLastFMData().getDiscordId());
        String userName = userInformation.username();
        List<TrackPlays> resultWrapper = uniqueDataUniqueWrapper.uniqueData();

        int rows = resultWrapper.size();
        if (rows == 0) {
            sendMessageQueue(e, userName + " doesn't have any " + getTitle(scrobbledArtist) + "crown :'(");
            return;
        }

        List<String> strings = resultWrapper.stream().map(x -> ". **[" +
                LinkUtils.cleanMarkdownCharacter(x.getTrack()) +
                "](" + LinkUtils.getLastFMArtistTrack(scrobbledArtist.getArtist(), x.getTrack()) +
                ")** - " + x.getCount() +
                " plays\n").toList();


        long discordId = params.getInnerParams().getLastFMData().getDiscordId();
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s's %scrowns", userName, getTitle(scrobbledArtist)), CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.lastFmId()), userInformation.urlImage())
                .setThumbnail(scrobbledArtist.getUrl())
                .setFooter(String.format("%s has %d %scrowns!!%n", CommandUtil.unescapedUser(userName, discordId, e), resultWrapper.size(), getTitle(scrobbledArtist)), null);

        new PaginatorBuilder<>(e, embedBuilder, strings).build().queue();
    }

    @Override
    public String getName() {
        return "Artist track crowns";
    }
}
