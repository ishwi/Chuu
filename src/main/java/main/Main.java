package main;

import DAO.DaoImplementation;
import main.last.ConcurrentLastFM;
import main.last.LastFMService;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class Main extends ListenerAdapter {


	public static void main(String[] args) {
		Map<String, String> map = readToken();
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		LastFMService last = new ConcurrentLastFM();
		DaoImplementation dao = new DaoImplementation();
		Spotify spotifyWrapper = new Spotify(map.get("clientId"), map.get("clientSecret"));
		builder.setToken(map.get("discordtoken"));
		builder.addEventListeners(new ListenerLauncher(last, dao, spotifyWrapper));


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

