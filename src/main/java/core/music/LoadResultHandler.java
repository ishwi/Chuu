package core.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.Chuu;
import core.music.utils.TrackContext;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LoadResultHandler implements AudioLoadResultHandler {


    private final String query;
    private String identifier;
    private final MusicManager musicManager;
    private final TrackContext trackContext;
    private final MessageReceivedEvent e;
    private final Boolean isNext;
    private final String footnote;

    private Object settings;
    private final boolean premiumGuild = false;
    private int retryCount = 0;
    private final Set<AudioItem> audioItemSet = new HashSet<>();

    public LoadResultHandler(String query, MessageReceivedEvent e, MusicManager musicManager, TrackContext trackContext, Boolean isNext, String footnote) {

        this.query = query;
        this.e = e;
        this.musicManager = musicManager;
        this.trackContext = trackContext;
        this.isNext = isNext;
        this.footnote = footnote;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        cache(track);

        if (!checkVoiceState() || !checkTrack(track, false)) {
            return;
        }

        var isImmediatePlay = musicManager.getPlayer().getPlayingTrack() == null && musicManager.getQueue().isEmpty();
        track.setUserData(trackContext);
        musicManager.enqueue(track, isNext);

        if (!isImmediatePlay) {
            e.getChannel().sendMessage(" queued").queue();
        }
    }


    public void playlistLoaded(AudioPlaylist playlist) {
        cache(playlist);

        if (playlist.isSearchResult()) {
            trackLoaded(playlist.getTracks().get(0));
            return;
        }

        if (!checkVoiceState()) {
            return;
        }

        var queueLimit = queueLimit();
        var pendingEnqueue = playlist.getTracks().stream().filter(it -> {
            return checkTrack(it, true);
        }).collect(Collectors.toList());
        var added = 0;
        for (AudioTrack track : pendingEnqueue) {

            if (musicManager.getQueue().size() + 1 >= queueLimit) {
                break;
            }
            track.setUserData(trackContext);
            musicManager.enqueue(track, isNext);
            added++;
        }

        var ignored = pendingEnqueue.size() - added;
        e.getChannel().sendMessage("Queued").queue();
    }

    public void loadFailed(Exception exception) {
        if (musicManager.isIdle()) {
            musicManager.destroy();
        }
        e.getChannel().sendMessage(" UNABLE TO LOAD " + exception.getMessage()).queue();
    }


    public void noMatches() {

        if (retryCount < MAX_LOAD_RETRIES && identifier != null) {
            retryCount++;
            Chuu.playerManager.loadItemOrdered(e.getGuild().getIdLong(), identifier, this);
            return;
        }

        if (musicManager.isIdle()) {
            musicManager.destroy();
        }
        e.getChannel().sendMessage("NOTHING FOUND").queue();
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        e.getChannel().sendMessage(exception.getMessage()).queue();
    }

    private boolean checkVoiceState() {
        AudioManager manager = e.getGuild().getAudioManager();
        if (manager == null) {
            return false;
        }
        if (manager.getConnectedChannel() == null) {

            if (e.getMember().getVoiceState().getChannel() == null) {
                e.getChannel().sendMessage("You left the voice channel before the track was loaded.").queue();


                if (musicManager.isIdle()) {
                    musicManager.destroy();
                }

                return false;
            }

            return musicManager.openAudioConnection(e.getMember().getVoiceState().getChannel(), e);
        }

        return true;
    }

    private boolean checkTrack(AudioTrack track, Boolean isPlaylist) {
        if (!isPlaylist) {
            var queueLimit = queueLimit();
            String queueLimitDisplay;
            if (queueLimit == Integer.MAX_VALUE) {
                queueLimitDisplay = "unlimited";
            } else {
                queueLimitDisplay = String.valueOf(queueLimit);
            }


            if (musicManager.getQueue().size() + 1 >= queueLimit) {
                if (!isPlaylist) {
                    e.getChannel().sendMessage("The queue can not exceed  songs." + queueLimitDisplay).queue();
                }
                return false;
            }
        }

        if (!track.getInfo().isStream) {
            var invalidDuration = false;

        }
        return true;

    }

    private int queueLimit() {
        return 500;
    }

    public void cache(AudioItem item) {
        if (identifier != null) {
            audioItemSet.add(item);
        }
    }

    private static final int MAX_LOAD_RETRIES = 2;

    public static void loadItem(String query, MessageReceivedEvent e, MusicManager musicManager, TrackContext trackContext, Boolean isNext, String footnote) {
        var resultHandler = new LoadResultHandler(query, e, musicManager, trackContext, isNext, footnote);

        Chuu.playerManager.loadItemOrdered(e.getGuild().getIdLong(), query, resultHandler);
    }
}

