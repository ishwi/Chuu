package core.parsers.params;

import core.parsers.OptionalEntity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

public class CommandParameters {
    final Map<OptionalEntity, Boolean> optionals = new HashMap<>();
    private final MessageReceivedEvent e;

    public CommandParameters(String[] message, MessageReceivedEvent e, OptionalParameter... opts) {
        this.e = e;
        handleOptParameters(message, opts);
    }


    void handleOptParameters(String[] message, OptionalParameter... opts) {
        iter(message, opts);
    }


    private void iter(String[] message, OptionalParameter[] opts) {
        for (OptionalParameter opt : opts) {
            if (opt.getExpectedPosition() <= message.length - 1) {
                String s = message[opt.getExpectedPosition()];
                optionals.put(opt.getOpt(), Boolean.parseBoolean(s));
            } else {
                optionals.put(opt.getOpt(), false);
            }
        }
    }


    public MessageReceivedEvent getE() {
        return e;
    }

    public boolean hasOptional(OptionalEntity optionalEntity) {
        return optionals.getOrDefault(optionalEntity, false);
    }

    public boolean hasOptional(String optional) {

        return optionals.getOrDefault(new OptionalEntity(optional, null), false);
    }


}
