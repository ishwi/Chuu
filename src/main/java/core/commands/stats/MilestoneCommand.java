package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.TrackWithArtistId;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class MilestoneCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {
    public MilestoneCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "The specific milestone you want to lookup";
        var parser = new NumberParser<>(new OnlyUsernameParser(db),
                1L,
                Integer.MAX_VALUE,
                map, s, false, true, false, "milestone");
        parser.setReverseOrder(true);
        return parser;

    }

    @Override
    public String getDescription() {
        return "Which track was your #x scrobble?";
    }

    @Override
    public List<String> getAliases() {
        return List.of("milestone", "mls", "ms");
    }

    @Override
    public String getName() {
        return "Milestone";
    }

    @Override
    protected void onCommand(Context e, @NotNull NumberParameters<ChuuDataParams> params) throws LastFmException {
        Long extraParam = params.getExtraParam();
        LastFMData lastFMData = params.getInnerParams().getLastFMData();
        Optional<TrackWithArtistId> milestoneOpt = lastFM.getMilestone(lastFMData, extraParam);
        milestoneOpt.ifPresentOrElse(tr -> buildEmbed(e, extraParam, lastFMData, tr), () -> sendMessageQueue(e, "You probably don't have %d scrobbles".formatted(extraParam)));
    }

    private void buildEmbed(Context e, Long extraParam, LastFMData lastFMData, TrackWithArtistId milestone) {
        DiscordUserDisplay uinfo = CommandUtil.getUserInfoUnescaped(e, lastFMData.getDiscordId());
        Instant instant = Instant.ofEpochSecond(milestone.getUtc());
        OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, lastFMData.getTimeZone().toZoneId());

        String day = offsetDateTime.toLocalDate().format(DateTimeFormatter.ISO_DATE);
        String date = CommandUtil.getAmericanizedDate(offsetDateTime);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s's #%d scrobble was:", uinfo.getUsername(), extraParam), String.format("%s/library?from=%s&rangetype=1day", PrivacyUtils.getLastFmUser(lastFMData.getName()), day), uinfo.getUrlImage())
                .setTitle(milestone.getName(), LinkUtils.getLastFMArtistTrack(milestone.getArtist(), milestone.getName()))
                .setThumbnail(milestone.getImageUrl() == null || milestone.getImageUrl().isBlank() ? null : milestone.getImageUrl())
                .setDescription("**" + milestone.getArtist() + "** | " + milestone.getAlbum())
                .setFooter("Obtained at " + date);
        e.sendMessage(embedBuilder.build()).queue();
    }


}
