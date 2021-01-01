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

//        val options = OptionsRegistry.ofGuild(event.guild)
//
//        if (options.music.channels.isNotEmpty() && event.channelJoined.id !in options.music.channels){
//            manager.announcementChannel
//                    ?.
//            sendMessage("Cannot join `${event.channelJoined.name}`, it isn't one of the designated music channels.")
//                    ?.queue()
//
//            return Launcher.players.destroy(event.guild.idLong)
//        }

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
