package core.commands.config;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.PrivacyUtils;
import core.parsers.EnumParser;
import core.parsers.Parser;
import core.parsers.params.EnumParameters;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.PrivacyMode;
import dao.exceptions.InstanceNotFoundException;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PrivacySetterCommand extends ConcurrentCommand<EnumParameters<PrivacyMode>> {
    public PrivacySetterCommand(ServiceView dao) {
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
    protected void onCommand(Context e, @NotNull EnumParameters<PrivacyMode> params) throws InstanceNotFoundException {


        PrivacyMode element = params.getElement();
        LastFMData lastFMData = db.findLastFMData(e.getAuthor().getIdLong());
        String publicStr = PrivacyUtils.getPublicString(element, lastFMData.getDiscordId(), lastFMData.getName(), new AtomicInteger(1), e, new HashSet<>()).discordName();
        if (element == PrivacyMode.DISCORD_NAME) {
            publicStr = e.getAuthor().getName();
        }
        if (lastFMData.getPrivacyMode().equals(element)) {
            sendMessageQueue(e, "You already had %s as your privacy config.%nYou are appearing as **%s** for users in other servers".formatted(element, publicStr));
            return;

        }
        db.setPrivacyMode(e.getAuthor().getIdLong(), element);
        sendMessageQueue(e, "Changed your privacy setting from %s to %s.%nNow you will appear as **%s** for users in other servers".formatted(lastFMData.getPrivacyMode(), element, publicStr));


    }

    @Override
    public String getUsageInstructions() {
        String usageInstructions = super.getUsageInstructions();
        return (usageInstructions + "\n Only if you set your mode to `Tag` or `last-name` you will be available for others in global affinity, global rec and global matching");
    }
}
