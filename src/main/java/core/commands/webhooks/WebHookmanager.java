package core.commands.webhooks;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.ButtonResult;
import core.otherlisteners.ButtonValidator;
import core.otherlisteners.ButtonValidatorBuilder;
import core.otherlisteners.Reaction;
import core.otherlisteners.ReactionaryResult;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import dao.entities.TriFunction;
import dao.exceptions.InstanceNotFoundException;
import dao.webhook.Webhook;
import dao.webhook.WebhookTypeData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static core.otherlisteners.Reactions.LEFT_ARROW;
import static core.otherlisteners.Reactions.REPORT;
import static core.otherlisteners.Reactions.RIGHT_ARROW;

public class WebHookmanager extends ConcurrentCommand<CommandParameters> {
    private final TriFunction<JDA, AtomicInteger, AtomicInteger, BiFunction<Webhook<?>, EmbedBuilder, EmbedBuilder>> builder = (_, _, _) -> (votingEntity, embedBuilder) -> switch (votingEntity.data().type()) {
        case BANDCAMP_RELEASE -> {
            WebhookTypeData.BandcampReleases data = (WebhookTypeData.BandcampReleases) votingEntity.data();
            yield embedBuilder.clearFields()
                    .setTitle(votingEntity.data().type().toString())
                    .setDescription(String.join(",", data.genres()))
                    .setColor(CommandUtil.pastelColor());
        }
    };

    public WebHookmanager(ServiceView dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Manage webhooks";
    }

    @Override
    public List<String> getAliases() {
        return List.of("managewebhooks");
    }

    @Override
    public String getName() {
        return "managewebhooks";
    }

    @Override
    public void onCommand(Context e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        List<Webhook<?>> webhooks = db.obtainAllGuildWebhooks(e.getGuild().getIdLong());

        Queue<Webhook<?>> queue = new ArrayDeque<>(webhooks);

        AtomicInteger totalImages = new AtomicInteger(queue.size());


        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle("Webhook manager");
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger size = new AtomicInteger(webhooks.size());


        UnaryOperator<EmbedBuilder> finisher = finalEmbed -> {
            String title;
            title = "Voting timed out";

            finalEmbed.setTitle(title)
                    .clearFields();

            finalEmbed.setFooter(null);
            return finalEmbed;
        };
        Supplier<Webhook<?>> fetcher = ButtonValidator.paginate(counter, webhooks);
        var result = processButtonActions(e, webhooks, counter, size);
        new ButtonValidatorBuilder<Webhook<?>>()
                .setGetLastMessage(finisher)
                .setElementFetcher(fetcher)
                .setFillBuilder(builder.apply(e.getJDA(), size, counter))
                .setWho(embedBuilder)
                .setContext(e)
                .setDiscordId(e.getAuthor().getIdLong())
                .setActionMap(result.map)
                .setActionRows(result.rows)
                .setAllowOtherUsers(true)
                .setRenderInSameElement(true)
                .setChannelId(e.getChannel().getIdLong())
                .queue();


    }

    private Result processButtonActions(Context e, List<Webhook<?>> allArtistImages, AtomicInteger counter, AtomicInteger size) {
        ActionRow of = ActionRow.of(Button.danger(REPORT, "Report").withEmoji(Emoji.fromUnicode(REPORT)));

        List<ActionRow> rows = new ArrayList<>();
        rows.add(of);
        List<ItemComponent> components = of.getComponents();
        if (allArtistImages.size() > 1) {
            components.add(Button.primary(RIGHT_ARROW, Emoji.fromUnicode(RIGHT_ARROW)));
        }

        List<ActionRow> copy = new ArrayList<>(rows);


        Map<String, Reaction<Webhook<?>, ButtonInteractionEvent, ButtonResult>> stringReactionMap = generateActions(e,
                GenericInteractionCreateEvent::getUser,
                () -> () -> new ButtonResult.Result(false, null),
                (ButtonInteractionEvent r, Boolean t) -> ButtonValidator.leftMove(allArtistImages.size(), counter, r, t, copy),
                (ButtonInteractionEvent r, Boolean t) -> ButtonValidator.rightMove(allArtistImages.size(), counter, r, t, copy),
                allArtistImages, counter, size);

        return new Result(stringReactionMap, rows);
    }

    private <T extends Event, Y extends ReactionaryResult> Map<String, Reaction<Webhook<?>, T, Y>> generateActions(Context e,
                                                                                                                   Function<T, User> owner,
                                                                                                                   Supplier<Y> normal,
                                                                                                                   BiFunction<T, Boolean, Y> left,
                                                                                                                   BiFunction<T, Boolean, Y> right,
                                                                                                                   List<Webhook<?>> allArtistImages, AtomicInteger counter, AtomicInteger size) {
        Map<String, Reaction<Webhook<?>, T, Y>> actionMap = new LinkedHashMap<>();


        if (allArtistImages.size() > 1) {

            actionMap.put(LEFT_ARROW, (aliasEntity, r) -> left.apply(r, true));
            actionMap.put(RIGHT_ARROW, (a, r) -> right.apply(r, true));
        }

        actionMap.put(REPORT, (a, r) -> {
            TextChannel tc = e.getGuild().getTextChannelById(a.channelId());

            tc.retrieveWebhooks()
                    .flatMap(rb -> rb.stream()
                            .filter(z -> z.getIdLong() == (a.webhookId()))
                            .findFirst()
                            .map(net.dv8tion.jda.api.entities.Webhook::delete)
                            .orElse(new VoidCompletedRestAction()))
                    .queue(_ -> db.deleteWebhook(a.url()));

            allArtistImages.removeIf(webhook -> webhook.webhookId() == a.webhookId());
            int i = allArtistImages.size() - 1;
            if (counter.get() == i && !allArtistImages.isEmpty())
                right.apply(r, false);
            else if (counter.get() < i) {
                left.apply(r, false);
            }  // Do nothing
            return normal.get();
        });

        return actionMap;
    }

    private record Result(Map<String, Reaction<Webhook<?>, ButtonInteractionEvent, ButtonResult>> map,
                          List<ActionRow> rows) {
    }

    private static class VoidCompletedRestAction extends CompletedRestAction<Void> {
        public VoidCompletedRestAction() {
            super(null, null, null);
        }
    }

}
