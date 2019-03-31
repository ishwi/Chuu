package main;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.LastFMData;
import DAO.Entities.ResultWrapper;
import DAO.Entities.UserInfo;
import main.ImageRenderer.imageRenderer;
import main.last.ConcurrentLastFM;
import main.last.LastFMService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.imageio.ImageIO;
import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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
        String[] subMessage = Arrays.copyOfRange(message, 1, message.length);


        switch (message[0]) {
            case "chart":
                onChartMessageReceived(subMessage, event);
                break;
            case "top":
                onTopMessageReceived(subMessage, event.getAuthor().getIdLong(), event);
                break;

            case "update":
                onUpdate(subMessage, event.getAuthor().getIdLong(), event);
                break;
            case "ping":
                returnText = "!pong";
                event.getChannel().sendMessage(returnText).queue();
                break;
            case "set":
                onSetterMessageReceived(subMessage, event.getAuthor().getIdLong());
                break;
            case "taste":
                onTaste(subMessage, event.getAuthor().getIdLong(), event);
        }


        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(estimatedTime);
        System.out.println(TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.MILLISECONDS));


    }

    private void onSetterMessageReceived(String[] message, long id) {
        if ((message.length > 1) || (message.length == 0)) {
            return;
        }
        String lastFmID = message[0];
        impl.addData(new LastFMData(lastFmID, id));


    }

    private void onWhoKnows(String artist, long id) {

        impl.whoKnows(artist);


    }


    private void onUpdate(String[] message, long id, MessageReceivedEvent event) {
        String username;

        MessageBuilder a = new MessageBuilder();
        a.setContent("Starting to update your profile");
        a.sendTo(event.getChannel()).queue();

        event.getChannel().sendTyping().queue();
        try {
            username = getLastFmUsername1input(message, id, event);
            LinkedList<ArtistData> list = lastAccess.getSimiliraties(username);
            impl.addData(list, username);
            a.setContent("Sucessfully updated" + username + " info !").sendTo(event.getChannel()).queue();


        } catch (InstanceNotFoundException e) {
            userNotOnDB(event);


        }

    }

    private void onTaste(String[] message, long id, MessageReceivedEvent event) {
        //message 0
        //message 1 *optional
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (message.length == 0) {
            messageBuilder.setContent(printUsage("taste"));
            messageBuilder.sendTo(event.getChannel()).queue();
            return;
        }
        event.getChannel().sendTyping().queue();
        String[] userList = {"", ""};
        if (message.length == 1) {
            userList[1] = message[0];
            try {
                userList[0] = impl.findShow(id).getName();
            } catch (InstanceNotFoundException e) {
                userNotOnDB(event);
                return;
            }
        } else {
            userList[0] = message[0];
            userList[1] = message[1];
        }

        // Si userList contains @ -> user
        try {
            List<User> list = event.getMessage().getMentionedUsers();
            List<String> lastfMNames = Arrays.stream(userList)
                    .map(s -> lambda(s, list))
                    .collect(Collectors.toList());
            lastfMNames.forEach(System.out::println);

            ResultWrapper resultWrapper = impl.getSimilarities(lastfMNames);
            System.out.println("resultWrapper = " + resultWrapper.getRows());
            java.util.List<String> users = new ArrayList<>();
            users.add(resultWrapper.getResultList().get(0).getUserA());
            users.add(resultWrapper.getResultList().get(0).getUserB());
            List<UserInfo> userInfoLiust = lastAccess.getUserInfo(users);
            BufferedImage image = imageRenderer.generateTasteImage(resultWrapper, userInfoLiust);

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "jpg", b);
                byte[] img = b.toByteArray();
                if (img.length < 8388608) {
                    messageBuilder.sendTo(event.getChannel()).addFile(img, "cat.png").queue();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (RuntimeException | InstanceNotFoundException e) {
            userNotOnDB(event);
        }

    }

    private String printUsage(String command) {
        switch (command) {
            case "taste":
                return "!taste user1 *user2* \n If user2 is missing it gets remplaced by Author user";

            case "update":
                return "!update lastFmUser";

            case "chart":
                return "!chart *[w,m,t,y,a] *Username SizeXSize \n" +
                        "If timeframe is not specified deafults to Weekly \n" +
                        "If useranme is not specified defaults to authors account \n" +
                        "If size is not specified defaults to 5x5 (As big as discord lets";


            case "top":
                return "!top username";

            case "set":
                return "!set lastFMuser";
            default:
                return "Something weird Happened";


        }
    }

    private void userNotOnDB(MessageReceivedEvent event) {

        System.out.println("Problemo");
        event.getChannel().sendMessage("User doesnt have an account set").queue();
    }


    private User findUSername(String name, List<User> userList) {
        Optional<User> match = userList.stream().filter(user -> user.getIdLong() == Long.valueOf(name.substring(2, name.indexOf(">")))).findFirst();
        return match.orElse(null);
    }

    private String lambda(String s, List<User> list) {
        if (s.startsWith("<@")) {
            User result = this.findUSername(s, list);
            if (result != null) {
                try {
                    return impl.findShow(result.getIdLong()).getName();
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException();
                }
            }
        }
        return s;
    }


    private void onChartMessageReceived(String[] message, MessageReceivedEvent event) {
        String time = "7day";
        MessageChannel channel = event.getChannel();
        MessageBuilder mes = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();

//
//        1     !command

//        2     timeFrame 1 char
//        3     Username whatever
//        4     Size    somethingXsomething
        String timeFrame = null;
        boolean isTime = false;
        boolean isName = false;

        String discordName = null;
        int x = 5;
        int y = 5;

        String pattern = "\\d+[xX]\\d+";

        for (String word : message) {
            if (word.length() == 1) {
                timeFrame = word;
                isTime = true;
                continue;
            }
            if (word.matches(pattern)) {
                String[] dim = word.split("[xX]");
                x = Integer.valueOf(dim[0]);
                y = Integer.valueOf(dim[1]);

                continue;
            }
            if (!isName) {
                isName = true;
                discordName = word;
            }
        }


        String username;
        if (isName) {
            List<User> list;
            list = event.getMessage().getMentionedUsers();
            username = discordName;
            if (!list.isEmpty()) {
                LastFMData data;
                try {
                    data = this.impl.findShow((list.get(0).getIdLong()));
                } catch (InstanceNotFoundException e) {
                    userNotOnDB(event);
                    return;
                }
                username = data.getName();
            }
            if (username.startsWith("@")) {
                channel.sendMessage("Trolled xD").queue();
                return;
            }
        } else {
            long id = event.getAuthor().getIdLong();
            try {
                username = this.impl.findShow(id).getName();
            } catch (InstanceNotFoundException e) {
                userNotOnDB(event);
                return;
            }
        }


        if (isTime) {
            if (timeFrame.startsWith("y"))
                time = "12month";
            if (timeFrame.startsWith("t"))
                time = "3month";
            if (timeFrame.startsWith("m"))
                time = "1month";
            if (timeFrame.startsWith("a"))
                time = "overall";
        }


        channel.sendTyping().queue();


        embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
                .setDescription("Most Listened Albums in last " + time);
        mes.setEmbed(embed.build());
        if (x * y > 100) {
            channel.sendMessage("Gonna Take a while").queue();
        }

        byte[] file = this.lastAccess.getUserList(username, time, x, y);

        // Max Discord File length
        if (file.length < 8388608) {
            channel.sendFile(file, "cat.png", mes.build()).queue();
            return;
        }

        channel.sendMessage("boot to big").queue();
        try {

            String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
                    .withZone(ZoneOffset.UTC)
                    .format(Instant.now());

            String path = "D:\\Games\\" + thisMoment + ".png";
            try (FileOutputStream fos = new FileOutputStream(path)) {
                fos.write(file);
                //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onTopMessageReceived(String[] message, long id, MessageReceivedEvent event) {
        MessageBuilder mes = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        if (message.length > 0) {
            try {
                getLastFmUsername1input(message, id, event);
                event.getChannel().sendTyping().queue();
                embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
                        .setDescription("Most Listened Artists");
                mes.setEmbed(embed.build());
                event.getChannel().sendFile(this.lastAccess.getUserList(message[0], "overall", 5, 5), "cat.png", mes.build()).queue();
            } catch (InstanceNotFoundException e) {
                userNotOnDB(event);
            }
        } else
            mes.setContent(printUsage("top")).sendTo(event.getChannel()).queue();
    }

    private String getLastFmUsername1input(String[] message, long id, MessageReceivedEvent event) throws InstanceNotFoundException {
        String username;
        if ((message.length > 1) || (message.length == 0)) {
            username = this.impl.findShow(id).getName();
        } else {
            //Caso con @ y sin @
            List<User> list = event.getMessage().getMentionedUsers();
            username = message[0];
            if (!list.isEmpty()) {
                LastFMData data = this.impl.findShow((list.get(0).getIdLong()));
                username = data.getName();
            }
            if (username.startsWith("@")) {
                event.getChannel().sendMessage("Trolled xD").queue();
            }
        }
        return username;
    }


}
