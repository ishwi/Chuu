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
 Thanks to Octave Bot. integrating with JAVA and the current bot
 */
package core.commands.music;

import core.Chuu;
import core.commands.Context;
import core.music.LoadResultHandler;
import core.music.MusicManager;
import core.music.utils.TrackContext;
import core.parsers.MusicInputParser;
import core.parsers.Parser;
import core.parsers.params.WordParameter;
import dao.ServiceView;

import javax.annotation.Nonnull;
import java.util.List;

public class EnqueueYTMusicCommand extends core.commands.abstracts.MusicCommand<WordParameter> {


    public EnqueueYTMusicCommand(ServiceView dao) {
        super(dao);
        requireManager = false;
        requireVoiceState = true;
    }

    public static void play(Context e, MusicManager manager, String query, boolean isSearchResult, boolean isNext) {
        MusicManager musicManager = Chuu.playerRegistry.get(e.getGuild());


        if (query.contains("https://") || query.contains("http://") || query.startsWith("spotify:")) {
            if (query.startsWith("<")) {
                query = query.substring(1);
            }
            if (query.endsWith(">")) {
                query = query.substring(0, query.length() - 1);
            }
        } else if (isSearchResult) {
            Chuu.getLogger().info(":)");
        } else {
            query = String.format("ytmsearch:%s", query.trim());
        }
        TrackContext trackContext = new TrackContext(e.getAuthor().getIdLong(), e.getChannel().getIdLong());
        LoadResultHandler.loadItem(query, e, musicManager, trackContext, isNext, "");
    }


    @Override
    public Parser<WordParameter> initParser() {
        return new MusicInputParser();
    }

    @Override
    public String getDescription() {
        return "Uses youtube music instead of youtube";
    }

    @Override
    public List<String> getAliases() {
        return List.of("playyoutubemusic", "pytm", "pytmusic");
    }

    @Override
    public String getName() {
        return "Play music from youtube music";
    }

    @Override
    public void onCommand(Context e, @Nonnull WordParameter params) {

        var newManager = Chuu.playerRegistry.get(e.getGuild());
        play(e, newManager, params.getWord(), false, false);
    }

}

