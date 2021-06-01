package core.otherlisteners;

import core.otherlisteners.util.ConfirmatorItem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import javax.annotation.Nonnull;
import java.util.Collections;
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

    public Confirmator(EmbedBuilder who, Message message, long author, List<ConfirmatorItem> items) {
        this(who, message, author, items, (z) -> z.clear().setTitle("Confirmation timed out"), true, 30);
    }

    public Confirmator(EmbedBuilder who, Message message, long author, List<ConfirmatorItem> items, UnaryOperator<EmbedBuilder> timeoutCallback, boolean runLastEmbed, long seconds) {
        super(who, message, seconds);
        this.author = author;
        this.items = items;
        this.timeoutCallback = timeoutCallback;
        this.idMap = items.stream().collect(Collectors.toMap(ConfirmatorItem::reaction, z -> z, (a, b) -> a, LinkedHashMap::new));
        this.runLastEmbed = runLastEmbed;
        init();
    }

    @Override
    public void init() {
    }

    @Override
    public void dispose() {
        if (!this.wasThisCalled.get()) {
            this.message.editMessage(timeoutCallback.apply(who).build()).setActionRows(Collections.emptyList()).queue();
        } else {
            if (runLastEmbed && this.didConfirm.get() != null) {
                ConfirmatorItem item = this.idMap.get(this.didConfirm.get());
                this.message.editMessage(item.builder().apply(who).build()).setActionRows(Collections.emptyList()).queue();
            } else {
                this.message.editMessage(message).setActionRows(Collections.emptyList()).queue();
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
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
        event.deferEdit().queue();
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
