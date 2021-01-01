package core.commands.music;

import core.Chuu;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.music.LoadResultHandler;
import core.music.MusicManager;
import core.music.utils.TrackContext;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class MusicCommand extends ConcurrentCommand<CommandParameters> {


    public MusicCommand(ChuuService dao) {
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
        return "play";
    }

    @Override
    public List<String> getAliases() {
        return List.of("play");
    }

    @Override
    public String getName() {
        return "Play music";
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

        prompt(e, hasManager).thenAccept(proceed -> {

            if (!proceed) {
                return;
            }

            var newManager = Chuu.playerRegistry.get(e.getGuild());
            play(e, newManager, e.getMessage().getContentRaw(), false, false);
        }).exceptionally(ex -> {
            sendMessageQueue(e, "ERROR ");
            ex.printStackTrace();
            return null;
        });
    }

    private CompletableFuture<Boolean> prompt(MessageReceivedEvent e, boolean hasManager) {
        var future = new CompletableFuture<Boolean>();
        var oldQueue = new LinkedBlockingQueue<String>();

        boolean complete = future.complete(true);
        return future;
    }

    public static void play(MessageReceivedEvent e, MusicManager manager, String uri, boolean isSearchResult, boolean isNext) {
        MusicManager musicManager = Chuu.playerRegistry.get(e.getGuild());
//        musicManager.getQueue().clear();
        String[] strings = commandArgs(e.getMessage());
        String query = String.join(" ", Arrays.copyOfRange(strings, 1, strings.length));


        if (query.contains("https://") || query.contains("http://") || query.startsWith("spotify:")) {
            if (query.startsWith("<")) {
                query = query.substring(1);
            }
            if (query.endsWith(">")) {
                query = query.substring(0, query.length() - 1);
            }
        } else if (isSearchResult) {
            query = uri;
        } else {
            query = String.format("ytsearch:%s", query.trim());
        }
        TrackContext trackContext = new TrackContext(e.getAuthor().getIdLong(), e.getChannel().getIdLong());
        LoadResultHandler.loadItem(query, e, musicManager, trackContext, isNext, "aa");
    }
}

