package core.otherlisteners;

import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.List;

public interface ButtonResult extends ReactionaryResult {

    Result newResult();

    record Result(boolean newElement, List<ActionRow> newRows) {
    }
}
