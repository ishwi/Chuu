package core.commands.utils;

import core.otherlisteners.Reactions;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.List;

public class ButtonUtils {
    public static final String FRIEND_REQUEST_REJECT = "FRIEND_REQUEST_REJECT";
    public static final String FRIEND_REQUEST_ACCEPT = "FRIEND_REQUEST_ACCEPT";

    private ButtonUtils() {
    }

    public static Button danger(String text) {
        return Button.of(ButtonStyle.DANGER, Reactions.REJECT, text, Emoji.fromUnicode(Reactions.REJECT));
    }

    public static Button primary(String text) {
        return Button.of(ButtonStyle.PRIMARY, Reactions.ACCEPT, text, Emoji.fromUnicode(Reactions.ACCEPT));
    }

    public static List<Component> getPaginationButtons() {
        return List.of(getRightButton());
    }

    public static Button getLeftButton() {
        return Button.of(ButtonStyle.PRIMARY, Reactions.LEFT_ARROW, Emoji.fromUnicode(Reactions.LEFT_ARROW));
    }


    public static Button getRightButton() {
        return Button.of(ButtonStyle.PRIMARY, Reactions.RIGHT_ARROW, Emoji.fromUnicode(Reactions.RIGHT_ARROW));
    }

    public static Button declineFriendRequest(long owner) {
        return Button.of(ButtonStyle.DANGER, FRIEND_REQUEST_REJECT + ":" + owner, Emoji.fromUnicode(Reactions.REJECT)).withLabel("Decline");
    }

    public static Button acceptFriendRequest(long owner) {
        return Button.of(ButtonStyle.PRIMARY, FRIEND_REQUEST_ACCEPT + ":" + owner, Emoji.fromUnicode(Reactions.ACCEPT)).withLabel("Accept");
    }
}
