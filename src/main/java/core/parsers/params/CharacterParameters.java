package core.parsers.params;

import core.commands.Context;

public class CharacterParameters extends CommandParameters {
    private final char aChar;

    public CharacterParameters(Context e, char aChar) {
        super(e);
        this.aChar = aChar;
    }

    public char getaChar() {
        return aChar;
    }
}
