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
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.music.MusicManager;
import core.music.sources.MetadataTrack;
import core.music.utils.TrackContext;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import dao.ChuuService;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class QueueCommand extends ConcurrentCommand<CommandParameters> {
    public QueueCommand(ChuuService dao) {
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
        return "Queued songs on a voice session";
    }

    @Override
    public List<String> getAliases() {
        return List.of("queue", "q");
    }

    @Override
    public String getName() {
        return "Queue";
    }

    @Override
    protected void onCommand(Context e, @NotNull CommandParameters params) {
        MusicManager manager = Chuu.playerRegistry.getExisting(e.getGuild().getIdLong());
        if (manager == null) {
            sendMessageQueue(e, "There's no music manager in this server");
            return;
        }
        Queue<String> queue = manager.getQueue();
        long length = 0L;
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder();
        StringBuilder stringBuilder = new StringBuilder();
        List<String> str = new ArrayList<>();
        for (String s : queue) {
            AudioTrack decodedTrack = Chuu.playerManager.decodeAudioTrack(s);
            StringBuilder a = new StringBuilder();
            decodedTrack.getUserData(TrackContext.class);
            String title = decodedTrack.getInfo().title;
            if (decodedTrack instanceof MetadataTrack mt) {
                title = decodedTrack.getInfo().title + " - " + decodedTrack.getInfo().author + " | " + mt.getAlbum();
            }
            a.append("`[").append(CommandUtil.getTimestamp(decodedTrack.getDuration()))
                    .append("]` __[")
                    .append(title)
                    .append("](").append(CommandUtil.cleanMarkdownCharacter(decodedTrack.getInfo().uri)).append(")__\n");
            str.add(a.toString());
            length += decodedTrack.getDuration();
        }
        embedBuilder.setAuthor("Music Queue");
        AudioTrack np = manager.getPlayer().getPlayingTrack();

        String title = "Nothing";

        if (np != null) {
            if (np instanceof MetadataTrack mt) {
                title = np.getInfo().title + " - " + np.getInfo().author + " | " + mt.getAlbum();
                embedBuilder.setAuthor("Music Queue", null, mt.getImage());
            } else {
                title = np.getInfo().author;
            }
        }
        embedBuilder.addField("Now Playing", np == null ? "Nothing" : String.format("**[%s](%s)**", title, CommandUtil.cleanMarkdownCharacter(np.getInfo().uri)), false);

        if (manager.getRadio() != null) {
            String b = "Currently streaming music from radio station " + manager.getRadio().getSource().getName() +
                       ", requested by <@" + manager.getRadio().requester() +
                       ">. When the queue is empty, random tracks from the station will be added.";
            embedBuilder.addField("Radio", b, false);
        }
        embedBuilder
                .addField("Entries", String.valueOf(queue.size()), true)
                .addField("Total Duration", CommandUtil.getTimestamp(length), true)
                .addField("Repeating", manager.getRepeatOption().name(), true);
//        DiscordUserDisplay uInfo = CommandUtil.getUserInfoNotStripped(e, e.getAuthor().getIdLong());
        embedBuilder
                .setDescription(str.stream().limit(10).collect(Collectors.joining()))
                .setColor(ColorService.computeColor(e));
        e.sendMessage(embedBuilder.build()).queue(m -> new Reactionary<>(str, m, 10, embedBuilder, false, true));
    }
}
