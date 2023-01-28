package core.commands.moderation;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import dao.entities.UsersWrapper;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserExportCommand extends ConcurrentCommand<CommandParameters> {

    public UserExportCommand(ServiceView dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
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
    public void onCommand(Context e, @NotNull CommandParameters params) {


        Member member = e.getGuild().getMember(e.getAuthor());
        if (CommandUtil.notEnoughPerms(e)) {
            sendMessageQueue(e, CommandUtil.notEnoughPermsTemplate() + "export the user list");
            return;
        }
        sendMessage(e, "Check your DMs!").queue();

        List<UsersWrapper> all = db.getAll(e.getGuild().getIdLong());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            new ObjectMapper().writer(new DefaultPrettyPrinter()).writeValue(baos, all);
            e.getAuthor().openPrivateChannel().flatMap(
                            x -> x.sendFiles(FileUpload.fromData(
                                    baos.toByteArray(),
                                    "users_%s%s.json".formatted(e.getGuild().getName(), LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)))))
                    .queue();
        } catch (IOException ex) {
            Chuu.getLogger().warn(ex.getMessage(), ex);
        }
    }
}
