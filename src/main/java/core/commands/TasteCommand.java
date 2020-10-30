package core.commands;

import core.Chuu;
import core.exceptions.LastFmException;
import core.imagerenderer.TasteRenderer;
import core.otherlisteners.Reactionary;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.TwoUsersParser;
import core.parsers.params.TwoUsersParamaters;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.ResultWrapper;
import dao.entities.UserArtistComparison;
import dao.entities.UserInfo;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TasteCommand extends ConcurrentCommand<TwoUsersParamaters> {
    public TasteCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<TwoUsersParamaters> initParser() {
        TwoUsersParser twoUsersParser = new TwoUsersParser(getService(), new OptionalEntity("list", "display in a list format"));
        twoUsersParser.setExpensiveSearch(true);
        return twoUsersParser;
    }

    @Override
    public String getDescription() {
        return "Compare Your musical taste with another user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("taste", "t");
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        List<String> lastfMNames;

        TwoUsersParamaters params = parser.parse(e);
        if (params == null)
            return;
        long ogDiscordID = params.getFirstUser().getDiscordId();
        String ogLastFmId = params.getFirstUser().getName();
        long secondDiscordId = params.getSecondUser().getDiscordId();
        String secondlastFmId = params.getSecondUser().getName();
        boolean isList = params.hasOptional("list");

        lastfMNames = Arrays.asList(ogLastFmId, secondlastFmId);

        ResultWrapper<UserArtistComparison> resultWrapper = getService().getSimilarities(lastfMNames, isList ? 200 : 10);
        //TODO this happens both when user is not on db and no mathching so fix pls
        if (resultWrapper.getRows() == 0) {
            sendMessageQueue(e, "You don't share any artist :(");
            return;
        }
        switch (CommandUtil.getEffectiveMode(params.getFirstUser().getRemainingImagesMode(), params)) {
            case PIE:
            case IMAGE:
                doImage(e, resultWrapper, ogDiscordID, secondDiscordId);
                break;
            case LIST:
                doList(e, ogDiscordID, secondDiscordId, resultWrapper);
                break;
        }
    }

    private void doImage(MessageReceivedEvent e, ResultWrapper<UserArtistComparison> resultWrapper, long firstId, long secondId) throws LastFmException {
        List<String> users = new ArrayList<>();
        users.add(resultWrapper.getResultList().get(0).getUserA());
        users.add(resultWrapper.getResultList().get(0).getUserB());
        List<UserInfo> userInfoList = lastFM.getUserInfo(users);
        UserInfo userInfo = userInfoList.get(0);
        if (Chuu.getLastFmId(userInfo.getUsername()).equals(Chuu.DEFAULT_LASTFM_ID)) {
            userInfo.setUsername(CommandUtil.getUserInfoNotStripped(e, firstId).getUsername());
        }
        UserInfo userInfo1 = userInfoList.get(1);
        if (Chuu.getLastFmId(userInfo1.getUsername()).equals(Chuu.DEFAULT_LASTFM_ID)) {
            userInfo1.setUsername(CommandUtil.getUserInfoNotStripped(e, secondId).getUsername());
        }
        BufferedImage image = TasteRenderer.generateTasteImage(resultWrapper, userInfoList);
        sendImage(image, e);
    }

    private void doList(MessageReceivedEvent e, long ogDiscordID, long secondDiscordId, ResultWrapper<UserArtistComparison> resultWrapper) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> strings = resultWrapper.getResultList().stream().map(x -> String.format(". [%s](%s) - %d vs %d plays%n",
                x.getArtistID(),
                LinkUtils.getLastFmArtistUrl(x.getArtistID()),
                x.getCountA(), x.getCountB())).collect(Collectors.toList());
        for (int i = 0, size = strings.size(); i < 10 && i < size; i++) {
            String text = strings.get(i);
            stringBuilder.append(i + 1).append(text);
        }
        DiscordUserDisplay uinfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, ogDiscordID);
        DiscordUserDisplay uinfo1 = CommandUtil.getUserInfoConsideringGuildOrNot(e, secondDiscordId);
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setDescription(stringBuilder)
                .setTitle(String.format("%s vs %s", uinfo.getUsername(), uinfo1.getUsername()))
                .setColor(CommandUtil.randomColor())
                .setFooter(String.format("Both user have %d common artists", resultWrapper.getRows()), null)
                .setThumbnail(uinfo1.getUrlImage());
        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(strings, message1, embedBuilder));
    }

    @Override
    public String getName() {
        return "Taste";
    }

}
