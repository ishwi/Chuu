package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.TimeframeExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.TimeFrameParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;

public class TimerFrameParser extends DaoParser<TimeFrameParameters> {
    private final TimeFrameEnum defaultTFE;

    public TimerFrameParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao);
        this.defaultTFE = defaultTFE;
    }

    @Override
    public TimeFrameParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        TimeFrameEnum tfe = InteractionAux.parseTimeFrame(e, defaultTFE);
        User user = InteractionAux.parseUser(e);
        return new TimeFrameParameters(ctx, findLastfmFromID(user, ctx), tfe);
    }

    public TimeFrameParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {


        ChartParserAux auxiliar = new ChartParserAux(subMessage);
        TimeFrameEnum timeFrame = auxiliar.parseTimeframe(defaultTFE);
        subMessage = auxiliar.getMessage();
        LastFMData data = atTheEndOneUser(e, subMessage);
        return new TimeFrameParameters(e, data, timeFrame);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new TimeframeExplanation(defaultTFE), new PermissiveUserExplanation());
    }


}
