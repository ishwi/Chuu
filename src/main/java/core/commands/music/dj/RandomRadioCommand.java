package core.commands.music.dj;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.MusicCommand;
import core.music.MusicManager;
import core.music.radio.RandomRadio;
import core.music.radio.RandomRadioTrackContext;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import dao.ChuuService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.VoiceChannel;

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
        return NoOpParser.INSTANCE;
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
    protected void onCommand(Context e, @NotNull CommandParameters params) {
        MusicManager musicManager = Chuu.playerRegistry.get(e.getGuild());
        RandomRadioTrackContext context = new RandomRadioTrackContext(e.getAuthor().getIdLong(), e.getChannel().getIdLong(), new RandomRadio("Random Radio", params.hasOptional("server") ? e.getGuild().getIdLong() : -1, params.hasOptional("server")));
        musicManager.setRadio(context);


        e.sendMessage(new EmbedBuilder().setColor(ColorService.computeColor(e))
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
