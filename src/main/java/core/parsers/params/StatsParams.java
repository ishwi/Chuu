package core.parsers.params;


import core.commands.Context;
import core.parsers.StatsParser;
import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;

import java.util.Set;

public class StatsParams extends CommandParameters {

    private final Set<StatsParser.StatsParam> enums;
    private final boolean isHelp;
    private final LastFMData user;
    private final CustomTimeFrame customTimeFrame;
    private final Integer globalParam;
    private final NowPlayingArtist np;
    private final boolean noMatch;

    public StatsParams(Context e, Set<StatsParser.StatsParam> enums, boolean isHelp, LastFMData user, CustomTimeFrame customTimeFrame, Integer globalParam, boolean noMatch) {
        this(e, enums, isHelp, user, customTimeFrame, globalParam, noMatch, null);
    }

    public StatsParams(Context e, Set<StatsParser.StatsParam> enums, boolean isHelp, boolean noMatch, LastFMData lastFMData) {
        super(e);
        this.enums = enums;
        this.user = lastFMData;
        this.customTimeFrame = null;
        this.globalParam = null;
        this.np = null;
        this.isHelp = isHelp;
        this.noMatch = noMatch;
    }

    public StatsParams(Context e, Set<StatsParser.StatsParam> enums, boolean isHelp, LastFMData user, CustomTimeFrame customTimeFrame, Integer globalParam, boolean noMatch, NowPlayingArtist np) {
        super(e);
        this.enums = enums;
        this.isHelp = isHelp;
        this.user = user;
        this.customTimeFrame = customTimeFrame;
        this.globalParam = globalParam;
        this.noMatch = noMatch;
        this.np = np;
    }

    public StatsParams(Context e, Set<StatsParser.StatsParam> building, LastFMData data, CustomTimeFrame ofTimeFrameEnum) {
        super(e);
        this.enums = building;
        this.isHelp = false;
        this.user = data;
        this.customTimeFrame = ofTimeFrameEnum;
        this.globalParam = null;
        this.noMatch = false;
        this.np = null;
    }

    public Set<StatsParser.StatsParam> getEnums() {
        return enums;
    }

    public boolean isHelp() {
        return isHelp;
    }


    public LastFMData getUser() {
        return user;
    }

    public CustomTimeFrame getCustomTimeFrame() {
        return customTimeFrame;
    }

    public Integer getGlobalParam() {
        return globalParam;
    }

    public boolean isNoMatch() {
        return noMatch;
    }

    public NowPlayingArtist getNp() {
        return np;
    }
}
