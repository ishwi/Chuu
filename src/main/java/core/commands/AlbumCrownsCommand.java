package core.commands;

import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.AlbumPlays;
import dao.entities.ArtistPlays;
import dao.entities.DiscordUserDisplay;
import dao.entities.UniqueWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class AlbumCrownsCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {
    public AlbumCrownsCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce a number to vary the number of plays to award a crown, " +
                "defaults to whatever the guild has configured (0 if not configured)";
        return new NumberParser<>(new OnlyUsernameParser(getService()),
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true);
    }

    @Override
    public String getDescription() {
        return ("List of albums you are the top listener within a server");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("albumcrowns", "crownsalbum", "crownsal");
    }

    @Override
    public String getName() {
        return "Your album crowns";
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        NumberParameters<ChuuDataParams> outer = parser.parse(e);
        ChuuDataParams params = outer.getInnerParams();

        DiscordUserDisplay userInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getLastFMData().getDiscordId());
        String name = userInfo.getUsername();
        String url = userInfo.getUrlImage();
        Long threshold = outer.getExtraParam();
        long idLong = params.getE().getGuild().getIdLong();

        if (threshold == null) {
            threshold = (long) getService().getGuildCrownThreshold(idLong);
        }
        UniqueWrapper<AlbumPlays> uniqueDataUniqueWrapper = getService()
                .getUserAlbumCrowns(params.getLastFMData().getName(), e.getGuild().getIdLong(), Math.toIntExact(threshold));
        List<AlbumPlays> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();

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



