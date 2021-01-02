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


import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.Chuu;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class PlaylistRadio implements RadioSource {
    private final String authorId;
    private final String name;

    public PlaylistRadio(String authorId, String name) {
        super();

        this.authorId = authorId;
        this.name = name;
    }

    @Override
    public String getName() {
        return null;
    }

    @Nullable
    @Override
    public CompletableFuture<AudioTrack> nextTrack(RadioTrackContext context) {
        CompletableFuture<Object> customPlaylist = Chuu.getDao().getCustomPlaylist(Long.parseLong(authorId), name);
        if (customPlaylist == null) {
            return CompletableFuture.completedFuture(null);
        }
        try {
            Chuu.playerManager.decodeTrack("");
            return CompletableFuture.completedFuture(null);
        } catch (IOException exception) {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public void serialize(ByteArrayOutputStream stream) throws IOException {
        var writer = new DataOutputStream(stream);
        writer.writeInt(2);
        writer.writeUTF(name);
        writer.writeUTF(authorId);
        writer.close();
    }
}
