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
package core.commands.music;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.parsers.MusicInputParser;
import core.parsers.Parser;
import core.parsers.params.WordParameter;
import dao.ChuuService;

import javax.validation.constraints.NotNull;
import java.util.List;

public class PlayNextCommand extends core.commands.abstracts.MusicCommand<WordParameter> {


    public PlayNextCommand(ChuuService dao) {
        super(dao);
        requireManager = false;
        requireVoiceState = true;

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public Parser<WordParameter> initParser() {
        return new MusicInputParser();
    }

    @Override
    public String getDescription() {
        return "Skips the current song and starts playing the new one";
    }

    @Override
    public List<String> getAliases() {
        return List.of("playnext", "pn");
    }

    @Override
    public String getName() {
        return "Play next";
    }

    @Override
    protected void onCommand(Context e, @NotNull WordParameter params) {

        var manager = Chuu.playerRegistry.get(e.getGuild());

        Enqueue.play(e, manager, params.getWord(), false, true);

    }

}

