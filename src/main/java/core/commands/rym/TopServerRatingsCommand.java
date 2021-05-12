package core.commands.rym;

import core.commands.Context;
import core.commands.abstracts.ListCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.RymStats;
import dao.entities.ScoredAlbumRatings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class TopServerRatingsCommand extends ListCommand<ScoredAlbumRatings, CommandParameters> {
    public TopServerRatingsCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
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
        return List.of("rymserver", "rymg");
    }

    @Override
    public String getName() {
        return "Top rated albums in this server";
    }

    @Override
    public List<ScoredAlbumRatings> getList(CommandParameters params) {
        return db.getServerTopRatings(params.getE().getGuild().getIdLong());
    }

    @Override
    public void printList(List<ScoredAlbumRatings> list, CommandParameters params) {
        Context e = params.getE();
        NumberFormat formatter = new DecimalFormat("#0.##");

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder().setColor(ColorService.computeColor(e))
                .setThumbnail(e.getGuild().getIconUrl());
        StringBuilder a = new StringBuilder();

        if (list.isEmpty()) {
            sendMessageQueue(e, "There are no ratings in the bot at alls");
            return;
        }

        for (int i = 0; i < 10 && i < list.size(); i++) {
            a.append(i + 1).append(list.get(i).toString());
        }
        String name = e.getGuild().getName();
        RymStats rymServerStats = db.getRYMServerStats(e.getGuild().getIdLong());
        embedBuilder.setDescription(a).setTitle(CommandUtil.cleanMarkdownCharacter(name) + "'s Top Ranked Albums")
                .setThumbnail(e.getGuild().getIconUrl())
                .setFooter(String.format("This server has rated a total of %s albums with an average of %s!", rymServerStats.getNumberOfRatings(), formatter.format(rymServerStats.getAverage() / 2f)), null)
                .setColor(ColorService.computeColor(e));

        e.sendMessage(embedBuilder.build()).queue(message ->
                new Reactionary<>(list, message, embedBuilder));
    }
}
