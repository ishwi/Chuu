package core.parsers.params;

import core.parsers.params.CommandParameters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EnumParameters<T> extends CommandParameters {
    private final T element;

    public EnumParameters(MessageReceivedEvent e, T element) {
        super(e);
        this.element = element;
    }


    public T getElement() {
        return element;
    }
}
