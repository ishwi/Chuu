package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.TasteRenderer;
import core.otherlisteners.Reactionary;
import core.parsers.OptionalEntity;
import core.parsers.TwoUsersParser;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.ResultWrapper;
import dao.entities.UserArtistComparison;
import dao.entities.UserInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TasteCommand extends ConcurrentCommand {
    public TasteCommand(ChuuService dao) {
        super(dao);
        parser = new TwoUsersParser(dao, new OptionalEntity("--list", "display in a list format"));
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

        String[] message = parser.parse(e);
        if (message == null)
            return;
        long ogDiscordID = Long.parseLong(message[0]);
        String ogLastFmId = message[1];
        long secondDiscordId = Long.parseLong(message[2]);
        String secondlastFmId = message[3];
        boolean isList = Boolean.parseBoolean(message[4]);
        lastfMNames = Arrays.asList(ogLastFmId, secondlastFmId);

        ResultWrapper<UserArtistComparison> resultWrapper = getService().getSimilarities(lastfMNames);
        //TODO this happens both when user is not on db and no mathching so fix pls
        if (resultWrapper.getRows() == 0) {
            sendMessageQueue(e, "You don't share any artist :(");
            return;
        }

        if (isList) {
            StringBuilder stringBuilder = new StringBuilder();
            List<String> strings = resultWrapper.getResultList().stream().map(x -> String.format(". [%s](%s) - %d vs %d plays\n",
                    x.getArtistID(),
                    CommandUtil.getLastFmArtistUrl(x.getArtistID()),
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
                    executor.execute(() -> new Reactionary<>(strings, message1, embedBuilder)));
        } else {
            java.util.List<String> users = new ArrayList<>();
            users.add(resultWrapper.getResultList().get(0).getUserA());
            users.add(resultWrapper.getResultList().get(0).getUserB());
            java.util.List<UserInfo> userInfoList = lastFM.getUserInfo(users);
            BufferedImage image = TasteRenderer.generateTasteImage(resultWrapper, userInfoList);
            sendImage(image, e);


        }
    }

    @Override
    public String getName() {
        return "Taste";
    }

}
