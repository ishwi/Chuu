package core;

import dao.DaoImplementation;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.SpotifySingleton;
import core.commands.*;
import core.scheduledtasks.ImageUpdaterThread;
import core.scheduledtasks.SpotifyUpdaterThread;
import core.scheduledtasks.UpdaterThread;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Chuu {

	public static final Character DEFAULT_PREFIX = '!';
	private static Map<Long, Character> prefixMap;
	private static JDA jda;
	private static Logger logger;

	public static void addGuildPrefix(long guildId, Character prefix) {
		prefixMap.replace(guildId, prefix);
	}

	public static Character getCorrespondingPrefix(MessageReceivedEvent e) {
		if (!e.isFromGuild())
			return DEFAULT_PREFIX;
		long id = e.getGuild().getIdLong();
		Character character = prefixMap.get(id);
		return character == null ? DEFAULT_PREFIX : character;

	}

	public static Map<Long, Character> getPrefixMap() {
		return prefixMap;
	}

	public static Presence getPresence() {
		return Chuu.jda.getPresence();

	}

	public static void main(String[] args) throws  InterruptedException {
		if (System.getProperty("file.encoding").equals("UTF-8")) {
			setupBot(false);
		} else {
			relaunchInUTF8();
		}
	}

	private static void relaunchInUTF8() throws InterruptedException {
		System.out.println("BotLauncher: We are not running in UTF-8 mode! This is a problem!");
		System.out.println("BotLauncher: Relaunching in UTF-8 mode using -Dfile.encoding=UTF-8");

		String[] command = new String[]{"java", "-Dfile.encoding=UTF-8", "-jar",
				Chuu.getThisJarFile().getAbsolutePath()};

		// Relaunches the bot using UTF-8 mode.
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.inheritIO(); // Tells the new process to use the same command line as this one.
		try {
			Process process = processBuilder.start();
			process.waitFor(); // We wait here until the actual bot stops. We do this so that we can keep using
			// the same command line.
			System.exit(process.exitValue());
		} catch (IOException e) {
			if (e.getMessage().contains("\"java\"")) {
				System.out.println(
						"BotLauncher: There was an error relaunching the bot. We couldn't find Java to launch with.");
				System.out.println(
						"BotLauncher: Attempted to relaunch using the command:\n   " + String.join(" ", command));
				System.out.println(
						"BotLauncher: Make sure that you have Java properly set in your Operating System's PATH variable.");
				System.out.println("BotLauncher: Stopping here.");
			} else {
				Chuu.getLogger().warn(e.getMessage(), e);
			}
		}
	}

	private static File getThisJarFile()  {
		// Gets the path of the currently running Jar file
		String path = Chuu.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

		// This is code especially written for running and testing this program in an
		// IDE that doesn't compile to .jar when running.
		if (!decodedPath.endsWith(".jar"))
			return new File("Chuu.jar");
		return new File(decodedPath); // We use File so that when we send the path to the ProcessBuilder, we will be
		// using the proper System path formatting.
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setupBot(boolean isTest) {
		logger = LoggerFactory.getLogger(Chuu.class);
		Properties properties = readToken();
		DaoImplementation dao = new DaoImplementation();
		prefixMap = initPrefixMap(dao);
		new DiscogsSingleton(properties.getProperty("DC_SC"), properties.getProperty("DC_KY"));
		new SpotifySingleton(properties.getProperty("client_ID"), properties.getProperty("client_Secret"));

		// Needs these three references
		HelpCommand help = new HelpCommand(dao);
		AdministrativeCommand commandAdministrator = new AdministrativeCommand(dao);
		PrefixCommand prefixCommand = new PrefixCommand(dao);

		ScheduledExecutorService scheduledManager = Executors.newScheduledThreadPool(3);
		if(!isTest) {
			scheduledManager.scheduleAtFixedRate(
					new UpdaterThread(dao, null, true, DiscogsSingleton.getInstanceUsingDoubleLocking()), 0, 60,
					TimeUnit.SECONDS);
			scheduledManager.scheduleAtFixedRate(new ImageUpdaterThread(dao), 3, 10, TimeUnit.MINUTES);
			scheduledManager.scheduleAtFixedRate(
					new SpotifyUpdaterThread(dao, SpotifySingleton.getInstanceUsingDoubleLocking()), 0, 10,
					TimeUnit.MINUTES);
		}
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(properties.getProperty("DISCORD_TOKEN")).setAutoReconnect(true)
				.setEventManager(new CustomInterfacedEventManager()).addEventListeners(help)
				.addEventListeners(help.registerCommand(commandAdministrator))
				.addEventListeners(help.registerCommand(new NowPlayingCommand(dao)))
				.addEventListeners(help.registerCommand(new WhoKnowsCommand(dao)))
				// .addEventListeners(help.registerCommand(new WhoKnowsNPCommand(dao)))
				.addEventListeners(help.registerCommand(new ChartCommand(dao)))
				.addEventListeners(help.registerCommand(new SetCommand(dao)))
				.addEventListeners(help.registerCommand(new AllPlayingCommand(dao)))
				.addEventListeners(help.registerCommand(new TasteCommand(dao)))
				.addEventListeners(help.registerCommand(new TopCommand(dao)))
				.addEventListeners(help.registerCommand(new UpdateCommand(dao)))
				.addEventListeners(help.registerCommand(new NPSpotifyCommand(dao)))
				.addEventListeners(help.registerCommand(new UniqueCommand(dao)))
				.addEventListeners(help.registerCommand(new ArtistCommand(dao)))
				.addEventListeners(help.registerCommand(new AlbumPlaysCommand(dao)))
				.addEventListeners(help.registerCommand(new GuildTopCommand(dao)))
				.addEventListeners(help.registerCommand(new ArtistUrlCommand(dao)))
				.addEventListeners(help.registerCommand(new BandInfoCommand(dao)))
				// .addEventListeners(help.registerCommand(new BandInfoNpCommand(dao)))
				.addEventListeners(help.registerCommand(new WhoKnowsAlbumCommand(dao)))
				.addEventListeners(help.registerCommand(new CrownLeaderboardCommand(dao)))
				.addEventListeners(help.registerCommand(new CrownsCommand(dao)))
				.addEventListeners(help.registerCommand(new RecentListCommand(dao)))
				.addEventListeners(help.registerCommand(new MusicBrainzCommand(dao)))
				.addEventListeners(help.registerCommand(new GenreCommand(dao)))
				.addEventListeners(help.registerCommand(new MbizThisYearCommand(dao)))
				.addEventListeners(help.registerCommand(new ArtistPlaysCommand(dao)))
				.addEventListeners(help.registerCommand(new TimeSpentCommand(dao)))
				.addEventListeners(help.registerCommand(new YoutubeSearchCommand(dao)))
				.addEventListeners(help.registerCommand(new UniqueLeaderboardCommand(dao)))
				.addEventListeners(help.registerCommand(new ProfileInfoCommand(dao)))
				.addEventListeners(help.registerCommand(new ArtistCountLeaderboard(dao)))
				.addEventListeners(help.registerCommand(new TotalArtistNumberCommand(dao)))
				.addEventListeners(help.registerCommand(new CountryCommand(dao)))
				.addEventListeners(help.registerCommand(new AlbumTracksDistributionCommand(dao)))
				.addEventListeners(help.registerCommand(new ObscurityLeaderboardCommand(dao)))
				.addEventListeners(help.registerCommand(new FeaturedCommand(dao, scheduledManager)))
				.addEventListeners(help.registerCommand(new RandomAlbumCommand(dao)))
				.addEventListeners(help.registerCommand(new WhoKnowsSongCommand(dao)))
				.addEventListeners(help.registerCommand(new CrownsStolenCommand(dao)))
				.addEventListeners(help.registerCommand(new FavesFromArtistCommand(dao)))
				.addEventListeners(help.registerCommand(new AlbumCrownsCommand(dao)))
				.addEventListeners(help.registerCommand(new AlbumCrownsLeaderboardCommand(dao)))
				.addEventListeners(help.registerCommand(new PrefixCommand(dao)))
				.addEventListeners(help.registerCommand(new DailyCommand(dao)))
				.addEventListeners(help.registerCommand(new WeeklyCommand(dao)))
				.addEventListeners(help.registerCommand(new UserTopTrackCommand(dao)))
				.addEventListeners(help.registerCommand(new SummaryArtistCommand(dao)));

		try {
			jda = builder.build().awaitReady();
			commandAdministrator.onStartup(jda);
			prefixCommand.onStartup(jda);
			updatePresence("Chuu");

		} catch (LoginException | InterruptedException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
		}
	}

	private static Map<Long, Character> initPrefixMap(DaoImplementation dao) {
		return (dao.getGuildPrefixes());
	}

	static private Properties readToken() {

		Properties properties = new Properties();
		try (InputStream in = Chuu.class.getResourceAsStream("/all.properties")) {
			properties.load(in);
			return properties;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void updatePresence(String artist) {
		Chuu.jda.getPresence().setActivity(Activity.playing(artist + " | !help for help"));

	}

}
