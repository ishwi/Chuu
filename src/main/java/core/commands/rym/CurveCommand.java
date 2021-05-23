package core.commands.rym;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CurveCommand extends ConcurrentCommand<ChuuDataParams> {
    public CurveCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RYM;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
    }

    @Override
    public String getDescription() {
        return "Your rym curve";
    }

    @Override
    public List<String> getAliases() {
        return List.of("curve", "average");
    }

    @Override
    public String getName() {
        return "RYM Curve";
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) {
        Long discordId = params.getLastFMData().getDiscordId();
        Map<Integer, Integer> userCurve = db.getUserCurve(discordId);
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, discordId);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor("Breakdown by rating for " + uInfo.getUsername(), PrivacyUtils.getLastFmUser(params.getLastFMData().getName()), uInfo.getUrlImage());
        userCurve.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEachOrdered(x -> {

            Integer key = x.getKey();
            boolean isOdd = key % 2 == 1;
            int repeating = key / 2;

            String s = "<:full:794269605170118658>".repeat(repeating) + (isOdd ? "<:half:794269605266849862>" : "") + ":" + ((EmbedBuilder.ZERO_WIDTH_SPACE + "\t").repeat(2)).repeat(5 - (key - 1) / 2);
            embedBuilder.appendDescription(s + x.getValue() + " ratings \n");
        });
        e.sendMessage(embedBuilder.build()).queue();
    }

}
