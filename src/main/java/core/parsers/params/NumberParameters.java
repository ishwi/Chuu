package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NumberParameters<T extends CommandParameters> extends ExtraParameters<T, Long> {

    public NumberParameters(MessageReceivedEvent e, T innerParams, Long extraParam) {
        super(e, innerParams, extraParam);
    }

}
