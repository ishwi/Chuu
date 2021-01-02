package core.commands.music.dj;

import core.Chuu;
import core.commands.abstracts.MusicCommand;
import core.exceptions.LastFmException;
import core.music.MusicManager;
import core.parsers.ChannelParser;
import core.parsers.Parser;
import core.parsers.params.ChannelParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

public class SummonCommand extends MusicCommand<ChannelParameters> {
    public SummonCommand(ChuuService dao) {
        super(dao);
        requireManager = false;
        requireVoiceState = true;
    }

    @Override
    public Parser<ChannelParameters> initParser() {
        return new ChannelParser();
    }

    @Override
    public String getDescription() {
        return "Makes the bot join an specified voice channel";
    }

    @Override
    public List<String> getAliases() {
        return List.of("join", "connect", "summon");
    }

    @Override
    public String getName() {
        return "Summon";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull ChannelParameters params) throws LastFmException, InstanceNotFoundException {
        MusicManager manager = Chuu.playerRegistry.get(e.getGuild());
        GuildChannel targetChannel = params.getGuildChannel();
        if (targetChannel instanceof VoiceChannel voiceChannel) {
            if (e.getGuild().getAudioManager().getConnectedChannel() != null) {
                manager.moveAudioConnection(voiceChannel);
            } else {
                manager.openAudioConnection(voiceChannel, e);
            }
        } else {
            parser.sendError("The channel specified is not a Voice channel", e);
        }
    }
}
