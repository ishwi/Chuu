package core.parsers.params;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.TopEntity;
import core.commands.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.OptionalEntity;
import dao.entities.DiscordUserDisplay;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;
import java.util.function.BiFunction;

public class ChartParameters extends CommandParameters {
    private final String lastfmID;
    private final long discordId;
    private final TimeFrameEnum timeFrameEnum;
    private final int x;
    private final int y;


    public ChartParameters(MessageReceivedEvent e, String lastfmID, long discordId, TimeFrameEnum timeFrameEnum, int x, int y) {
        super(e);
        this.lastfmID = lastfmID;
        this.discordId = discordId;
        this.timeFrameEnum = timeFrameEnum;
        this.x = x;
        this.y = y;
    }

    public ChartParameters(MessageReceivedEvent e, String lastfmID, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList, boolean pieFormat) {
        super(e);
        this.lastfmID = lastfmID;
        this.discordId = discordId;
        this.timeFrameEnum = timeFrameEnum;
        this.x = x;
        this.y = y;
        this.optionals.put(new OptionalEntity("--notitles", ""), !writeTitles);
        this.optionals.put(new OptionalEntity("--plays", ""), writePlays);
        this.optionals.put(new OptionalEntity("--list", ""), isList);
        this.optionals.put(new OptionalEntity("--pie", ""), isPieFormat());

    }


    public static ChartParameters toListParams() {
        return new ChartParameters(null, "", -1L, null, 0, 0, true
                , true, true, false);
    }

    public int makeCommand(ConcurrentLastFM lastFM, BlockingQueue<UrlCapsule> queue, TopEntity topEntity, BiFunction<JSONObject, Integer, UrlCapsule> parser) throws LastFmException {
        return lastFM.getChart(lastfmID, timeFrameEnum.toApiFormat(), x, y, topEntity, parser, queue);
    }


    public String getLastfmID() {
        return lastfmID;
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
        return !hasOptional("--notitles");
    }


    public boolean isWritePlays() {
        return hasOptional("--plays");
    }

    public boolean isList() {
        return hasOptional("--list");
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
        return hasOptional("--pie");
    }
}
