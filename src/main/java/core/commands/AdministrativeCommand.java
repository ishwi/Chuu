package core.commands;

import core.Chuu;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.UrlParser;
import dao.ChuuService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.MultiValuedMap;
import org.imgscalr.Scalr;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AdministrativeCommand extends ConcurrentCommand {

    public AdministrativeCommand(ChuuService dao) {
        super(dao);
        parser = new UrlParser();
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {

        long idLong = event.getUser().getIdLong();
        Chuu.getLogger().info("USER LEFT {}", idLong);
        Executors.newSingleThreadExecutor()
                .execute(() -> {

                    try {
                        getService().findLastFMData(idLong);
                        Guild guild = event.getJDA().getGuildById(event.getGuild().getIdLong());
                        Chuu.getLogger().info("USER was a registered user {} ", idLong);


                        // Making sure they really left?
                        if (guild != null && guild.getMember(event.getUser()) == null)
                            getService()
                                    .removeUserFromOneGuildConsequent(idLong, event.getGuild().getIdLong())
                                    ;

                    } catch (InstanceNotFoundException ignored) {

                    }
                });
    }

    public void onStartup(JDA jda) {
        MultiValuedMap<Long, Long> map = getService().getMapGuildUsers();
        map.mapIterator().forEachRemaining(key -> {
            List<Long> usersToDelete;
            //Users in guild key
            List<Long> user = (List<Long>) map.get(key);
            Guild guild = jda.getGuildById(key);
            if (guild != null) {
                //Get all members in guild
                List<Member> memberList = guild.getMembers();
                //Gets all ids
                List<Long> guildList = memberList.stream().map(x -> x.getUser().getIdLong())
                        .collect(Collectors.toList());

                //if user in app but not in guild -> mark to delete
                usersToDelete = user.stream().filter(eachUser -> !guildList.contains(eachUser))
                        .collect(Collectors.toList());
                usersToDelete.forEach(u -> getService().removeUserFromOneGuildConsequent(u, key));
                Chuu.getLogger().info("Deleted Users: {}", usersToDelete.size());


            } else {
                user.forEach(u -> getService().removeUserFromOneGuildConsequent(u, key));
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
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        String[] urlParsed = parser.parse(e);
        if (urlParsed == null)
            return;

        if (urlParsed.length == 0) {
            getService().removeLogo(e.getGuild().getIdLong());
            sendMessageQueue(e, "Removed logo from the server");
        } else {

            try (InputStream in = new URL(urlParsed[0]).openStream()) {
                BufferedImage image = ImageIO.read(in);
                if (image == null) {
                    sendMessageQueue(e, "Couldn't get an image from the supplied link");
                    return;
                }
                image = Scalr.resize(image, Scalr.Method.QUALITY, 75, Scalr.OP_ANTIALIAS);

                getService().addLogo(e.getGuild().getIdLong(), image);
                sendMessageQueue(e, "Logo updated");
            } catch (IOException exception) {
                Chuu.getLogger().warn(exception.getMessage(), exception);
                sendMessageQueue(e, "Something happened while processing the image");
            }

        }
    }


}
