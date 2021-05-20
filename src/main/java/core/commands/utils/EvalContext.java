package core.commands.utils;

import com.zaxxer.hikari.HikariDataSource;
import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import dao.ChuuService;
import dao.ServiceView;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.reflect.FieldUtils;

public record EvalContext(JDA jda, Context e,
                          User owner, Guild guild,
                          String[] params, ChuuService db, ConcurrentLastFM lastFM) {

    public void sendMessage(Object message) {
        e.sendMessage(message.toString()).queue();
    }


    public void checkDB() throws Exception {
        var db = (ServiceView) FieldUtils.readStaticField(core.Chuu.class, "db", true);
        var b = FieldUtils.readField(db.longService(), "dataSource", true);
        var longPool = ((HikariDataSource) (FieldUtils.readField(b, "ds", true))).getHikariPoolMXBean();


        var db2 = (ServiceView) FieldUtils.readStaticField(core.Chuu.class, "db", true);
        var b2 = FieldUtils.readField(db.normalService(), "dataSource", true);
        var shortPool = ((HikariDataSource) (FieldUtils.readField(b, "ds", true))).getHikariPoolMXBean();

        String a = "Short pool => %d total | %d active | %d idle | %d waiting".formatted(shortPool.getTotalConnections(), shortPool.getActiveConnections(), shortPool.getIdleConnections(), shortPool.getThreadsAwaitingConnection());
        String b3 = "Long pool => %d total | %d active | %d idle | %d waiting".formatted(longPool.getTotalConnections(), longPool.getActiveConnections(), longPool.getIdleConnections(), longPool.getThreadsAwaitingConnection());
        sendMessage(a + "\n" + b3);
    }
}
