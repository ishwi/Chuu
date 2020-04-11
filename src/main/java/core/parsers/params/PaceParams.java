package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PaceParams extends NumberParameters<NumberParameters<NaturalTimeParams>> {

    public PaceParams(MessageReceivedEvent e, CountableNaturalTimeParams innerParams, Long extraParam) {
        super(e, innerParams, extraParam);
    }
}
