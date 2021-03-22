package core.commands.config;

import core.apis.lyrics.TextSplitter;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.EnumListParser;
import core.parsers.Parser;
import core.parsers.params.EnumListParameters;
import dao.ChuuService;
import dao.entities.NPMode;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
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
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<EnumListParameters<NPMode>> initParser() {
        return new EnumListParser<>("NP Modes", NPMode.class, EnumSet.of(NPMode.UNKNOWN), mapper);
    }

    @Override
    public String getDescription() {
        return "Customize your nowPlayingInfo/fm commands with several extra fields";
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
    protected void onCommand(MessageReceivedEvent e, @NotNull EnumListParameters<NPMode> params) {

        EnumSet<NPMode> modes = params.getEnums();
        if (params.isHelp()) {
            if (modes.isEmpty()) {
                sendMessageQueue(e, getUsageInstructions());
                return;
            }
            String collect = modes.stream().map(x -> "**%s** -> %s".formatted(NPMode.getListedName(List.of(x)), x.getHelpMessage())).collect(Collectors.joining("\n"));
            List<String> split = TextSplitter.split(collect, 2000);
            for (String s : split) {
                sendMessageQueue(e, s);
            }
            return;
        }
        if (params.isListing()) {
            modes = db.getNPModes(e.getAuthor().getIdLong());
            String strMode = NPMode.getListedName(modes);
            sendMessageQueue(e,
                    "Do `" + e.getMessage().getContentRaw().split("\\s+")[0] + " help` for a list of all options.\n" +
                            "Current modes: " +
                            strMode);
        } else {
            String strMode = NPMode.getListedName(modes);
            db.changeNpMode(e.getAuthor().getIdLong(), modes);
            sendMessageQueue(e, String.format("Successfully changed to the following %s: %s", CommandUtil.singlePlural(modes.size(), "mode", "modes"), strMode));
        }
    }


}
