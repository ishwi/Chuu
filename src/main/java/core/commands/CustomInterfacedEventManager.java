package core.commands;

import com.google.common.util.concurrent.RateLimiter;
import core.Chuu;
import core.otherlisteners.ReactionListener;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CustomInterfacedEventManager implements IEventManager {

    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, MyCommand<?>> commandListeners = new HashMap<>();
    private final Map<ReactionListener, ScheduledFuture<?>> reactionaries = new HashMap<>();

    public CustomInterfacedEventManager() {
        //Default constructor
    }

    @Override
    public void register(@Nonnull Object listener) {
        if (!(listener instanceof EventListener))
            throw new IllegalArgumentException("Listener must implement EventListener");
        if ((listener instanceof MyCommand)) {
            MyCommand<?> myCommand = (MyCommand<?>) listener;
            List<String> aliases = myCommand.getAliases();
            for (String alias : aliases) {
                commandListeners.put(alias, myCommand);
            }

        }
        if (listener instanceof ReactionListener) {
            ReactionListener reactionListener = (ReactionListener) listener;
            long activeSeconds = reactionListener.getActiveSeconds();
            ScheduledFuture<?> schedule = Chuu.getScheduledExecutorService().schedule((() -> {
                reactionaries.remove(reactionListener);
                listeners.remove(listener);
                reactionListener.dispose();
            }), activeSeconds, TimeUnit.SECONDS);
            reactionaries.put(reactionListener, schedule);
        }
        listeners.add(((EventListener) listener));
    }

    @Override
    public void unregister(@Nonnull Object listener) {
        if ((listener instanceof MyCommand)) {
            MyCommand<?> myCommand = (MyCommand<?>) listener;
            List<String> aliases = myCommand.getAliases();
            for (String alias : aliases) {
                commandListeners.remove(alias);
            }
        }
        if (listener instanceof ReactionListener) {
            ReactionListener reactionListener = (ReactionListener) listener;
            ScheduledFuture<?> scheduledFuture = this.reactionaries.remove(reactionListener);
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
                reactionListener.dispose();
            }
        }
        listeners.remove(listener);
    }

    /**
     * @param event We are taking advantage from only using three types of events in
     *              the whole bot , so we avoid less checking, for the momnent those
     *              are MessageReceivedEvent, GuildMemberLeaveEvent and
     *              MessageReactionAddEvent If you are using any other , pls modify
     *              the code or use the default one
     */
    @Override
    public void handle(@Nonnull GenericEvent event) {

        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent mes = (MessageReceivedEvent) event;
            if (mes.getAuthor().isBot()) {
                return;
            }
            Character correspondingPrefix = Chuu.getCorrespondingPrefix(mes);
            String contentRaw = mes.getMessage().getContentRaw();
            if (contentRaw.length() <= 1 || contentRaw.charAt(0) != correspondingPrefix)
                return;
            Map<Long, RateLimiter> ratelimited = Chuu.getRatelimited();
            RateLimiter rateLimiter = ratelimited.get(mes.getAuthor().getIdLong());
            if (rateLimiter != null) {
                if (!rateLimiter.tryAcquire()) {
                    mes.getChannel().sendMessage("You have been rate limited, try again later.").queue();
                    return;
                }
            }
            String substring = contentRaw.substring(1).split("\\s+")[0];
            MyCommand<?> myCommand = commandListeners.get(substring.toLowerCase());
            if (myCommand != null) {
                if (!Chuu.isMessageAllowed(myCommand, mes)) {
                    mes.getChannel().sendMessage("This command is disabled in this channel.").queue();
                    return;
                }
                try {
                    myCommand.onMessageReceived(mes);
                } catch (Throwable throwable) {
                    JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
                }
            }
            return;
        }
        if (event instanceof GuildMemberRemoveEvent || event instanceof MessageReactionAddEvent) {

            for (EventListener listener : listeners) {
                try {
                    listener.onEvent(event);
                } catch (Throwable throwable) {
                    JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
                }
            }
        }
    }

    @Nonnull
    @Override
    public List<Object> getRegisteredListeners() {
        return Collections.unmodifiableList(new LinkedList<>(listeners));
    }

    public void refreshReactionay(ReactionListener reactionListener, long seconds) {
        ScheduledFuture<?> scheduledFuture = this.reactionaries.get(reactionListener);
        if (scheduledFuture != null && scheduledFuture.cancel(false)) {
            this.reactionaries.put(reactionListener, Chuu.getScheduledExecutorService().schedule((() -> {
                reactionaries.remove(reactionListener);
                listeners.remove(reactionListener);
                reactionListener.dispose();
            }), seconds, TimeUnit.SECONDS));
        }
    }
}
