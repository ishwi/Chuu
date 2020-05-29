package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.DiscordUserDisplay;
import dao.entities.UniqueWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class AlbumCrownsCommand extends ConcurrentCommand<ChuuDataParams> {
    public AlbumCrownsCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public Parser<ChuuDataParams> getParser() {
        return new OnlyUsernameParser(getService());
    }

    @Override
    public String getDescription() {
        return ("List of albums you are the top listener within a server");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("crownsalbum", "crownsal");
    }

    @Override
    public String getName() {
        return "Your album crowns";
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ChuuDataParams params = parser.parse(e);
        DiscordUserDisplay userInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getLastFMData().getDiscordId());
        String name = userInfo.getUsername();
        String url = userInfo.getUrlImage();

        UniqueWrapper<ArtistPlays> uniqueDataUniqueWrapper = getService()
                .getUserAlbumCrowns(params.getLastFMData().getName(), e.getGuild().getIdLong());
        List<ArtistPlays> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();

        int rows = resultWrapper.size();
        if (rows == 0) {
            sendMessageQueue(e, name + " doesn't have any album crown :'(");
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < rows; i++) {
            ArtistPlays g = resultWrapper.get(i);
            a.append(i + 1).append(g.toString());
        }
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setDescription(a)
                .setColor(CommandUtil.randomColor())
                .setTitle(String.format("%s's album crowns", name), CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.getLastFmId()))
                .setFooter(String.format("%s has %d album crowns!!%n", CommandUtil.markdownLessUserString(name, params.getLastFMData().getDiscordId(), e), resultWrapper.size()), null)
                .setThumbnail(url);

        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(resultWrapper, message1, embedBuilder));
    }

}



