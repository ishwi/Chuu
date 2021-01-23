package core.commands.moderation;

import core.Chuu;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ChartableParser;
import core.parsers.FileParser;
import core.parsers.Parser;
import core.parsers.params.UrlParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ImportCommand extends ConcurrentCommand<UrlParameters> {

    @FunctionalInterface
    interface ImportFunctional {
        Callback executeCallback(LastFMData a, StringBuilder b, Message message, EmbedBuilder embedBuilder, User user, int position, int[] errorCounter);

    }


    private final ImportFunctional consumer = (u, m, message, embedBuilder, author, pos, errorCounter) -> () -> {

        embedBuilder.setDescription("Processing user #" + pos);
        message.editMessage(embedBuilder.build()).queue();
        String lastfmid = u.getName();
        long userId = u.getDiscordId();
        User userById = Chuu.getShardManager().getUserById(userId);
        if (userById != null && userById.isBot()) {
            return;
        }
        long guildID = u.getGuildID();
        String error = "\t{\"discordId\":" + userId + ",\t\t\n\"lastFMUsername\":" + lastfmid + ",\t\t\n\"reason\":\"";
        try {
            lastFM.getUserInfo(List.of(lastfmid), null);
        } catch (LastFmEntityNotFoundException ex) {
            m.append(error).append("This username doesn't exist on last.fm\"}\n");
            errorCounter[0]++;
            return;
        } catch (LastFmException ex) {
            m.append(error).append("Temporal Last.fm error\"}\n");
            errorCounter[0]++;
            return;
        }
        Guild guild;
        if ((guild = message.getJDA().getGuildById(guildID)) == null || guild.getMemberById(userId) == null) {
            m.append(error).append(" This discordId is not in the given guild!\"}\n");
            errorCounter[0]++;
            return;
        }
        List<UsersWrapper> guildlist = getService().getAll(guildID);


        if (guildlist.isEmpty()) {
            getService().createGuild(guildID);
        }

        List<UsersWrapper> list = getService().getAllALL();
        Optional<UsersWrapper> globalName = (list.stream().filter(user -> user.getLastFMName().equals(lastfmid)).findFirst());
        if (globalName.isPresent() && globalName.get().getDiscordID() != userId) {
            m.append(error).append(" That last.fm id was already taken\"}\n");
            errorCounter[0]++;

            return;
        }

        Optional<UsersWrapper> name = (guildlist.stream().filter(user -> user.getLastFMName().equals(lastfmid)).findFirst());
        //If name is already registered in this server
        if (name.isPresent()) {
            if (name.get().getDiscordID() != userId) {
                m.append(error).append(" That last.fm id was already taken\"}\n");
                errorCounter[0]++;

                return;
            }

            Optional<UsersWrapper> t = (guildlist.stream().filter(user -> user.getDiscordID() == userId).findFirst());
            //User was already registered in this guild
            if (t.isPresent()) {
                if (!t.get().getLastFMName().equals(lastfmid))
                    errorCounter[0]++;
                m.append(error).append(" This user is already registered with another last.fm name. Won't do anything\"}\n");
            } else {
                //If it was registered in at least other  guild there's no need to update
                if (getService().getGuildList(userId).stream().anyMatch(s -> s != guildID)) {
                    //Adds the user to the guild
                    getService().addGuildUser(userId, guildID);
                }
            }
            return;
        }


        //Never registered before
        LastFMData lastFMData = new LastFMData(lastfmid, userId, Role.USER, false, true, WhoKnowsMode.IMAGE, ChartMode.IMAGE, RemainingImagesMode.IMAGE, ChartableParser.DEFAULT_X, ChartableParser.DEFAULT_Y, PrivacyMode.NORMAL, true, false, true, TimeZone.getDefault(), null, null, true);
        lastFMData.setGuildID(guildID);

        getService().

                insertNewUser(lastFMData);

        try {

            List<ScrobbledArtist> allArtists = lastFM.getAllArtists(lastFMData, new CustomTimeFrame(TimeFrameEnum.ALL));
            getService().insertArtistDataList(allArtists, lastfmid);
            List<ScrobbledAlbum> albumData = lastFM.getAllAlbums(lastFMData, new CustomTimeFrame(TimeFrameEnum.ALL));
            getService().albumUpdate(albumData, allArtists, lastfmid);
            List<ScrobbledTrack> trackData = lastFM.getAllTracks(lastFMData, CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
            getService().trackUpdate(trackData, allArtists, lastfmid);
        } catch (
                LastFMNoPlaysException ignored) {
        } catch (
                LastFmEntityNotFoundException ex) {
            getService().removeUserCompletely(userId);
            m.append(error).append("This username doesn't exist on last.fm\"}\n");
            errorCounter[0]++;

        } catch (
                Throwable ex) {
            m.append(error).append("Error downloading users library, try to run !update, try again later or contact bot admins if the error persists\"}\n");
            errorCounter[0]++;

        }
    };


    public ImportCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<UrlParameters> initParser() {
        return new FileParser("json");
    }

    @Override
    public String getDescription() {
        return "Import a user list from a JSON file, Compatible with .fmbot import/export";
    }

    @Override
    public List<String> getAliases() {
        return List.of("import");
    }

    @Override
    public String getName() {
        return "Import";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull UrlParameters params) {
        Member member = e.getGuild().getMember(e.getAuthor());
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            sendMessageQueue(e, "Only an admin can export the data");
            return;
        }


        long guildID = e.getGuild().getIdLong();


        String url = params.getUrl();
        JSONArray arr;
        try (InputStream in = new URL(url).openStream()) {
            BufferedReader bR = new BufferedReader(new InputStreamReader(in));
            String line;

            StringBuilder responseStrBuilder = new StringBuilder();
            while ((line = bR.readLine()) != null) {

                responseStrBuilder.append(line);
            }
            arr = new JSONArray(responseStrBuilder.toString());
        } catch (IOException exception) {
            sendMessageQueue(e, "Couldn't get a valid json from file");
            return;
        }
//        PrivateChannel privateChannel = e.getAuthor().openPrivateChannel().complete();

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setTitle("Import In Progress")
                .setFooter("Total users to import: " + arr.length())
                .setThumbnail("https://cdnjs.cloudflare.com/ajax/libs/prettyPhoto/3.1.6/images/prettyPhoto/dark_rounded/loader.gif");
        Message complete = e.getChannel().sendMessage(embedBuilder.build()).complete();

        final Queue<Callback> queue = new ArrayDeque<>();
        final int[] counter = new int[]{0};
        StringBuilder stringBuilder = new StringBuilder("{\"errors\": [\n");

        for (int i = 0; i < arr.length(); i++) {
            JSONObject jsonObject = arr.getJSONObject(i);
            if (!jsonObject.has("discordUserID") || !jsonObject.has("lastFMUsername")) {
                stringBuilder.append("\n Item number:").append(i);
                continue;
            }

            long userId = Long.parseLong(jsonObject.optString("discordUserID"));
            String lastfmid = jsonObject.getString("lastFMUsername");
            LastFMData lastFMData = new LastFMData(lastfmid, userId, guildID, respondInPrivate, true, WhoKnowsMode.IMAGE, ChartMode.IMAGE, RemainingImagesMode.IMAGE, ChartableParser.DEFAULT_X, ChartableParser.DEFAULT_Y, PrivacyMode.NORMAL, true, false, true, TimeZone.getDefault(), null, null, true);
            queue.add(consumer.executeCallback(lastFMData, stringBuilder, complete, embedBuilder, e.getAuthor(), i, counter));

        }
        Callback call;
        while ((call = queue.poll()) != null) {
            int length = stringBuilder.length();
            call.executeCallback();
            if (stringBuilder.length() != length) {
                stringBuilder.append(",");
            }
        }
        StringBuilder description = new StringBuilder("Successfully processed " + (arr.length() - counter[0]) + " users\n");
        embedBuilder.setTitle("Finished processing everything").setThumbnail(null);
        if (counter[0] != 0) {
            stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length() - 1, "]\n}");
            String s = stringBuilder.toString();
            JSONObject jsonObject = new JSONObject(s);
            description.append("Finished with errors");

            e.getAuthor().openPrivateChannel().flatMap(p ->
                    complete.editMessage(embedBuilder.setDescription(description).build()))
                    .flatMap(x -> x.getChannel().sendFile(jsonObject.toString().getBytes(StandardCharsets.UTF_8), "errors.json"))
                    .queue();
        } else {
            description.append("\n Finished with no errors");
            embedBuilder.setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
            e.getAuthor().openPrivateChannel()
                    .flatMap(p -> complete.editMessage(embedBuilder.setDescription(description).build())).queue();
        }
    }

}
