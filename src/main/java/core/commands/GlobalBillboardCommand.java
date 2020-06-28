package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.HotMaker;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class GlobalBillboardCommand extends BillboardCommand {

    public static final AtomicBoolean settingUp = new AtomicBoolean(false);


    public GlobalBillboardCommand(ChuuService dao) {

        super(dao);
        respondInPrivate = false;

    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.BOT_STATS;
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        NumberParameters<CommandParameters> params = parser.parse(e);
        Week week = getService().getCurrentWeekId();
        if (week.getId() == 1) {
            sendMessageQueue(e, "A full week needs to be completed before this command can work");
            return;
        }
        boolean doListeners = !params.hasOptional("--scrobbles");

        List<BillboardEntity> entities = getEntities(week.getId() - 1, doListeners);
        if (entities.isEmpty()) {
            if (settingUp.compareAndSet(false, true)) {
                getService().insertGlobalBillboardData(week.getId() - 1);
                settingUp.set(false);
            } else {
                sendMessage(e, "The global billboard chart was already being computed, check again later!");
                return;
            }

        }
        entities = getEntities(week.getId() - 1, doListeners);
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

    public List<BillboardEntity> getEntities(int weekId, boolean doListeners) {
        return getService().getGlobalBillboard(weekId, doListeners);

    }


}
