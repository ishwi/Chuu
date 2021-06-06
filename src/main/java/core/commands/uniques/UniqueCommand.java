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
import dao.entities.ArtistPlays;
import dao.entities.DiscordUserDisplay;
import dao.entities.UniqueWrapper;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class UniqueCommand extends ConcurrentCommand<ChuuDataParams> {
    public UniqueCommand(ServiceView dao) {
        this(dao, true);
    }

    public UniqueCommand(ServiceView dao, boolean isLongRunningCommand) {
        super(dao, isLongRunningCommand);
        this.respondInPrivate = false;

    }

    @Override
    public String slashName() {
        return "artists";
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
        return ("Returns lists of all the unique artist you have scrobbled");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("unique");
    }

    @Override
    public String getName() {
        return "Unique list of artists";
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) {

        String lastFmName = params.getLastFMData().getName();

        UniqueWrapper<ArtistPlays> resultWrapper = getList(isGlobal() ? -1 : e.getGuild().getIdLong(), lastFmName);
        int rows = resultWrapper.getUniqueData().size();
        if (rows == 0) {
            sendMessageQueue(e, String.format("You have no %sunique artists :(", isGlobal() ? "global " : ""));
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < rows; i++) {
            ArtistPlays g = resultWrapper.getUniqueData().get(i);
            a.append(i + 1).append(g.toString());
        }

        long discordId = params.getLastFMData().getDiscordId();
        DiscordUserDisplay userInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId);


        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setDescription(a)
                .setAuthor(String.format("%s's%s unique artists", userInfo.getUsername(), isGlobal() ? " global" : ""), CommandUtil.getLastFmUser(lastFmName), userInfo.getUrlImage())
                .setDescription(a)
                .setFooter(String.format("%s has %d%s unique artists!%n", CommandUtil.unescapedUser(userInfo.getUsername(), discordId, e), rows, isGlobal() ? " global" : ""), null);

        e.sendMessage(embedBuilder.build()).queue(m ->
                new Reactionary<>(resultWrapper.getUniqueData(), m, embedBuilder));

    }

    public boolean isGlobal() {
        return false;
    }

    public UniqueWrapper<ArtistPlays> getList(long guildId, String lastFmName) {
        return db.getUniqueArtist(guildId, lastFmName);
    }


}
