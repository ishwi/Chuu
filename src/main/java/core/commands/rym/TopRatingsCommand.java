package core.commands.rym;

import core.commands.Context;
import core.commands.abstracts.ListCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import dao.entities.RymStats;
import dao.entities.ScoredAlbumRatings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.SelfUser;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class TopRatingsCommand extends ListCommand<ScoredAlbumRatings, CommandParameters> {

    public TopRatingsCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RYM;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Top Ranked Albums according to all bot users";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rymtop", "rymt");
    }

    @Override
    public String getName() {
        return "Top Rated Albums";
    }

    @Override
    public List<ScoredAlbumRatings> getList(CommandParameters params) {

        return db.getGlobalTopRatings();
    }

    @Override
    public void printList(List<ScoredAlbumRatings> ratings, CommandParameters params) {
        Context e = params.getE();
        NumberFormat formatter = new DecimalFormat("#0.##");

        SelfUser botAccount = e.getJDA().getSelfUser();
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setThumbnail(botAccount.getAvatarUrl());

        if (ratings.isEmpty()) {
            sendMessageQueue(e, "There are no ratings in the bot at alls");
            return;
        }


        RymStats rymServerStats = db.getRYMBotStats();
        embedBuilder.setTitle(CommandUtil.escapeMarkdown(botAccount.getName()) + "'s Top Ranked Albums")
                .setThumbnail(botAccount.getAvatarUrl())
                .setFooter(String.format(botAccount.getName() + " users have rated a total of %s albums with an average of %s!", rymServerStats.getNumberOfRatings(), formatter.format(rymServerStats.getAverage() / 2f)), null);

        new PaginatorBuilder<>(e, embedBuilder, ratings).build().queue();
    }
}
