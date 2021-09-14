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
import core.parsers.utils.OptionalEntity;
import dao.ServiceView;
import dao.entities.AudioFeatures;
import dao.entities.DiscordUserDisplay;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ObscurityCommand extends ConcurrentCommand<ChuuDataParams> {
    private static final DecimalFormat formatter = new DecimalFormat("#0.#");

    public ObscurityCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        OnlyUsernameParser p = new OnlyUsernameParser(db);
        p.addOptional(new OptionalEntity("refresh", "force an update on your obscurity score"));
        return p;
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
            String format = formatter.format((user - avg) * 100);
            engStr = "%s%% higher than the average".formatted(format);
        } else {
            String format = formatter.format((avg - user) * 100);
            engStr = "%s%% lower than the average".formatted(format);
        }
        return engStr;
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) {
        String name = params.getLastFMData().getName();
        CompletableFuture<AudioFeatures> uF = CompletableFuture.supplyAsync(() -> db.getUserFeatures(name));
        double v;
        if (params.hasOptional("refresh") || CommandUtil.rand.nextBoolean()) {
            v = db.processObscurity(name);
        } else {
            v = db.obtainObscurity(name).orElseGet(() -> db.processObscurity(name));
        }
        long id = params.getLastFMData().getDiscordId();
        CompletableFuture<String> titleCF = CompletableFuture.supplyAsync(() -> {
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
            return title;
        });
        final var avgDanceability = 0.57;
        final var avgEnergy = 0.65;
        final var avgHapinnes = 0.45;
        final var avgAcousticness = 0.22;
        AudioFeatures userFeatures = uF.join();
        String danceStr = str(userFeatures.danceability(), avgDanceability);
        String energyStr = str(userFeatures.energy(), avgEnergy);
        String hapinessStr = str(userFeatures.valence(), avgHapinnes);
        String accStr = str(userFeatures.acousticness(), avgAcousticness);
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, id);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(uInfo.username() + "'s obscurity details", PrivacyUtils.getLastFmUser(name), uInfo.urlImage())
                .setTitle(formatter.format(100 - v) + "% obscure" + titleCF.join())
                .addField("**Happiness:** %s%%".formatted(formatter.format(userFeatures.valence() * 100)), "**" + hapinessStr + "**", false)
                .addField("**Energy:** %s%%".formatted(formatter.format(userFeatures.energy() * 100)), "**" + energyStr + "**", false)
                .addField("**Danceability:** %s%%".formatted(formatter.format(userFeatures.danceability() * 100)), "**" + danceStr + "**", false)
                .addField("**Acousticness:** %s%%".formatted(formatter.format(userFeatures.acousticness() * 100)), "**" + accStr + "**", false);
        if (CommandUtil.rand.nextFloat() > 0.92f) {
            embedBuilder.setFooter("Data comes from Spotify");
        }

        e.sendMessage(embedBuilder.build()).queue();


    }


}
