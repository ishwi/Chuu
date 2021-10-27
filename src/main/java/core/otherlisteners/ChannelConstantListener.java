package core.otherlisteners;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public abstract class ChannelConstantListener implements ConstantListener {
    private final long channelId;

    public ChannelConstantListener(long channelId) {
        this.channelId = channelId;
    }

    public long getChannelId() {
        return channelId;
    }

    @Override
    public boolean isValid(ButtonClickEvent e) {
        return e.getChannel().getIdLong() == channelId;
    }


}
