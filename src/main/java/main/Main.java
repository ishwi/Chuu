package main;

import DAO.DaoImplementation;
import main.APIs.Discogs.DiscogsSingleton;
import main.APIs.Spotify.SpotifySingleton;
import main.Commands.*;
import main.ScheduledTasks.ImageUpdaterThread;
import main.ScheduledTasks.SpotifyUpdaterThread;
import main.ScheduledTasks.UpdaterThread;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class Main {


	public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {

		if (System.getProperty("file.encoding").equals("UTF-8")) {
			setupBot();
		} else {
			relaunchInUTF8();
		}
	}

	private static File getThisJarFile() throws UnsupportedEncodingException {
		//Gets the path of the currently running Jar file
		String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, "UTF-8");

		//This is code especially written for running and testing this program in an IDE that doesn't compile to .jar when running.
		if (!decodedPath.endsWith(".jar")) {
			return new File("Yui.jar");
		}
		return new File(decodedPath);   //We use File so that when we send the path to the ProcessBuilder, we will be using the proper System path formatting.
	}


	private static void relaunchInUTF8() throws InterruptedException, UnsupportedEncodingException {
		System.out.println("BotLauncher: We are not running in UTF-8 mode! This is a problem!");
		System.out.println("BotLauncher: Relaunching in UTF-8 mode using -Dfile.encoding=UTF-8");

		String[] command = new String[]{"java", "-Dfile.encoding=UTF-8", "-jar", Main.getThisJarFile().getAbsolutePath()};

		//Relaunches the bot using UTF-8 mode.
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.inheritIO(); //Tells the new process to use the same command line as this one.
		try {
			Process process = processBuilder.start();
			process.waitFor();  //We wait here until the actual bot stops. We do this so that we can keep using the same command line.
			System.exit(process.exitValue());
		} catch (IOException e) {
			if (e.getMessage().contains("\"java\"")) {
				System.out.println("BotLauncher: There was an error relaunching the bot. We couldn't find Java to launch with.");
				System.out.println("BotLauncher: Attempted to relaunch using the command:\n   " + String.join(" ", command));
				System.out.println("BotLauncher: Make sure that you have Java properly set in your Operating System's PATH variable.");
				System.out.println("BotLauncher: Stopping here.");
			} else {
				e.printStackTrace();
			}
		}
	}


	private static void setupBot() {
		Properties properties = readToken();
		JDABuilder builder = new JDABuilder(AccountType.BOT);

		DaoImplementation dao = new DaoImplementation();
		SpotifySingleton a = new SpotifySingleton(properties.getProperty("client_ID"), properties.getProperty("client_Secret"));
		//Spotify spotifyWrapper = new Spotify(properties.getProperty("client_ID"), properties.getProperty("client_Secret"));
		DiscogsSingleton b = new DiscogsSingleton(properties.getProperty("DC_SC"), properties.getProperty("DC_KY"));
		builder.setToken(properties.getProperty("DISCORD_TOKEN"));
		HelpCommand help = new HelpCommand();
		//Dao get all users discord ID and if someone not already -> erase
		// Add event listener for someone when he leaves -> erase

		builder.addEventListeners(help);
		AdministrativeCommand commandAdministrator = new AdministrativeCommand(dao);
		builder.addEventListeners(help.registerCommand(commandAdministrator));
		builder.addEventListeners(help.registerCommand(new NowPlayingCommand(dao)));
		builder.addEventListeners(help.registerCommand(new WhoKnowsCommand(dao)));
		builder.addEventListeners(help.registerCommand(new WhoKnowsNPCommand(dao)));
		builder.addEventListeners(help.registerCommand(new ChartCommand(dao)));
		builder.addEventListeners(help.registerCommand(new SetCommand(dao)));
		builder.addEventListeners(help.registerCommand(new AllPlayingCommand(dao)));
		builder.addEventListeners(help.registerCommand(new TasteCommand(dao)));
		builder.addEventListeners(help.registerCommand(new TopCommand(dao)));
		builder.addEventListeners(help.registerCommand(new UpdateCommand(dao)));
		builder.addEventListeners(help.registerCommand(new NPSpotifyCommand(dao)));
		builder.addEventListeners(help.registerCommand(new UniqueCommand(dao)));
		builder.addEventListeners(help.registerCommand(new NPYoutubeCommand(dao)));
		builder.addEventListeners(help.registerCommand(new ArtistCommand(dao)));
		builder.addEventListeners(help.registerCommand(new AlbumSongPlaysCommand(dao)));
		builder.addEventListeners(help.registerCommand(new GuildTopCommand(dao)));
		builder.addEventListeners(help.registerCommand(new ArtistUrlCommand(dao)));
		builder.addEventListeners(help.registerCommand(new BandInfoCommand(dao)));
		builder.addEventListeners(help.registerCommand(new BandInfoNpCommand(dao)));
		builder.addEventListeners(help.registerCommand(new AlbumGuildPlays(dao)));


		//EventWaiter waiter = new EventWaiter(Executors.newSingleThreadScheduledExecutor(), false);
		//builder.addEventListener(waiter);
		builder.addEventListeners(help.registerCommand(new CrownsCommand(dao)));

		ScheduledExecutorService scheduledManager = Executors.newScheduledThreadPool(2);
		scheduledManager.scheduleAtFixedRate(new UpdaterThread(dao, null, true, DiscogsSingleton.getInstanceUsingDoubleLocking()), 0, 30, TimeUnit.SECONDS);
		scheduledManager.scheduleAtFixedRate(new ImageUpdaterThread(dao), 3, 10, TimeUnit.MINUTES);
		scheduledManager.scheduleAtFixedRate(new SpotifyUpdaterThread(dao, SpotifySingleton.getInstanceUsingDoubleLocking()), 0, 10, TimeUnit.MINUTES);


		try {
			JDA jda = builder.build().awaitReady();
			commandAdministrator.onStartup(jda);


		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();

		}
	}

	static private Properties readToken() {

		Properties properties = new Properties();

		try (InputStream in = Main.class.getResourceAsStream("/" + "all.properties")) {
			properties.load(in);

			return properties;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

