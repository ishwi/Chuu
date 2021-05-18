package core.commands.uniques;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.TrackPlays;
import dao.entities.UniqueWrapper;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

public class UniqueSongCommand extends ConcurrentCommand<ChuuDataParams> {
    public UniqueSongCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
        isLongRunningCommand = true;

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.UNIQUES;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
    }

    @Override
    public String getDescription() {
        return ("Returns lists of all the unique songs you have scrobbled");
    }

    @Override
    public List<String> getAliases() {
        return List.of("uniquesong", "uniquesongs");
    }

    @Override
    public String getName() {
        return "Unique list of songs";
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) {

        String lastFmName = params.getLastFMData().getName();

        UniqueWrapper<TrackPlays> resultWrapper = getList(isGlobal() ? -1 : e.getGuild().getIdLong(), lastFmName);
        int rows = resultWrapper.getUniqueData().size();
        if (rows == 0) {
            sendMessageQueue(e, String.format("You have no %sunique songs :(", isGlobal() ? "global " : ""));
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < rows; i++) {
            TrackPlays g = resultWrapper.getUniqueData().get(i);
            a.append(i + 1).append(g.toString());
        }

        DiscordUserDisplay userInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, resultWrapper.getDiscordId());

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setDescription(a)
                .setAuthor(String.format("%s's%s unique songs", userInfo.getUsername(), isGlobal() ? " global" : ""), CommandUtil.getLastFmUser(lastFmName), userInfo.getUrlImage())
                .setFooter(String.format("%s has %d%s unique songs!%n", CommandUtil.markdownLessUserString(userInfo.getUsername(), resultWrapper.getDiscordId(), e), rows, isGlobal() ? " global" : ""), null);

        e.sendMessage(embedBuilder.build()).queue(m ->
                new Reactionary<>(resultWrapper.getUniqueData(), m, embedBuilder));

    }

    public boolean isGlobal() {
        return false;
    }

    public UniqueWrapper<TrackPlays> getList(long guildId, String lastFmName) {
        return db.getUniqueTracks(guildId, lastFmName);
    }


}
