package core.commands.billboard;

import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import dao.ServiceView;
import dao.entities.BillboardEntity;
import dao.entities.Week;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GlobalBillboardCommand extends BillboardCommand {

    public static final AtomicBoolean settingUp = new AtomicBoolean(false);


    public GlobalBillboardCommand(ServiceView dao) {

        super(dao);
        respondInPrivate = false;

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.TRENDS;
    }


    @Override
    public String getDescription() {
        return "The most popular tracks last week on this server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalbillboard", "globaltrend", "gtrend", "gt");
    }

    @Override
    public String getName() {
        return "Bot's Billboard Top 100";
    }

    @Override
    protected void onCommand(Context e, @NotNull NumberParameters<CommandParameters> params) {

        Week week = db.getCurrentWeekId();
        if (week.getId() == 1) {
            sendMessageQueue(e, "A full week needs to be completed before this command can work");
            return;
        }
        boolean doListeners = !params.hasOptional("scrobbles");

        List<BillboardEntity> entities = getEntities(week.getId() - 1, doListeners, e);
        if (entities.isEmpty()) {
            try {
                if (settingUp.compareAndSet(false, true)) {
                    sendMessageQueue(e, "The global billboard chart didn't exist. Will begin the process to build it.");
                    db.insertGlobalBillboardData(week.getId() - 1);
                    settingUp.set(false);
                } else {
                    sendMessageQueue(e, "The global billboard chart was already being computed, check again later!");
                    return;
                }
            } finally {
                settingUp.set(false);
            }
        }
        entities = getEntities(week.getId() - 1, doListeners, e);
        String name = e.getJDA().getSelfUser().getName();
        LocalDateTime weekStart = week.getWeekStart().toLocalDate().atStartOfDay();
        LocalDateTime weekBeggining = weekStart.minus(1, ChronoUnit.WEEKS);
        weekStart = weekBeggining.minus(1, ChronoUnit.WEEKS);

        if (entities.isEmpty()) {
            sendMessageQueue(e, "Didn't found any scrobble in the whole bot");
            return;
        }
        doBillboard(e, params, doListeners, entities, weekBeggining, weekStart, name);
    }

    // You have to call the insert_weeks procedure first that is declared in MariadBnew. on the mysql client it would be something like `call inert_weeks()`

    public List<BillboardEntity> getEntities(int weekId, boolean doListeners, Context e) {
        return db.getGlobalBillboard(weekId, doListeners);

    }


}
