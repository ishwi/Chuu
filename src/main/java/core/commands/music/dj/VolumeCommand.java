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

import core.commands.abstracts.MusicCommand;
import core.commands.utils.CommandUtil;
import core.music.MusicManager;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import core.services.ColorService;
import dao.ChuuService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class VolumeCommand extends MusicCommand<NumberParameters<CommandParameters>> {
    private static final long MAX_VOLUME = 250;
    private static final int TOTAL_BLOCKS = 20;

    public VolumeCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can introduce a goal that will be the number of scrobbles that you want to obtain.";
        return new NumberParser<>(new NoOpParser(),
                null,
                MAX_VOLUME,
                map, s, false, true, true);
    }

    @Override
    public String getDescription() {
        return "Set the volume of the music player.";
    }

    @Override
    public List<String> getAliases() {
        return List.of("vol", "volume");
    }

    @Override
    public String getName() {
        return "Volume";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<CommandParameters> params) {
        Long volume = params.getExtraParam();
        MusicManager manager = getManager(e);
        int currentVolume = manager.getPlayer().getVolume();
        if (volume == null) {
            String bar = buildBar(currentVolume);
            e.getChannel().sendMessage(new EmbedBuilder().setColor(ColorService.computeColor(e)).setTitle("Volume").setDescription(bar)
                    .setFooter("Set the volume by using " + CommandUtil.getMessagePrefix(e) + "volume (number)").build()).queue();
            return;
        }
        String bar = buildBar(volume);
        manager.getPlayer().setVolume(volume.intValue());
        e.getChannel().sendMessage(new EmbedBuilder().setColor(ColorService.computeColor(e)).setTitle("Volume").setDescription(bar)
                .setFooter("Volume changed from " + currentVolume + " to " + volume).build()).queue();
    }

    private String buildBar(long value) {
        double v = (double) value / VolumeCommand.MAX_VOLUME;
        if (v < 0.0) {
            v = 0.0;
        } else if (v > 1.0) {
            v = 1.0;
        }
        var percent = v;
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < TOTAL_BLOCKS; i++) {
            if ((int) (percent * (TOTAL_BLOCKS - 1)) == i) {
                a.append("__**▬**__");
            } else {
                a.append("―");
            }
        }
        a.append(String.format(" **%.0f**%%", percent * VolumeCommand.MAX_VOLUME));
        return a.toString();
    }
}
