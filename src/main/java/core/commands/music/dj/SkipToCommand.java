package core.commands.music.dj;

import core.commands.abstracts.MusicCommand;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.music.MusicManager;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class SkipToCommand extends MusicCommand<NumberParameters<CommandParameters>> {
    public SkipToCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You need to specify the position of the track in the queue that you want to skip to.";
        return new NumberParser<>(new NoOpParser(),
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true);
    }

    @Override
    public String getDescription() {
        return "Skip the current song and plays the one at position x";
    }

    @Override
    public List<String> getAliases() {
        return List.of("skt");
    }

    @Override
    public String getName() {
        return "Skip To Position";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<CommandParameters> params) throws LastFmException, InstanceNotFoundException {
        Long toIndex = params.getExtraParam();
        MusicManager manager = getManager(e);
        if (toIndex == null || toIndex <= 0 || toIndex >= manager.getQueue().size()) {
            sendMessageQueue(e, "You need to specify the position of the track in the queue that you want to skip to.");
            return;
        }
        if (toIndex - 1 == 0) {
            sendMessageQueue(e, "Use the `" + CommandUtil.getMessagePrefix(e) + "skip` command to skip single tracks.");
            return;
        }
        for (int i = 0; i < toIndex - 1; i++) {
            manager.getQueue().remove();
        }
        manager.nextTrack();
        sendMessageQueue(e, "Skipped **" + (toIndex - 1) + "** tracks.");

    }
}
