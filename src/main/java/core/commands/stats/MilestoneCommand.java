package core.commands.stats;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.TrackWithArtistId;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
    public MilestoneCommand(ChuuService dao) {
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
        String s = "You can also introduce a number to vary the number of plays to award a crown, " +
                "defaults to whatever the guild has configured (0 if not configured)";
        return new NumberParser<>(new OnlyUsernameParser(getService()),
                1L,
                Integer.MAX_VALUE,
                map, s, false, true, false);

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
    protected void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<ChuuDataParams> params) throws LastFmException, InstanceNotFoundException {
        Long extraParam = params.getExtraParam();
        LastFMData lastFMData = params.getInnerParams().getLastFMData();
        Optional<TrackWithArtistId> milestoneOpt = lastFM.getMilestone(lastFMData.getName(), extraParam);
        milestoneOpt.ifPresentOrElse(tr -> buildEmbed(e, extraParam, lastFMData, tr), () -> sendMessageQueue(e, "There was a problem getting your milestone"));
    }

    private void buildEmbed(MessageReceivedEvent e, Long extraParam, LastFMData lastFMData, TrackWithArtistId milestone) {
        DiscordUserDisplay uinfo = CommandUtil.getUserInfoNotStripped(e, lastFMData.getDiscordId());

        OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(milestone.getUtc()), lastFMData.getTimeZone().toZoneId());

        String day = offsetDateTime.toLocalDate().format(DateTimeFormatter.ISO_DATE);
        String date = CommandUtil.getAmericanizedDate(offsetDateTime);
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(CommandUtil.randomColor())
                .setAuthor(String.format("%s's #%d scrobble was:", uinfo.getUsername(), extraParam), String.format("%s/library?from=%s&rangetype=1day", PrivacyUtils.getLastFmUser(lastFMData.getName()), day), uinfo.getUrlImage())
                .setTitle(milestone.getName(), LinkUtils.getLastFMArtistTrack(milestone.getArtist(), milestone.getName()))
                .setThumbnail(milestone.getImageUrl() == null || milestone.getImageUrl().isBlank() ? null : milestone.getImageUrl())
                .setDescription("**" + milestone.getArtist() + "** | " + milestone.getAlbum())
                .setFooter("Obtained at " + date);
        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }


}
