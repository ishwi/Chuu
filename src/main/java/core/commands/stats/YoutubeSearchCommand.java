package core.commands.stats;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.music.utils.YoutubeSearchManagerSingleton;
import core.parsers.Parser;
import core.parsers.UsernameAndNpQueryParser;
import core.parsers.params.ExtraParameters;
import core.parsers.params.WordParameter;
import dao.ServiceView;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class YoutubeSearchCommand extends ConcurrentCommand<ExtraParameters<WordParameter, User>> {
    public static boolean ONLY_YT = false;
    private final YoutubeAudioSourceManager audioSourceManager;

    public YoutubeSearchCommand(ServiceView dao) {
        super(dao);
        audioSourceManager = YoutubeSearchManagerSingleton.getInstance();

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.NOW_PLAYING;
    }

    @Override
    public Parser<ExtraParameters<WordParameter, User>> initParser() {
        return new UsernameAndNpQueryParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Searches in Youtube inputted query or now playing song";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("yt", "npyt", "youtube", "you");
    }

    @Override
    protected void onCommand(Context e, @Nonnull ExtraParameters<WordParameter, User> params) {


        String query = params.getInnerParams().getWord();
        AudioItem audioItem = audioSourceManager.loadItem(null, new AudioReference("ytsearch: " + query, null));
        String s = null;
        if (audioItem instanceof BasicAudioPlaylist playlist) {
            s = playlist.getTracks().stream().findFirst().map(t -> t.getInfo().uri).orElse(null);
        }
        s = s == null || s.isBlank() ? String.format("Couldn't find \"%s\" on youtube", CommandUtil.escapeMarkdown(query)) : s;
        sendMessageQueue(e, s);

    }

    @Override
    public String getName() {
        return "Youtube Search";
    }
}
