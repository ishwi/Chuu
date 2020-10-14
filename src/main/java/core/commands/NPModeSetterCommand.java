package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.EnumListParser;
import core.parsers.Parser;
import core.parsers.params.EnumListParameters;
import core.parsers.params.NPMode;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NPModeSetterCommand extends ConcurrentCommand<EnumListParameters<NPMode>> {

    public static final Function<String, EnumSet<NPMode>> mapper = (String value) -> {
        String[] split = value.trim().replaceAll(" +", " ").split("[|,& ]+");
        EnumSet<NPMode> modes = EnumSet.noneOf(NPMode.class);
        for (String mode : split) {
            try {
                NPMode npMode = NPMode.valueOf(mode.replace("-", "_").toUpperCase());
                modes.add(npMode);
            } catch (IllegalArgumentException ignored) {
                //Ignore
            }
        }
        return modes;
    };

    public NPModeSetterCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<EnumListParameters<NPMode>> getParser() {
        return new EnumListParser<>("NP Modes", NPMode.class, EnumSet.of(NPMode.UNKNOWN), mapper);
    }

    @Override
    public String getDescription() {
        return "Customize your np/fm commands with several extra fields";
    }

    @Override
    public List<String> getAliases() {
        return List.of("npmode", "npconfig", "npc", "fmmode", "fmconfig", "fmc");
    }

    @Override
    public String getName() {
        return "Now Playing command configuration";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        EnumListParameters<NPMode> parse = parser.parse(e);
        if (parse == null) {
            return;
        }


        EnumSet<NPMode> modes = parse.getEnums();
        if (parse.isHelp()) {
            if (modes.isEmpty()) {
                sendMessageQueue(e, getUsageInstructions());
                return;
            }
            String collect = modes.stream().map(x -> NPMode.getListedName(List.of(x)) + " -> " + x.getHelpMessage()).collect(Collectors.joining("\n"));
            sendMessageQueue(e, collect);
            return;
        }
        if (parse.isListing()) {
            modes = getService().getNPModes(e.getAuthor().getIdLong());
            String strMode = NPMode.getListedName(modes);
            sendMessageQueue(e,
                    "Do `" + e.getMessage().getContentRaw().split("\\s+")[0] + " help` for a list of all options.\n" +
                            "Current modes: " +
                            strMode);
        } else {
            String strMode = NPMode.getListedName(modes);
            getService().changeNpMode(e.getAuthor().getIdLong(), modes);
            sendMessageQueue(e, String.format("Successfully changed to the following %s: %s", CommandUtil.singlePlural(modes.size(), "mode", "modes"), strMode));
        }
    }


}
