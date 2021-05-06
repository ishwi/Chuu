package core.parsers.explanation.util;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public interface Interactible {
    List<OptionData> options();

    String header();

    String usage();

}
