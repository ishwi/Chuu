package core.commands;

import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.RymStats;
import dao.entities.ScoredAlbumRatings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class TopRatingsCommand extends ListCommand<ScoredAlbumRatings, CommandParameters> {

    public TopRatingsCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.RYM_BETA;
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
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

        return getService().getGlobalTopRatings();
    }

    @Override
    public void printList(List<ScoredAlbumRatings> list, CommandParameters params) {
        MessageReceivedEvent e = params.getE();
        MessageBuilder messageBuilder = new MessageBuilder();
        NumberFormat formatter = new DecimalFormat("#0.##");

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
        StringBuilder a = new StringBuilder();

        if (list.isEmpty()) {
            sendMessageQueue(e, "There are no ratings in the bot at alls");
            return;
        }

        for (int i = 0; i < 10 && i < list.size(); i++) {
            a.append(i + 1).append(list.get(i).toString());
        }
        RymStats rymServerStats = getService().getRYMBotStats();
        embedBuilder.setDescription(a).setTitle(CommandUtil.cleanMarkdownCharacter(e.getJDA().getSelfUser().getName()) + "'s Top Ranked Albums")
                .setThumbnail(e.getJDA().getSelfUser().getAvatarUrl())
                .setFooter(String.format(e.getJDA().getSelfUser().getName() + " users have rated a total of %s albums with an average of %s!", rymServerStats.getNumberOfRatings(), formatter.format(rymServerStats.getAverage() / 2f)), null)
                .setColor(CommandUtil.randomColor());

        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build()).queue(message ->
                new Reactionary<>(list, message, embedBuilder));
    }
}
