package core.parsers;

import core.parsers.params.EnumParameters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class EnumParser<T extends Enum<T>> extends Parser<EnumParameters<T>> {
    protected final Class<T> clazz;

    public EnumParser(Class<T> tClass) {
        this.clazz = tClass;
    }

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected EnumParameters<T> parseLogic(MessageReceivedEvent e, String[] words) {
        EnumSet<T> ts = EnumSet.allOf(clazz);
        List<String> lines = ts.stream().map(x -> x.name().replaceAll("_", "-").toLowerCase()).toList();
        if (words.length != 1) {
            sendError("Pls introduce only one of the following: **" + String.join("**, **", lines) + "**", e);
            return null;
        }

        Optional<String> first = lines.stream().filter(x -> words[0].equalsIgnoreCase(x)).findFirst();
        if (first.isEmpty()) {
            sendError("Pls introduce one of the following: " + String.join(",", lines), e);
            return null;
        }
        return new EnumParameters<>(e, Enum.valueOf(clazz, first.get().toUpperCase().replaceAll("-", "_")));


    }

    @Override
    public String getUsageLogic(String commandName) {
        List<String> lines = EnumSet.allOf(clazz).stream().map(x -> x.name().replaceAll("_", "-").toLowerCase()).toList();

        return "**" + commandName + " *config_value*** \n" +
                "\tConfig value being one of: **" + String.join("**, **", lines) + "**";
    }
}

