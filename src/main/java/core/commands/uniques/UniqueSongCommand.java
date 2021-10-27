package core.commands.uniques;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.NotOnServerGuard;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.TrackPlays;
import dao.entities.UniqueWrapper;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.List;

public class UniqueSongCommand extends ConcurrentCommand<ChuuDataParams> {
    public UniqueSongCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = false;

    }

    @Override
    public String slashName() {
        return "tracks";
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
    public void onCommand(Context e, @Nonnull ChuuDataParams params) {

        String lastFmName = params.getLastFMData().getName();
        long discordId = params.getLastFMData().getDiscordId();

        UniqueWrapper<TrackPlays> resultWrapper = getList(isGlobal() ? -1 : e.getGuild().getIdLong(), lastFmName);

        if (new NotOnServerGuard(db).notOnServer(e.getGuild().getIdLong(), discordId)) {
            sendMessageQueue(e, ("You are not registered in this server.\n" +
                    "You need to do %sset or %slogin to get tracked in this server.").formatted(e.getPrefix(), e.getPrefix()));
            return;
        }

        int rows = resultWrapper.getUniqueData().size();
        if (rows == 0) {
            sendMessageQueue(e, String.format("You have no %sunique songs :(", isGlobal() ? "global " : ""));
            return;
        }

        DiscordUserDisplay userInfo = CommandUtil.getUserInfoEscaped(e, discordId);

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s's%s unique songs", userInfo.username(), isGlobal() ? " global" : ""), CommandUtil.getLastFmUser(lastFmName), userInfo.urlImage())
                .setFooter(String.format("%s has %d%s unique songs!%n", CommandUtil.unescapedUser(userInfo.username(), discordId, e), rows, isGlobal() ? " global" : ""), null);

        new PaginatorBuilder<>(e, embedBuilder, resultWrapper.getUniqueData()).build().queue();


    }

    public boolean isGlobal() {
        return false;
    }

    public UniqueWrapper<TrackPlays> getList(long guildId, String lastFmName) {
        return db.getUniqueTracks(guildId, lastFmName);
    }


}
