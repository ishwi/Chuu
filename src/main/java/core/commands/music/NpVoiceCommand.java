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
import core.commands.abstracts.MusicCommand;
import core.commands.utils.CommandUtil;
import core.music.MusicManager;
import core.music.sources.MetadataTrack;
import core.music.utils.TrackContext;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.Metadata;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import static core.commands.music.MetadataCommand.mapper;

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
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) {
        MusicManager manager = Chuu.playerRegistry.get(e.getGuild());
        AudioTrack track = manager.getPlayer().getPlayingTrack();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Now Playing")
                .setDescription(String.format("[%s](%s)", track.getInfo().title, CommandUtil.cleanMarkdownCharacter(track.getInfo().uri)));
        if (manager.getRadio() != null) {
            String b = "Currently streaming music from radio station " + manager.getRadio().getSource().getName() +
                    ", requested by " + manager.getRadio().requester() +
                    ". When the queue is empty, random tracks from the station will be added.";
            embedBuilder.addField("Radio", b, false);
        }
        String album = null;
        String url = null;
        if (track instanceof MetadataTrack spo) {
            album = spo.getAlbum();
            url = spo.getImage();
        }
        Optional<Metadata> metadata = Optional.ofNullable(manager.getMetadata());
        String finalUrl = url;
        String finalAlbum = album;
        metadata.ifPresentOrElse(meta -> embedBuilder.setDescription(mapper.apply(meta.artist(), meta.album(), meta.song())).setThumbnail(finalUrl),
                () -> embedBuilder.setDescription(mapper.apply(track.getInfo().author, finalAlbum, track.getInfo().title)).setThumbnail(finalUrl));

        int scroobblers = manager.getScroobblers();
        embedBuilder.addField("Requester", track.getUserData(TrackContext.class).getRequesterStr(), true)
                .addField("Request Channel", track.getUserData(TrackContext.class).getChannelRequesterStr(), true)
                .addBlankField(true)
                .addField("Scrobbling", String.format("%s %s scrobbling%s", scroobblers, CommandUtil.singlePlural(scroobblers, "person", "people"), scroobblers > 0 ? "!" : " :("), true)
                .addField("Repeating", manager.getRepeatOption().name().toLowerCase(), true);
        String timeString;
        if (track.getDuration() == Long.MAX_VALUE) {
            timeString = "Streaming";
        } else {
            var position = CommandUtil.getTimestamp(track.getPosition());
            var duration = CommandUtil.getTimestamp(track.getDuration());
            timeString = "`[" + position + " / " + duration + "]`";
        }
        embedBuilder.addField("Time", timeString, true);
        double percent = track.getPosition() / (double) track.getDuration();
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
        } else {
            embedBuilder.setFooter("Use " + CommandUtil.getMessagePrefix(e) + "lyrics to see the lyrics of the song!");
        }
        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }

}
