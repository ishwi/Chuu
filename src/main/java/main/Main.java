package main;

import DAO.DaoImplementation;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import main.Commands.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class Main extends ListenerAdapter {


	public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {

		if (System.getProperty("file.encoding").equals("UTF-8")) {
			setupBot();
		} else {
			relaunchInUTF8();
		}
	}

	static File getThisJarFile() throws UnsupportedEncodingException {
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
		Map<String, String> map = readToken();
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		DaoImplementation dao = new DaoImplementation();
		Spotify spotifyWrapper = new Spotify(map.get("clientId"), map.get("clientSecret"));
		builder.setToken(map.get("discordtoken"));
		HelpCommand help = new HelpCommand();
		builder.addEventListener(help);
		builder.addEventListener(help.registerCommand(new NowPlayingCommand(dao)));
		builder.addEventListener(help.registerCommand(new WhoKnowsCommand(dao)));
		builder.addEventListener(help.registerCommand(new WhoKnowsNPCommand(dao)));
		builder.addEventListener(help.registerCommand(new ChartCommand(dao)));
		builder.addEventListener(help.registerCommand(new SetCommand(dao)));
		builder.addEventListener(help.registerCommand(new AllPlayingCommand(dao)));
		builder.addEventListener(help.registerCommand(new TasteCommand(dao)));
		builder.addEventListener(help.registerCommand(new TopCommand(dao)));
		builder.addEventListener(help.registerCommand(new UpdateCommand(dao)));
		builder.addEventListener(help.registerCommand(new NPSpotifyCommand(dao, spotifyWrapper)));
		builder.addEventListener(help.registerCommand(new UniqueCommand(dao)));
		builder.addEventListener(help.registerCommand(new NPYoutubeCommand(dao)));
		builder.addEventListener(help.registerCommand(new ArtistCommand(dao)));

//		builder.addEventListeners(help.registerCommand(new AlbumSongPlaysCommand(dao)));
		EventWaiter waiter = new EventWaiter(Executors.newSingleThreadScheduledExecutor(), false);

//		builder.addEventListener(waiter);
//		builder.addEventListener(help.registerCommand(new GuildTopCommand(dao, waiter)));
//		builder.addEventListener(help.registerCommand(new CrownsCommand(dao, waiter)));

		ScheduledExecutorService scheduledManager = Executors.newScheduledThreadPool(1);
		scheduledManager.scheduleAtFixedRate(new UpdaterThread(dao, null, true), 0, 2, TimeUnit.MINUTES);

		try {
			builder.build().awaitReady();


		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();

		}
	}

	private static Map<String, String> readToken() {
		BufferedReader br = null;

		Map<String, String> map = new HashMap<>();
		try {
			br = new BufferedReader(new FileReader("C:\\Users\\Ishwi\\token.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			StringBuilder sb = new StringBuilder();
			assert br != null;
			map.put("discordtoken", br.readLine());
			map.put("clientId", br.readLine());
			map.put("clientSecret", br.readLine());

			return map;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert br != null;
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new RuntimeException();

	}
}

