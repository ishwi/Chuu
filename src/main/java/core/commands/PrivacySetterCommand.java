package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.EnumParameters;
import core.parsers.EnumParser;
import core.parsers.Parser;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.PrivacyMode;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class PrivacySetterCommand extends ConcurrentCommand<EnumParameters<PrivacyMode>> {
    public PrivacySetterCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<EnumParameters<PrivacyMode>> getParser() {
        return new EnumParser<>(PrivacyMode.class);
    }

    @Override
    public String getDescription() {
        return "The privacy affects how you will be shown to other people in other server. By default your name won't be shown.";
    }

    @Override
    public List<String> getAliases() {
        return List.of("privacy");
    }

    @Override
    public String getName() {
        return "Privacy Setter";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        EnumParameters<PrivacyMode> parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        PrivacyMode element = parse.getElement();
        LastFMData lastFMData = getService().findLastFMData(e.getAuthor().getIdLong());
        if (lastFMData.getPrivacyMode().equals(element)) {
            sendMessageQueue(e, "You already had " + element + " as your privacy config");
            return;

        }
        getService().setPrivacyMode(e.getAuthor().getIdLong(), element);
        sendMessageQueue(e, "Changed your privacy setting from " + lastFMData.getPrivacyMode() + " to " + element);

    }

    @Override
    public String getUsageInstructions() {
        String usageInstructions = super.getUsageInstructions();
        return (usageInstructions + "\n Only if you set your mode to `Tag` or `last-name` you will be available for others in global affinity, global rec and global matching");
    }
}
