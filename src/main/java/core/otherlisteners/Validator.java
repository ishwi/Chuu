package core.otherlisteners;

import core.Chuu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Validator<T> extends ListenerAdapter {

    private final Supplier<T> elementFetcher;
    private final BiFunction<T, EmbedBuilder, EmbedBuilder> fillBuilder;
    private final BiConsumer<T, JDA> succesFunction;
    private final Consumer<T> rejectFunction;
    private final EmbedBuilder who;
    private final long whom;
    private final MessageChannel messageChannel;
    private T currentElement;
    private int counter = 0;
    private Message message;


    public Validator(Supplier<T> elementFetcher, BiFunction<T, EmbedBuilder, EmbedBuilder> fillBuilder, BiConsumer<T, JDA> succesFunction, Consumer<T> rejectFunction, EmbedBuilder who, MessageChannel channel, long discordId) {
        this.elementFetcher = elementFetcher;
        this.succesFunction = succesFunction;
        this.rejectFunction = rejectFunction;
        this.fillBuilder = fillBuilder;
        this.who = who;
        this.messageChannel = channel;
        this.whom = discordId;
        try {
            initReactionary();
        } catch (Throwable e) {
            Chuu.getLogger().warn(e.getMessage());
        }
    }

    private void endItAll(JDA jda) {
        jda.removeEventListener(this);
        message.clearReactions().queue();
    }

    private void noMoreElements(JDA jda) {
        if (message == null) {
            this.message = messageChannel.sendMessage("No aliases to review!").complete();
        } else
            message.editMessage(who.clearFields().setTitle("No more aliases to review!").build()).complete();
        endItAll(jda);
    }

    private MessageAction doTheThing(JDA jda) {
        T t = elementFetcher.get();
        if (t == null || t.equals(currentElement)) {
            noMoreElements(jda);
            return null;
        }
        this.currentElement = t;
        EmbedBuilder apply = fillBuilder.apply(t, who);
        return messageChannel.sendMessage(apply.build());
    }

    private void initReactionary() {
        MessageAction messageAction = doTheThing(messageChannel.getJDA());
        if (messageAction == null) {
            return;
        }
        this.message = messageAction.complete();
        message.addReaction("U+274c")
                .flatMap(x -> message.addReaction("U+2714"))
                .queue(t -> message.getJDA().addEventListener(this));
        while (true) {
            int prevCounter = counter;
            try {
                Thread.sleep(20000);
            } catch (InterruptedException ex) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
            }
            if (this.counter == prevCounter) {
                noMoreElements(messageChannel.getJDA());
                break;
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getUserIdLong() != whom || event.getMessageIdLong() != message.getIdLong())
            return;

        switch (event.getReaction().getReactionEmote().getAsCodepoints()) {
            case "U+2714":
                this.succesFunction.accept(currentElement, event.getJDA());
                MessageAction messageAction = this.doTheThing(event.getJDA());
                if (messageAction != null)
                    messageAction.queue(x -> accept(x, "Alias Accepted"));
                break;
            case "U+274c":
                this.rejectFunction.accept(currentElement);
                messageAction = this.doTheThing(event.getJDA());
                if (messageAction != null)
                    messageAction.queue(x -> accept(x, "Alias Rejected"));
                break;
            default:
        }

    }

    private void accept(Message mes, String content) {
        this.message.delete().queue(t -> {
            this.message = mes;
            counter++;
            message.addReaction("U+274c")
                    .flatMap(y -> message.addReaction("U+2714")).queue();
        });
    }

}
