package main;

import main.last.ConcurrentLastFM;
import main.last.DaoImplementation;
import main.last.LastFMService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class ListenerLauncher extends ListenerAdapter {
    private LastFMService lastAccess;
    private DaoImplementation impl;

    public ListenerLauncher() {

        this.lastAccess = new ConcurrentLastFM();
        this.impl = new DaoImplementation();

    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String returnText;
        long startTime;

        if (event.getAuthor().isBot() || !event.getMessage().getContentRaw().startsWith("!")) {
            return;
        }

        startTime = System.currentTimeMillis();

        System.out.println("We received a message from " +
                event.getAuthor().getName() + "; " + event.getMessage().getContentDisplay());

        String[] message = event.getMessage().getContentRaw().substring(1).split("\\s+");

        switch (message[0]) {

            case "chart":
                onChartMessageReceived(message, event);
                break;
            case "top":
                onTopMessageReceived(message, event.getChannel());
                break;


            case "ping":
                returnText = "!pong";
                event.getChannel().sendMessage(returnText).queue();
                break;
            case "setLastFm":
                onSetterMessageReceived(message, event.getAuthor().getIdLong());
        }


        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(estimatedTime);
        System.out.println(TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.MILLISECONDS));


    }

    private void onSetterMessageReceived(String[] message, long id) {
        if ((message.length > 2) || (message.length == 1)) {
            return;
        }
        String lastFmID = message[1];
        impl.addData(new LastFMData(lastFmID, id));


    }

    private void onChartMessageReceived(String[] message, MessageReceivedEvent event) {
        String time = "7day";
        String username = "";
        MessageChannel channel = event.getChannel();

        if (message.length == 1 || message.length == 2) {
            long id = event.getAuthor().getIdLong();
            username = this.impl.findShow(id).getName();
            if (username == null) {
                System.out.println("error");
                return;
            }
        }
        if (message.length >= 2) {

            if (message[1].startsWith("y"))
                time = "12month";
            if (message[1].startsWith("t"))
                time = "3month";
            if (message[1].startsWith("m"))
                time = "1month";
            if (message[1].startsWith("a"))
                time = "overall";
        }
        if (message.length > 2) {
            List<User> list;
            list = event.getMessage().getMentionedUsers();
            username = message[2];
            if (!list.isEmpty()) {
                LastFMData data = this.impl.findShow((list.get(0).getIdLong()));
                if (data == null) {
                    System.out.println("Problemo");
                    return;
                }
                username = data.getName();
            }
        }


        channel.sendTyping().queue();

        MessageBuilder mes = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
                .setDescription("Most Listened Albums in last " + time);
        mes.setEmbed(embed.build());
        channel.sendFile(this.lastAccess.getUserList(username, time), "cat.png", mes.build()).queue();


    }

    private void onTopMessageReceived(String[] message, MessageChannel chan) {
        if (message.length > 1) {


            chan.sendTyping().queue();

            MessageBuilder mes = new MessageBuilder();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
                    .setDescription("Most Listened Artists");
            mes.setEmbed(embed.build());
            chan.sendFile(this.lastAccess.getUserList(message[1], "overall"), "cat.png", mes.build()).queue();
        }


    }


}
