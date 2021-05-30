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
import dao.ServiceView;
import dao.entities.AlbumPlays;
import dao.entities.DiscordUserDisplay;
import dao.entities.UniqueWrapper;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

public class UniqueAlbumCommand extends ConcurrentCommand<ChuuDataParams> {
    public UniqueAlbumCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = false;
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
        return ("Returns lists of all the unique albums you have scrobbled");
    }

    @Override
    public List<String> getAliases() {
        return List.of("uniquealbums", "uniquealb");
    }

    @Override
    public String getName() {
        return "Unique list of albums";
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) {

        String lastFmName = params.getLastFMData().getName();

        UniqueWrapper<AlbumPlays> resultWrapper = getList(e.isFromGuild() ? e.getGuild().getIdLong() : -1, lastFmName);
        int rows = resultWrapper.getUniqueData().size();
        if (rows == 0) {
            sendMessageQueue(e, String.format("You have no %sunique albums :(", isGlobal() ? "global " : ""));
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < rows; i++) {
            AlbumPlays g = resultWrapper.getUniqueData().get(i);
            a.append(i + 1).append(g.toString());
        }

        long discordId = params.getLastFMData().getDiscordId();
        DiscordUserDisplay userInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId);

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setAuthor(String.format("%s's%s unique albums", userInfo.getUsername(), isGlobal() ? " global" : ""), CommandUtil.getLastFmUser(lastFmName), userInfo.getUrlImage())
                .setDescription(a)
                .setFooter(String.format("%s has %d%s unique albums!%n", CommandUtil.unescapedUser(userInfo.getUsername(), discordId, e), rows, isGlobal() ? " global" : ""), null);

        e.sendMessage(embedBuilder.build()).queue(m ->
                new Reactionary<>(resultWrapper.getUniqueData(), m, embedBuilder));

    }

    public boolean isGlobal() {
        return false;
    }

    public UniqueWrapper<AlbumPlays> getList(long guildId, String lastFmName) {
        return db.getUniquAlbums(guildId, lastFmName);
    }


}
