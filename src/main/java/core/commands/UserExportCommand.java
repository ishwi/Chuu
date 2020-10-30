package core.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.Chuu;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.UsersWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserExportCommand extends ConcurrentCommand<CommandParameters> {

    public UserExportCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Export the list of the current users in a server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("export");
    }

    @Override
    public String getName() {
        return "User Export";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {


        Member member = e.getGuild().getMember(e.getAuthor());
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            sendMessageQueue(e, "Only an admin can export the data");
            return;
        }
        sendMessage(e, "Check your DMs!").queue();

        List<UsersWrapper> all = getService().getAll(e.getGuild().getIdLong());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            new ObjectMapper().writeValue(baos, all);
            e.getAuthor().openPrivateChannel().flatMap(
                    x -> x.sendFile(baos.toByteArray(),
                            "users_" + e.getGuild().getName() + LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME) + ".json"))
                    .queue();
        } catch (IOException ex) {
            Chuu.getLogger().warn(ex.getMessage(), ex);
        }
    }
}
