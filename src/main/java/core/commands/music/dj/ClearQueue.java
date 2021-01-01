package core.commands.music.dj;

import core.Chuu;
import core.commands.abstracts.MusicCommand;
import core.exceptions.LastFmException;
import core.music.MusicManager;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Queue;

public class ClearQueue extends MusicCommand<CommandParameters> {
    public ClearQueue(ChuuService dao) {
        super(dao);
        sameChannel = true;
    }


    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Clears the queue";
    }

    @Override
    public List<String> getAliases() {
        return List.of("clear", "cq");
    }

    @Override
    public String getName() {
        return "Clear queue";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        MusicManager manager = Chuu.playerRegistry.get(e.getGuild());
        Queue<String> queue = manager.getQueue();
        if (queue.isEmpty()) {
            sendMessageQueue(e, "There is nothing to clear");
        }
        queue.clear();
        sendMessageQueue(e, "Queue was cleared");
    }
}
