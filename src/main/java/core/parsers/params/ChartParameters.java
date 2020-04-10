package core.parsers.params;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.TopEntity;
import core.commands.CommandUtil;
import core.exceptions.LastFmException;
import dao.entities.DiscordUserDisplay;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;
import java.util.function.BiFunction;

public class ChartParameters extends CommandParameters {
    private final String username;
    private final long discordId;
    private final TimeFrameEnum timeFrameEnum;
    private final int x;
    private final int y;
    private final boolean writeTitles;
    private final boolean writePlays;
    private final boolean isList;


    private final boolean pieFormat;

    public ChartParameters(String[] message, MessageReceivedEvent e, OptionalParameter... opts) {
        this(message, e);
        handleOptParameters(message, opts);
    }

    public ChartParameters(String[] returned, MessageReceivedEvent e) {
        super(returned, e, new OptionalParameter("--notitles", 5),
                new OptionalParameter("--plays", 6),
                new OptionalParameter("--list", 7),
                new OptionalParameter("--pie", 8));
        int tempX = Integer.parseInt(returned[0]);
        int tempY = Integer.parseInt(returned[1]);
        long tempDiscordId = Long.parseLong(returned[2]);
        String tempUsername = returned[3];
        String time = returned[4];
        this.username = tempUsername;
        this.discordId = tempDiscordId;
        this.timeFrameEnum = TimeFrameEnum.fromCompletePeriod(time);
        this.x = tempX;
        this.y = tempY;
        this.writeTitles = !this.hasOptional("--notitles");
        this.writePlays = this.hasOptional("--plays");
        this.isList = this.hasOptional("--list");
        this.pieFormat = this.hasOptional("--pie");
    }

    public ChartParameters(String[] message, String username, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, MessageReceivedEvent e) {
        this(message, username, discordId, timeFrameEnum, x, y, e, new OptionalParameter("--notiles", 5),
                new OptionalParameter("--plays", 6),
                new OptionalParameter("--list", 7),
                new OptionalParameter("--pie", 8));


    }


    public ChartParameters(String[] message, MessageReceivedEvent e, String username, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList, boolean pieFormat, OptionalParameter... opts) {
        super(message, e, opts);
        this.username = username;
        this.discordId = discordId;
        this.timeFrameEnum = timeFrameEnum;
        this.x = x;
        this.y = y;
        this.writeTitles = writeTitles;
        this.writePlays = writePlays;
        this.isList = isList;
        this.pieFormat = pieFormat;
    }

    public ChartParameters(String[] message, String username, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, MessageReceivedEvent e, OptionalParameter... opts) {
        super(message, e, opts);
        this.username = username;
        this.discordId = discordId;
        this.timeFrameEnum = timeFrameEnum;
        this.x = x;
        this.y = y;
        this.writeTitles = !this.hasOptional("--notitles");
        this.writePlays = this.hasOptional("--plays");
        this.isList = this.hasOptional("--list");
        this.pieFormat = this.hasOptional("--pie");
    }


    public static ChartParameters toListParams() {
        return new ChartParameters(null, null, "", -1L, null, 0, 0, true
                , true, true, false);
    }

    public int makeCommand(ConcurrentLastFM lastFM, BlockingQueue<UrlCapsule> queue, TopEntity topEntity, BiFunction<JSONObject, Integer, UrlCapsule> parser) throws LastFmException {
        return lastFM.getChart(username, timeFrameEnum.toApiFormat(), x, y, topEntity, parser, queue);
    }


    public String getUsername() {
        return username;
    }

    public TimeFrameEnum getTimeFrameEnum() {
        return timeFrameEnum;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    public boolean isWriteTitles() {
        return writeTitles;
    }

    public boolean isWritePlays() {
        return writePlays;
    }

    public boolean isList() {
        return isList;
    }

    public long getDiscordId() {
        return discordId;
    }


    public EmbedBuilder initEmbed(String titleInit, EmbedBuilder embedBuilder, String footerText) {
        DiscordUserDisplay discordUserDisplay = CommandUtil.getUserInfoConsideringGuildOrNot(getE(), discordId);
        return embedBuilder.setTitle(discordUserDisplay.getUsername() + titleInit + this.getTimeFrameEnum().getDisplayString())
                .setFooter(CommandUtil.markdownLessString(discordUserDisplay.getUsername()) + footerText + this.getTimeFrameEnum().getDisplayString());
    }


    public boolean isPieFormat() {
        return pieFormat;
    }
}
