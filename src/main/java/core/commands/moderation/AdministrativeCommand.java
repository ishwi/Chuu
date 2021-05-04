package core.commands.moderation;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.Parser;
import core.parsers.UrlParser;
import core.parsers.params.UrlParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.UsersWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.commons.collections4.MultiValuedMap;
import org.imgscalr.Scalr;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AdministrativeCommand extends ConcurrentCommand<UrlParameters> {


    private static final ExecutorService executor = (new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(250), (r, executor1) -> Chuu.getLogger().warn("Disarded thread on executor1")));
    private static final ExecutorService executor2 =
            (new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(250), (r, executor1) -> Chuu.getLogger().warn("Disarded thread on executor2")));


    public AdministrativeCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<UrlParameters> initParser() {
        return new UrlParser();
    }

    @Override
    public void onEvent(@org.jetbrains.annotations.NotNull GenericEvent event) {
        if (event instanceof GuildJoinEvent e) {
            onGuildJoin(e);
        } else if (event instanceof GuildMemberJoinEvent e2) {
            onGuildMemberJoin(e2);
        } else if (event instanceof GuildMemberRemoveEvent e3) {
            onGuildMemberRemove(e3);
        } else {
            super.onEvent(event);
        }
    }

    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        executor.submit(() -> {
            db.createGuild(event.getGuild().getIdLong());
            Set<Long> allBot = db.getAllALL().stream().map(UsersWrapper::getDiscordID).collect(Collectors.toUnmodifiableSet());
            Set<Long> thisServer = db.getAll(event.getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toUnmodifiableSet());
            event.getGuild().loadMembers().onSuccess(members -> {
                List<Long> toInsert = members.stream().filter(x -> !x.getUser().isBot()).map(x -> x.getUser().getIdLong()).filter(x -> allBot.contains(x) && !thisServer.contains(x)).toList();
                toInsert.forEach(x -> db.addGuildUser(x, event.getGuild().getIdLong()));
                Chuu.getLogger().info("Successfully added {} {} to server: {}", toInsert.size(), CommandUtil.singlePlural(toInsert.size(), "member", "members"), event.getGuild().getName());
            });
        });
    }

    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        executor2.submit(() -> {
            try {
                LastFMData lastFMData = db.findLastFMData(event.getUser().getIdLong());
                db.addGuildUser(lastFMData.getDiscordId(), event.getGuild().getIdLong());
                Chuu.getLogger().info("Successfully added {} to server: {} ", lastFMData.getDiscordId(), event.getGuild().getName());
            } catch (InstanceNotFoundException e) {
                //Ignored
            }
        });
    }

    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {

        long idLong = event.getUser().getIdLong();
        Chuu.getLogger().info("USER LEFT {}", idLong);
        executor2
                .submit(() -> {

                    try {
                        db.findLastFMData(idLong);
                        Guild guild = event.getJDA().getGuildById(event.getGuild().getIdLong());
                        Chuu.getLogger().info("USER was a registered user {} ", idLong);

                        // Making sure they really left?
                        if (guild != null && guild.getMember(event.getUser()) == null)
                            db.removeUserFromOneGuildConsequent(idLong, event.getGuild().getIdLong())
                                    ;
                    } catch (InstanceNotFoundException ignored) {
                    }
                });
    }

    public void onStartup(ShardManager jda) {
        MultiValuedMap<Long, Long> map = db.getMapGuildUsers();
        map.mapIterator().forEachRemaining(key -> {
            List<Long> usersToDelete;
            //Users in guild key
            List<Long> user = (List<Long>) map.get(key);
            Guild guild = jda.getGuildById(key);
            if (guild != null) {
                //Get all members in guild

                //Task<Void> voidTask = guild.loadMembers(Chuu::caching);
//                //Get all members in guild
//                voidTask.onSuccess((Void t) -> {
//                    List<Long> usersToDelete;
//                    List<Member> memberList = guild.getMembers();
//                    //Gets all ids
//                    List<Long> guildList = memberList.stream().map(x -> x.getUser().getIdLong())
//                            .toList();
//
//                    //if user in app but not in guild -> mark to delete
//                    usersToDelete = user.stream().filter(eachUser -> !guildList.contains(eachUser))
//                            .toList();
//                    usersToDelete.forEach(u -> db.removeUserFromOneGuildConsequent(u, key));
//                    Chuu.getLogger().info("Deleted Users: {}", usersToDelete.size());
//                });
                List<Member> memberList = guild.getMembers();
                //Gets all ids
                List<Long> guildList = memberList.stream().map(x -> x.getUser().getIdLong())
                        .toList();

                //if user in app but not in guild -> mark to delete
                usersToDelete = user.stream().filter(eachUser -> !guildList.contains(eachUser))
                        .toList();
                usersToDelete.forEach(u -> db.removeUserFromOneGuildConsequent(u, key));
                if (!usersToDelete.isEmpty())
                    Chuu.getLogger().info("Deleted Users in {}: {}", guild.getName(), usersToDelete.size());


            } else {
                user.forEach(u -> db.removeUserFromOneGuildConsequent(u, key));
            }
        });
    }

    @Override
    public String getDescription() {
        return "Adds a logo that will be displayed on some bot functionalities";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("logo");
    }

    @Override
    public String getName() {
        return "Logo";
    }

    @Override
    protected void onCommand(Context e, @NotNull UrlParameters urlParameters) {
        String url = urlParameters.getUrl();
        if (url.length() == 0) {
            db.removeLogo(e.getGuild().getIdLong());
            sendMessageQueue(e, "Removed logo from the server");
        } else {

            try (InputStream in = new URL(url).openStream()) {
                BufferedImage image = ImageIO.read(in);
                if (image == null) {
                    sendMessageQueue(e, "Couldn't get an image from the supplied link");
                    return;
                }
                image = Scalr.resize(image, Scalr.Method.QUALITY, 75, Scalr.OP_ANTIALIAS);

                db.addLogo(e.getGuild().getIdLong(), image);
                sendMessageQueue(e, "Logo updated");
            } catch (IOException exception) {
                Chuu.getLogger().warn(exception.getMessage(), exception);
                sendMessageQueue(e, "Something happened while processing the image");
            }

        }
    }
}
