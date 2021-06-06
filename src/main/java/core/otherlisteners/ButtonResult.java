package core.otherlisteners;

import core.otherlisteners.util.Response;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.List;

public interface ButtonResult extends ReactionaryResult {

    ButtonResult defaultResponse = () -> Response.def;

    Result newResult();

    record Result(boolean newElement, List<ActionRow> newRows) {
    }
}
