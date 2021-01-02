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

public class StopCommand extends MusicCommand<CommandParameters> {
    public StopCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Stops and clears the music player";
    }

    @Override
    public List<String> getAliases() {
        return List.of("end", "st", "fuckoff");
    }

    @Override
    public String getName() {
        return "Stop music";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        MusicManager manager = getManager(e);
        manager.setRadio(null);
        manager.getQueue().clear();
        e.getGuild().getAudioManager().closeAudioConnection();
        Chuu.playerRegistry.destroy(e.getGuild().getIdLong());
        sendMessageQueue(e, ("Playback has been completely stopped."));


    }
}
