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
import core.commands.utils.CommandUtil;
import core.music.MusicManager;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import core.util.ServiceView;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class SkipToCommand extends MusicCommand<NumberParameters<CommandParameters>> {
    public SkipToCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You need to specify the position of the track in the queue that you want to skip to.";
        return new NumberParser<>(NoOpParser.INSTANCE,
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true, "index");
    }

    @Override
    public String getDescription() {
        return "Skip the current song and plays the one at position x";
    }

    @Override
    public List<String> getAliases() {
        return List.of("skt");
    }

    @Override
    public String getName() {
        return "Skip To Position";
    }

    @Override
    public void onCommand(Context e, @NotNull NumberParameters<CommandParameters> params) {
        Long toIndex = params.getExtraParam();
        MusicManager manager = getManager(e);
        if (toIndex == null || toIndex <= 0) {
            sendMessageQueue(e, "You need to specify the position of the track in the queue that you want to skip to.");
            return;
        }
        if (toIndex >= manager.getQueue().size()) {
            sendMessageQueue(e, "You wanted to skip to the %s position but there are only %s songs in the queue".formatted(toIndex + CommandUtil.getRank(toIndex), manager.getQueue().size()));
            return;
        }
        if (toIndex - 1 == 0) {
            sendMessageQueue(e, "Use the `" + CommandUtil.getMessagePrefix(e) + "skip` command to skip single tracks.");
            return;
        }
        for (int i = 0; i < toIndex - 1; i++) {
            manager.getQueue().remove();
        }
        sendMessageQueue(e, "Skipped **" + (toIndex - 1) + "** tracks.");
        manager.nextTrack();

    }
}
