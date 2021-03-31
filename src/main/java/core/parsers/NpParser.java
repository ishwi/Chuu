package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.NowPlayingParameters;
import core.services.NPService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class NpParser extends DaoParser<NowPlayingParameters> {
    private final ConcurrentLastFM lastFM;

    public NpParser(ChuuService dao, ConcurrentLastFM lastFM) {
        super(dao);
        this.lastFM = lastFM;
    }

    public NowPlayingParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException, LastFmException {
        LastFMData data = atTheEndOneUser(e, subMessage);
        NPService npService = new NPService(lastFM, data);
        NPService.NPUpdate nowPlayingBoth = npService.getNowPlayingBoth();
        NowPlayingArtist nowPlayingArtist = nowPlayingBoth.np();
        return new NowPlayingParameters(e, data, nowPlayingArtist, nowPlayingBoth.data());
    }

    @Override
    public List<Explanation> getUsages() {
        return Collections.singletonList(new PermissiveUserExplanation());
    }

}
