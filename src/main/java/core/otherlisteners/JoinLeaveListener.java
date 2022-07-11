package core.otherlisteners;

import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.util.ChuuVirtualPool;
import dao.ChuuService;
import dao.entities.GuildProperties;
import dao.entities.LastFMData;
import dao.entities.UsersWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public record JoinLeaveListener(ChuuService db, ConcurrentLastFM lastFM) implements EventListener {
    private static final ExecutorService GUILD_JOIN_POOL = ChuuVirtualPool.of("Guild-Join-Pool");

    private static final ExecutorService MEMBER_JOIN_POOL = ChuuVirtualPool.of("Guild-Member-Join-Pool");
    private static final ExecutorService DELETE_POOL = ChuuVirtualPool.of("Delete-Pool");


    @Override
    public void onEvent(@NotNull GenericEvent event) {
        switch (event) {
            case GuildJoinEvent e -> onGuildJoin(e);
            case GuildMemberJoinEvent e2 -> onGuildMemberJoin(e2);
            case GuildMemberRemoveEvent e3 -> onGuildMemberRemove(e3);
            default -> {
            }
        }
    }

    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        GUILD_JOIN_POOL.execute(() -> {
            db.createGuild(event.getGuild().getIdLong());
            Set<Long> thisServer = db.getAll(event.getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toUnmodifiableSet());
            event.getGuild().loadMembers().onSuccess(members -> {

                List<Long> temp = members.stream().filter(x -> !x.getUser().isBot()).map(x -> x.getUser().getIdLong())
                        .filter(x -> !thisServer.contains(x)).toList(); // Not on this server (In case leave/rejoin)
                if (temp.isEmpty()) {
                    return;
                }
                Set<Long> existingInDb = db.findExistingById(temp);
                var toInsert = temp.stream().filter(existingInDb::contains).toList();
                toInsert.forEach(x -> db.addGuildUser(x, event.getGuild().getIdLong()));
            });
        });
    }

    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        MEMBER_JOIN_POOL.execute(() -> {
            try {
                GuildProperties guildProperties = db.getGuildProperties(event.getGuild().getIdLong());
                if (guildProperties.setOnJoin()) {
                    LastFMData lastFMData = db.findLastFMData(event.getUser().getIdLong());
                    db.addGuildUser(lastFMData.getDiscordId(), event.getGuild().getIdLong());
                }
            } catch (InstanceNotFoundException e) {
                //Ignored
            }
        });
    }

    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {

        long idLong = event.getUser().getIdLong();
        MEMBER_JOIN_POOL
                .execute(() -> {

                    try {
                        db.findLastFMData(idLong);
                        Guild guild = event.getJDA().getGuildById(event.getGuild().getIdLong());
                        Chuu.getLogger().info("USER was a registered user {} ", idLong);
                        // Making sure they really left?
                        if (guild != null && guild.getMember(event.getUser()) == null) {
                            DELETE_POOL.execute(() -> db.removeUserFromOneGuildConsequent(idLong, event.getGuild().getIdLong()));
                        }
                    } catch (InstanceNotFoundException ignored) {
                    }
                });
    }
}
