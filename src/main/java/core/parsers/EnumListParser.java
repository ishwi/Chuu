package core.parsers;

import core.exceptions.LastFmException;
import core.parsers.params.EnumListParameters;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnumListParser<T extends Enum<T>> extends Parser<EnumListParameters<T>> {
    protected final Class<T> clazz;
    private final String name;
    private final EnumSet<T> excluded;
    private final Function<String, EnumSet<T>> mapper;


    public EnumListParser(String name, Class<T> tClass, EnumSet<T> excluded, Function<String, EnumSet<T>> mapper) {
        this.name = name;
        this.clazz = tClass;
        this.excluded = excluded;
        this.mapper = mapper;
    }

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected EnumListParameters<T> parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        EnumSet<T> building = EnumSet.noneOf(clazz);

        if (words.length == 0) {
            return new EnumListParameters<>(e, building, false, true);
        }

        if (words[0].equalsIgnoreCase("help")) {

            if (words.length > 1) {
                if (words[1].equals("all")) {
                    building = EnumSet.complementOf(excluded);
                } else {
                    String remaining = String.join(" ", Arrays.copyOfRange(words, 1, words.length));
                    building = mapper.apply(remaining);
                }
            }
            return new EnumListParameters<>(e, building, true, false);
        } else if (words[0].equalsIgnoreCase("list")) {
            return new EnumListParameters<>(e, building, false, true);
        } else {
            String remaining = String.join(" ", Arrays.copyOfRange(words, 0, words.length));
            building = mapper.apply(remaining);
            if (building.isEmpty()) {
                return new EnumListParameters<>(e, building, true, true);
            }
            return new EnumListParameters<>(e, building, false, false);
        }
    }

    @Override
    public String getUsageLogic(String commandName) {
        EnumSet<T> set = EnumSet.complementOf(excluded);
        List<String> collect = set.stream().map(x -> WordUtils.capitalizeFully(x.name().replaceAll("_", "-"), '-')).collect(Collectors.toList());
        String join = String.join("** | **", collect);
        return "**" + commandName + "** **[help|help all|list|]** **" + name + "**\n" +
                "\t Writing **__help__** will give you a brief description of all the " + name + " that you include in the command or alternatively all the options with **__help__**\n" +
                "\t Writing **__list__** will give you all your current set " + name + "\n" +
                "\t " + name + " being any combination of: **" + join + " **";
    }
}
