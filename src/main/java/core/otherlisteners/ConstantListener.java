package core.otherlisteners;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.annotation.Nonnull;

public interface ConstantListener extends EventListener {

    @Override
    default void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof ButtonClickEvent e) {
            onButtonClicked(e);
        }
    }

    boolean isValid(ButtonClickEvent e);

    void handleClick(ButtonClickEvent e);

    default void onButtonClicked(ButtonClickEvent e) {
        if (e.getUser().isBot()) {
            return;
        }
        if (!isValid(e)) {
            return;
        }
        handleClick(e);
    }
}
