package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public abstract class DaoParser<T extends CommandParameters> extends Parser<T> {
    final ChuuService dao;


    public DaoParser(ChuuService dao, OptionalEntity... opts) {
        super(opts);
        this.dao = dao;
    }

    LastFMData getLastFmUsername1input(Long id, MessageReceivedEvent event) throws InstanceNotFoundException {
        if (event.isFromGuild()) {
            LastFMData data;
            List<User> list = event.getMessage().getMentionedUsers();
            data = list.isEmpty()
                    ? this.dao.findLastFMData((id))
                    : this.dao.findLastFMData(list.get(0).getIdLong());

            return data;
        } else {
            return dao.findLastFMData((id));
        }

    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(1, "User not on database");
        errorMessages.put(2, "Internal Server Error, try again later");
        errorMessages.put(3, "User hasn't played anything recently");
        errorMessages.put(4, "User does not exist on last.fm");
    }
}
