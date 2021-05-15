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
import dao.ChuuService;

import javax.validation.constraints.NotNull;
import java.util.List;

public class ResumeCommand extends MusicCommand<CommandParameters> {
    public ResumeCommand(ChuuService dao) {
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
        return "Resumes the current track";
    }

    @Override
    public List<String> getAliases() {
        return List.of("resume");
    }

    @Override
    public String getName() {
        return "Resume";
    }

    @Override
    protected void onCommand(Context e, @NotNull CommandParameters params) {
        MusicManager manager = getManager(e);
        manager.getPlayer().setPaused(false);
        e.sendMessageQueue("Resumed current song");
    }
}
