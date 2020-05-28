package core.commands;

import core.Chuu;
import core.exceptions.DuplicateInstanceException;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Validator;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.AliasEntity;
import dao.entities.LastFMData;
import dao.entities.Role;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public class AliasReviewCommand extends ConcurrentCommand<CommandParameters> {
    private final AtomicBoolean isActive = new AtomicBoolean(false);
    private final BiFunction<AliasEntity, EmbedBuilder, EmbedBuilder> builder = (aliasEntity, embedBuilder) ->
            embedBuilder.clearFields()
                    .addField("Alias:", aliasEntity.getAlias(), false)
                    .addField("Artist to be aliased:", aliasEntity.getArtistName(), false)
                    .addField("Added:", aliasEntity.getDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-dd-mm HH:mm 'UTC'")), false)
                    .setColor(CommandUtil.randomColor());

    public AliasReviewCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Aliases review";
    }

    @Override
    public List<String> getAliases() {
        return List.of("aliasreview", "alrev");
    }

    @Override
    public String getName() {
        return "Aliases Review";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = getService().findLastFMData(idLong);
        if (lastFMData.getRole() != Role.ADMIN) {
            sendMessageQueue(e, "Only bot admins can review the alias queue!");
            return;
        }
        if (!this.isActive.compareAndSet(false, true)) {
            sendMessageQueue(e, "Other admin is reviewing the aliases, pls wait till they have finished!");
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();

        try {

            HashMap<String, BiFunction<AliasEntity, MessageReactionAddEvent, Boolean>> actionMap = new HashMap<>();
            actionMap.put("U+2714", (aliasEntity, r) -> {
                try {
                    getService().addAlias(aliasEntity.getAlias(), aliasEntity.getArtistId());
                    getService().deleteAliasById(aliasEntity.getId());
                    r.getJDA().retrieveUserById(aliasEntity.getDiscorId())
                            .queue(user -> user.openPrivateChannel()
                                    .flatMap(privateChannel -> privateChannel.sendMessage("Your alias: " + aliasEntity.getAlias() + " has been approved!"))
                                    .queue(), throwable -> Chuu.getLogger().warn(throwable.getMessage(), throwable));
                } catch (DuplicateInstanceException | InstanceNotFoundException ignored) {
                    try {
                        getService().deleteAliasById(aliasEntity.getId());
                    } catch (InstanceNotFoundException ignored1) {
                        //Doesnt exists on the server
                    }
                }
                return true;

            });
            actionMap.put("U+274c", (a, r) -> {
                try {
                    getService().deleteAliasById(a.getId());
                } catch (InstanceNotFoundException e1) {
                    Chuu.getLogger().error(e1.getMessage());
                }
                return true;
            });
            new Validator<>(
                    embedBuilder1 -> embedBuilder.setTitle("No more  Aliases to Review").clearFields(),
                    () -> getService().getNextInAliasQueue(),
                    builder
                    , embedBuilder, e.getChannel(), e.getAuthor().getIdLong(), actionMap, false, false);
        } catch (Exception ex) {
            Chuu.getLogger().warn(ex.getMessage());
        } finally {
            this.isActive.set(false);
        }


    }
}
