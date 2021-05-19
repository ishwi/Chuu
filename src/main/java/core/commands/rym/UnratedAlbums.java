package core.commands.rym;

import core.commands.Context;
import core.commands.abstracts.ListCommand;
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
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

public class UnratedAlbums extends ListCommand<AlbumPlays, ChuuDataParams> {
    public UnratedAlbums(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RYM;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
    }

    @Override
    public String getDescription() {
        return "List your top unrated rym albums";
    }

    @Override
    public List<String> getAliases() {
        return List.of("unrated");
    }

    @Override
    public String getName() {
        return "Unrated Albums";
    }


    @Override
    public List<AlbumPlays> getList(ChuuDataParams params) {
        return db.getUnratedAlbums(params.getLastFMData().getDiscordId());
    }

    @Override
    public void printList(List<AlbumPlays> list, ChuuDataParams params) {
        Long discordId = params.getLastFMData().getDiscordId();
        Context e = params.getE();
        DiscordUserDisplay dp = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId);
        if (list.isEmpty()) {
            sendMessageQueue(e, "Couldn't find any unrated album in " + dp.getUsername() + " albums");
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < list.size(); i++) {
            a.append(i + 1).append(list.get(i));
        }

        char prefix = CommandUtil.getMessagePrefix(e);

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setDescription(a)
                .setTitle(dp.getUsername() + "'s Unrated Albums")
                .setFooter("You can link your rym account using " + prefix + "rymimport\n You have " + list.size() + " unrated albums", null)
                .setThumbnail(dp.getUrlImage());
        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(list, message1, embedBuilder));
    }
}
