package test.commands.utils;

import core.Chuu;
import core.commands.CustomInterfacedEventManager;
import core.util.ServiceView;
import dao.ChuuDatasource;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestResources implements BeforeAllCallback {
    public static ChuuService dao;
    public static JDA testerJDA;
    public static JDA ogJDA;
    public static TextChannel channelWorker;
    public static long channelId;
    public static boolean setUp = false;
    public static long developerId;
    public static String commonArtist;
    public static String testerJdaUsername;
    private static CustomInterfacedEventManager manager;
    private final AtomicBoolean started = new AtomicBoolean();

    private TestResources() {

    }

    public static void deleteCommonArtists() {
        dao.insertNewUser(LastFMData.ofUser("guilleecs"));
        ArrayList<ScrobbledArtist> scrobbledArtistData = new ArrayList<>();
        dao.insertArtistDataList(scrobbledArtistData, "guilleecs");
        dao.updateUserTimeStamp("guilleecs", Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static void insertOnlyKnownSecond(String second, int plays) {
        dao.insertNewUser(LastFMData.ofUser("guilleecs"));
        ArrayList<ScrobbledArtist> scrobbledArtistData = new ArrayList<>();
        scrobbledArtistData.add(new ScrobbledArtist("guilleecs", second, plays));
        dao.insertArtistDataList(scrobbledArtistData, "guilleecs");
        dao.updateUserTimeStamp("guilleecs", Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static void deleteOnlyKnownSecond() {
        dao.insertNewUser(LastFMData.ofUser("guilleecs"));
        ArrayList<ScrobbledArtist> scrobbledArtistData = new ArrayList<>();
        dao.insertArtistDataList(scrobbledArtistData, "guilleecs");
        dao.updateUserTimeStamp("guilleecs", Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static void insertCommonArtistWithPlays(int plays) {
        dao.insertNewUser(LastFMData.ofUser("guilleecs"));
        ArrayList<ScrobbledArtist> scrobbledArtistData = new ArrayList<>();
        scrobbledArtistData.add(new ScrobbledArtist("guilleecs", commonArtist, plays));
        dao.insertArtistDataList(scrobbledArtistData, "guilleecs");
        dao.updateUserTimeStamp("guilleecs", Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static void runLiquibase(Connection connection) throws Exception {

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db/changelog.yaml", new ClassLoaderResourceAccessor(), database);

        liquibase.update(new Contexts(), new LabelExpression());

    }

    public static void callEvent(GenericEvent event) {
        manager.handle(event);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        init();
    }

    private void deleteAllMessage(TextChannel channel) {
        List<Message> messages = channel.getHistory().retrievePast(50).complete();
        OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);

        messages.removeIf(m -> m.getTimeCreated().isBefore(twoWeeksAgo));

        while (messages.size() >= 2) {
            channel.deleteMessages(messages).complete();
            messages = channel.getHistory().retrievePast(50).complete();
            messages.removeIf(m -> m.getTimeCreated().isBefore(twoWeeksAgo));

        }
    }

    private void init() {
        if (!setUp) {
            ChuuDatasource chuuDatasource = new ChuuDatasource();
            dao = new ChuuService(chuuDatasource);
            manager = new CustomInterfacedEventManager();
            ServiceView db = new ServiceView(dao, dao, dao);
            Chuu.initFields();
            Chuu.addAll(db, manager::register);

        }
    }


}
