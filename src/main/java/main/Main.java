package main;

import DAO.DaoImplementation;
import main.last.ConcurrentLastFM;
import main.last.LastFMService;
import main.last.UpdaterThread;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {


	public static void main(String[] args) throws IOException, GeneralSecurityException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		LastFMService last = new ConcurrentLastFM();
		DaoImplementation dao = new DaoImplementation();

		String token = readToken();
		builder.setToken(token);
		builder.addEventListeners(new ListenerLauncher(last, dao));


		try {
			builder.build().awaitReady();

		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();

		}
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new UpdaterThread(dao, last), 0, 15, TimeUnit.MINUTES);
	}

	private static String readToken() {
		BufferedReader br = null;
		String token = null;
		try {
			br = new BufferedReader(new FileReader("C:\\Users\\Ishwi\\token.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			StringBuilder sb = new StringBuilder();
			assert br != null;
			token = br.readLine();

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
		return token;
	}
}

