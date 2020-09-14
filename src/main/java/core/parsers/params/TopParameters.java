package core.parsers.params;

import dao.entities.ChartMode;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TopParameters extends ChartParameters {

    public TopParameters(MessageReceivedEvent e, String lastfmID, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, ChartMode chartMode, LastFMData lastFMData) {
        super(e, lastfmID, discordId, chartMode, lastFMData, timeFrameEnum, x, y);

    }


    @Override
    public boolean isWritePlays() {
        return !hasOptional("notitles");
    }

    public boolean isDoAlbum() {
        return hasOptional("album");
    }
}
