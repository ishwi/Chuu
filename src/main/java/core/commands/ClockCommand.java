package core.commands;

import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.services.BillboardHoarder;
import core.services.ClockService;
import dao.ChuuService;
import dao.entities.PreBillboardUserDataTimestamped;
import dao.entities.Role;
import dao.entities.UsersWrapper;
import dao.entities.Week;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class ClockCommand extends ConcurrentCommand<ChuuDataParams> {

    public ClockCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> getParser() {
        return new OnlyUsernameParser(getService(), new OptionalEntity("week", "show of the last week"));
    }

    @Override
    public String getDescription() {
        return "Your ";
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        ChuuDataParams parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        Long discordId = parse.getLastFMData().getDiscordId();
        Week currentWeekId = getService().getCurrentWeekId();
        TimeZone userTimezone = getService().getUserTimezone(discordId);
        BillboardHoarder billboardHoarder = new BillboardHoarder(Collections.singletonList(new UsersWrapper(discordId, parse.getLastFMData().getName(), Role.ADMIN, userTimezone)), getService(), currentWeekId, lastFM);
        billboardHoarder.hoardUsers();
        List<PreBillboardUserDataTimestamped> ungroupedUserData = getService().getUngroupedUserData(currentWeekId.getId(), parse.getLastFMData().getName());
        if (ungroupedUserData.isEmpty()) {
            sendMessageQueue(e, "Couldn't get any data from you in the previous week");
            return;
        }
        ClockService clockService;
        if (parse.hasOptional("week")) {
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
