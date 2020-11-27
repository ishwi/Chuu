package core.commands;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.OptionableParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import dao.entities.UsersWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayingCommand extends ConcurrentCommand<CommandParameters> {

    private final LoadingCache<Long, LocalDateTime> controlAccess;

    public PlayingCommand(ChuuService dao) {
        super(dao);

        this.respondInPrivate = false;
        controlAccess = CacheBuilder.newBuilder().concurrencyLevel(2).expireAfterWrite(12, TimeUnit.HOURS).build(
                new CacheLoader<>() {
                    public LocalDateTime load(@org.jetbrains.annotations.NotNull Long guild) {
                        return LocalDateTime.now().plus(12, ChronoUnit.HOURS);
                    }
                });
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.NOW_PLAYING;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new OptionableParser(new OptionalEntity("recent", "show last song from ALL users"));
    }


    @Override
    public String getDescription() {
        return ("Returns lists of all people that are playing music right now");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("playing");
    }

    @Override
    public void onCommand(MessageReceivedEvent e, @javax.validation.constraints.NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {


        boolean showFresh = !params.hasOptional("recent");

        List<UsersWrapper> list = getService().getAll(e.getGuild().getIdLong());
        if (list.size() > 50) {
            LocalDateTime ifPresent = controlAccess.getIfPresent(e.getGuild().getIdLong());
            if (ifPresent != null) {
                LocalDateTime now = LocalDateTime.now();
                long hours = now.until(ifPresent, ChronoUnit.HOURS);
                now = now.plus(hours, ChronoUnit.HOURS);
                long minutes = now.until(ifPresent, ChronoUnit.MINUTES);

                sendMessageQueue(e, "This server has too many user, so the playing command can only be executed twice per day (usable in " + hours + " hours and " + minutes + " minutes)");
                return;
            } else {
                controlAccess.refresh(e.getGuild().getIdLong());
            }
        }
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(e.getGuild().getIconUrl())
                .setTitle(
                        (showFresh ? "What is being played now in " : "What was being played in ")
                                + CommandUtil.cleanMarkdownCharacter(e.getGuild().getName()));

        List<String> result = list.parallelStream().map(u ->
        {
            Optional<NowPlayingArtist> opt;
            try {
                opt = Optional.of(lastFM.getNowPlayingInfo(u.getLastFMName()));
            } catch (Exception ex) {
                opt = Optional.empty();
            }
            return Map.entry(u, opt);
        }).filter(x -> {
            Optional<NowPlayingArtist> value = x.getValue();
            return value.isPresent() && !(showFresh && !value.get().isNowPlaying());
        }).map(x -> {
                    UsersWrapper usersWrapper = x.getKey();
                    assert x.getValue().isPresent();
                    NowPlayingArtist value = x.getValue().get(); //Checked previous filter
                    String username = getUserString(e, usersWrapper.getDiscordID(), usersWrapper.getLastFMName());
                    String started = !showFresh && value.isNowPlaying() ? "#" : "+";
                    return started + " [" +
                            username + "](" +
                            CommandUtil.getLastFmUser(usersWrapper.getLastFMName()) +
                            "): " +
                            CommandUtil.cleanMarkdownCharacter(value.getSongName() +
                                    " - " + value.getArtistName() +
                                    " | " + value.getAlbumName() + "\n");
                }
        ).collect(Collectors.toList());
        Collections.shuffle(result, CommandUtil.rand);
        if (result.isEmpty()) {
            sendMessageQueue(e, "None is listening to music at the moment UwU");
            return;
        }
        StringBuilder a = new StringBuilder();
        int count = 0;
        for (String string : result) {
            count++;
            if ((a.length() > 1500) || (count == 30)) {
                break;
            }
            a.append(string);
        }
        int pageSize = count;
        int pages = (int) Math.ceil(result.size() / (float) count);
        if (pages != 1) {
            a.append("\n1").append("/").append(pages);
        }
        embedBuilder.setDescription(a);
        e.getChannel().sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(result, message1, pageSize, embedBuilder, false, true));

    }

    @Override
    public String getName() {
        return "Playing";
    }


}
