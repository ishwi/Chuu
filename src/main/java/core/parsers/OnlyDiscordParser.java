package core.parsers;

import core.parsers.params.DiscordParameters;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Returns A User that might not exists on the system!!
 */
public class OnlyDiscordParser extends Parser<DiscordParameters> {

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected DiscordParameters parseLogic(MessageReceivedEvent e, String[] words) {
        ParserAux aux = new ParserAux(words);
        User oneUserPermissive = aux.getOneUserPermissive(e);
        return new DiscordParameters(e, oneUserPermissive);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *username***\n" +
               "\t If the username is not specified it defaults to author's account\n";
    }
}
