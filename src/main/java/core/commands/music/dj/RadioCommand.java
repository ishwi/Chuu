package core.commands.music.dj;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.MusicCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.GenreDisambiguator;
import core.commands.utils.PrivacyUtils;
import core.music.MusicManager;
import core.music.radio.*;
import core.parsers.EnumParser;
import core.parsers.Parser;
import core.parsers.params.EnumParameters;
import dao.ChuuService;
import dao.everynoise.NoiseGenre;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.commons.text.WordUtils;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RadioCommand extends MusicCommand<EnumParameters<Station>> {
    public RadioCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = false;
        requireManager = false;
        requireVoiceState = true;
    }


    @Override
    public Parser<EnumParameters<Station>> initParser() {
        return new EnumParser<>(Station.class, true, true, true);
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
    protected void onCommand(Context e, @NotNull EnumParameters<Station> params) {
        Station element = params.getElement();
        String input = params.getParams();
        if (element == null) {
            EnumSet<Station> stations = EnumSet.allOf(Station.class);

            boolean falltrough = false;
            if (input != null) {
                String finalInput = input.split("\\s+")[0];
                Optional<Station> found = stations.stream().filter(z -> z.getAliases().stream().anyMatch(w -> w.equalsIgnoreCase(finalInput))).findFirst();
                falltrough = found.isEmpty();
                if (!falltrough) {
                    element = found.get();
                    input = input.replaceFirst(finalInput, "").strip();
                }
            }

            if (input == null || falltrough) {

                String str = stations.stream().filter(Station::isActive).map(z -> "__**%s**__  \u279C %s".formatted(WordUtils.capitalizeFully(z.name()), z.getDescription())).collect(Collectors.joining("\n"));
                var eb = new ChuuEmbedBuilder(e).setDescription(str)
                        .setAuthor(e.getJDA().getSelfUser().getName() + "'s Radio stations", PrivacyUtils.getLastFmUser(Chuu.DEFAULT_LASTFM_ID), e.getJDA().getSelfUser().getAvatarUrl())
                        .setFooter("Example: " + e.getPrefix() + "radio random");
                if (falltrough) {
                    eb.setTitle("Didn't find any station with that name");
                }
                e.sendMessage(eb.build()).queue();
                return;
            }
        }
        MusicManager musicManager = Chuu.playerRegistry.get(e.getGuild());

        RadioTrackContext a = switch (element) {
            case RANDOM -> new RandomRadioTrackContext(e.getAuthor().getIdLong(), e.getChannel().getIdLong(), new RandomRadio(params.hasOptional("server") ? e.getGuild().getIdLong() : -1, params.hasOptional("server")), -1, null);
            case RELEASES, GENRE -> {
                if (input == null) {
                    parser.sendError("Pls Introduce the name of a genre to search for.", e);
                    yield null;
                }
                Function<NoiseGenre, RadioTrackContext> factory;
                if (element == Station.RELEASES) {
                    factory = (s) -> new ReleaseRadioTrackContext(e.getAuthor().getIdLong(), e.getChannel().getIdLong(), new ReleaseRadio(s.name(), s.uri()), null, null, null, s.name(), s.uri());
                } else {
                    factory = (s) -> new GenreRadioTrackContext(e.getAuthor().getIdLong(), e.getChannel().getIdLong(), new GenreRadio(s.name(), s.uri()), s.name(), s.uri(), -1, 1);
                }
                new GenreDisambiguator(db).disambiguate(e, input, z -> new Params(musicManager, factory.apply(z)), this::buildEmbed);
                yield null;
            }
            case CURATED -> null;
        };
        if (a == null) {
            return;
        }
        buildEmbed(e, null, new Params(musicManager, a), input);


    }

    private void buildEmbed(Context e, @Nullable Message message, Params params, String input) {
        MusicManager musicManager = params.manager;
        musicManager.setRadio(params.context);


        MessageEmbed radio = new ChuuEmbedBuilder(e)
                .setTitle("Radio")
                .setDescription("Radio set to `%s`. The radio will be played when there's nothing else queued".formatted(params.context.getSource().getName())).build();

        RestAction<Message> messageRestAction;
        if (message == null) {
            messageRestAction = e.sendMessage(radio);
        } else {
            messageRestAction = message.editMessage(radio);
        }
        messageRestAction.queue();
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

    record Params(MusicManager manager, RadioTrackContext context) {
    }

}
