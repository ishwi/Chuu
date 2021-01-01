package core.commands.abstracts;

import core.Chuu;
import core.commands.utils.CommandCategory;
import core.music.MusicManager;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class MusicCommand<T extends CommandParameters> extends ConcurrentCommand<T> {
    protected boolean sameChannel = false;
    protected boolean requirePlayingTrack = false;
    protected boolean requireManager = true;
    protected boolean requirePlayer = false;
    protected boolean requireVoiceState = false;

    public MusicCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (check(e)) {
            super.onMessageReceived(e);
        }
    }

    private boolean check(MessageReceivedEvent e) {
        long idLong = e.getGuild().getIdLong();
        MusicManager manager = Chuu.playerRegistry.getExisting(idLong);

        if (requireManager && manager == null) {
            e.getChannel().sendMessage("There's no music player in this server").queue();
            return false;
        }
        GuildVoiceState voiceState = e.getGuild().getSelfMember().getVoiceState();

        if (e.getMember() == null || e.getMember().getVoiceState() == null) {
            return false;
        }
        GuildVoiceState memberVoiceState = e.getMember().getVoiceState();
        if (requireVoiceState && memberVoiceState.getChannel() == null) {
            e.getChannel().sendMessage("You're not in a voice channel").queue();
            return false;

        }
        if (requirePlayer && (voiceState == null || voiceState.getChannel() == null)) {
            e.getChannel().sendMessage("The bot is not currently in a voice channel.\n").queue();
            return false;

        }
        if (voiceState == null) {
            return false;
        }
        if (sameChannel && memberVoiceState.getChannel() != voiceState.getChannel()) {
            sendMessage(e, "You're not in the same channel as the bot.");
            return false;
        }

        if (requirePlayingTrack && manager.getPlayer().getPlayingTrack() == null) {
            sendMessage(e, "The player is not playing anything.");
            return false;
        }
        return true;

    }
}
