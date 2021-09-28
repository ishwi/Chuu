package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.WordParameter;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

public class SetParser extends Parser<WordParameter> {
    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(0, "You need to introduce a valid last.fm account!");
    }


    @Override
    public WordParameter parseSlashLogic(ContextSlashReceived e) {
        SlashCommandEvent e1 = e.e();
        String lfmName = e1.getOption("lfm-user").getAsString();
        if (lfmName == null) {
            sendError(getErrorMessage(0), e);
            return null;
        }
        return new WordParameter(e, lfmName);
    }

    @Override
    public WordParameter parseLogic(Context e, String[] subMessage) {
        if (subMessage.length != 1) {
            sendError(getErrorMessage(0), e);
            return null;
        }
        return new WordParameter(e, subMessage[0]);
    }

    @Override
    public List<Explanation> getUsages() {
        OptionData optionData = new OptionData(OptionType.STRING, "lfm-user", "The last.fm username you wish to link with your discord account");
        optionData.setRequired(true);
        return Collections.singletonList(() -> new ExplanationLine("Last.fm username", "The last.fm username you wish to link with your discord account", optionData));
    }


}
