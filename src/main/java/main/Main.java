package main;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main extends ListenerAdapter {


    public static void main(String[] args) throws IOException, GeneralSecurityException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = readToken();
        builder.setToken(token);
        builder.addEventListeners(new ListenerLauncher());

        try {
            builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }

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

