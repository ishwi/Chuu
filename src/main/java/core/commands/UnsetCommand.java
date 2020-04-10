package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Confirmator;
import core.parsers.NoOpParser;
import dao.ChuuService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class UnsetCommand extends ConcurrentCommand {
    public UnsetCommand(ChuuService dao) {
        super(dao);
        this.parser = new NoOpParser();
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        // Check if it exists
        getService().findLastFMData(idLong);
        String userString = getUserString(e, idLong);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("User Deletion Confirmation")
                .setDescription(String.format("%s, are you sure you want to delete all your info from the bot?", userString));
        new MessageBuilder(embedBuilder.build()).sendTo(e.getChannel())
                .queue(message -> new Confirmator(embedBuilder, message, idLong,
                        () -> getService().removeUserCompletely(idLong), () -> {
                }
                        , who -> who.clear().setTitle(String.format("%s was removed completely from the bot", userString)),
                        who -> who.clear().setTitle(String.format("Didn't do anything with user %s", userString))));
    }
}
