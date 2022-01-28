package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.WordParameter;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
    public WordParameter parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) {
        CommandInteraction e1 = ctx.e();
        OptionMapping option = e1.getOption("lfm-user");
        if (option == null) {
            sendError(getErrorMessage(0), ctx);
            return null;
        }
        String lfmName = option.getAsString();
        return new WordParameter(ctx, lfmName);
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
