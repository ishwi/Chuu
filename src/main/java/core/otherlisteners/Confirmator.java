package core.otherlisteners;

import dao.entities.Callback;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;

public class Confirmator extends ReactionListener {

    private static final String ACCEPT = "U+2714";
    private static final String REJECT = "U+274c";
    private final long author;
    private final Callback onAccept;
    private final Callback onReject;
    private final UnaryOperator<EmbedBuilder> buildAccept;
    private final UnaryOperator<EmbedBuilder> buildReject;
    private final AtomicBoolean didConfirm = new AtomicBoolean(false);
    private final AtomicBoolean wasThisCalled = new AtomicBoolean(false);


    public Confirmator(EmbedBuilder who, Message message, long author, Callback onAccept, Callback onReject, UnaryOperator<EmbedBuilder> buildAccept, UnaryOperator<EmbedBuilder> buildReject) {
        super(who, message);
        this.author = author;
        this.onAccept = onAccept;
        this.onReject = onReject;
        this.buildAccept = buildAccept;
        this.buildReject = buildReject;
        init();
    }

    @Override
    public void init() {
        message.addReaction(ACCEPT).queue();
        message.addReaction(REJECT).queue();
    }

    @Override
    public void dispose() {
        if (!this.wasThisCalled.get()) {
            this.message.editMessage(who.clear().setTitle("Time Out").build()).queue();
        } else {
            if (this.didConfirm.get()) {
                this.message.editMessage(buildAccept.apply(who).build()).queue();
            } else {
                this.message.editMessage(buildReject.apply(who).build()).queue();
            }
        }
        clearReacts();
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getMessageIdLong() != message.getIdLong() || (event.getUser() != null && event.getUser().isBot() || event.getUserIdLong() != author))
            return;
        switch (event.getReaction().getReactionEmote().getAsCodepoints()) {
            case ACCEPT:
                wasThisCalled.set(true);
                onAccept.executeCallback();
                this.didConfirm.set(true);
                unregister();
                break;
            case REJECT:
                wasThisCalled.set(true);
                onReject.executeCallback();
                this.didConfirm.set(false);
                unregister();
                break;
            default:
        }
    }
}
