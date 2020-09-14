package core.commands.utils;

import core.apis.last.ConcurrentLastFM;
import dao.ChuuService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EvalContext {
    public final JDA jda;
    public final MessageReceivedEvent e;
    public final User owner;
    public final Guild guild;
    public final String[] params;
    public final ChuuService db;
    public final ConcurrentLastFM lastFM;


    public EvalContext(JDA jda, MessageReceivedEvent e, User owner, Guild guild, String[] params, ChuuService
            db, ConcurrentLastFM lastFM) {
        this.jda = jda;
        this.e = e;
        this.owner = owner;
        this.guild = guild;
        this.params = params;
        this.db = db;
        this.lastFM = lastFM;
    }

    public void sendMessage(String message) {
        e.getChannel().sendMessage(message).queue();
    }

}
