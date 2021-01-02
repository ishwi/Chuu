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
import core.apis.last.LastFMFactory;
import core.commands.utils.CommandUtil;
import core.music.radio.PlaylistRadio;
import core.music.radio.RadioTrackContext;
import core.music.utils.ScrobblerEventListener;
import core.music.utils.Task;
import core.music.utils.TrackContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MusicManager extends AudioEventAdapter implements AudioSendHandler {


    private final ExtendedAudioPlayerManager manager;
    private RepeatOption repeatOption = RepeatOption.NONE;
    private RadioTrackContext radio = null;

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
        return flip;
    }

    private final long guildId;

    public Guild getGuild() {
        if (guild == null) {
            guild = core.Chuu.getShardManager().getGuildById(this.guildId);
        }
        return guild;
    }

    private String dbAnnouncementChannel;
    private TextChannel announcementChannel;

    public TextChannel getAnnouncementChannel() {
        String dbAnnouncmentChannel = getDbAnnouncmentChannel();
        if (dbAnnouncmentChannel == null) {
            return getCurrentRequestChannel();
        }
        return getGuild().getTextChannelById(dbAnnouncmentChannel);
    }

    public String getDbAnnouncmentChannel() {
        if (dbAnnouncementChannel == null) {
            dbAnnouncementChannel = core.Chuu.getRandomSong(guildId + "");
        }
        return dbAnnouncementChannel;
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

    private final AudioPlayer player;
    private Guild guild;
    private final Queue<String> queue = new ArrayDeque<>();


    // Playback/Music related.
    private AudioTrack lastTrack;
    private AudioTrack currentTrack;
    // Settings/internals.
    private final Task leaveTask = new core.music.utils.Task(30, TimeUnit.SECONDS, this::destroy);

    public boolean isLeaveQueued() {
        return this.leaveTask.isRunning();
    }


    private long lastTimeAnnounced = 0L;
    private long lastErrorAnnounced = 0L;

    private final long lastVoteTime = 0L;
    private final boolean isVotingToSkip = false;
    private final boolean isVotingToPlay = false;
    private final long lastPlayVoteTime = 0L;
    private long lastPlayedAt = 0L;

    private long loops = 0L;


    // ---------- End Properties ----------


    public MusicManager(long guildId, AudioPlayer player, ExtendedAudioPlayerManager manager) {
        this.guildId = guildId;
        this.player = player;
        this.manager = manager;
        this.player.addListener(this);
        this.player.setVolume(100);
        player.addListener(new ScrobblerEventListener(this, LastFMFactory.getNewInstance()));
    }


    public void enqueue(AudioTrack track, boolean isNext) {
        if (!player.startTrack(track, true)) {
            var encoded = manager.encodeAudioTrack(track);
            if (isNext) {
                queue.add(encoded);
            } else {
                queue.offer(encoded);
            }
        }
    }

    public boolean openAudioConnection(VoiceChannel channel, MessageReceivedEvent e) {
        if (!getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
            e.getChannel().sendMessage("Unable to connect to **${channel.name}**. I must have permission to `Connect` and `Speak`.").queue();
            destroy();
            return false;
        }
        if (channel.getUserLimit() == 0 && channel.getMembers().size() >= channel.getUserLimit() && !getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_MOVE_OTHERS)) {
            e.getChannel().sendMessage("The bot can't join due to the user limit. Grant me `${Permission.VOICE_MOVE_OTHERS.name}` or raise the user limit.").queue();
            destroy();
            return false;
        } else {
            AudioManager audioManager = getGuild().getAudioManager();
            audioManager.setSendingHandler(this);
            audioManager.openAudioConnection(channel);

            e.getChannel().sendMessage(new EmbedBuilder().setColor(CommandUtil.randomColor()).setTitle("Music Playback").setDescription("Joining channel <#" + channel.getId() + ">").build()).queue();

            return true;
        }
    }

    public void moveAudioConnection(VoiceChannel channel) {
        Member selfMember = getGuild().getSelfMember();
        if (selfMember.getVoiceState() == null || !selfMember.getVoiceState().inVoiceChannel()) {
            destroy();
        }
        if (!selfMember.hasPermission(channel, Permission.VOICE_CONNECT)) {
            getCurrentRequestChannel().sendMessage("I don't have permission to join `${channel.name}`.").queue();
            destroy();
            return;
        }

        player.setPaused(true);
        getGuild().getAudioManager().openAudioConnection(channel);
        player.setPaused(false);
        getCurrentRequestChannel().sendMessage("Moved").queue();

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
        if (radio == null) {
            return;
        }
        var radioTrack = radio.nextTrack();
        if (radioTrack == null) {
            player.stopTrack();
            return;
        }
        radioTrack.thenCompose(it -> {
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

            StringBuilder a = new StringBuilder();
            a.append("Now playing __**[").append(track.getInfo().title)
                    .append("](").append(track.getInfo().uri).append(")**__")
                    .append(" requested by ").append("<@").append(reqData.requester()).append(">");
            announcementChannel.sendMessage(new EmbedBuilder()
                    .setDescription(a)
                    .setColor(CommandUtil.randomColor()).build()).queue(t -> lastTimeAnnounced = System.currentTimeMillis());
        }
    }


    public void destroy() {
        Chuu.playerRegistry.destroy(guildId);
//        Launcher.players.destroy(guildId)
    }

    public void cleanup() {
        player.destroy();
//        dspFilter.clearFilters()
//        queue.expire(4, TimeUnit.HOURS)

        closeAudioConnection();
    }

    // *----------- Scheduler/Event Handling -----------*
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        lastPlayedAt = System.currentTimeMillis();
        this.lastTrack = track;

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
        guild.getTextChannelById(userData.channelRequester()).sendMessage("Song stuck").queue();
        nextTrack();
    }


    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        repeatOption = RepeatOption.NONE;

        if (exception.toString().contains("decoding")) {
            return;
        }

        var channel = getGuild().getTextChannelById(track.getUserData(TrackContext.class).channelRequester());

        if (System.currentTimeMillis() > lastErrorAnnounced + 5000) {
            channel.sendMessage(String.format("An unknown error occurred while playing **%s**:\n%s", track.getInfo().title, exception.getMessage())).queue((t) -> lastErrorAnnounced = System.currentTimeMillis());
        }

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

    // *----------- AudioSendHandler -----------*
    private final ByteBuffer frameBuffer = ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize());
    private final MutableAudioFrame lastFrame = genMut();


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

    public TextChannel getCurrentRequestChannel() {
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
        return guild.getTextChannelById(userData.channelRequester());
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
}
