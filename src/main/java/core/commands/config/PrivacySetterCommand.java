package core.commands.config;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.EnumParser;
import core.parsers.Parser;
import core.parsers.params.EnumParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.PrivacyMode;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

public class PrivacySetterCommand extends ConcurrentCommand<EnumParameters<PrivacyMode>> {
    public PrivacySetterCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<EnumParameters<PrivacyMode>> initParser() {
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
    protected void onCommand(MessageReceivedEvent e, @NotNull EnumParameters<PrivacyMode> params) throws InstanceNotFoundException {


        PrivacyMode element = params.getElement();
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
