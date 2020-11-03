package core.commands.utils;

import core.apis.last.ConcurrentLastFM;
import core.commands.MyCommand;
import core.services.CommandReportGenerator;
import dao.ChuuService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.stream.Collectors;

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

    public void sendMessage(Object message) {
        e.getChannel().sendMessage(message.toString()).queue();
    }

    public void report() {
        CommandReportGenerator commandReportGenerator = new CommandReportGenerator(jda.getRegisteredListeners().stream().filter(x -> x instanceof MyCommand<?>).map(x -> (MyCommand<?>) x).collect(Collectors.toList()));
        commandReportGenerator.generateReport();
    }

}
