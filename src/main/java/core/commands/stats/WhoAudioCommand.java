package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.LeaderboardCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.EnumParser;
import core.parsers.Parser;
import core.parsers.params.EnumParameters;
import dao.ServiceView;
import dao.entities.AudioStats;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.text.WordUtils;

import java.util.List;

public class WhoAudioCommand extends LeaderboardCommand<EnumParameters<AudioStats>, Float> {

    public WhoAudioCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = false;
    }

    @Override
    public String getEntryName(EnumParameters<AudioStats> params) {
        return WordUtils.capitalizeFully(params.getElement().toString().replaceAll("_", " "));
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_LEADERBOARDS;
    }

    @Override
    public Parser<EnumParameters<AudioStats>> initParser() {
        return new EnumParser<>(AudioStats.class);
    }

    @Override
    public List<LbEntry<Float>> getList(EnumParameters<AudioStats> params) {
        return db.getServerAudioLeadearboard(params.getElement(), params.getE().getGuild().getIdLong());
    }

    @Override
    public String getDescription() {
        return "Who listened first to an artist on a server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("whoaudio");
    }

    @Override
    public String getName() {
        return "Who audio";
    }

    @Override
    protected void setFooter(EmbedBuilder embedBuilder, List<LbEntry<Float>> list, EnumParameters<AudioStats> params) {
        Context e = params.getE();
        embedBuilder.setFooter(e.getGuild().getName() + " has " + list.size() + " " + CommandUtil.singlePlural(list.size(), "user", "users") + " with audio stats!\n", null);
    }
}
