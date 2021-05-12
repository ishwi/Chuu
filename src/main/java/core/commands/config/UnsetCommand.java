package core.commands.config;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.otherlisteners.Confirmator;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class UnsetCommand extends ConcurrentCommand<CommandParameters> {
    public UnsetCommand(ChuuService dao) {
        super(dao);
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
    protected void onCommand(Context e, @NotNull CommandParameters params) throws InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        // Check if it exists
        db.findLastFMData(idLong);
        String userString = getUserString(e, idLong);

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder()
                .setTitle("User Deletion Confirmation")
                .setDescription(String.format("%s, are you sure you want to delete all your info from the bot?", userString));
        e.sendMessage(embedBuilder.build())
                .queue(message -> new Confirmator(embedBuilder, message, idLong,
                        () -> db.removeUserCompletely(idLong), () -> {
                }
                        , who -> who.clear().setTitle(String.format("%s was removed completely from the bot", userString)),
                        who -> who.clear().setTitle(String.format("Didn't do anything with user %s", userString))));
    }
}
