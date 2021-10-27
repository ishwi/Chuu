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

import core.commands.Context;
import core.commands.abstracts.MusicCommand;
import core.music.MusicManager;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;

import javax.annotation.Nonnull;
import java.util.List;

public class PauseCommand extends MusicCommand<CommandParameters> {
    public PauseCommand(ServiceView dao) {
        super(dao);
        sameChannel = true;
        requirePlayingTrack = true;
        requirePlayer = true;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Pauses the current track";
    }

    @Override
    public List<String> getAliases() {
        return List.of("pause");
    }

    @Override
    public String getName() {
        return "Pause";
    }

    @Override
    public void onCommand(Context e, @Nonnull CommandParameters params) {
        MusicManager manager = getManager(e);
        manager.getPlayer().setPaused(true);
        e.sendMessageQueue("Paused current song");
    }
}
