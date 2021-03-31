package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CharacterParameters extends CommandParameters {
    private final char aChar;

    public CharacterParameters(MessageReceivedEvent e, char aChar) {
        super(e);
        this.aChar = aChar;
    }

    public char getaChar() {
        return aChar;
    }
}
