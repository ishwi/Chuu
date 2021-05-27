package core.otherlisteners;

import core.otherlisteners.util.ConfirmatorItem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
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
    private final Map<String, ConfirmatorItem> idMap;
    private final boolean runLastEmbed;
    private final AtomicReference<String> didConfirm = new AtomicReference<>(null);
    private final AtomicBoolean wasThisCalled = new AtomicBoolean(false);
    private final Mode type;

    public Confirmator(EmbedBuilder who, Message message, long author, List<ConfirmatorItem> items, Mode mode) {
        this(who, message, author, items, (z) -> z.clear().setTitle("Time Out"), true, 30, mode);
    }

    public Confirmator(EmbedBuilder who, Message message, long author, List<ConfirmatorItem> items, UnaryOperator<EmbedBuilder> timeoutCallback, boolean runLastEmbed, long seconds, Mode mode) {
        super(who, message, seconds);
        this.author = author;
        this.items = items;
        this.timeoutCallback = timeoutCallback;
        this.idMap = items.stream().collect(Collectors.toMap(ConfirmatorItem::reaction, z -> z, (a, b) -> a, LinkedHashMap::new));
        this.runLastEmbed = runLastEmbed;
        this.type = mode;
        init();
    }

    @Override
    public void init() {
        if (this.type == Mode.REACTION) {
            List<RestAction<Void>> reacts = this.idMap.keySet().stream().map(x -> message.addReaction(x)).toList();
            RestAction.allOf(reacts).queue();
        }
    }

    @Override
    public void dispose() {
        if (!this.wasThisCalled.get()) {
            this.message.editMessage(timeoutCallback.apply(who).build()).queue();
        } else {
            if (runLastEmbed && this.didConfirm.get() != null) {
                ConfirmatorItem item = this.idMap.get(this.didConfirm.get());
                this.message.editMessage(item.builder().apply(who).build()).queue();
            }
        }
        if (this.type == Mode.REACTION) {
            clearReacts();
        }
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getMessageIdLong() != message.getIdLong() || (event.getUser() != null && event.getUser().isBot() || event.getUserIdLong() != author) || !event.getReaction().getReactionEmote().isEmoji())
            return;
        ConfirmatorItem item = this.idMap.get(event.getReaction().getReactionEmote().getAsReactionCode());
        if (item != null) {
            wasThisCalled.set(true);
            this.didConfirm.set(item.reaction());
            CompletableFuture.runAsync(() -> item.callback().accept(this.message));
            unregister();

        }
    }

    @Override
    public void onButtonClickedEvent(@Nonnull ButtonClickEvent event) {
        if (event.getUser() == null) {
            return;
        }
        if (event.getMessageIdLong() != message.getIdLong()) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }
        if (event.getUser().getIdLong() != author) {
            return;
        }
        ConfirmatorItem item = this.idMap.get(event.getComponentId());
        if (item != null) {
            wasThisCalled.set(true);
            this.didConfirm.set(item.reaction());
            CompletableFuture.runAsync(() -> item.callback().accept(this.message));
            unregister();
        }
    }

    public enum Mode {
        REACTION, BUTTON
    }
}
