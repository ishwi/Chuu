package core.parsers.params;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.utils.CustomTimeFrame;
import core.parsers.utils.OptionalEntity;
import dao.entities.ChartMode;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiFunction;

public class ChartParameters extends CommandParameters {
    private final LastFMData user;
    private final CustomTimeFrame timeFrameEnum;
    private int x;
    private int y;


    public ChartParameters(Context e, @NotNull LastFMData lastFMData, CustomTimeFrame timeFrameEnum, int x, int y) {
        super(e);
        this.user = lastFMData;
        this.timeFrameEnum = timeFrameEnum;
        this.x = x;
        this.y = y;
    }

    public ChartParameters(Context e, @NotNull LastFMData user, CustomTimeFrame timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList) {
        super(e);
        this.user = user;
        this.timeFrameEnum = timeFrameEnum;
        this.x = x;
        this.y = y;
        this.optionals.put(new OptionalEntity("notitles", ""), !writeTitles);
        this.optionals.put(new OptionalEntity("plays", ""), writePlays);
        this.optionals.put(new OptionalEntity("list", ""), isList);
        this.optionals.put(new OptionalEntity("pie", ""), isPie());
        this.optionals.put(new OptionalEntity("aside", ""), isAside());


    }


    public static ChartParameters toListParams() {
        return new ChartParameters(null, LastFMData.ofDefault(), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL), 5, 5, true
                , true, true);
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
        ChartMode chartMode = chartMode();
        return ((chartMode == ChartMode.LIST && !hasOptional("pie") && !hasOptional("aside"))
                ||
                (chartMode != ChartMode.LIST && hasOptional("list")));
    }

    public boolean needCount() {
        return isList() || isPie() || chartMode() == ChartMode.IMAGE_INFO;
    }

    public boolean isPie() {
        ChartMode chartMode = chartMode();
        return ((chartMode == ChartMode.PIE) && !hasOptional("list") && !hasOptional("aside"))
               || ((chartMode != ChartMode.PIE) && hasOptional("pie"));
    }

    public boolean isAside() {
        ChartMode chartMode = chartMode();
        return !isList() && !isPie() && (hasOptional("aside") || chartMode == ChartMode.IMAGE_ASIDE || chartMode == ChartMode.IMAGE_ASIDE_INFO);
    }

    public long getDiscordId() {
        return getUser().getDiscordId();
    }


    public EmbedBuilder initEmbed(String titleInit, EmbedBuilder embedBuilder, String footerText, String lastfmid) {
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(getE(), getDiscordId());
        return embedBuilder.setAuthor(uInfo.getUsername() + titleInit + this.getTimeFrameEnum().getDisplayString(), CommandUtil.getLastFmUser(lastfmid), uInfo.getUrlImage())
                .setFooter(CommandUtil.stripEscapedMarkdown(uInfo.getUsername()) + footerText + this.getTimeFrameEnum().getDisplayString());
    }


    public boolean isBubble() {
        return hasOptional("bubble");
    }

    public ChartMode chartMode() {
        return getUser().getChartMode();
    }

}
