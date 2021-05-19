package core.otherlisteners;

import core.otherlisteners.util.ConfirmatorItem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Uses reaction code instead of codepoints!!
 */
public class Confirmator extends ReactionListener {

    private final long author;
    private final List<ConfirmatorItem> items;
    private final UnaryOperator<EmbedBuilder> timeoutCallback;
    private final Map<String, ConfirmatorItem> emoteMap;
    private final boolean runLastEmbed;
    private final AtomicReference<String> didConfirm = new AtomicReference<>(null);
    private final AtomicBoolean wasThisCalled = new AtomicBoolean(false);


    public Confirmator(EmbedBuilder who, Message message, long author, List<ConfirmatorItem> items) {
        this(who, message, author, items, (z) -> z.clear().setTitle("Time Out"), true, 30);
    }


    public Confirmator(EmbedBuilder who, Message message, long author, List<ConfirmatorItem> items, UnaryOperator<EmbedBuilder> timeoutCallback, boolean runLastEmbed, long seconds) {
        super(who, message, seconds);
        this.author = author;
        this.items = items;
        this.timeoutCallback = timeoutCallback;
        this.emoteMap = items.stream().collect(Collectors.toMap(ConfirmatorItem::reaction, z -> z, (a, b) -> a, LinkedHashMap::new));
        this.runLastEmbed = runLastEmbed;
        init();
    }

    @Override
    public void init() {
        List<RestAction<Void>> reacts = this.emoteMap.keySet().stream().map(x -> message.addReaction(x)).toList();
        RestAction.allOf(reacts).queue();
    }

    @Override
    public void dispose() {
        if (!this.wasThisCalled.get()) {
            this.message.editMessage(timeoutCallback.apply(who).build()).queue();
            this.clearReacts();
        } else {
            if (runLastEmbed && this.didConfirm.get() != null) {
                ConfirmatorItem item = this.emoteMap.get(this.didConfirm.get());
                this.message.editMessage(item.builder().apply(who).build()).queue();
            }
        }
        clearReacts();
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getMessageIdLong() != message.getIdLong() || (event.getUser() != null && event.getUser().isBot() || event.getUserIdLong() != author) || !event.getReaction().getReactionEmote().isEmoji())
            return;
        ConfirmatorItem item = this.emoteMap.get(event.getReaction().getReactionEmote().getAsReactionCode());
        if (item != null) {
            wasThisCalled.set(true);
            this.didConfirm.set(item.reaction());
            CompletableFuture.runAsync(() -> item.callback().accept(this.message));
            unregister();

        }
    }
}
