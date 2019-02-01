package main;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class Main extends ListenerAdapter {


    public static void main(String[] args) throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = "NTM3MzUzNzc0MjA1ODk0Njc2.DykBoQ.T0D6ajlonlcuKCCcJCBZ0eAy2p8";
        builder.setToken(token);
        builder.addEventListeners(new ListenerLauncher());
        try {
            builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }

    }
}

