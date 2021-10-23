package core.commands.utils;

import core.otherlisteners.Reactions;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.Component;

import java.util.List;

public class ButtonUtils {
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
}
