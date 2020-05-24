package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Set;

public class ColorChartParams extends ChartParameters {
    private final Set<Color> colors;
    private int x;
    private int y;

    public ColorChartParams(MessageReceivedEvent e, String lastfmID, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, Set<Color> colors, boolean doAdditionalEmbed) {
        super(e, lastfmID, discordId, doAdditionalEmbed, timeFrameEnum, x, y);
        this.colors = colors;
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean isWritePlays() {
        return hasOptional("--plays");
    }

    @Override
    public boolean isWriteTitles() {
        return hasOptional("--titles");
    }

    public boolean isArtist() {
        return hasOptional("--artist");
    }

    public boolean isInverse() {
        return hasOptional("--inverse");
    }

    public boolean isColumn() {
        return hasOptional("--column");
    }

    public boolean isLinear() {
        return hasOptional("--linear");
    }

    public boolean isSorted() {
        return hasOptional("--ordered");
    }

    public boolean isStrict() {
        return hasOptional("--strict");
    }

    @Override
    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isColor() {
        return hasOptional("--color");
    }

    @Override
    public boolean isPieFormat() {
        return false;
    }

    @Override
    public boolean isList() {
        return false;
    }

    public Set<Color> getColors() {
        return colors;
    }
}
