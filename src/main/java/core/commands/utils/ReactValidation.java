package core.commands.utils;

import core.parsers.params.EmotiParameters;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.Result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReactValidation {


    public static final BiFunction<AtomicLong, List<Emote>, Function<Message, RestAction<List<Result<Void>>>>> sender = (AtomicLong id, List<Emote> emotes) -> (message) -> {
        List<RestAction<Result<Void>>> actions = new ArrayList<>();
        id.set(message.getIdLong());
        for (Emote emote : emotes) {
            try {
                actions.add(message.addReaction(emote).mapToResult());
            } catch (IllegalArgumentException ignored) {
                System.out.println("Check failed because emote not available in guild");
            }
        }
        return RestAction.allOf(actions);
    };

    @org.jetbrains.annotations.NotNull
    public static Consumer<Message> getMessageConsumer(MessageReceivedEvent e, EmotiParameters params, Consumer<List<Emote>> finals) {
        return freshMessage -> {
            Set<Long> emotesReacted = new HashSet<>();
            CompletableFuture<?>[] completables = freshMessage.getReactions().stream().map(reaction -> reaction.retrieveUsers().forEachAsync(user -> {
                if (user.getIdLong() == e.getJDA().getSelfUser().getIdLong()) {
                    MessageReaction.ReactionEmote reactionEmote = reaction.getReactionEmote();
                    Long idLong = reactionEmote.getEmote().getIdLong();
                    emotesReacted.add(idLong);
                    return false;
                }
                return true;
            })).toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(completables).thenAccept(finished -> {
                List<Emote> toIgnore = params.getEmotes().stream().filter(emote -> !emotesReacted.contains(emote.getIdLong())).collect(Collectors.toList());
                if (toIgnore.size() != 0) {
                    freshMessage.getChannel().sendMessage("Couldn't use some emotes because of permissions or unknown emotes.\n" +
                            "The following emotes were ignored: " + toIgnore.stream().map(Emote::getAsMention).collect(Collectors.joining(" "))).queue();
                }
                finals.accept(toIgnore);
            });
        };
    }

    interface TriConsumer<A, B, C> {
        void execute(A a, B b, C c);

    }
}
