package core.parsers.params;

import core.parsers.utils.CustomTimeFrame;
import dao.entities.ChartMode;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartableGenreParameters extends ChartParameters {
    private final GenreParameters genreParameters;

    public ChartableGenreParameters(MessageReceivedEvent e, LastFMData lastFMData, CustomTimeFrame timeFrameEnum, int x, int y, ChartMode chartMode, GenreParameters genreParameters) {
        super(e, lastFMData, timeFrameEnum, x, y);
        this.genreParameters = genreParameters;
    }

    public ChartableGenreParameters(MessageReceivedEvent e, LastFMData user, CustomTimeFrame timeFrameEnum, int x, int y, ChartMode chartMode, boolean writeTitles, boolean writePlays, boolean isList, GenreParameters genreParameters) {
        super(e, user, timeFrameEnum, x, y, writeTitles, writePlays, isList);
        this.genreParameters = genreParameters;
    }


    public GenreParameters getGenreParameters() {
        return genreParameters;
    }
}
