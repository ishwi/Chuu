package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.TasteRenderer;
import core.parsers.TwoUsersParser;
import dao.ChuuService;
import dao.entities.ResultWrapper;
import dao.entities.UserArtistComparison;
import dao.entities.UserInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TasteCommand extends ConcurrentCommand {
    public TasteCommand(ChuuService dao) {
        super(dao);
        parser = new TwoUsersParser(dao);
    }

    @Override
    public String getDescription() {
        return "Compare Your musical taste with another user";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("taste");
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        List<String> lastfMNames;

        String[] returned = parser.parse(e);
        if (returned == null)
            return;
        lastfMNames = Arrays.asList(returned);

        ResultWrapper<UserArtistComparison> resultWrapper;
        resultWrapper = getService().getSimilarities(lastfMNames);
        //TODO this happens both when user is not on db and no mathching so fix pls
        if (resultWrapper.getRows() == 0) {
            sendMessageQueue(e, "You don't share any artist :(");
            return;
        }
        java.util.List<String> users = new ArrayList<>();
        users.add(resultWrapper.getResultList().get(0).getUserA());
        users.add(resultWrapper.getResultList().get(0).getUserB());
        java.util.List<UserInfo> userInfoList = lastFM.getUserInfo(users);
        BufferedImage image = TasteRenderer.generateTasteImage(resultWrapper, userInfoList);
        sendImage(image, e);


    }

    @Override
    public String getName() {
        return "Taste";
    }

}
