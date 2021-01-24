package core.commands.stats;

import core.commands.abstracts.ListCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.otherlisteners.Reactionary;
import core.parsers.Parser;
import core.parsers.UserStringParser;
import core.parsers.params.UserStringParameters;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.ScrobbledAlbum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class AlbumSearchCommand extends ListCommand<ScrobbledAlbum, UserStringParameters> {
    public AlbumSearchCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<UserStringParameters> initParser() {
        return new UserStringParser(db, false);
    }

    @Override
    public String getDescription() {
        return "Search artist on your library by name";
    }

    @Override
    public List<String> getAliases() {
        return List.of("findalbum", "falb", "fal", "searchalbum", "searchalb");
    }

    @Override
    public String getName() {
        return "Find albums by name in your library";
    }


    @Override
    public List<ScrobbledAlbum> getList(UserStringParameters params) {
        String value = params.getValue();
        LastFMData lastFMData = params.getLastFMData();
        return db.regexAlbum(lastFMData.getDiscordId(), value);
    }

    @Override
    public void printList(List<ScrobbledAlbum> list, UserStringParameters params) {
        MessageReceivedEvent e = params.getE();
        String value = params.getValue();
        String abbreviate = StringUtils.abbreviate(value, 120);

        DiscordUserDisplay uInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getLastFMData().getDiscordId());
        if (list.isEmpty()) {
            e.getChannel().sendMessage(uInfo.getUsername() + " doesnt have any album searching by `" + abbreviate + '`').queue();
            return;
        }
        StringBuilder a = new StringBuilder();

        for (int i = 0; i < 10 && i < list.size(); i++) {
            a.append(i + 1).append(list.get(i).toString());
        }

        String title = uInfo.getUsername() + "'s albums that match " + abbreviate;
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setAuthor(title, PrivacyUtils.getLastFmUser(params.getLastFMData().getName()), uInfo.getUrlImage())
                .setFooter(list.size() + " matching albums!")
                .setDescription(a);
        e.getChannel().sendMessage(embedBuilder.build()).queue(mes ->
                new Reactionary<>(list, mes, embedBuilder));
    }


}
