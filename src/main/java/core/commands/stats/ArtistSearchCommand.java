package core.commands.stats;

import core.commands.abstracts.ListCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.otherlisteners.Reactionary;
import core.parsers.Parser;
import core.parsers.UserStringParser;
import core.parsers.params.UserStringParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ArtistSearchCommand extends ListCommand<ScrobbledArtist, UserStringParameters> {
    public ArtistSearchCommand(ChuuService dao) {
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
        return List.of("findartist", "fa", "searchartist", "searcha");
    }

    @Override
    public String getName() {
        return "Find artist by name in your library";
    }


    @Override
    public List<ScrobbledArtist> getList(UserStringParameters params) {
        String value = params.getValue();
        LastFMData lastFMData = params.getLastFMData();
        return db.regexArtist(lastFMData.getDiscordId(), value);
    }

    @Override
    public void printList(List<ScrobbledArtist> list, UserStringParameters params) {
        MessageReceivedEvent e = params.getE();
        String value = params.getValue();
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getLastFMData().getDiscordId());
        String abbreviate = StringUtils.abbreviate(value, 120);

        if (list.isEmpty()) {
            e.getChannel().sendMessage(uInfo.getUsername() + " doesnt have any artist searching by `" + abbreviate + "`").queue();
            return;
        }
        List<String> strs = list.stream().map(t ->
                String.format(". **[%s](%s)** - %d %s%n",
                        LinkUtils.cleanMarkdownCharacter(t.getArtist()), PrivacyUtils.getLastFmArtistUserUrl(t.getArtist(), params.getLastFMData().getName()),
                        t.getCount(), CommandUtil.singlePlural(t.getCount(), "play", "plays"))).toList();
        StringBuilder a = new StringBuilder();

        for (int i = 0; i < 10 && i < strs.size(); i++) {
            a.append(i + 1).append(strs.get(i));
        }

        String title = uInfo.getUsername() + "'s artist that match " + abbreviate;
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(ColorService.computeColor(e))
                .setAuthor(title, PrivacyUtils.getLastFmUser(params.getLastFMData().getName()), uInfo.getUrlImage())
                .setFooter(list.size() + " matching artists!")
                .setDescription(a);
        e.getChannel().sendMessage(embedBuilder.build()).queue(mes ->
                new Reactionary<>(strs, mes, embedBuilder));
    }


}
