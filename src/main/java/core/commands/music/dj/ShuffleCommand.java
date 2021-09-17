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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class ShuffleCommand extends MusicCommand<CommandParameters> {
    public ShuffleCommand(ServiceView dao) {
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
        return "Shuffles the queue";
    }

    @Override
    public List<String> getAliases() {
        return List.of("shuffle");
    }

    @Override
    public String getName() {
        return "Shuffle";
    }

    @Override
    protected void onCommand(Context e, @Nonnull CommandParameters params) {
        MusicManager manager = getManager(e);
        Queue<String> queue = manager.getQueue();
        if (queue.isEmpty()) {
            sendMessageQueue(e, "Can't shuffle the queue because its empty\n" + PLAY_MESSAGE.apply(e.getPrefix()));
            return;
        }
        if (queue.size() != 1) {
            List<String> strings = new ArrayList<>(queue);
            queue.clear();
            Collections.shuffle(strings);
            queue.addAll(strings);
        }
        e.sendMessageQueue("Shuffled the queue");
    }
}
