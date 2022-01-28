package core.parsers;

import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.WordParameter;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;
import java.util.Optional;

public class MusicInputParser extends Parser<WordParameter> {
    @Override
    protected void setUpErrorMessages() {
        //
    }

    @Override
    public WordParameter parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        return new WordParameter(ctx, ctx.e().getOption("input").getAsString());
    }

    @Override
    protected WordParameter parseLogic(Context e, String[] words) throws InstanceNotFoundException, LastFmException {
        if (words.length == 0) {
            if (e instanceof ContextMessageReceived mes) {
                List<Message.Attachment> attachments = mes.e().getMessage().getAttachments();
                Optional<Message.Attachment> first = attachments.stream().findFirst();
                if (first.isPresent()) {
                    return new WordParameter(e, first.get().getUrl());
                }
            }
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
