package core.commands;

import core.Chuu;
import core.exceptions.DuplicateInstanceException;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Validator;
import core.parsers.NoOpParser;
import dao.ChuuService;
import dao.entities.AliasEntity;
import dao.entities.LastFMData;
import dao.entities.Role;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.BiFunction;

public class AliasReviewCommand extends ConcurrentCommand {
    public boolean isActive = false;
    private BiFunction<AliasEntity, EmbedBuilder, EmbedBuilder> builder = (aliasEntity, embedBuilder) ->
            embedBuilder.clearFields()
                    .addField("Alias:", aliasEntity.getAlias(), false)
                    .addField("Artist to be aliased:", aliasEntity.getArtistName(), false)
                    .addField("Added:", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").format(aliasEntity.getDateTime()), false)
                    .setColor(CommandUtil.randomColor());

    public AliasReviewCommand(ChuuService dao) {
        super(dao);
        this.parser = new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Review Aliases";
    }

    @Override
    public List<String> getAliases() {
        return List.of("review");
    }

    @Override
    public String getName() {
        return "review";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        if (this.isActive) {
            sendMessageQueue(e, "Other admin is reviewing the aliases, pls wait till they have finished!");
            return;
        }
        this.isActive = true;
        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = getService().findLastFMData(idLong);
        if (lastFMData.getRole() != Role.ADMIN) {
            sendMessageQueue(e, "Only bot admins can review the alias queue!");
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Alias Review");
        this.executor.submit(() -> {
            new Validator<>(
                    () -> getService().getNextInAliasQueue(),
                    builder,
                    (aliasEntity, jda) -> {
                        try {
                            getService().addAlias(aliasEntity.getAlias(), aliasEntity.getArtistId());
                            getService().deleteAliasById(aliasEntity.getId());
                            jda.retrieveUserById(aliasEntity.getDiscorId())
                                    .queue(user -> user.openPrivateChannel()
                                            .flatMap(privateChannel -> privateChannel.sendMessage("Your alias: " + aliasEntity.getAlias() + " has been approved!"))
                                            .queue());
                        } catch (DuplicateInstanceException | InstanceNotFoundException ignored) {
                            try {
                                getService().deleteAliasById(aliasEntity.getId());
                            } catch (InstanceNotFoundException ignored1) {

                            }
                        }
                    },
                    (a) -> {
                        try {
                            getService().deleteAliasById(a.getId());
                        } catch (InstanceNotFoundException e1) {
                            Chuu.getLogger().error(e1.getMessage());
                        }
                    }, embedBuilder, e.getChannel(), e.getAuthor().getIdLong());
            this.isActive = false;
        });
    }
}
