package core.commands.config;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.EmojeParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.EmotiParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static core.commands.utils.ReactValidation.validateEmotes;

public class NpReactionsCommand extends ConcurrentCommand<EmotiParameters> {
    public NpReactionsCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<EmotiParameters> initParser() {
        EmojeParser emojeParser = new EmojeParser();
        emojeParser.addOptional(new OptionalEntity("check", "check the current reactions"));
        return emojeParser;
    }

    @Override
    public String getDescription() {
        return "Sets reactions for your nps";
    }

    @Override
    public List<String> getAliases() {
        return List.of("reactions", "reacts", "react");
    }

    @Override
    public String getName() {
        return "Np reactions";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull EmotiParameters params) {
        if (params.hasOptional("check")) {
            List<String> serverReactions = db.getUserReacts(e.getAuthor().getIdLong());
            if (serverReactions.isEmpty()) {
                sendMessageQueue(e, "Don't have any reaction set");
                return;
            }
            List<String> displaying = serverReactions.stream().map(EmotiParameters.Emotable::toDisplay).collect(Collectors.toList());
            sendMessageQueue(e, "Have these reactions: " + String.join(" ", displaying));
            return;

        }

        if (params.getEmotis().isEmpty()) {
            sendMessageQueue(e, "Clearing your reactions");
            db.clearUserReacts(e.getAuthor().getIdLong());
            return;
        }

        AtomicLong messageId = new AtomicLong();
        if (e.isFromGuild() && !e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_ADD_REACTION)) {
            sendMessageQueue(e, "Don't have permissions to add reactions in this server!");
            return;
        }

        if (params.hasEmotes()) {
            List<String> content = validateEmotes(e, params);
            if (content.isEmpty()) {
                sendMessageQueue(e, "Didn't add any reaction.");
            } else {
                db.insertUserReactions(e.getAuthor().getIdLong(), content);
                sendMessageQueue(e, "Will set the following reactions: " + content.stream().map(EmotiParameters.Emotable::toDisplay).collect(Collectors.joining(" ")));
            }
        } else {
            if (params.hasEmojis()) {
                String collect = params.getEmojis().stream().map(EmotiParameters.Emotable::toDisplay).collect(Collectors.joining(" "));
                sendMessageQueue(e, "Will set the following reactions: " + String.join(" ", collect));
                db.insertUserReactions(e.getAuthor().getIdLong(), params.getEmojis());
            }
        }
    }
}
