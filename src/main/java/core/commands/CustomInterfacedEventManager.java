package core.commands;

import core.Chuu;
import core.commands.abstracts.MyCommand;
import core.music.listeners.VoiceListener;
import core.otherlisteners.*;
import core.parsers.params.CommandParameters;
import core.services.ChuuRunnable;
import core.util.ChuuVirtualPool;
import core.util.StringUtils;
import io.github.bucket4j.Bucket;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.internal.JDAImpl;
import org.jetbrains.annotations.NotNull;

import java.nio.CharBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class CustomInterfacedEventManager implements IEventManager {

    private static final ExecutorService reactionExecutor = ChuuVirtualPool.of("Reaction-handle-");
    private static final ExecutorService autocompleteExecutor = ChuuVirtualPool.of("AutoComplete-handle-");
    private final Set<EventListener> otherListeners = ConcurrentHashMap.newKeySet();
    private final Map<String, MyCommand<? extends CommandParameters>> commandListeners = new HashMap<>();
    private final Map<Long, ChannelConstantListener> channelConstantListeners = new HashMap<>();
    private final Set<ConstantListener> constantListeners = new HashSet<>();
    private final Map<Long, Map<ReactionListener, ScheduledFuture<?>>> reactionaries = new ConcurrentHashMap<>();
    private final Map<String, MyCommand<? extends CommandParameters>> slashVariants = new HashMap<>();
    public boolean isReady;
    private VoiceListener voiceListener;
    private AutoCompleteListener autoCompleteListener;
    private JoinLeaveListener joinLeaveListener;

    public CustomInterfacedEventManager() {
    }

    private void handleReaction(@NotNull GenericEvent event) {
        assert event instanceof MessageReactionAddEvent || event instanceof ButtonInteractionEvent || event instanceof StringSelectInteractionEvent;
        long channelId = switch (event) {
            case MessageReactionAddEvent e3 -> e3.getChannel().getIdLong();
            case ButtonInteractionEvent e3 -> Optional.of(e3.getChannel()).map(Channel::getIdLong).orElse(0L);
            case StringSelectInteractionEvent e3 -> e3.getChannel().getIdLong();
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };
        ChannelConstantListener c = channelConstantListeners.get(channelId);
        if (c != null) {
            c.onEvent(event);
            return;
        }
        Map<ReactionListener, ScheduledFuture<?>> channelReactionaries = reactionaries.getOrDefault(channelId, Collections.emptyMap());
        for (ReactionListener listener : channelReactionaries.keySet()) {
            listener.onEvent(event);
        }

        for (ConstantListener constantListener : constantListeners) {
            constantListener.onEvent(event);
        }


    }


    @Override
    public void register(@NotNull Object listener) {
        if (!(listener instanceof EventListener))
            throw new IllegalArgumentException("Listener must implement EventListener");
        if (listener instanceof VoiceListener voiceListener) {
            this.voiceListener = voiceListener;
        }
        if (listener instanceof AutoCompleteListener acl) {
            this.autoCompleteListener = acl;
        }
        if (listener instanceof JoinLeaveListener jl) {
            this.joinLeaveListener = jl;
        }
        if ((listener instanceof MyCommand<?> myCommand)) {
            List<String> aliases = myCommand.getAliases();
            for (String alias : aliases) {
                commandListeners.put(alias, myCommand);
            }

        }
        if (listener instanceof ReactionListener reactionListener) {
            ScheduledFuture<?> schedule = Chuu.getScheduledService().addSchedule((() -> {
                Map<ReactionListener, ScheduledFuture<?>> prev = reactionaries.get(reactionListener.channelId);
                if (prev != null) {
                    prev.remove(reactionListener);
                }
                reactionListener.dispose();
            }), reactionListener.getActiveSeconds(), TimeUnit.SECONDS);
            Map<ReactionListener, ScheduledFuture<?>> map = new WeakHashMap<>();
            map.put(reactionListener, schedule);
            reactionaries.merge(reactionListener.channelId, map, (m1, m2) -> {
                m1.putAll(m2);
                return m1;
            });
        } else if (listener instanceof ConstantListener cl) {
            if (listener instanceof ChannelConstantListener ccl) {
                channelConstantListeners.put(ccl.getChannelId(), ccl);
            } else {
                constantListeners.add(cl);
            }
        }
        if (listener instanceof AwaitReady) {
            otherListeners.add((EventListener) listener);
        }

    }

    @Override
    public void unregister(@NotNull Object listener) {
        switch (listener) {
            case MyCommand<?> myCommand -> {
                List<String> aliases = myCommand.getAliases();
                for (String alias : aliases) {
                    commandListeners.remove(alias);
                }
            }
            case ReactionListener reactionListener -> {
                Map<ReactionListener, ScheduledFuture<?>> prev = this.reactionaries.get(reactionListener.channelId);
                if (prev != null) {
                    ScheduledFuture<?> scheduledFuture = prev.remove(reactionListener);
                    if (prev.isEmpty()) {
                        this.reactionaries.remove(reactionListener.channelId);
                    }
                    if (scheduledFuture != null) {
                        scheduledFuture.cancel(true);
                        reactionListener.dispose();
                    }
                }

            }
            case AwaitReady a -> otherListeners.remove(a);
            case ChannelConstantListener ccl -> channelConstantListeners.remove(ccl.getChannelId(), ccl);
            case ConstantListener cl -> constantListeners.remove(cl);
            default -> {
            }
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
    public void handle(@NotNull GenericEvent event) {
        try {
            switch (event) {
                case CommandAutoCompleteInteractionEvent cacie ->
                        autocompleteExecutor.submit((ChuuRunnable) () -> autoCompleteListener.handle(cacie));
                case MessageReceivedEvent mes ->
                        handleMessageReceived(mes); // Delegate running in pool if its a valid message
                case UserContextInteractionEvent ucie -> handleUserCommand(ucie);
                case SlashCommandInteractionEvent sce -> handleSlashCommand(sce);
                case ReadyEvent re -> {
                    for (EventListener listener : otherListeners)
                        listener.onEvent(re);
                }
                case MessageReactionAddEvent react ->
                        reactionExecutor.submit((ChuuRunnable) () -> this.handleReaction(react));
                case ButtonInteractionEvent button -> this.handleReaction(button);
                case StringSelectInteractionEvent selected ->
                        reactionExecutor.submit((ChuuRunnable) () -> this.handleReaction(selected));

                // TODO cant group then on one
                case GuildVoiceUpdateEvent gvje -> this.voiceListener.onEvent(gvje);

                case GuildMemberRemoveEvent gmre -> this.joinLeaveListener.onEvent(gmre);
                case GuildMemberJoinEvent gmje -> this.joinLeaveListener.onEvent(gmje);
                case GuildJoinEvent gje -> this.joinLeaveListener.onEvent(gje);
                default -> {
                }

            }
        } catch (Throwable throwable) {
            JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
        }
    }

    private void handleSlashCommand(SlashCommandInteractionEvent sce) {
        if (!isReady) {
            return;
        }
        MyCommand<? extends CommandParameters> myCommand = parseCommand(sce);
        if (myCommand == null) {
            Chuu.getLogger().warn("Not found command {} ", sce.getFullCommandName());
            return;
        }
        ContextSlashReceived ctx = new ContextSlashReceived(sce);
        if (!Chuu.getMessageDisablingService().isMessageAllowed(myCommand, ctx)) {
            if (Chuu.getMessageDisablingService().doResponse(ctx))
                sce.reply("This command is disabled in this channel.").queue();
            else {
                sce.reply("This command is disabled in this channel.").setEphemeral(true).queue();
            }
            return;
        }
        myCommand.onSlashCommandReceived(sce);
    }

    public MyCommand<? extends CommandParameters> parseCommand(CommandInteraction sce) {
        MyCommand<? extends CommandParameters> myCommand;
        if (sce.getSubcommandName() == null) {
            myCommand = commandListeners.get(sce.getName().toLowerCase(Locale.ROOT));
        } else {
            myCommand = slashVariants.get(sce.getFullCommandName());
            if (myCommand == null) {
                myCommand = commandListeners.get(sce.getSubcommandName());
            }
        }
        return myCommand;
    }

    public MyCommand<? extends CommandParameters> parseCommand(CommandAutoCompleteInteractionEvent sce) {
        MyCommand<? extends CommandParameters> myCommand;
        if (sce.getSubcommandName() == null) {
            myCommand = commandListeners.get(sce.getName().toLowerCase(Locale.ROOT));
        } else {
            myCommand = slashVariants.get(sce.getFullCommandName());
            if (myCommand == null) {
                myCommand = commandListeners.get(sce.getSubcommandName());
            }
        }
        return myCommand;
    }


    private void handleUserCommand(UserContextInteractionEvent ucie) {
        if (!isReady) {
            return;
        }

        MyCommand<? extends CommandParameters> myCommand = parseCommand(ucie);

        myCommand.onUserCommandReceived(ucie);
    }

    private void handleMessageReceived(MessageReceivedEvent mes) {
        if (mes.getAuthor().isBot()) {
            return;
        }
        Character correspondingPrefix = Chuu.prefixService.getCorrespondingPrefix(mes);
        String contentRaw = mes.getMessage().getContentRaw();
        int length = contentRaw.length();
        if ((length <= 1)) {
            return;
        }
        boolean pingPrefix = false;
        if (mes.isFromGuild() && contentRaw.charAt(0) != correspondingPrefix) {
            if (mes.getMessage().getMentions().isMentioned(mes.getJDA().getSelfUser(),
                    Message.MentionType.USER)
                && mes.getMessage().getType() != MessageType.INLINE_REPLY) {
                if (mes.getMessage().getContentRaw().contains("prefix")) {
                    mes.getChannel().sendMessage("My prefix is: `" + correspondingPrefix + "`").queue();
                } else {
                    contentRaw = Chuu.PING_REGEX.matcher(contentRaw).replaceAll("");
                    pingPrefix = true;
                }
            } else {
                return;
            }
        }
        Map<Long, Bucket> ratelimited = Chuu.getRatelimited();
        Bucket rateLimiter = ratelimited.get(mes.getAuthor().getIdLong());
        if (rateLimiter != null) {
            if (!rateLimiter.tryConsume(1)) {
                mes.getChannel().sendMessage("You have been rate limited, try again later.").queue();
                return;
            }
        }
        CharSequence substring;
        if (pingPrefix) {
            substring = contentRaw;
        } else {
            substring = CharBuffer.wrap(contentRaw, 1, length);
        }
        String command = StringUtils.WORD_SPLITTER.split(substring)[0];
        MyCommand<?> myCommand = commandListeners.get(command.toLowerCase());
        if (myCommand != null) {
            ContextMessageReceived ctx = new ContextMessageReceived(mes, pingPrefix);
            if (!Chuu.getMessageDisablingService().isMessageAllowed(myCommand, ctx)) {
                if (Chuu.getMessageDisablingService().doResponse(ctx))
                    mes.getChannel().sendMessage("This command is disabled in this channel.").queue();
                return;
            }
            myCommand.onMessageReceived(ctx);
        }
    }

    @NotNull
    @Override
    public List<Object> getRegisteredListeners() {
        return Stream.concat(
                commandListeners.values().stream().distinct(),
                Stream.concat(
                        reactionaries.keySet().stream(),
                        otherListeners.stream()
                )).toList();
    }

    public void refreshReactionay(ReactionListener reactionListener, long seconds) {
        Map<ReactionListener, ScheduledFuture<?>> channelReactionaires = this.reactionaries.get(reactionListener.channelId);
        if (channelReactionaires != null) {
            ScheduledFuture<?> scheduledFuture = channelReactionaires.get(reactionListener);
            if (scheduledFuture != null && scheduledFuture.cancel(false)) {
                channelReactionaires.put(reactionListener, Chuu.getScheduledService().addSchedule((() -> {
                    channelReactionaires.remove(reactionListener);
                    if (channelReactionaires.isEmpty()) {
                        this.reactionaries.remove(reactionListener.channelId);
                    }
                    otherListeners.remove(reactionListener);
                    reactionListener.dispose();
                }), seconds, TimeUnit.SECONDS));
            }
        }
    }

    public void addSlashVariants(Map<String, MyCommand<?>> slashVariants) {
        this.slashVariants.putAll(slashVariants);
    }

}


