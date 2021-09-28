package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.CharacterParameters;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrefixParser extends Parser<CharacterParameters> {
    public static final List<Character> acceptecChars = (Arrays
            .asList('!', '@', '#', '$', '%', '^', '_', '.', ',', ';', ':', '~', '>', '<', '-', '?', '|'));


    @Override
    public CharacterParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        return new CharacterParameters(ctx, ctx.e().getOption("prefix").getAsString().charAt(0));
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(0, "Pls only introduce the prefix you want the bot to use");
        StringBuilder s = new StringBuilder();
        acceptecChars.forEach(s::append);
        errorMessages.put(1, "The prefix must be one of the following: " + s);
        errorMessages.put(2, "Insufficient Permissions, only a mod can");

    }

    @Override
    protected CharacterParameters parseLogic(Context e, String[] words) {
        if (CommandUtil.notEnoughPerms(e)) {
            sendError(CommandUtil.notEnoughPermsTemplate() + "change the prefix", e);
            return null;
        }
        if (words.length != 1) {
            sendError(this.getErrorMessage(0), e);
            return null;
        }

        String expectedChar = words[0];
        if (expectedChar.length() != 1 || !acceptecChars.contains(expectedChar.charAt(0))) {
            sendError(this.getErrorMessage(1), e);
            return null;
        }
        return new CharacterParameters(e, expectedChar.charAt(0));
    }

    @Override
    public List<Explanation> getUsages() {

        OptionData optionData = new OptionData(OptionType.STRING, "prefix", "The prefix to use").setRequired(true);
        acceptecChars.forEach(z -> optionData.addChoice(String.valueOf(z), String.valueOf(z)));

        return Collections.singletonList(() -> new ExplanationLine("[!@#$%^_.,;:~><-?|]", "Only one of the characters listed", optionData));
    }

}
