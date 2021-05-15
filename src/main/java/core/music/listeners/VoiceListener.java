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
package core.music.listeners;

import core.Chuu;
import core.music.MusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public class VoiceListener implements EventListener {

    public void onEvent(@NotNull GenericEvent event) {
        if (!(event instanceof GenericGuildVoiceEvent) || !core.Chuu.isLoaded()) {
            return;
        }

        if (event instanceof GuildVoiceJoinEvent e) {
            onGuildVoiceJoin(e);
        } else if (event instanceof GuildVoiceLeaveEvent e) {
            onGuildVoiceLeave(e);
        } else if (event instanceof GuildVoiceMoveEvent e) {
            onGuildVoiceMove(e);
        }
    }

    private void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getMember().getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong()) {
            checkVoiceState(event.getGuild());
        }
    }

    private void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getMember().getUser().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            Chuu.playerRegistry.destroy(event.getGuild());
        } else {
            checkVoiceState(event.getGuild());
        }
    }

    private void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getMember().getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong()) {
            return;
        }

        MusicManager manager = Chuu.playerRegistry.getExisting(event.getGuild());
        if (manager == null) {
            return;
        }
        if (event.getGuild().getAfkChannel() != null && event.getChannelJoined().getId().equals(event.getGuild().getAfkChannel().getId())) {
            Chuu.playerRegistry.destroy(event.getGuild());
            return;
        }


        checkVoiceState(event.getGuild());
    }

    private void checkVoiceState(Guild guild) {
        MusicManager manager = Chuu.playerRegistry.getExisting(guild);
        if (manager == null) {
            return;
        }

        if (guild.getAudioManager().getConnectedChannel() == null) {
            Chuu.playerRegistry.destroy(guild);
        }

//        val guildData = OptionsRegistry.ofGuild(guild.id)
//        val premiumGuild = Launcher.database.getPremiumGuild(guild.id)
//        val avoidLeave = (premiumGuild != null || guildData.isPremium) && guildData.music.isAllDayMusic

        if (manager.isAlone() && !manager.isLeaveQueued()) {
            manager.queueLeave();
        } else if (!manager.isAlone() && manager.isLeaveQueued()) {
            manager.cancelLeave();
        }
    }
}
