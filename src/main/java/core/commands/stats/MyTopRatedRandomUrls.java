package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScoredAlbumRatings;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

public class MyTopRatedRandomUrls extends ConcurrentCommand<ChuuDataParams> {
    public MyTopRatedRandomUrls(ChuuService dao) {
        super(dao);
    }

    static void RandomUrlDisplay(Context e, List<ScoredAlbumRatings> ratings, String title, String url) {
        List<String> list = ratings.stream().map(x -> {
            String average;
            String count;
            if (x.getNumberOfRatings() == 0) {
                average = "-";
                count = "-";
            } else {
                average = ScoredAlbumRatings.formatter.format(x.getAverage() / 2f);
                count = String.valueOf(x.getNumberOfRatings());
            }
            return ". ***[" + x.getUrl()
                   +
                   "](" + x.getUrl() +
                   ")***\n\t" + String.format("Average: **%s** | # of Ratings: **%s**", average, count) +
                   "\n";
        }).toList();
        StringBuilder a = new StringBuilder();
        for (
                int i = 0;
                i < 10 && i < list.size(); i++) {
            a.append(i + 1).append(list.get(i));
        }

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder()
                .setDescription(a).setTitle(title)
                .setThumbnail(url)
                .setColor(ColorService.computeColor(e));

        e.sendMessage(embedBuilder.build()).
                queue(message ->
                        new Reactionary<>(list, message, embedBuilder));
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
    }

    @Override
    public String getDescription() {
        return "The top rated random urls by yourself, this server or the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("mytoprandoms", "mytopr", "myurls");
    }

    @Override
    public String getName() {
        return "My top random urls";
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) {


        long idLong = e.getAuthor().getIdLong();
        List<ScoredAlbumRatings> ratings = db.getUserTopRatedUrlsByEveryoneElse(idLong);
        if (ratings.isEmpty()) {
            sendMessageQueue(e, "Your random urls are yet to be rated :pensive:");
            return;

        }
        DiscordUserDisplay userInfoConsideringGuildOrNot = CommandUtil.getUserInfoConsideringGuildOrNot(params.getE(), idLong);
        String title = userInfoConsideringGuildOrNot.getUsername();
        String url = userInfoConsideringGuildOrNot.getUrlImage();


        RandomUrlDisplay(e, ratings, title + "'s top rated urls by everyone else", url);
    }
}
