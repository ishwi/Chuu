package core.commands.utils;

import core.apis.last.ConcurrentLastFM;
import dao.ChuuService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public record EvalContext(JDA jda, MessageReceivedEvent e,
                          User owner, Guild guild,
                          String[] params, ChuuService db, ConcurrentLastFM lastFM) {

    public void sendMessage(Object message) {
        e.getChannel().sendMessage(message.toString()).queue();
    }

}
