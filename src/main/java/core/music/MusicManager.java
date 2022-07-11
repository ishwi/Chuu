/*
 * MIT License
 *
 * Copyright (c) 2020 Melms Media LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 From Octave bot https://github.com/Stardust-Discord/Octave/ Modified for integrating with JAVA and the current bot
 */
package core.music;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import core.Chuu;
import core.apis.last.entities.Scrobble;
import core.commands.Context;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandUtil;
import core.music.listeners.ScrobblerEventListener;
import core.music.radio.PlaylistRadio;
import core.music.radio.RadioTrackContext;
import core.music.sources.youtube.webscrobbler.processers.Processed;
import core.music.utils.*;
import core.services.VoiceAnnounceService;
import dao.entities.Metadata;
import dao.entities.VoiceAnnouncement;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MusicManager extends AudioEventAdapter implements AudioSendHandler {


    private final ExtendedAudioPlayerManager manager;
    private final VoiceAnnounceService voiceAnnounceService;
    private final ScrobblerEventListener listener;
    private final ScrobbleProcesser scrobbleProcesser;
    private final long guildId;
    private final AudioPlayer player;
    private final Deque<String> queue = new ArrayDeque<>();
    private final long lastVoteTime = 0L;
    private final boolean isVotingToSkip = false;
    private final boolean isVotingToPlay = false;
    private final long lastPlayVoteTime = 0L;
    // *----------- AudioSendHandler -----------*
    private final ByteBuffer frameBuffer = ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize());
    private final MutableAudioFrame lastFrame = genMut();
    private List<Long> breakpoints;
    private RepeatOption repeatOption = RepeatOption.NONE;
    private RadioTrackContext radio = null;
    private Scrobble scrobble;
    private Scrobble lastScrobble;
    private String dbAnnouncementChannel;
    private TextChannel announcementChannel;
    private Guild guild;
    // Playback/Music related.
    private AudioTrack lastTrack;
    private AudioTrack currentTrack;
    private long lastTimeAnnounced = 0L;
    private long lastErrorAnnounced = 0L;
    private long lastPlayedAt = 0L;
    private long loops = 0L;
    private Long channelId = null;
    // Settings/internals.
    private final Task leaveTask = new core.music.utils.Task(30, TimeUnit.SECONDS, this::destroy);

    public MusicManager(long guildId, AudioPlayer player, ExtendedAudioPlayerManager manager, VoiceAnnounceService voiceAnnounceService) {
        this.guildId = guildId;
        this.player = player;
        this.manager = manager;
        this.voiceAnnounceService = voiceAnnounceService;
        this.scrobbleProcesser = Chuu.getScrobbleProcesser();
        this.player.addListener(this);
        this.player.setVolume(100);
        listener = new ScrobblerEventListener(this);
        player.addListener(listener);
    }

    @Override
    public boolean canProvide() {
        return player.provide(lastFrame);
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        ByteBuffer flip = frameBuffer.flip();
        lastFrame.setBuffer(flip);
        if (this.breakpoints != null && !this.breakpoints.isEmpty()) {
            long position = this.currentTrack.getPosition();
            if (position > this.breakpoints.get(0)) {
                long baseline = this.breakpoints.remove(0);
                signalChapter(position, this.player.getPlayingTrack().getInfo().length, baseline);
            }
        }
        return flip;
    }

    public void advance(long position) {
        currentTrack.setPosition(currentTrack.getPosition() + position);

    }

    public void seekTo(long position) {
        long startingPosition = currentTrack.getPosition();
        if (position < startingPosition) {
            Chuu.getLogger().warn("Trying to seek to the past!!");
            return;
        }
        if (this.breakpoints != null && !this.breakpoints.isEmpty()) {
            long baseline = this.breakpoints.remove(0);
            signalChapter(startingPosition, this.player.getPlayingTrack().getInfo().length, baseline);
            this.currentTrack.setPosition(position);
            this.breakpoints = this.breakpoints.stream().filter(z -> z < position).toList();
        } else {
            // Last chapter
            this.currentTrack.setPosition(position);
        }
    }

    public Guild getGuild() {
        if (guild == null) {
            guild = core.Chuu.getShardManager().getGuildById(this.guildId);
        }
        return guild;
    }

    public @Nullable TextChannel getAnnouncementChannel() {
        VoiceAnnouncement voiceAnnouncement = voiceAnnounceService.getVoiceAnnouncement(this.guildId);
        if (!voiceAnnouncement.enabled()) {
            return null;
        }
        if (voiceAnnouncement.channelId() != null) {
            TextChannel textChannelById = getGuild().getTextChannelById(voiceAnnouncement.channelId());
            if (textChannelById == null) {
                return getCurrentRequestChannel();
            }
            return textChannelById;
        }
        return getCurrentRequestChannel();
    }

    public Boolean isAlone() {
        GuildVoiceState voiceState = getGuild().getSelfMember().getVoiceState();
        if (voiceState == null) {
            return false;
        }
        if (voiceState.getChannel() == null) {
            return false;
        }
        return voiceState.getChannel().getMembers().stream().allMatch(x -> x.getUser().isBot());
    }

    public Boolean isIdle() {
        return player.getPlayingTrack() == null && queue.isEmpty();
    }

    public boolean isLeaveQueued() {
        return this.leaveTask.isRunning();
    }

    public void enqueue(AudioTrack track, boolean isNext) {
        if (!player.startTrack(track, true)) {
            var encoded = manager.encodeAudioTrack(track);
            if (isNext) {
                queue.addFirst(encoded);
            } else {
                queue.offer(encoded);
            }
        }
    }

    public boolean openAudioConnection(AudioChannel channel, Context e) {
        if (!getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
            e.sendMessage("Unable to connect to **" + channel.getName() + "**. I must have permission to `Connect` and `Speak`.").queue();
            destroy();
            return false;
        }
        if (channel instanceof VoiceChannel vc && vc.getUserLimit() != 0 && channel.getMembers().size() >= vc.getUserLimit() && !getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_MOVE_OTHERS)) {
            e.sendMessage("The bot can't join due to the user limit. Grant me `" + Permission.VOICE_MOVE_OTHERS.getName() + "` or raise the user limit.").queue();
            destroy();
            return false;
        } else {
            AudioManager audioManager = getGuild().getAudioManager();
            audioManager.setSendingHandler(this);
            audioManager.openAudioConnection(channel);

            e.sendMessage(new ChuuEmbedBuilder(e).setTitle("Music Playback").setDescription("Joining channel " + channel.getAsMention()).build()).queue();

            return true;
        }
    }


    public void moveAudioConnection(VoiceChannel channel) {
        moveAudioConnection(channel, getCurrentRequestChannel());
    }

    public void moveAudioConnection(AudioChannel channel, @Nullable MessageChannel source) {
        Member selfMember = getGuild().getSelfMember();
        if (selfMember.getVoiceState() == null || !selfMember.getVoiceState().inAudioChannel()) {
            destroy();
        }
        if (!selfMember.hasPermission(channel, Permission.VOICE_CONNECT)) {
            if (source != null) {
                source.sendMessage("I don't have permission to join `" + channel.getName() + "`.").queue();
            }
            destroy();
            return;
        }

        player.setPaused(true);
        getGuild().getAudioManager().openAudioConnection(channel);
        player.setPaused(false);
        if (source != null) {
            source.sendMessage("Moved").queue();
        }

    }

    public void closeAudioConnection() {
        AudioManager audioManager = getGuild().getAudioManager();
        audioManager.closeAudioConnection();
        audioManager.setSendingHandler(null);
    }

    public void queueLeave() {
        leaveTask.start();
        player.setPaused(true);
    }

    public void cancelLeave() {
        leaveTask.stop(false);
        player.setPaused(false);
    }

    public void nextChapter() {

    }

    public void nextTrack() {
        if (repeatOption != RepeatOption.NONE) {
            if (currentTrack == null) {
                return;
            }
            var cloneThis = currentTrack;


            var cloned = cloneThis.makeClone();
            cloned.setUserData(cloneThis.getUserData());

            if (repeatOption == RepeatOption.SONG) {

                player.playTrack(cloned);
                return;

            } else if (repeatOption == RepeatOption.QUEUE) {
                queue.offer(manager.encodeAudioTrack(cloned));
            } // NONE doesn't need any handling.
        }

        if (!queue.isEmpty()) {
            String track = queue.poll();
            var decodedTrack = manager.decodeAudioTrack(track);
            player.playTrack(decodedTrack);
            return;
        }
        CompletableFuture<AudioTrack> radioTrack;
        if (radio == null || ((radioTrack = radio.nextTrack()) == null)) {
            player.stopTrack();
            return;
        }
        radioTrack.whenComplete((audioTrack, throwable) -> {
            if (throwable != null) {
                if (player.getPlayingTrack() != null) {
                    player.stopTrack();
                    Optional.ofNullable(getCurrentRequestChannel()).ifPresent(z -> z.sendMessage("Couldn't get a new song from the radio: " + radio.getSource().getName() + "\nStopping playback").queue());
                }
            }
        }).thenCompose(it -> {
            if (radio.getSource() instanceof PlaylistRadio || it == null || lastTrack == null || !it.getIdentifier().equals(lastTrack.getIdentifier())) {
                return CompletableFuture.completedFuture(it);
            }
            CompletableFuture<AudioTrack> cf = radio.getSource().nextTrack(radio);
            if (cf == null) {
                return CompletableFuture.completedFuture(it);
            }
            return cf;
        }).thenAccept(it -> player.startTrack(it, false));

    }

    private void announceNext(AudioTrack track) {
        TextChannel announcementChannel = getAnnouncementChannel();
        if (announcementChannel == null) {
            return;
        }


        // Avoid spamming by just sending it if the last time it was announced was more than 10s ago.
        if (lastTimeAnnounced == 0L || lastTimeAnnounced + 10000 < System.currentTimeMillis()) {
            var reqData = track.getUserData(TrackContext.class);
            getScrobble().thenAccept(ts -> announcementChannel.sendMessageEmbeds(new ChuuEmbedBuilder(true).setDescription("Now playing __**%s**__ requested by <@%d>".formatted(ts.toLink(track.getInfo().uri), reqData.requester())).setColor(CommandUtil.pastelColor()).build()).queue(t -> lastTimeAnnounced = System.currentTimeMillis()));
        }
    }

    public void destroy() {
        Chuu.playerRegistry.destroy(guildId);
    }

    public void cleanup() {
        this.channelId = Optional.ofNullable(getGuild()).map(Guild::getAudioManager).map(AudioManager::getConnectedChannel).map(ISnowflake::getIdLong).orElse(null);
        player.destroy();
        closeAudioConnection();
    }

    // *----------- Scheduler/Event Handling -----------*
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        lastPlayedAt = System.currentTimeMillis();
        this.lastTrack = track;
        this.lastScrobble = scrobble;
        this.breakpoints = Collections.emptyList();
        this.scrobble = null;

        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    void onTrackStuck(AudioPlayer player, AudioTrack track, Long thresholdMs, Array stackTrace) {
        Guild guild = getGuild();
        if (guild == null) {
            return;
        }
        TrackContext userData = track.getUserData(TrackContext.class);
        if (userData == null) {
            return;
        }
        TextChannel textChannel = guild.getTextChannelById(userData.channelRequester());
        if (textChannel != null) {
            textChannel.sendMessage("Song stuck").queue();
        }
        nextTrack();
    }

    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        repeatOption = RepeatOption.NONE;

        if (exception.toString().contains("decoding")) {
            return;
        }

        var channel = getGuild().getTextChannelById(track.getUserData(TrackContext.class).channelRequester());

        if (System.currentTimeMillis() > lastErrorAnnounced + 5000) {
            if (channel != null) {
                channel.sendMessage(String.format("An unknown error occurred while playing **%s**:\n%s", track.getInfo().title, exception.getMessage())).queue((t) -> lastErrorAnnounced = System.currentTimeMillis());
            }
        }
        Chuu.getLogger().warn(exception.getMessage(), exception);

    }

    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if (currentTrack != null) {
            if (currentTrack.getIdentifier().equals(track.getIdentifier())) {
                loops++;
            } else {
                loops = 0;
            }
        }

        var announce = (currentTrack == null && track != null) || (currentTrack != null && !currentTrack.getIdentifier().equals(track.getIdentifier()));
        currentTrack = track;
        if (announce) {
            announceNext(track);
        }

    }

    private MutableAudioFrame genMut() {
        MutableAudioFrame mutableAudioFrame = new MutableAudioFrame();
        mutableAudioFrame.setBuffer(frameBuffer);
        return mutableAudioFrame;
    }

    public ExtendedAudioPlayerManager getManager() {
        return manager;
    }

    public RepeatOption getRepeatOption() {
        return repeatOption;
    }

    public void setRepeatOption(RepeatOption repeatOption) {
        this.repeatOption = repeatOption;
    }

    public long getGuildId() {
        return guildId;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public Queue<String> getQueue() {
        return queue;
    }

    public AudioTrack getLastTrack() {
        return lastTrack;
    }

    public AudioTrack getCurrentTrack() {
        return currentTrack;
    }

    public Task getLeaveTask() {
        return leaveTask;
    }

    public long getLastTimeAnnounced() {
        return lastTimeAnnounced;
    }

    public long getLastErrorAnnounced() {
        return lastErrorAnnounced;
    }

    public long getLastVoteTime() {
        return lastVoteTime;
    }

    public boolean isVotingToSkip() {
        return isVotingToSkip;
    }

    public boolean isVotingToPlay() {
        return isVotingToPlay;
    }

    public long getLastPlayVoteTime() {
        return lastPlayVoteTime;
    }

    public long getLastPlayedAt() {
        return lastPlayedAt;
    }

    public long getLoops() {
        return loops;
    }

    public @Nullable TextChannel getCurrentRequestChannel() {
        AudioTrack track = player.getPlayingTrack();
        if (track == null) {
            track = lastTrack;
        }
        if (track == null) {
            return null;
        }
        TrackContext userData = track.getUserData(TrackContext.class);
        if (userData == null) {
            return null;
        }
        return getGuild().getTextChannelById(userData.channelRequester());
    }

    public ByteBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public MutableAudioFrame getLastFrame() {
        return lastFrame;
    }

    public RadioTrackContext getRadio() {
        return radio;
    }

    public void setRadio(RadioTrackContext radio) {
        this.radio = radio;
    }


    public int getScroobblers() {
        return this.listener.getScrooblersCount();
    }


    public CompletableFuture<Scrobble> getScrobble(AudioTrack audioTrack) {
        return this.getTrackScrobble(audioTrack).thenApply(z -> z.scrobble(audioTrack.getPosition(), audioTrack.getDuration()));
    }

    public CompletableFuture<Scrobble> getScrobble() {
        return getScrobble(player.getPlayingTrack());
    }

    public AudioTrack getLastValidTrack() {
        return player.getPlayingTrack() == null ? currentTrack == null ? lastTrack : currentTrack : player.getPlayingTrack();
    }

    public CompletableFuture<TrackScrobble> getTrackScrobble() {
        return CommandUtil.supplyLog(() -> scrobbleProcesser.processScrobble(null, getLastValidTrack())).thenApply(z -> {
            this.scrobble = z.scrobble();
            this.breakpoints = z.processeds().stream().skip(1).dropWhile(l -> l.msStart() < player.getPlayingTrack().getPosition()).map(Processed::msStart).collect(Collectors.toCollection(ArrayList::new));
            return z;
        });
    }

    public CompletableFuture<TrackScrobble> getTrackScrobble(AudioTrack anyTrack) {
        if (anyTrack == player.getPlayingTrack()) {
            return getTrackScrobble();
        }
        return CommandUtil.supplyLog(() -> scrobbleProcesser.processScrobble(null, anyTrack)).thenApply(z -> {
            this.scrobble = z.scrobble();
            this.breakpoints = z.processeds().stream().skip(1).dropWhile(l -> l.msStart() < anyTrack.getPosition()).map(Processed::msStart).collect(Collectors.toCollection(ArrayList::new));
            return z;
        });
    }

    public CompletableFuture<Void> setMetadata(Metadata metadata) {
        return getTrackScrobble().thenAccept(z -> {
            long remaining = currentTrack.getDuration() - currentTrack.getPosition();
            TrackScrobble newInfo;
            if (z.processeds().size() > 1) {
                newInfo = this.scrobbleProcesser.setMetadata(metadata, currentTrack, z.uuid(), currentTrack.getPosition(), currentTrack.getDuration());
                this.scrobble = newInfo.scrobble();
            } else {
                newInfo = this.scrobbleProcesser.setMetadata(metadata, currentTrack, z.uuid());
            }
            this.listener.signalMetadataChange(newInfo);

        });
    }

    public CompletableFuture<Void> signalChapter(long currentMs, long totalMs, long baseline) {
        return getTrackScrobble().thenAccept(z -> this.listener.signalChapterEnd(z, currentMs, totalMs, baseline));
    }

    public Long getChannelId() {
        return channelId;
    }
}
