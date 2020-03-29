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

public class ChartParameters {
    private final String username;
    private final long discordId;
    private final TimeFrameEnum timeFrameEnum;
    private final int x;
    private final int y;
    private final net.dv8tion.jda.api.events.message.MessageReceivedEvent e;
    private final boolean writeTitles;
    private final boolean writePlays;
    private final boolean isList;

    public ChartParameters(String[] returned, MessageReceivedEvent e) {

        int x = Integer.parseInt(returned[0]);
        int y = Integer.parseInt(returned[1]);
        long discordId = Long.parseLong(returned[2]);
        String username = returned[3];
        String time = returned[4];
        boolean titleWrite = !Boolean.parseBoolean(returned[5]);
        boolean playsWrite = Boolean.parseBoolean(returned[6]);
        boolean listFormat = Boolean.parseBoolean(returned[7]);
        this.username = username;
        this.discordId = discordId;
        this.timeFrameEnum = TimeFrameEnum.fromCompletePeriod(time);
        this.x = x;
        this.y = y;
        this.e = e;
        this.writeTitles = titleWrite;
        this.writePlays = playsWrite;
        this.isList = listFormat;
    }

    public ChartParameters(String username, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, MessageReceivedEvent e, boolean writeTitles, boolean writePlays, boolean isList) {
        this.username = username;
        this.discordId = discordId;
        this.timeFrameEnum = timeFrameEnum;
        this.x = x;
        this.y = y;
        this.e = e;
        this.writeTitles = writeTitles;
        this.writePlays = writePlays;
        this.isList = isList;
    }

    public static ChartParameters toListParams() {
        return new ChartParameters(null, 0, null, 0, 0, null, true, true, true);
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

    public MessageReceivedEvent getE() {
        return e;
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

    public DiscordUserDisplay calculateUser() {
        return CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId);
    }

    public EmbedBuilder initEmbed(String titleInit, EmbedBuilder embedBuilder, String footerText) {
        DiscordUserDisplay discordUserDisplay = this.calculateUser();
        return embedBuilder.setTitle(discordUserDisplay.getUsername() + titleInit + this.getTimeFrameEnum().getDisplayString())
                .setFooter(CommandUtil.markdownLessString(discordUserDisplay.getUsername()) + footerText + this.getTimeFrameEnum().getDisplayString());
    }

}
