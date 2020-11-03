package core.commands;

import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class InviteCommandImpl extends InviteCommand {
    public InviteCommandImpl(ChuuService dao) {
        super(dao);
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        super.onCommand(e);
    }
}
