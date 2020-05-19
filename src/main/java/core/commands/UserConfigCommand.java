package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.UserConfigParser;
import core.parsers.params.UserConfigParameters;
import core.parsers.params.UserConfigType;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class UserConfigCommand extends ConcurrentCommand<UserConfigParameters> {
    public UserConfigCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<UserConfigParameters> getParser() {
        return new UserConfigParser(getService());
    }

    @Override
    public String getDescription() {
        return "Configuration per user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("configuration", "config");
    }

    @Override
    public String getName() {
        return "User Configuration";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        UserConfigParameters parse = this.parser.parse(e);
        if (parse == null) {
            return;
        }
        UserConfigType config = parse.getConfig();
        String value = parse.getValue();
        switch (config) {
            case PRIVATE_UPDATE:
                boolean b = Boolean.parseBoolean(value);
                getService().setPrivateUpdate(e.getAuthor().getIdLong(), b);
                if (b) {
                    sendMessageQueue(e, "Successfully made private the update for user " + getUserString(e, e.getAuthor().getIdLong()));
                } else {
                    sendMessageQueue(e, "Successfully made non private the update for user " + getUserString(e, e.getAuthor().getIdLong()));
                }
                break;
        }
    }
}
