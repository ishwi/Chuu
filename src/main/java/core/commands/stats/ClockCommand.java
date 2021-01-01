package core.commands.stats;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.services.BillboardHoarder;
import core.services.ClockService;
import dao.ChuuService;
import dao.entities.PreBillboardUserDataTimestamped;
import dao.entities.UpdaterUserWrapper;
import dao.entities.Week;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class ClockCommand extends ConcurrentCommand<ChuuDataParams> {

    public ClockCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(getService(), new OptionalEntity("week", "show of the last week"));
    }

    @Override
    public String getDescription() {
        return "Displays your listening habits with a 24 hour format. You can also use the tz command for it to be accurate for yourself.";
    }

    @Override
    public List<String> getAliases() {
        return List.of("clock");
    }

    @Override
    public String getName() {
        return "Clock";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {


        Long discordId = params.getLastFMData().getDiscordId();
        Week currentWeekId = getService().getCurrentWeekId();
        TimeZone userTimezone = getService().getUserTimezone(discordId);
        UpdaterUserWrapper userUpdateStatus = getService().getUserUpdateStatus(discordId);

        BillboardHoarder billboardHoarder = new BillboardHoarder(Collections.singletonList(userUpdateStatus), getService(), currentWeekId, lastFM);
        billboardHoarder.hoardUsers();
        List<PreBillboardUserDataTimestamped> ungroupedUserData = getService().getUngroupedUserData(currentWeekId.getId(), params.getLastFMData().getName());
        if (ungroupedUserData.isEmpty()) {
            sendMessageQueue(e, "Couldn't get any data from you in the previous week");
            return;
        }
        ClockService clockService;
        if (params.hasOptional("week")) {
            clockService = new ClockService(ClockService.ClockMode.BY_WEEK, ungroupedUserData, userTimezone);
        } else {
            clockService = new ClockService(ClockService.ClockMode.BY_DAY, ungroupedUserData, userTimezone);
        }
        byte[] bytes = clockService.clockDoer();
        if (bytes == null) {
            parser.sendError("Unknown error happened while creating the clock", e);
            return;
        }
        if (bytes.length < e.getGuild().getMaxFileSize()) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setImage("attachment://cat.gif").setDescription("");
            e.getChannel().sendFile(bytes, "cat.gif").embed(embed.build()).queue();
        } else
            e.getChannel().sendMessage("File was too big").queue();
    }
}
