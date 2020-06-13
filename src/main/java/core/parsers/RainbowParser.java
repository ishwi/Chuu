package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.params.ChartParameters;
import core.parsers.params.RainbowParams;
import dao.ChuuService;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RainbowParser extends ChartableParser<RainbowParams> {
    private final ChartParser inner;

    public RainbowParser(ChuuService dao, TimeFrameEnum defaultTFE, OptionalEntity... opts) {
        super(dao, defaultTFE, opts);
        inner = new ChartParser(dao, defaultTFE);
    }

    @Override
    public void replaceOptional(String previousOptional, OptionalEntity optionalEntity) {
    }

    @Override
    void setUpOptionals() {
        this.opts.add(new OptionalEntity("--titles", "display titles"));
        this.opts.add(new OptionalEntity("--plays", "display play count"));
        this.opts.add(new OptionalEntity("--artist", "use artists instead of albums"));
        this.opts.add(new OptionalEntity("--linear", "display the rainbow line by line instead of stair"));
        this.opts.add(new OptionalEntity("--color", "sort by color instead of brightness"));
        this.opts.add(new OptionalEntity("--column", "display rainbow column by column instead of in stair"));
        this.opts.add(new OptionalEntity("--inverse", "show it black to white instead of white to black"));
    }

    @Override
    public RainbowParams parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        ChartParameters chartParameters = inner.parseLogic(e, subMessage);
        if (chartParameters == null) {
            return null;
        }
        return new RainbowParams(e, chartParameters.getLastfmID(), chartParameters.getDiscordId(), chartParameters.getTimeFrameEnum(), chartParameters.getX(), chartParameters.getY(), chartParameters.chartMode(), chartParameters.getLastFMData());
    }

    @Override
    public String getUsageLogic(String commandName) {
        return super.getUsageLogic(commandName) +
                "\tOriginal tool: http://thechurchofkoen.com/\n";
    }
}
