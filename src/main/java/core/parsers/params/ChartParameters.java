package core.parsers.params;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.OptionalEntity;
import core.parsers.utils.CustomTimeFrame;
import dao.entities.ChartMode;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;
import java.util.function.BiFunction;

public class ChartParameters extends CommandParameters {
    private final LastFMData user;
    private final long discordId;
    private final ChartMode chartMode;
    private final LastFMData lastFMData;
    private final CustomTimeFrame timeFrameEnum;
    private int x;
    private int y;


    public ChartParameters(MessageReceivedEvent e, LastFMData user, long discordId, ChartMode chartMode, LastFMData lastFMData, CustomTimeFrame timeFrameEnum, int x, int y) {
        super(e);
        this.user = user;
        this.discordId = discordId;
        this.chartMode = chartMode;
        this.lastFMData = lastFMData;
        this.timeFrameEnum = timeFrameEnum;
        this.x = x;
        this.y = y;
    }

    public ChartParameters(MessageReceivedEvent e, LastFMData user, long discordId, CustomTimeFrame timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList, ChartMode chartMode, LastFMData lastFMData) {
        super(e);
        this.user = user;
        this.discordId = discordId;
        this.timeFrameEnum = timeFrameEnum;
        this.x = x;
        this.y = y;
        this.chartMode = chartMode;
        this.lastFMData = lastFMData;
        this.optionals.put(new OptionalEntity("notitles", ""), !writeTitles);
        this.optionals.put(new OptionalEntity("plays", ""), writePlays);
        this.optionals.put(new OptionalEntity("list", ""), isList);
        this.optionals.put(new OptionalEntity("pie", ""), isPieFormat());
        this.optionals.put(new OptionalEntity("aside", ""), isAside());


    }


    public static ChartParameters toListParams() {
        return new ChartParameters(null, null, -1L, null, 0, 0, true
                , true, true, ChartMode.LIST, null);
    }

    public int makeCommand(ConcurrentLastFM lastFM, BlockingQueue<UrlCapsule> queue, TopEntity topEntity, BiFunction<JSONObject, Integer, UrlCapsule> parser) throws LastFmException {
        return lastFM.getChart(user, timeFrameEnum, x, y, topEntity, parser, queue);
    }


    public LastFMData getUser() {
        return user;
    }

    public CustomTimeFrame getTimeFrameEnum() {
        return timeFrameEnum;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isWriteTitles() {
        return !hasOptional("notitles");
    }


    public boolean isWritePlays() {
        return hasOptional("plays");
    }

    public boolean isList() {
        return hasOptional("list");
    }

    public boolean isAside() {
        return hasOptional("aside");
    }

    public long getDiscordId() {
        return discordId;
    }


    public EmbedBuilder initEmbed(String titleInit, EmbedBuilder embedBuilder, String footerText, String lastfmid) {
        DiscordUserDisplay discordUserDisplay = CommandUtil.getUserInfoNotStripped(getE(), discordId);
        return embedBuilder.setAuthor(discordUserDisplay.getUsername() + titleInit + this.getTimeFrameEnum().getDisplayString(), CommandUtil.getLastFmUser(lastfmid), discordUserDisplay.getUrlImage())
                .setFooter(CommandUtil.markdownLessString(discordUserDisplay.getUsername()) + footerText + this.getTimeFrameEnum().getDisplayString()).setColor(CommandUtil.randomColor(getE()));
    }


    public boolean isPieFormat() {
        return hasOptional("pie");
    }

    public ChartMode chartMode() {
        return chartMode;
    }

    public LastFMData getLastFMData() {
        return lastFMData;
    }
}
