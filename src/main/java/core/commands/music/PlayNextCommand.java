package core.commands.music;

import core.Chuu;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

public class PlayNextCommand extends ConcurrentCommand<CommandParameters> {


    public PlayNextCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.ARTIST_IMAGES;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "pn";
    }

    @Override
    public List<String> getAliases() {
        return List.of("pn");
    }

    @Override
    public String getName() {
        return "pn";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        var botChannel = e.getMember().getVoiceState().getChannel();
        var userChannel = e.getMember().getVoiceState().getChannel();
        if (userChannel == null) {
            sendMessageQueue(e, "You're not in a voice channel.");
        }

        if (botChannel != null && botChannel != userChannel) {
            sendMessageQueue(e, "The bot is already playing music in another channel.");
        }

        var attachment = e.getMessage().getAttachments().stream().findFirst().orElse(null);
        boolean hasManager = Chuu.playerRegistry.contains(e.getGuild());
        var newManager = Chuu.playerRegistry.get(e.getGuild());

        String[] original = commandArgs(e.getMessage());
        MusicCommand.play(e, newManager, String.join(" ", Arrays.copyOfRange(original, 1, original.length)), false, true);

    }

}

