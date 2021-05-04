package core.parsers.explanation.util;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface Interactible {
    OptionData optionData();

    String header();

    String usage();

}
