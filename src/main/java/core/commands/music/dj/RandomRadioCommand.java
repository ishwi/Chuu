package core.commands.music.dj;

import core.Chuu;
import core.commands.abstracts.MusicCommand;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.music.MusicManager;
import core.music.radio.RadioTrackContext;
import core.music.radio.RandomRadio;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

public class RandomRadioCommand extends MusicCommand<CommandParameters> {
    public RandomRadioCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = false;
        requireManager = false;
    }


    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Plays music using random songs from the random pool";
    }

    @Override
    public List<String> getAliases() {
        return List.of("radio");
    }

    @Override
    public String getName() {
        return "Random Radio";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        MusicManager musicManager = Chuu.playerRegistry.get(e.getGuild());
        RadioTrackContext context = new RadioTrackContext(e.getAuthor().getIdLong(), e.getChannel().getIdLong(), new RandomRadio("Random Radio", params.hasOptional("server") ? e.getGuild().getIdLong() : null));
        musicManager.setRadio(context);


        e.getChannel().sendMessage(new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setTitle("Radio")
                .setDescription("Radio set to " + "`Random Radio`. The radio will be played when there's nothing else queued").build()).queue();
        if (musicManager.isIdle()) {
            musicManager.nextTrack();
        }
        if (e.getGuild().getSelfMember().getVoiceState() == null || !e.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            if (e.getMember() != null && e.getMember().getVoiceState() != null && e.getMember().getVoiceState().inVoiceChannel()) {
                VoiceChannel channel = e.getMember().getVoiceState().getChannel();

                if (e.getGuild().getAudioManager().getConnectedChannel() != null) {
                    musicManager.moveAudioConnection(channel);
                } else {
                    musicManager.openAudioConnection(channel, e);
                }
                musicManager.nextTrack();
            }
        }
    }
}
