package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ServerStats;
import dao.ServiceView;
import dao.entities.AudioFeatures;
import dao.entities.DiscordUserDisplay;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.util.List;

public class ObscurityCommand extends ConcurrentCommand<ChuuDataParams> {
    private static final DecimalFormat formatter = new DecimalFormat("#0.#");

    public ObscurityCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
    }

    @Override
    public String getDescription() {
        return "Obscurity resume about an user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("obscurity");
    }

    @Override
    public String getName() {
        return "Obscurity";
    }

    private String str(double user, double avg) {
        String engStr;
        if (user > avg) {
            String format = formatter.format((user - avg));
            engStr = "%s%% higher than the average".formatted(format);
        } else {
            String format = formatter.format((avg - user));
            engStr = "%s%% lower than the average".formatted(format);
        }
        return engStr;
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) {
        String name = params.getLastFMData().getName();
        double v = db.obtainObscurity(name);
        long id = params.getLastFMData().getDiscordId();
        String title = "";
        if (e.isFromGuild()) {
            List<LbEntry<Double>> obscurityRankings = db.getObscurityRankings(e.getGuild().getIdLong());
            for (int i = 0; i < obscurityRankings.size(); i++) {
                LbEntry<Double> entry = obscurityRankings.get(i);
                if (entry.getDiscordId() == id) {
                    title = " (%d%s)".formatted(i + 1, CommandUtil.getRank(i + 1));
                }
            }
        }
        AudioFeatures userFeatures = db.getUserFeatures(name);
        ServerStats serverStats = db.getServerStats(e.getGuild().getIdLong());
        final var avgDanceability = 0.57 * 100;
        final var avgEnergy = 0.65 * 100;
        final var avgHapinnes = 0.45 * 100;
        final var avgAcousticness = 0.22 * 100;

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);
        String danceStr = str(userFeatures.danceability(), avgDanceability);
        String energyStr = str(userFeatures.energy(), avgEnergy);
        String hapinessStr = str(userFeatures.liveness(), avgHapinnes);
        String accStr = str(userFeatures.acousticness(), avgAcousticness);
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, id);

        embedBuilder.setAuthor(e.getGuild().getName() + "'s stats", null, e.getGuild().getIconUrl())
                .setAuthor(uInfo.getUsername() + "'s obscurity details", PrivacyUtils.getLastFmUser(name), uInfo.getUrlImage())
                .setTitle(formatter.format(100 - v) + "% obscure" + title)
                .addField("**Happines:**", "**" + hapinessStr + "**", false)
                .addField("**Energy:**", "**" + energyStr + "**", false)
                .addField("**Danceability:**", "**" + danceStr + "**", false)
                .addField("**Acousticness:**", "**" + accStr + "**", false);

        e.sendMessage(embedBuilder.build()).queue();


    }


}
