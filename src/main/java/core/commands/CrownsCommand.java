package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.DiscordUserDisplay;
import dao.entities.UniqueWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class CrownsCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {
    public CrownsCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> getParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce a number to vary the number of plays to award a crown, " +
                "defaults to whatever the guild has configured (0 if not configured)";
        return new NumberParser<>(new OnlyUsernameParser(getService()),
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true);
    }

    public boolean isGlobal() {
        return false;
    }

    public UniqueWrapper<ArtistPlays> getList(NumberParameters<ChuuDataParams> params) {
        Long threshold = params.getExtraParam();
        long idLong = params.getE().getGuild().getIdLong();

        if (threshold == null) {
            threshold = (long) getService().getGuildCrownThreshold(idLong);
        }
        return getService().getCrowns(params.getInnerParams().getLastFMData().getName(), idLong, Math.toIntExact(threshold));
    }

    @Override
    public String getDescription() {
        return ("List of artist you are the top listener within a server");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("crowns");
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        NumberParameters<ChuuDataParams> outer = parser.parse(e);
        if (outer == null) {
            return;
        }
        ChuuDataParams params = outer.getInnerParams();
        UniqueWrapper<ArtistPlays> uniqueDataUniqueWrapper = getList(outer);
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getLastFMData().getDiscordId());
        String userName = userInformation.getUsername();
        String userUrl = userInformation.getUrlImage();
        List<ArtistPlays> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();

        int rows = resultWrapper.size();
        if (rows == 0) {
            sendMessageQueue(e, userName + " doesn't have any" + (isGlobal() ? " global " : " ") + "crown :'(");
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < rows; i++) {
            ArtistPlays g = resultWrapper.get(i);
            a.append(i + 1).append(g.toString());
        }


        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(a);
        embedBuilder.setColor(CommandUtil.randomColor());
        embedBuilder.setTitle(String.format("%s's %scrowns", userName, isGlobal() ? "global " : ""), CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.getLastFmId()));
        embedBuilder.setFooter(String.format("%s has %d%s crowns!!%n", CommandUtil.markdownLessUserString(userName, uniqueDataUniqueWrapper.getDiscordId(), e), resultWrapper.size(), isGlobal() ? " global" : ""), null);
        embedBuilder.setThumbnail(userUrl);

        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->

                new Reactionary<>(resultWrapper, message1, embedBuilder));
    }

    @Override
    public String getName() {
        return "Crowns";
    }


}



