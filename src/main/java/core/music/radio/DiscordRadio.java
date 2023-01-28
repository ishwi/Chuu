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
package core.music.radio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.Chuu;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public record DiscordRadio(String name) implements RadioSource {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CompletableFuture<AudioTrack> nextTrack(RadioTrackContext context) {
        return this.nextTrack0(context, 1);
    }

    @Override
    public void serialize(ByteArrayOutputStream stream) throws IOException {
        var writer = new DataOutputStream(stream);
        writer.writeInt(1);
        writer.writeUTF(name);
        writer.close(); // This invokes flush.
    }

    private CompletableFuture<AudioTrack> nextTrack0(RadioTrackContext context, int attempts) {
        if (attempts > 3) {
            return CompletableFuture.completedFuture(null);
        }

        String randomSong = Chuu.getRandomSong(getName());
        if (randomSong == null) {
            return nextTrack0(context, attempts + 1);
        }
        var future = new CompletableFuture<AudioTrack>();

        Chuu.playerManager.loadItemOrdered(this, randomSong, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(context);
                future.complete(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                trackLoaded(playlist.getTracks().get(0));
            }

            @Override
            public void noMatches() {
                future.complete(null);

            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (attempts >= 3) {
                    future.complete(null);
                } else {
                    nextTrack0(context, attempts + 1);
                }
            }


        });

        return future;
    }
}
