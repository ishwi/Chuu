package test.commands.utils;

import core.Chuu;
import core.commands.CustomInterfacedEventManager;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.UniqueWrapper;
import dao.exceptions.ChuuServiceException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;

public class TestResourcesSingleton extends ExternalResource {
    public static final TestRule INSTANCE = new TestResourcesSingleton();
    public static ChuuService dao;
    public static JDA testerJDA;
    public static JDA ogJDA;
    public static TextChannel channelWorker;
    public static long channelId;
    public static boolean setUp = false;
    public static long developerId;
    public static String commonArtist;
    public static String testerJdaUsername;
    private final AtomicBoolean started = new AtomicBoolean();

    public static void deleteCommonArtists() {
        dao.insertNewUser(LastFMData.ofUser("guillecs"));
        ArrayList<ScrobbledArtist> scrobbledArtistData = new ArrayList<>();
        dao.insertArtistDataList(scrobbledArtistData, "guilleecs");
        dao.updateUserTimeStamp("guilleecs", Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static void insertCommonArtistWithPlays(int plays) {
        dao.insertNewUser(LastFMData.ofUser("guillecs"));
        ArrayList<ScrobbledArtist> scrobbledArtistData = new ArrayList<>();
        scrobbledArtistData.add(new ScrobbledArtist("guilleecs", commonArtist, plays));
        dao.insertArtistDataList(scrobbledArtistData, "guilleecs");
        dao.updateUserTimeStamp("guilleecs", Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    protected void before() throws LoginException {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        init();
        // Initialization code goes here
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

    private void init() throws LoginException {
        if (!setUp) {
            dao = new ChuuService(null);

            Properties properties = new Properties();
            try (InputStream in = Chuu.class.getResourceAsStream("/tester.properties")) {
                properties.load(in);
            } catch (IOException e) {
                throw new ChuuServiceException(e);
            }
            developerId = Long.parseLong(properties.getProperty("DEVELOPER_ID"));


            ogJDA = JDABuilder.createDefault(properties.getProperty("DISCORD_TOKEN")).setEventManager(new CustomInterfacedEventManager(0)).build();
            Chuu.setupBot(true, false, true, false);
            ogJDA = Chuu.getShardManager().getShards().get(0);

            Guild testing_server = testerJDA.getGuildById(properties.getProperty("TESTING_SERVER"));
            assert (testing_server != null);

            channelWorker = testing_server.getDefaultChannel();
            assert (channelWorker != null);
            channelId = channelWorker.getIdLong();
            deleteAllMessage(channelWorker);

            //Setting up one bot
            long id = channelWorker.sendMessage("!set pablopita").complete().getIdLong();
            await().atMost(2, TimeUnit.MINUTES).until(() ->
            {
                MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
                if (complete.getRetrievedHistory().size() == 3) {
                    return true;
                }
                if (complete.getRetrievedHistory().size() == 1) {
                    Message message = complete.getRetrievedHistory().get(0);
                    return message.getContentRaw().equals("That username is already registered in this server sorry");
                }
                return false;
            });

            UniqueWrapper<ArtistPlays> artistLeaderboard = dao
                    .getUniqueArtist(channelWorker.getGuild().getIdLong(), "pablopita");
            assert artistLeaderboard.getUniqueData().size() >= 1;
            ArtistPlays artistPlays = artistLeaderboard.getUniqueData().stream().findFirst().get();
            commonArtist = artistPlays.getArtistName();

            //Insert one artist so both have one in common for further tests
            insertCommonArtistWithPlays(1);
            Optional<Member> first = channelWorker.getMembers().stream().filter(x -> x.getId()
                    .equals(testerJDA.getSelfUser().getId())).findFirst();
            assert first.isPresent();
            testerJdaUsername = first.get().getEffectiveName();
            setUp = true;
        }
    }

    @Override
    protected void after() {
    }
}
