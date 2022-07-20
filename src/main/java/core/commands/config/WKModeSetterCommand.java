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
import dao.ServiceView;
import dao.entities.WKMode;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WKModeSetterCommand extends ConcurrentCommand<EnumListParameters<WKMode>> {

    public static final Function<String, EnumSet<WKMode>> mapper = (String value) -> {
        String[] split = value.trim().replaceAll(" +", " ").split("[|,& ]+");
        EnumSet<WKMode> modes = EnumSet.noneOf(WKMode.class);
        for (String mode : split) {
            try {
                WKMode npMode = WKMode.valueOf(mode.replace("-", "_").toUpperCase());
                modes.add(npMode);
            } catch (IllegalArgumentException ignored) {
                //Ignore
            }
        }
        return modes;
    };

    public WKModeSetterCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<EnumListParameters<WKMode>> initParser() {
        return new EnumListParser<>(db, "wk-image-modes", WKMode.class, EnumSet.of(WKMode.UNKNOWN), mapper);
    }

    @Override
    public String getDescription() {
        return "Customize your wk image commands";
    }

    @Override
    public List<String> getAliases() {
        return List.of("wkmode", "wkconfg", "wkc");
    }

    @Override
    public String getName() {
        return "Whoknows image configuration";
    }

    @Override
    public void onCommand(Context e, @Nonnull EnumListParameters<WKMode> params) {

        EnumSet<WKMode> modes = params.getEnums();
        if (params.isHelp()) {
            if (modes.isEmpty()) {
                sendMessageQueue(e, getUsageInstructions());
                return;
            }
            String lines = modes.stream().map(x -> "**%s** ➜ %s".formatted(WKMode.getListedName(List.of(x)), x.getHelpMessage())).collect(Collectors.joining("\n"));
            List<String> split = TextSplitter.split(lines, 2000);

            EmbedBuilder eb = new ChuuEmbedBuilder(e).setTitle("Whoknows image configuration help")
                    .setDescription(split.get(0));

            new PaginatorBuilder<>(e, eb, split).pageSize(1).unnumered().build().queue();
            return;
        }
        if (params.isListing()) {
            modes = db.getWkModes(params.getUser().getIdLong());
            String strMode = WKMode.getListedName(modes);
            sendMessageQueue(e,
                    "Do `" + CommandUtil.getMessagePrefix(e) + "npc help` for a list of all options.\n" +
                    "%surrent modes: ".formatted(params.getUser().getIdLong() != e.getAuthor().getIdLong() ? getUserString(e, params.getUser().getIdLong()) + "'s c" : "C") +
                    strMode);
        } else {
            if (params.isAdding() || params.isRemoving()) {
                EnumSet<WKMode> npModes = db.getWkModes(e.getAuthor().getIdLong());
                if (params.isAdding()) {
                    npModes.addAll(modes);
                } else {
                    npModes.removeAll(modes);
                }
                modes = npModes;
            }
            String strMode = WKMode.getListedName(modes);
            db.changeWkMode(e.getAuthor().getIdLong(), modes);
            sendMessageQueue(e, String.format("Successfully changed to the following %s: %s", CommandUtil.singlePlural(modes.size(), "mode", "modes"), strMode));
        }
    }

}
