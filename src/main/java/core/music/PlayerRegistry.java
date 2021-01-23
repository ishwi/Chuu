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
package core.music;

import net.dv8tion.jda.api.entities.Guild;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerRegistry {
    private final ExtendedAudioPlayerManager playerManager;

    public PlayerRegistry(ExtendedAudioPlayerManager playerManager) {
        this.playerManager = playerManager;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::sweep, 3, 3, TimeUnit.MINUTES);
    }

    final Map<Long, MusicManager> registry = new ConcurrentHashMap<>(10);

    private final long playTimeout = TimeUnit.MINUTES.toMillis(2);


    public void sweep() {
        registry.values().stream().filter(it -> {
            // If guild null, or if connected, and not playing, and not queued for leave,
            // if last played >= IDLE_TIMEOUT minutes ago, and not 24/7 (all day) music, destroy/queue leave.
            Guild guild = it.getGuild();
            return guild == null || !guild.getAudioManager().isConnected() && it.isIdle() &&
                    !it.isLeaveQueued() && System.currentTimeMillis() - it.getLastPlayedAt() > playTimeout &&
                    !isAllDayMusic(it.getGuildId());
        }).forEach(it -> {
            if (it.getGuild() == null) {
                destroy(it.getGuildId());
            } else {
                it.queueLeave();//Then queue leave.
            }
        });
    }


    public synchronized MusicManager get(Guild guild) {
        return registry.computeIfAbsent(guild.getIdLong(), (k) -> new MusicManager(k, playerManager.createPlayer(), this.playerManager));
    }

    public MusicManager getExisting(long id) {
        return registry.get(id);
    }

    public MusicManager getExisting(Guild guild) {
        return registry.get(guild.getIdLong());
    }


    public void destroy(long id) {
        MusicManager remove = registry.remove(id);
        if (remove != null) {
            remove.cleanup();
        }
    }

    public void destroy(Guild guild) {
        destroy(guild.getIdLong());
    }

    public boolean contains(long id) {
        return registry.containsKey(id);
    }

    public boolean contains(Guild guild) {
        return contains(guild.getIdLong());
    }

    public int getSize() {
        return registry.size();
    }


    private boolean isAllDayMusic(long guildId) {
        return false;
    }

}
