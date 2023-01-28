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
package core.commands.music.dj;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.commands.Context;
import core.commands.abstracts.MusicCommand;
import core.music.MusicManager;
import core.parsers.MinutesParser;
import core.parsers.Parser;
import core.parsers.params.MinutesParameters;
import core.util.ServiceView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JumpCommand extends MusicCommand<MinutesParameters> {
    public JumpCommand(ServiceView dao) {
        super(dao);
        sameChannel = true;
        requirePlayingTrack = true;
        requirePlayer = true;
    }

    @Override
    public Parser<MinutesParameters> initParser() {
        return new MinutesParser();
    }

    @Override
    public String getDescription() {
        return "Advances on the current song";
    }

    @Override
    public List<String> getAliases() {
        return List.of("jump", "seek");
    }

    @Override
    public String getName() {
        return "Seek in song";
    }

    @Override
    public void onCommand(Context e, @NotNull MinutesParameters params) {
        MusicManager manager = getManager(e);
        AudioTrack track = manager.getPlayer().getPlayingTrack();
        if (!track.isSeekable()) {
            sendMessageQueue(e, "Current song doesn't support seeking :pensive:");
            return;
        }
        int hours = params.getHours();
        int minutes = params.getMinutes();
        int seconds = params.getSeconds();
        String seeked = (hours == 0 ? "" : hours + " hours ") + (minutes == 0 ? "" : minutes + " minutes ") + (seconds == 0 ? "" : seconds + " seconds");
        seeked = seeked.replaceAll("\\s+", " ").trim();
        int seekedMs = (seconds + minutes * 60 + hours * 60 * 60) * 1000;
        manager.advance(seekedMs);
        sendMessageQueue(e, "Jumped " + seeked);

    }
}
