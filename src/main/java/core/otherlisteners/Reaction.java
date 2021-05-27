package core.otherlisteners;

import net.dv8tion.jda.api.events.Event;

public interface Reaction<Y, T extends Event, Z extends ReactionaryResult> {

    Z release(Y item, T event);
}
