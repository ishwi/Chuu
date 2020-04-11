package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.DiscordParameters;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class OnlyDiscordParser extends Parser<DiscordParameters> {
    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected DiscordParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        ParserAux parserAux = new ParserAux(words);
        User oneUserPermissive = parserAux.getOneUserPermissive(e);
        return new DiscordParameters(e, oneUserPermissive);
    }

    @Override
    public String getUsageLogic(String commandName) {
        //TODO
        return "";
    }
}
