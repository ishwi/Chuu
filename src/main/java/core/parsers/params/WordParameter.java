package core.parsers.params;

import core.commands.Context;

public class WordParameter extends CommandParameters {
    private final String word;

    public WordParameter(Context e, String word) {
        super(e);
        this.word = word;
    }

    public String getWord() {
        return word;
    }
}
