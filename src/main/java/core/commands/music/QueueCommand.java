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
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import jdk.incubator.concurrent.StructuredTaskScope;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class QueueCommand extends MusicCommand<CommandParameters> {
    public QueueCommand(ServiceView dao) {
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


    private void handleList(List<DecodedAndCoded> result, MusicManager manager, Context e, DecodedAndCoded dataNp) {


        TrackScrobble np = dataNp.scrobble;
        AudioTrack current = dataNp.track;

        long duration = result.stream().mapToLong(z -> z.track().getDuration()).sum();

        List<String> str = result.stream()
                .map(z -> switch (z) {
                    case DecodedAndCoded(AudioTrack tr, TrackScrobble sc) -> {
                        String item;
                        if (sc.processeds().size() > 1) {
                            item = "__%s__ [%d/%d]".formatted(sc.scrobble().toLink(tr.getInfo().uri), 1, sc.processeds().size());
                        } else {
                            item = "__%s__".formatted(sc.scrobble().toLink(tr.getInfo().uri));
                        }
                        yield "`[%s]` %s\n".formatted(CommandUtil.msToString(tr.getDuration()), item);
                    }
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

        if (str.isEmpty()) {
            // Thinking emoji
            str = Collections.singletonList("Nothing in the queue-");
        } else {
            eb.addField("Entries", String.valueOf(result.size()), true)
                    .addField("Total Duration", CommandUtil.msToString(duration), true);
        }


        eb.addField("Repeating", manager.getRepeatOption().getEmoji(), true);

        new PaginatorBuilder<>(e, eb, str).unnumered().withIndicator().build().queue();

    }

    @Override
    public void onCommand(Context e, @NotNull CommandParameters params) {
        MusicManager manager = Chuu.playerRegistry.getExisting(e.getGuild().getIdLong());
        if (manager == null) {
            sendMessageQueue(e, "There's no music manager in this server");
            return;
        }
        Queue<String> queue = manager.getQueue();


        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<Future<DecodedAndCoded>> items = queue.stream().map(decoding ->
                    scope.fork(() ->
                    {
                        AudioTrack track = Chuu.playerManager.decodeAudioTrack(decoding);
                        TrackScrobble cf = manager.getTrackScrobble(track).get(10, TimeUnit.SECONDS);
                        return new DecodedAndCoded(track, cf);
                    })).toList();
            Future<DecodedAndCoded> last = scope.fork(() -> new DecodedAndCoded(manager.getCurrentTrack(), manager.getTrackScrobble().join()));
            scope.join();

            List<DecodedAndCoded> decodedAndCodeds = items.stream().map(Future::resultNow).toList();
            handleList(decodedAndCodeds, manager, e, last.resultNow());
        } catch (InterruptedException ex) {
            sendMessageQueue(e, "An error happened while processing the queue!");
            throw new RuntimeException(ex);
        }


    }

    record DecodedAndCoded(AudioTrack track, TrackScrobble scrobble) {

    }

}
