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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.Chuu;
import core.apis.last.entities.Scrobble;
import core.commands.Context;
import core.commands.abstracts.MusicCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandUtil;
import core.music.MusicManager;
import core.music.utils.TrackContext;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;


public class NpVoiceCommand extends MusicCommand<CommandParameters> {
    public NpVoiceCommand(ChuuService dao) {
        super(dao);
        requirePlayingTrack = true;
        requirePlayer = true;

    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Your now playing song on voice";
    }

    @Override
    public List<String> getAliases() {
        return List.of("voice", "current", "current", "npv", "song");
    }

    @Override
    public String getName() {
        return "Voice now playing";
    }

    @Override
    protected void onCommand(Context e, @NotNull CommandParameters params) {
        MusicManager manager = Chuu.playerRegistry.get(e.getGuild());
        AudioTrack playingTrack = manager.getPlayer().getPlayingTrack();
        manager.getTrackScrobble().thenAccept(l -> {
            Scrobble z = l.scrobble(playingTrack.getPosition(), playingTrack.getDuration());
            var index = l.mapDuration(playingTrack.getPosition(), playingTrack.getDuration());
            long fakePosition = index.duration();

            EmbedBuilder embedBuilder = new ChuuEmbedBuilder()
                    .setTitle("Now Playing", playingTrack.getInfo().uri)
                    .setColor(CommandUtil.pastelColor());
            if (manager.getRadio() != null) {
                String b = "Currently streaming music from radio station " + manager.getRadio().getSource().getName() +
                           ", requested by @<" + manager.getRadio().requester() +
                           ">. When the queue is empty, random tracks from the station will be added.";
                embedBuilder.addField("Radio", b, false);
            }

            String description = z.toLines();
            if (l.processeds().size() > 1) {
                description += "\nChapter: **%s**/**%s**".formatted(index.index(), l.processeds().size());
            }
            embedBuilder.setDescription(description).setThumbnail(z.image());

            int scroobblers = manager.getScroobblers();
            TrackContext ctx = playingTrack.getUserData(TrackContext.class);
            embedBuilder.addField("Requester", ctx.getRequesterStr(), true)
                    .addField("Request Channel", ctx.getChannelRequesterStr(), true)
                    .addBlankField(true)
                    .addField("Scrobbling", String.format("%s %s scrobbling%s", scroobblers, CommandUtil.singlePlural(scroobblers, "person", "people"), scroobblers > 0 ? "!" : " :("), true)
                    .addField("Repeating", manager.getRepeatOption().name().toLowerCase(), true);
            String timeString;
            if (playingTrack.getDuration() == Long.MAX_VALUE) {
                timeString = "Streaming";
            } else {
                var position = CommandUtil.getTimestamp(fakePosition);
                var duration = CommandUtil.getTimestamp(z.duration());
                timeString = "`[" + position + " / " + duration + "]`";
            }
            embedBuilder.addField("Time", timeString, true);
            double percent = fakePosition / (double) z.duration();
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < 20; i++) {
                if ((int) (percent * (20 - 1)) == i) {
                    str.append("__**▬**__");
                } else {
                    str.append("―");
                }
            }
            str.append(String.format(" **%.1f**%%", percent * 100.0));
            embedBuilder.addField("Progress", str.toString(), false);

            if (manager.getLoops() > 5) {
                embedBuilder.setFooter("bröther may i have some lööps | You've looped " + manager.getLoops() + " times");
            } else if (l.processeds().size() > 1) {
                embedBuilder.setFooter("Use " + CommandUtil.getMessagePrefix(e) + "skipchapter to skip to the next chapter!");
            } else {
                embedBuilder.setFooter("Use " + CommandUtil.getMessagePrefix(e) + "lyrics to see the lyrics of the song!");
            }
            e.sendMessage(embedBuilder.build()).queue();
        });
    }

}
