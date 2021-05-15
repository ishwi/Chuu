package core.commands.abstracts;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.music.MusicManager;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.function.Function;

public abstract class MusicCommand<T extends CommandParameters> extends ConcurrentCommand<T> {
    protected static final Function<Character, String> PLAY_MESSAGE = (s) -> "\uD83C\uDFB6 `" + s + "play (song/url)` in a voice channel to start playing some music!";
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
    protected boolean handleCommand(Context e) {
        if (check(e)) {
            return super.handleCommand(e);
        }
        return true;
    }


    public MusicManager getManager(Context e) {
        return Chuu.playerRegistry.getExisting(e.getGuild().getIdLong());
    }

    private boolean check(Context e) {
        long idLong = e.getGuild().getIdLong();
        MusicManager manager = Chuu.playerRegistry.getExisting(idLong);
        char messagePrefix = CommandUtil.getMessagePrefix(e);
        if (requireManager && manager == null) {
            e.sendMessage("There's no music player in this server.\n" + PLAY_MESSAGE.apply(messagePrefix)).queue();
            return false;
        }
        GuildVoiceState voiceState = e.getGuild().getSelfMember().getVoiceState();

        if (e.getMember() == null || e.getMember().getVoiceState() == null) {
            return false;
        }
        GuildVoiceState memberVoiceState = e.getMember().getVoiceState();
        if (requireVoiceState && memberVoiceState.getChannel() == null) {
            e.sendMessage("You're not in a voice channel").queue();
            return false;

        }
        if (requirePlayer && (voiceState == null || voiceState.getChannel() == null)) {
            e.sendMessage("The bot is not currently in a voice channel.\n" + PLAY_MESSAGE.apply(messagePrefix)).queue();
            return false;

        }
        assert voiceState != null;
        if (sameChannel && memberVoiceState.getChannel() != voiceState.getChannel()) {
            sendMessageQueue(e, "You're not in the same channel as the bot.");
            return false;
        }

        if (requirePlayingTrack && manager.getPlayer().getPlayingTrack() == null) {
            sendMessageQueue(e, "The player is not playing anything.");
            return false;
        }
        return true;
    }
}
