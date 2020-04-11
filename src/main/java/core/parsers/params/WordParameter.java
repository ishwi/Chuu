package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WordParameter extends CommandParameters {
    private final String word;

    public WordParameter(MessageReceivedEvent e, String word) {
        super(e);
        this.word = word;
    }

    public String getWord() {
        return word;
    }
}
