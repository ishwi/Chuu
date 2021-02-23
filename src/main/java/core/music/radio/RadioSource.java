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

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface RadioSource {
    String getName();

    @Nullable
    CompletableFuture<AudioTrack> nextTrack(RadioTrackContext context);

    void serialize(ByteArrayOutputStream stream) throws IOException;

    default RadioSource deserialize(ByteArrayInputStream stream) throws IOException {
        if (stream.available() == 0) {
            throw new IllegalStateException("Cannot parse RadioSource with no remaining bytes");
        }

        var reader = new DataInputStream(stream);

        RadioSource ctx = switch (reader.readInt()) {
            case 1 -> new DiscordRadio(reader.readUTF());
            case 2 -> new PlaylistRadio(reader.readUTF(), reader.readUTF());
            case 3 -> new RandomRadio(reader.readUTF(), reader.readLong(), reader.readBoolean());
            default -> throw new IllegalArgumentException("Invalid contextType $sourceType!");
        };

        reader.close();
        return ctx;
    }
}
