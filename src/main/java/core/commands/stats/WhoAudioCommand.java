package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.LeaderboardCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.EnumParser;
import core.parsers.Parser;
import core.parsers.params.EnumParameters;
import core.parsers.utils.OptionalEntity;
import core.util.ServiceView;
import dao.entities.AudioStats;
import dao.entities.LbEntry;
import dao.utils.Order;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.text.WordUtils;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class WhoAudioCommand extends LeaderboardCommand<EnumParameters<AudioStats>, Float> {

    public WhoAudioCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = false;
    }

    @Override
    public String getEntryName(EnumParameters<AudioStats> params) {
        String elment = params.getElement().toString().toLowerCase(Locale.ROOT).replaceAll("_", " ");
        return params.hasOptional("reverse") ? "inverted " + elment : elment;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_LEADERBOARDS;
    }

    @Override
    public Parser<EnumParameters<AudioStats>> initParser() {
        var parser = new EnumParser<>(AudioStats.class, true, false, false);
        parser.addOptional(new OptionalEntity("reverse", "sort by lowest to highest instead", "r"));
        return parser;
    }

    @Override
    public List<LbEntry<Float>> getList(EnumParameters<AudioStats> params) {
        AudioStats element = params.getElement();
        Order order = Order.DESC;
        if (params.hasOptional("reverse")) {
            order = order.getInverse();
        }
        if (!element.isReal()) {
            order = order.getInverse();
        }
        return db.getServerAudioLeadearboard(params.getElement().mapToReal(), params.getE().getGuild().getIdLong(), order);
    }

    @Override
    public String getDescription() {
        return "Leaderboard of the different audio stats";
    }

    @Override
    public List<String> getAliases() {
        return List.of("whoaudio", "whoa", "audiolb", "ftlb", "featureslb");
    }

    @Override
    public String getName() {
        return "Audio leaderboard";
    }

    @Override
    public void onCommand(Context e, @Nonnull EnumParameters<AudioStats> params) {
        if (params.getElement() == null) {
            EmbedBuilder eb = new ChuuEmbedBuilder(e).setAuthor("Audio leadearboard help");
            EnumSet<AudioStats> stats = EnumSet.allOf(AudioStats.class);
            stats.forEach(z -> eb.addField(WordUtils.capitalizeFully(z.toString().replaceAll("_", " ")), z.description, false));
            eb.setFooter("Select one of the following stats for the corresponding server leaderboard!");
            e.sendMessage(eb.build()).queue();
            return;
        }
        super.onCommand(e, params);
    }

    @Override
    protected void setFooter(EmbedBuilder embedBuilder, List<LbEntry<Float>> list, EnumParameters<AudioStats> params) {
        Context e = params.getE();
        String text = "";
        if (list.size() > 10) {
            text = "%s has %d %s with audio stats!\n".formatted(e.getGuild().getName(), list.size(), CommandUtil.singlePlural(list.size(), "user", "users"));
        }
        embedBuilder.setFooter(text + userStringFooter(embedBuilder, list, params), null);
    }
}
