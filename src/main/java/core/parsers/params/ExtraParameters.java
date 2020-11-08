package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;

public class ExtraParameters<T extends CommandParameters, @NotNull J> extends CommandParameters {
    private final T innerParams;
    private final J extraParam;

    public ExtraParameters(MessageReceivedEvent e, T innerParams, J extraParam) {
        super(e);
        this.innerParams = innerParams;
        this.extraParam = extraParam;
    }

    public T getInnerParams() {
        return innerParams;
    }

    public J getExtraParam() {
        return extraParam;
    }
}
