package core.parsers;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.StringParameters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class GreedyStringParser extends Parser<StringParameters> {
    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected StringParameters parseLogic(MessageReceivedEvent e, String[] words) {
        return new StringParameters(e, String.join(" ", words));
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(() -> new ExplanationLine("Phrase", null));
    }

}
