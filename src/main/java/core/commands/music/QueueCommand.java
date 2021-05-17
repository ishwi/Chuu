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
import core.commands.abstracts.MusicCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandUtil;
import core.music.MusicManager;
import core.music.utils.TrackScrobble;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class QueueCommand extends MusicCommand<CommandParameters> {
    public QueueCommand(ChuuService dao) {
        super(dao);
        requirePlayingTrack = true;
        requirePlayer = true;

    }

    static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> com) {
        return CompletableFuture.allOf(com.toArray(new CompletableFuture[0]))
                .thenApply(v -> com.stream()
                        .map(CompletableFuture::join)
                        .toList()
                );
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


    private void handleList(Pair<TrackScrobble, List<Pair<AudioTrack, TrackScrobble>>> result, MusicManager manager, Context e) {


        TrackScrobble np = result.getLeft();
        List<Pair<AudioTrack, TrackScrobble>> queue = result.getRight();
        long duration = queue.stream().mapToLong(z -> z.getKey().getDuration()).sum();

        AudioTrack current = manager.getLastValidTrack();
        List<String> str = queue.stream()
                .map(z -> {
                    AudioTrack tr = z.getLeft();
                    TrackScrobble sc = z.getRight();
                    String item;
                    if (sc.processeds().size() > 1) {
                        item = "__%s__ [%d/%d]".formatted(sc.scrobble().toLink(tr.getInfo().uri), 1, sc.processeds().size());
                    } else {
                        item = "__%s__".formatted(sc.scrobble().toLink(tr.getInfo().uri));
                    }
                    return "`[%s]` %s\n".formatted(CommandUtil.getTimestamp(tr.getDuration()), item);
                }).toList();
        EmbedBuilder eb = new ChuuEmbedBuilder(e)
                .setAuthor("Music Queue", null, np.scrobble().image())
                .addField("Now Playing", np.scrobble(current.getPosition(), current.getDuration()).toLink(current.getInfo().uri), false);


        if (manager.getRadio() != null) {
            String b = "Currently streaming music from radio station " + manager.getRadio().getSource().getName() +
                       ", requested by <@" + manager.getRadio().requester() +
                       ">. When the queue is empty, random tracks from the station will be added.";
            eb.addField("Radio", b, false);
        }

        if (queue.isEmpty()) {
            eb.setDescription("Nothing in the queue-");
        } else {
            eb.setDescription(str.stream().limit(10).collect(Collectors.joining()))
                    .addField("Entries", String.valueOf(queue.size()), true)
                    .addField("Total Duration", CommandUtil.getTimestamp(duration), true);
        }


        eb.addField("Repeating", manager.getRepeatOption().getEmoji(), true);
        e.sendMessage(eb.build()).queue(m -> new Reactionary<>(str, m, 10, eb, false, true));
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
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);
        StringBuilder stringBuilder = new StringBuilder();
        List<String> str = new ArrayList<>();
        List<CompletableFuture<Pair<AudioTrack, TrackScrobble>>> completableFutures = queue.stream()
                .map(z ->
                        CompletableFuture.supplyAsync(() -> Chuu.playerManager.decodeAudioTrack(z))
                                .thenCompose(track -> manager.getTrackScrobble(track)
                                        .thenApply(l -> Pair.of(track, l))))
                .toList();
        CompletableFuture<List<Pair<AudioTrack, TrackScrobble>>> finish = sequence(completableFutures);
        finish.thenCompose(result -> manager.getTrackScrobble().thenApply(l -> Pair.of(l, result)))
                .handle((result, tr) -> {
                    if (tr != null) {
                        sendMessageQueue(e, "An error happened while processing the queue!");
                    } else {
                        try {
                            handleList(result, manager, e);
                        } catch (Exception ex) {
                            Chuu.getLogger().warn(ex.getMessage(), ex);
                        }
                    }
                    return null;
                });
    }

}
