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
import core.music.utils.RepeatOption;
import core.parsers.EnumParser;
import core.parsers.Parser;
import core.parsers.params.EnumParameters;
import dao.ServiceView;

import javax.validation.constraints.NotNull;
import java.util.List;

public class RepeatCommand extends MusicCommand<EnumParameters<RepeatOption>> {
    public RepeatCommand(ServiceView dao) {
        super(dao);
        sameChannel = true;
        requirePlayer = true;
    }

    @Override
    public Parser<EnumParameters<RepeatOption>> initParser() {
        return new EnumParser<>(RepeatOption.class);
    }

    @Override
    public String getDescription() {
        return "Set if the music should be repeated";
    }

    @Override
    public List<String> getAliases() {
        return List.of("loop", "repeat");
    }

    @Override
    public String getName() {
        return "Repeat";
    }

    @Override
    protected void onCommand(Context e, @NotNull EnumParameters<RepeatOption> params) {
        RepeatOption element = params.getElement();
        MusicManager manager = getManager(e);
        manager.setRepeatOption(element);
        sendMessageQueue(e, element.getEmoji() + " Track repeating was set to __**" + element.name().toLowerCase() + "**__.");

    }
}
