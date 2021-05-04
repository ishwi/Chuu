package core.commands.crowns;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.AlbumPlays;
import dao.entities.DiscordUserDisplay;
import dao.entities.UniqueWrapper;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

public class UniqueAlbumCommand extends ConcurrentCommand<ChuuDataParams> {
    public UniqueAlbumCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;

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

        UniqueWrapper<AlbumPlays> resultWrapper = getList(e.getGuild().getIdLong(), lastFmName);
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

        DiscordUserDisplay userInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, resultWrapper.getDiscordId());


        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(ColorService.computeColor(e))
                .setThumbnail(e.getGuild().getIconUrl());
        embedBuilder.setDescription(a).setTitle(String.format("%s's%s unique albums", userInfo.getUsername(), isGlobal() ? " global" : ""), CommandUtil
                .getLastFmUser(lastFmName))
                .setThumbnail(userInfo.getUrlImage())
                .setFooter(String.format("%s has %d%s unique albums!%n", CommandUtil.markdownLessUserString(userInfo.getUsername(), resultWrapper.getDiscordId(), e), rows, isGlobal() ? " global" : ""), null);

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
