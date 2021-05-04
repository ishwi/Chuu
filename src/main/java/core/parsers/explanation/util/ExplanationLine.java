package core.parsers.explanation.util;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public record ExplanationLine(String header, String usage, OptionData optionData) implements Interactible {

}
