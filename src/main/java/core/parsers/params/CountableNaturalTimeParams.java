package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CountableNaturalTimeParams extends NumberParameters<NaturalTimeParams> {

    public CountableNaturalTimeParams(MessageReceivedEvent e, NaturalTimeParams timeParams, long count) {
        super(e, timeParams, count);
    }
}
