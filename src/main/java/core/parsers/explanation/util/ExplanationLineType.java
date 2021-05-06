package core.parsers.explanation.util;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public record ExplanationLineType(String header, String usage, OptionType type) implements Interactible {

    @Override
    public List<OptionData> options() {
        return List.of(new OptionData(type, header.replaceAll("\\s", "-").replaceAll("\\.", "-"), StringUtils.abbreviate(usage, 100)));
    }
}
