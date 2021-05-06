package core.parsers.explanation.util;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public record ExplanationLine(String header, String usage, List<OptionData> options) implements Interactible {

    public ExplanationLine(String header, String usage, OptionData optionData) {
        this(header, usage, List.of(optionData));
    }
}
