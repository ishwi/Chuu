package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.NaturalTimeframeExplanation;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.NaturalTimeParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NaturalTimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;

public class NaturalTimeFrameParser extends DaoParser<NaturalTimeParams> {
    private final NaturalTimeFrameEnum defaultTFE;

    public NaturalTimeFrameParser(ChuuService dao, NaturalTimeFrameEnum defaultTFE) {
        super(dao);
        this.defaultTFE = defaultTFE;
    }

    @Override
    public NaturalTimeParams parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        User user = InteractionAux.parseUser(e);
        NaturalTimeFrameEnum ntfe = InteractionAux.parseNaturalTimeFrame(e, defaultTFE);

        return new NaturalTimeParams(ctx, findLastfmFromID(user, ctx), ntfe);
    }

    public NaturalTimeParams parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {

        String[] message = getSubMessage(e);
        NaturalTimeFrameEnum timeFrame = defaultTFE;

        ChartParserAux auxiliar = new ChartParserAux(message);
        timeFrame = auxiliar.parseNaturalTimeFrame(timeFrame);
        message = auxiliar.getMessage();
        LastFMData data = atTheEndOneUser(e, message);

        return new NaturalTimeParams(e, data, timeFrame);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new NaturalTimeframeExplanation(defaultTFE), new PermissiveUserExplanation());
    }

}
