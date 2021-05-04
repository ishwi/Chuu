package core.parsers.explanation.util;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.StringUtils;

public record ExplanationLineType(String header, String usage, OptionType type) implements Interactible {

    @Override
    public OptionData optionData() {
        return new OptionData(type, header.replaceAll("\\s", "-").replaceAll("\\.", "-"), StringUtils.abbreviate(usage, 100));
    }
}
