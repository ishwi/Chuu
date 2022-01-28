package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.NowPlayingParameters;
import core.services.NPService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

import java.util.Collections;
import java.util.List;

public class NpParser extends DaoParser<NowPlayingParameters> {
    private final ConcurrentLastFM lastFM;

    public NpParser(ChuuService dao, ConcurrentLastFM lastFM) {
        super(dao);
        this.lastFM = lastFM;
    }

    @Override
    public NowPlayingParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        CommandInteraction e = ctx.e();
        User user = InteractionAux.parseUser(e);

        LastFMData data = findLastfmFromID(user, ctx);
        NPService npService = new NPService(lastFM, data);
        NPService.NPUpdate nowPlayingBoth = npService.getNowPlayingBoth();
        NowPlayingArtist nowPlayingArtist = nowPlayingBoth.np();
        return new NowPlayingParameters(ctx, data, nowPlayingArtist, nowPlayingBoth.data());
    }

    public NowPlayingParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException, LastFmException {
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
