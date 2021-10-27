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

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CurveCommand extends ConcurrentCommand<ChuuDataParams> {
    private final static String EMPTY_STAR = "<:empty:903016787686350898>";
    private final static String FULL_STAR = "<:full:903016787526955089>";
    private final static String HALF_STAR = "<:half:903016787602460682>";

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
    public void onCommand(Context e, @Nonnull ChuuDataParams params) {
        long discordId = params.getLastFMData().getDiscordId();
        Map<Integer, Integer> userCurve = db.getUserCurve(discordId);
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, discordId);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor("Breakdown by rating for " + uInfo.username(), PrivacyUtils.getLastFmUser(params.getLastFMData().getName()), uInfo.urlImage());
        userCurve.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEachOrdered(x -> {

            Integer key = x.getKey();
            boolean isOdd = key % 2 == 1;
            int repeating = key / 2;

            String s = FULL_STAR.repeat(repeating) + (isOdd ? HALF_STAR : "") + EMPTY_STAR.repeat(5 - (repeating + (isOdd ? 1 : 0)));
            embedBuilder.appendDescription(s + "\t\t" + x.getValue() + " " + CommandUtil.singlePlural(x.getValue(), "rating", "ratings") + " \n");
        });
        e.sendMessage(embedBuilder.build()).queue();
    }

}
