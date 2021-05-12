package core.commands.stats;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayingCommand extends ConcurrentCommand<CommandParameters> {

    private final LoadingCache<Long, LocalDateTime> controlAccess;
    private final LoadingCache<Long, LocalDateTime> serverControlAccess;

    public PlayingCommand(ChuuService dao) {
        super(dao);

        this.respondInPrivate = false;
        controlAccess = CacheBuilder.newBuilder().concurrencyLevel(2).expireAfterWrite(12, TimeUnit.HOURS).build(
                new CacheLoader<>() {
                    public LocalDateTime load(@org.jetbrains.annotations.NotNull Long guild) {
                        return LocalDateTime.now().plus(12, ChronoUnit.HOURS);
                    }
                });
        serverControlAccess = CacheBuilder.newBuilder().concurrencyLevel(2).expireAfterWrite(1, TimeUnit.MINUTES).build(
                new CacheLoader<>() {
                    public LocalDateTime load(@org.jetbrains.annotations.NotNull Long guild) {
                        return LocalDateTime.now().plus(1, ChronoUnit.MINUTES);
                    }
                });
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.NOW_PLAYING;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser(new OptionalEntity("recent", "show last song from ALL users"));
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
    protected void onCommand(Context e, @NotNull CommandParameters params) {


        boolean showFresh = !params.hasOptional("recent");

        List<LastFMData> users = db.getAllData(e.getGuild().getIdLong());
        long unaothorized = users.stream().filter(t -> t.getSession() == null).count();
        LocalDateTime cooldown;
        if (unaothorized > 66 || (users.size() - unaothorized > 150)) {
            LocalDateTime ifPresent = controlAccess.getIfPresent(e.getGuild().getIdLong());
            if (ifPresent != null) {
                format(e, ifPresent, "This server has too many users, so the playing command can only be executed twice per day ");
                return;
            }
            controlAccess.refresh(e.getGuild().getIdLong());
        } else if ((cooldown = serverControlAccess.getIfPresent(e.getGuild().getIdLong())) != null) {
            format(e, cooldown, "This command has a 1 min cooldown between uses.");
            return;
        } else {
            serverControlAccess.refresh(e.getGuild().getIdLong());
        }

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder().setColor(ColorService.computeColor(e))
                .setThumbnail(e.getGuild().getIconUrl())
                .setTitle(
                        (showFresh ? "What is being played now in " : "What was being played in ")
                        + CommandUtil.cleanMarkdownCharacter(e.getGuild().getName()));

        List<String> result = users.parallelStream().map(u ->
        {
            Optional<NowPlayingArtist> opt;
            try {
                opt = Optional.of(lastFM.getNowPlayingInfo(u));
            } catch (Exception ex) {
                opt = Optional.empty();
            }
            return Map.entry(u, opt);
        }).filter(x -> {
            Optional<NowPlayingArtist> value = x.getValue();
            return value.isPresent() && !(showFresh && !value.get().current());
        }).map(x -> {
                    LastFMData usersWrapper = x.getKey();
                    assert x.getValue().isPresent();
                    NowPlayingArtist value = x.getValue().get(); //Checked previous filter
                    String username = getUserString(e, usersWrapper.getDiscordId(), usersWrapper.getName());
                    String started = !showFresh && value.current() ? "#" : "+";
                    return started + " [" +
                           username + "](" +
                           CommandUtil.getLastFmUser(usersWrapper.getName()) +
                           "): " +
                           CommandUtil.cleanMarkdownCharacter(value.songName() +
                                                              " - " + value.artistName() +
                                                              " | " + value.albumName() + "\n");
                }
        ).collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(result, CommandUtil.rand);
        if (result.isEmpty()) {
            sendMessageQueue(e, "No one is listening to music at the moment UwU");
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
        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(result, message1, pageSize, embedBuilder, false, true));

    }

    private void format(Context e, LocalDateTime cooldown, String s) {
        LocalDateTime now = LocalDateTime.now();
        long hours = now.until(cooldown, ChronoUnit.HOURS);
        now = now.plus(hours, ChronoUnit.HOURS);
        long minutes = now.until(cooldown, ChronoUnit.MINUTES);
        String hstr = hours <= 0 ? "" : "%d %s and ".formatted(hours, CommandUtil.singlePlural(hours, "hour", "hours"));
        String mStr;
        if (minutes <= 0 && hours <= 0) {
            long seconds = now.until(cooldown, ChronoUnit.SECONDS);
            mStr = "%d %s".formatted(seconds, CommandUtil.singlePlural(seconds, "second", "seconds"));
        } else {
            mStr = "%d %s".formatted(minutes, CommandUtil.singlePlural(minutes, "minute", "minutes"));
        }
        sendMessageQueue(e, "%s (usable in %s%s)".formatted(s, hstr, mStr));
    }

    @Override
    public String getName() {
        return "Playing";
    }


}
