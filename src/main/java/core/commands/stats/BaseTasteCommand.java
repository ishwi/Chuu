package core.commands.stats;

import core.Chuu;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.TasteRenderer;
import core.otherlisteners.Reactionary;
import core.parsers.params.CommandParameters;
import core.services.UserInfoService;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseTasteCommand<T extends CommandParameters> extends ConcurrentCommand<T> {
    boolean thumbnailPerRow = false;

    public BaseTasteCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    public abstract String getEntity(T params);

    public abstract @Nullable String hasCustomUrl(T params);

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull T params) throws LastFmException, InstanceNotFoundException {
        Pair<LastFMData, LastFMData> userDatas = getUserDatas(e, params);
        if (userDatas == null) {
            return;
        }
        ResultWrapper<UserArtistComparison> result = getResult(userDatas.getKey(), userDatas.getRight(), params);
        handleResult(e, result, userDatas.getKey(), userDatas.getValue(), params);
    }

    public abstract @Nullable Pair<LastFMData, LastFMData> getUserDatas(MessageReceivedEvent e, T params) throws InstanceNotFoundException;

    public abstract ResultWrapper<UserArtistComparison> getResult(LastFMData og, LastFMData second, T params) throws LastFmException;

    public abstract Pair<Integer, Integer> getTasteBar(ResultWrapper<UserArtistComparison> resultWrapper, UserInfo og, UserInfo second, T params) throws LastFmException;

    public void handleResult(MessageReceivedEvent e, ResultWrapper<UserArtistComparison> resultWrapper, LastFMData og, LastFMData second, T params) throws LastFmException {
        if (resultWrapper.getRows() == 0) {
            sendMessageQueue(e, String.format("You don't share any %s :(", getEntity(params)));
            return;
        }
        switch (CommandUtil.getEffectiveMode(og.getRemainingImagesMode(), params)) {
            case PIE:
            case IMAGE:
                doImage(e, resultWrapper, og.getDiscordId(), second.getDiscordId(), params);
                break;
            case LIST:
                doList(e, og.getDiscordId(), second.getDiscordId(), resultWrapper, params);
                break;
        }
    }

    private void doImage(MessageReceivedEvent e, ResultWrapper<UserArtistComparison> resultWrapper, long firstId, long secondId, T params) throws LastFmException {
        String userA = resultWrapper.getResultList().get(0).getUserA();
        String userB = resultWrapper.getResultList().get(0).getUserB();
        UserInfoService userInfoService = new UserInfoService(getService());
        UserInfo userInfo = userInfoService.getUserInfo(userA);
        UserInfo userInfo1 = userInfoService.getUserInfo(userB);
        if (Chuu.getLastFmId(userInfo.getUsername()).equals(Chuu.DEFAULT_LASTFM_ID)) {
            userInfo.setUsername(CommandUtil.getUserInfoNotStripped(e, firstId).getUsername());
        }
        if (Chuu.getLastFmId(userInfo1.getUsername()).equals(Chuu.DEFAULT_LASTFM_ID)) {
            userInfo1.setUsername(CommandUtil.getUserInfoNotStripped(e, secondId).getUsername());
        }
        Pair<Integer, Integer> tasteBar = getTasteBar(resultWrapper, userInfo, userInfo1, params);
        BufferedImage image = TasteRenderer.generateTasteImage(resultWrapper, List.of(userInfo, userInfo1), getEntity(params), hasCustomUrl(params), this.thumbnailPerRow, tasteBar);
        sendImage(image, e);
    }

    private void doList(MessageReceivedEvent e, long ogDiscordID, long secondDiscordId, ResultWrapper<UserArtistComparison> resultWrapper, T params) {
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
                .setFooter(String.format("Both user have %d common %s", resultWrapper.getRows(), getEntity(params)), null)
                .setThumbnail(uinfo1.getUrlImage());
        e.getChannel().sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(strings, message1, embedBuilder));
    }

    @Override
    public String getName() {
        return "Taste";
    }

}
