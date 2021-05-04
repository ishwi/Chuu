package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class UrlExplanation implements Explanation {


    @Override
    public Interactible explanation() {
        return new ExplanationLineType("url",
                "A url to an image. It can be a plain image or you can upload directly to discord using the command", OptionType.STRING);
    }
}
