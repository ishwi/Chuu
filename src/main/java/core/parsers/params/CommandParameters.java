package core.parsers.params;

import core.parsers.OptionalEntity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommandParameters {
    final Map<OptionalEntity, Boolean> optionals = new HashMap<>();
    private final MessageReceivedEvent e;

    public CommandParameters(MessageReceivedEvent e) {
        this.e = e;
    }


    public void initParams(Collection<String> optionals) {
        optionals.forEach(x -> this.optionals.put(new OptionalEntity(x, ""), true));
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
