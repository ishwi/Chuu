package core.commands.webhooks;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.GreedyStringParser;
import core.parsers.Parser;
import core.parsers.params.StringParameters;
import core.util.ServiceView;
import dao.exceptions.InstanceNotFoundException;
import dao.webhook.Webhook;
import dao.webhook.WebhookTypeData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class WebhookCreator extends ConcurrentCommand<StringParameters> {

    public WebhookCreator(ServiceView dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.DISCOVERY;
    }


    @Override
    public Parser<StringParameters> initParser() {
        var parser = new GreedyStringParser();
        parser.removeOptional("any");
        return parser;
    }

    @Override
    public String getDescription() {
        return "Creates a webhook";
    }

    @Override
    public List<String> getAliases() {
        return List.of("webhook");
    }

    @Override
    public String getName() {
        return "Webhooks";
    }

    @Override
    public void onCommand(Context e, @NotNull StringParameters params) throws LastFmException, InstanceNotFoundException {
        if (CommandUtil.notEnoughPerms(e, Permission.MANAGE_WEBHOOKS)) {
            sendMessageQueue(e, CommandUtil.notEnoughPermsTemplate() + "manage webhooks");
            return;
        }
        TextChannel tc = e.getChannelUnion().asTextChannel();
        String[] genres = params.getValue().split("\\s+");
        var mappedGenres = Arrays.stream(genres).map(genre -> genre.toLowerCase(Locale.ROOT)).distinct().toList();
        if (mappedGenres.isEmpty()) {
            parser.sendError("Need at least one genre", e);
            return;
        }
        String id = "[%s]".formatted(String.join(",", mappedGenres));
        e.getChannelUnion().asTextChannel().createWebhook("Chuu-Bandcamp-Discover-%s".formatted(id)).queue(webhook -> {
            db.createWebhook(new Webhook<>(e.getGuild().getIdLong(), webhook.getIdLong(), tc.getIdLong(), webhook.getToken(), new WebhookTypeData.BandcampReleases(mappedGenres)));
            e.sendMessage("Created webhook succesfully").queue();
        });
    }
}
