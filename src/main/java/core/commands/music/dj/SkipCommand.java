package core.commands.music.dj;

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

public class SkipCommand extends MusicCommand<CommandParameters> {
    public SkipCommand(ChuuService dao) {
        super(dao);
        sameChannel = true;
        requirePlayingTrack = true;
        requirePlayer = true;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Skips the current track";
    }

    @Override
    public List<String> getAliases() {
        return List.of("sk", "skip");
    }

    @Override
    public String getName() {
        return "Skip";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        MusicManager manager = getManager(e);
        manager.nextTrack();
        sendMessageQueue(e, "Skipped current song");
    }
}
