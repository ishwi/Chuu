package ish.services.dtos;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

public record CommandInfo(String name, String category, List<String> aliases, List<Parameters> parameters) {

    record Parameters(String header, String usage, List<CustomOption> options) {
    }

    record CustomOption(OptionType type, String name, String description, boolean required,
                        List<CustomChoice> choices) {

    }

    record CustomChoice(String name, String asDouble, long asLong, String asString) {
    }

}
