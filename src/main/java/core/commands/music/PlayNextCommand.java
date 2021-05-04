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
import core.commands.ContextMessageReceived;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.entities.Message;

import javax.validation.constraints.NotNull;
import java.util.List;

public class PlayNextCommand extends ConcurrentCommand<CommandParameters> {


    public PlayNextCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Play";
    }

    @Override
    public List<String> getAliases() {
        return List.of("playnext", "pn");
    }

    @Override
    public String getName() {
        return "pn";
    }

    @Override
    protected void onCommand(Context e, @NotNull CommandParameters params) {
        if (e.getGuild().getSelfMember().getVoiceState() == null) {
            sendMessageQueue(e, "I'm not on a voice channel");
            return;
        }

        var botChannel = e.getGuild().getSelfMember().getVoiceState().getChannel();
        if (e.getMember() == null || e.getMember().getVoiceState() == null || e.getMember().getVoiceState().getChannel() == null) {
            sendMessageQueue(e, "You're not in a voice channel.");
            return;
        }
        var userChannel = e.getMember().getVoiceState().getChannel();


        if (botChannel != null && botChannel != userChannel) {
            sendMessageQueue(e, "The bot is already playing music in another channel.");
            return;
        }

        Message.Attachment attachment = (e instanceof ContextMessageReceived mes) ? mes.e().getMessage().getAttachments().stream().findFirst().orElse(null) : null;
        boolean hasManager = Chuu.playerRegistry.contains(e.getGuild());
        var newManager = Chuu.playerRegistry.get(e.getGuild());

        String[] original = parser.getSubMessage(e);
        MusicCommand.play(e, newManager, String.join(" ", original), false, true);

    }

}

