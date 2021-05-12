package core.commands;

import com.google.common.util.concurrent.RateLimiter;
import core.Chuu;
import core.commands.abstracts.MyCommand;
import core.commands.moderation.AdministrativeCommand;
import core.music.listeners.VoiceListener;
import core.otherlisteners.AwaitReady;
import core.otherlisteners.ConstantListener;
import core.otherlisteners.ReactionListener;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class CustomInterfacedEventManager implements IEventManager {

    private final Set<EventListener> otherListeners = ConcurrentHashMap.newKeySet();
    private final Map<String, MyCommand<?>> commandListeners = new HashMap<>();
    private final Map<Long, ConstantListener> constantListeners = new HashMap<>();
    private final Map<ReactionListener, ScheduledFuture<?>> reactionaries = new ConcurrentHashMap<>();
    private AdministrativeCommand administrativeCommand;
    private VoiceListener voiceListener;

    public CustomInterfacedEventManager(int a) {
    }


    public void setVoiceListener(VoiceListener voiceListener) {
        this.voiceListener = voiceListener;
    }

    @Override
    public void register(@Nonnull Object listener) {
        if (!(listener instanceof EventListener))
            throw new IllegalArgumentException("Listener must implement EventListener");
        if (listener instanceof VoiceListener voiceListener) {
            this.voiceListener = voiceListener;
        }
        if ((listener instanceof MyCommand<?> myCommand)) {
            List<String> aliases = myCommand.getAliases();
            for (String alias : aliases) {
                commandListeners.put(alias, myCommand);
            }
            if (listener instanceof AdministrativeCommand admin) {
                this.administrativeCommand = admin;
            }
        }
        if (listener instanceof ReactionListener reactionListener) {
            ScheduledFuture<?> schedule = Chuu.getScheduledExecutorService().schedule((() -> {
                reactionaries.remove(reactionListener);
                reactionListener.dispose();
            }), reactionListener.getActiveSeconds(), TimeUnit.SECONDS);
            reactionaries.put(reactionListener, schedule);
        } else if (listener instanceof ConstantListener cl) {
            constantListeners.put(cl.channelId(), cl);
        }
        if (listener instanceof AwaitReady) {
            otherListeners.add((EventListener) listener);
        }

    }

    @Override
    public void unregister(@Nonnull Object listener) {
        if ((listener instanceof MyCommand<?> myCommand)) {
            List<String> aliases = myCommand.getAliases();
            for (String alias : aliases) {
                commandListeners.remove(alias);
            }
        } else if (listener instanceof ReactionListener reactionListener) {
            ScheduledFuture<?> scheduledFuture = this.reactionaries.remove(reactionListener);
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
                reactionListener.dispose();
            }
        } else if (listener instanceof AwaitReady) {
            otherListeners.remove(listener);
        } else if (listener instanceof ConstantListener cl) {
            constantListeners.remove(cl.channelId(), cl);

        }
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
        if (event instanceof MessageReceivedEvent mes) {
            if (mes.getAuthor().isBot()) {
                return;
            }
            ContextMessageReceived ctx = new ContextMessageReceived(mes);
            Character correspondingPrefix = Chuu.getCorrespondingPrefix(ctx);
            String contentRaw = mes.getMessage().getContentRaw();
            if (mes.isFromGuild() && ((contentRaw.length() <= 1) || contentRaw.charAt(0) != correspondingPrefix)) {
                if (mes.getMessage().getMentionedUsers().contains(mes
                        .getJDA().getSelfUser()) && mes.getMessage().getType() != MessageType.INLINE_REPLY) {
                    mes.getChannel().sendMessage("My prefix is: `" + Chuu.getCorrespondingPrefix(ctx) + "`").queue();
                }
                return;
            }
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
                if (!Chuu.getMessageDisablingService().isMessageAllowed(myCommand, ctx)) {
                    if (Chuu.getMessageDisablingService().doResponse(ctx))
                        mes.getChannel().sendMessage("This command is disabled in this channel.").queue();
                    return;
                }
                try {
                    myCommand.onMessageReceived(mes);
                } catch (Throwable throwable) {
                    JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
                }
            }

        } else if (event instanceof SlashCommandEvent sce) {

            MyCommand<?> myCommand;
            if (sce.getSubcommandName() == null) {
                myCommand = commandListeners.get(sce.getName().toLowerCase(Locale.ROOT));
            } else {
                myCommand = commandListeners.get(sce.getSubcommandName().toLowerCase(Locale.ROOT));
            }
            try {
                myCommand.onSlashCommandReceived(sce);
            } catch (Throwable throwable) {
                JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
            }
        } else
            try {
                if (event instanceof GuildMemberRemoveEvent || event instanceof GuildMemberJoinEvent || event instanceof GuildJoinEvent) {
                    administrativeCommand.onEvent(event);
                } else if (event instanceof MessageReactionAddEvent e3) {
                    ConstantListener c = constantListeners.get(e3.getChannel().getIdLong());
                    if (c != null) {
                        c.onMessageReactionAdd(e3);
                        return;
                    }
                    for (ReactionListener listener : reactionaries.keySet()) {
                        listener.onMessageReactionAdd(e3);
                    }
                } else if (event instanceof GuildVoiceJoinEvent || event instanceof GuildVoiceLeaveEvent || event instanceof GuildVoiceMoveEvent) {

                    this.voiceListener.onEvent(event);

                } else if (event instanceof ReadyEvent) {
                    for (EventListener listener : otherListeners) {
                        listener.onEvent(event);
                    }
                }
            } catch (Throwable throwable) {
                JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
            }
    }

    @Nonnull
    @Override
    public List<Object> getRegisteredListeners() {
        return Stream.<Object>concat(
                commandListeners.values().stream().distinct(),
                Stream.concat(
                        reactionaries.keySet().stream(),
                        otherListeners.stream()
                )).toList();
    }

    public void refreshReactionay(ReactionListener reactionListener, long seconds) {
        ScheduledFuture<?> scheduledFuture = this.reactionaries.get(reactionListener);
        if (scheduledFuture != null && scheduledFuture.cancel(false)) {
            this.reactionaries.put(reactionListener, Chuu.getScheduledExecutorService().schedule((() -> {
                reactionaries.remove(reactionListener);
                otherListeners.remove(reactionListener);
                reactionListener.dispose();
            }), seconds, TimeUnit.SECONDS));
        }
    }
}
