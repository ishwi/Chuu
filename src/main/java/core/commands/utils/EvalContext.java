package core.commands.utils;

import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import dao.ChuuService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public record EvalContext(JDA jda, Context e,
                          User owner, Guild guild,
                          String[] params, ChuuService db, ConcurrentLastFM lastFM) {

    public void sendMessage(Object message) {
        e.sendMessage(message.toString()).queue();
    }

}
