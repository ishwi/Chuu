package core.commands.config;

import core.apis.lyrics.TextSplitter;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.EnumListParser;
import core.parsers.Parser;
import core.parsers.params.EnumListParameters;
import core.util.ServiceView;
import dao.entities.NPMode;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
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

    public NPModeSetterCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<EnumListParameters<NPMode>> initParser() {
        return new EnumListParser<>(db, "np-modes", NPMode.class, EnumSet.of(NPMode.UNKNOWN), mapper);
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
        return "NP command configuration";
    }

    @Override
    public void onCommand(Context e, @Nonnull EnumListParameters<NPMode> params) {

        EnumSet<NPMode> modes = params.getEnums();
        if (params.isHelp()) {
            if (modes.isEmpty()) {
                sendMessageQueue(e, getUsageInstructions());
                return;
            }
            String lines = modes.stream().map(x -> "**%s** ➜ %s".formatted(NPMode.getListedName(List.of(x)), x.getHelpMessage())).collect(Collectors.joining("\n"));
            List<String> split = TextSplitter.split(lines, 2000);

            EmbedBuilder eb = new ChuuEmbedBuilder(e).setTitle("NP Configuration help")
                    .setDescription(split.get(0));

            new PaginatorBuilder<>(e, eb, split).pageSize(1).unnumered().build().queue();
            return;
        }
        if (params.isListing()) {
            modes = db.getNPModes(params.getUser().getIdLong());
            String strMode = NPMode.getListedName(modes);
            sendMessageQueue(e,
                    "Do `" + CommandUtil.getMessagePrefix(e) + "npc help` for a list of all options.\n" +
                            "%surrent modes: ".formatted(params.getUser().getIdLong() != e.getAuthor().getIdLong() ? getUserString(e, params.getUser().getIdLong()) + "'s c" : "C") +
                            strMode);
        } else {
            if (params.isAdding() || params.isRemoving()) {
                EnumSet<NPMode> npModes = db.getNPModes(e.getAuthor().getIdLong());
                if (params.isAdding()) {
                    npModes.addAll(modes);
                } else {
                    npModes.removeAll(modes);
                }
                modes = npModes;
            }
            String strMode = NPMode.getListedName(modes);
            db.changeNpMode(e.getAuthor().getIdLong(), modes);
            sendMessageQueue(e, String.format("Successfully changed to the following %s: %s", CommandUtil.singlePlural(modes.size(), "mode", "modes"), strMode));
        }
    }

}
