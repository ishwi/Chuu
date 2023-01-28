package core.otherlisteners;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public interface ConstantListener extends EventListener {

    @Override
    default void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ButtonInteractionEvent e) {
            onButtonClicked(e);
        }
    }

    boolean isValid(ButtonInteractionEvent e);

    void handleClick(ButtonInteractionEvent e);

    default void onButtonClicked(ButtonInteractionEvent e) {
        if (e.getUser().isBot()) {
            return;
        }
        if (!isValid(e)) {
            return;
        }
        handleClick(e);
    }
}
