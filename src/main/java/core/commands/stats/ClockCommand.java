package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
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
import net.dv8tion.jda.api.entities.Message;

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
        return new OnlyUsernameParser(db, new OptionalEntity("week", "show of the last week"));
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
    protected void onCommand(Context e, @NotNull ChuuDataParams params) throws InstanceNotFoundException {


        Long discordId = params.getLastFMData().getDiscordId();
        Week currentWeekId = db.getCurrentWeekId();
        TimeZone userTimezone = db.getUserTimezone(discordId);
        UpdaterUserWrapper userUpdateStatus = db.getUserUpdateStatus(discordId);

        BillboardHoarder billboardHoarder = new BillboardHoarder(Collections.singletonList(userUpdateStatus), db, currentWeekId, lastFM);
        billboardHoarder.hoardUsers();
        List<PreBillboardUserDataTimestamped> ungroupedUserData = db.getUngroupedUserData(currentWeekId.getId(), params.getLastFMData().getName());
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
        long maxSize = e.isFromGuild() ? e.getGuild().getMaxFileSize() : Message.MAX_FILE_SIZE;

        if (bytes.length < maxSize) {
            EmbedBuilder embed = new ChuuEmbedBuilder(e);
            embed.setImage("attachment://cat.gif").setDescription("");
            e.doSendImage(bytes, ".gif", null);

        } else
            e.sendMessage("File was too big").queue();
    }
}
