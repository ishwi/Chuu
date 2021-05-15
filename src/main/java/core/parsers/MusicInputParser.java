package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.WordParameter;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

public class MusicInputParser extends Parser<WordParameter> {
    @Override
    protected void setUpErrorMessages() {
        //
    }

    @Override
    public WordParameter parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        return new WordParameter(ctx, ctx.e().getOption("input").getAsString());
    }

    @Override
    protected WordParameter parseLogic(Context e, String[] words) throws InstanceNotFoundException, LastFmException {
        if (words.length == 0) {
            sendError("Need a search term or a link!", e);
            return null;
        }
        return new WordParameter(e, String.join(" ", words));
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(InteractionAux.required(() -> new ExplanationLineType("input", "A search term or a link", OptionType.STRING)));
    }
}
