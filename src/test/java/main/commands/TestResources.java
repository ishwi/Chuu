package main.commands;

import dao.DaoImplementation;
import dao.entities.ArtistData;
import dao.entities.LastFMData;
import dao.entities.UniqueData;
import dao.entities.UniqueWrapper;
import main.Chuu;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.rules.ExternalResource;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class TestResources extends ExternalResource {
	public static DaoImplementation dao;
	public static JDA testerJDA;
	public static JDA ogJDA;
	public static TextChannel channelWorker;
	public static long channelId;
	public static boolean setUp = false;
	public static long developerId;

	protected synchronized void before() {
		if (!setUp) {
			dao = new DaoImplementation();

			Properties properties = new Properties();
			try (InputStream in = Chuu.class.getResourceAsStream("/tester.properties")) {
				properties.load(in);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			developerId = Long.parseLong(properties.getProperty("DEVELOPER_ID"));

			JDABuilder builder = new JDABuilder(AccountType.BOT).setEventManager(new CustomInterfacedEventManager());
			try {
				testerJDA = builder.setToken(properties.getProperty("DISCORD_TOKEN")).setAutoReconnect(true)
						.build().awaitReady();
			} catch (LoginException | InterruptedException e) {
				e.printStackTrace();
			}

			Chuu.setupBot();
			ogJDA = Chuu.getPresence().getJDA();

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

			UniqueWrapper<UniqueData> artistLeaderboard = dao
					.getUniqueArtist(channelWorker.getGuild().getIdLong(), "pablopita");
			assert artistLeaderboard.getUniqueData().size() >= 1;
			UniqueData uniqueData = artistLeaderboard.getUniqueData().stream().findAny().get();

			//Insert one artist so both have one in common for further tests
			dao.insertArtistDataList(new LastFMData("guilleecs", ogJDA.getSelfUser().getIdLong(), channelWorker
					.getGuild().getIdLong()));
			ArrayList<ArtistData> artistData = new ArrayList<>();
			artistData.add(new ArtistData("guilleecs", uniqueData.getArtistName(), Integer.MAX_VALUE));
			dao.insertArtistDataList(artistData, "guilleecs");
			dao.updateUserTimeStamp("guilleecs", Integer.MAX_VALUE, Integer.MAX_VALUE);

			setUp = true;
		}
	}

	protected void after() {

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
}