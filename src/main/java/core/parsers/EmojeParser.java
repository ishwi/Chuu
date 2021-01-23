package core.parsers;

import com.google.common.collect.ImmutableSortedSet;
import com.vdurmont.emoji.EmojiParser;
import core.parsers.params.EmotiParameters;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EmojeParser extends Parser<EmotiParameters> {
    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected EmotiParameters parseLogic(MessageReceivedEvent e, String[] words) {
        Set<Emote> emotes = new HashSet<>(e.getMessage().getEmotes());
        Map<String, Emote> collect = emotes.stream().collect(Collectors.toMap(Emote::getAsMention, t -> t));
        List<EmotiParameters.Emotable<?>> emotable = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        for (String word : words) {
            Emote emote = collect.get(word);
            if (emote != null) {
                emotable.add(new EmotiParameters.CustomEmote(counter.incrementAndGet(), emote));
            } else {
                List<String> strings = EmojiParser.extractEmojis(word);
                if (!strings.isEmpty()) {
                    strings.forEach(emoji ->
                            emotable.add(new EmotiParameters.CustomEmoji(counter.incrementAndGet(), emoji)));
                }
            }
        }
        Comparator<EmotiParameters.Emotable<?>> tComparator = Comparator.comparingInt(EmotiParameters.Emotable::position);
        SortedSet<EmotiParameters.Emotable<?>> emotables = ImmutableSortedSet.copyOf(tComparator, new HashSet<>(emotable));
        return new EmotiParameters(e, emotables);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "";
    }
}
