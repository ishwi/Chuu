package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class ColorExplanation implements Explanation {
    public static final String NAME = "colour";

    @Override
    public Interactible explanation() {
        return new ExplanationLineType(NAME, "Input one or multiple by color name, by hex code (starting with # or 0x) or any other valid HTML color constructor like rgb(0,0,0)", OptionType.STRING);
    }
}
