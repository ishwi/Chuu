package core.commands.config;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.*;
import core.otherlisteners.Confirmator;
import core.otherlisteners.Reactions;
import core.otherlisteners.util.ConfirmatorItem;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.List;


public class UnsetCommand extends ConcurrentCommand<CommandParameters> {
    public UnsetCommand(ServiceView dao) {
        super(dao);
        this.ephemeral = true;
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STARTING;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Removes a user completely from the bot system";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("unset");
    }

    @Override
    public String getName() {
        return "Unset";
    }

    @Override
    protected void onCommand(Context e, @Nonnull CommandParameters params) throws InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        // Check if it exists
        LastFMData data = db.findLastFMData(idLong);
        String userString = getUserString(e, idLong);
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, idLong);

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setColor(Color.RED)
                .setAuthor("User Deletion Confirmation", PrivacyUtils.getLastFmUser(data.getName()), uInfo.urlImage())
                .setFooter("Unsetting doesn't fix any issue that might exist with scrobbles and will delete all your info including pictures submitted!")
                .setDescription(String.format("%s, are you sure you want to delete all your info from the bot?", userString));

        List<ConfirmatorItem> list = List.of(
                new ConfirmatorItem(Reactions.ACCEPT, who -> who.clear().setTitle(String.format("%s was removed completely from the bot", userString)).setColor(Color.RED), (z) -> db.removeUserCompletely(idLong)),
                new ConfirmatorItem(Reactions.REJECT, who -> who.clear().setTitle(String.format("Didn't do anything with user %s", userString)).setColor(Color.GREEN), (z) -> {
                }));
        ActionRow of = ActionRow.of(
                ButtonUtils.danger("Delete account"),
                ButtonUtils.primary("Don't do anything"));

        e.sendMessage(embedBuilder.build(), of)
                .queue(message -> new Confirmator(embedBuilder, e, message, idLong, list));
    }
}
