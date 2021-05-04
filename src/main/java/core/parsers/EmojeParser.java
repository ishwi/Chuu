package core.parsers;

import com.google.common.collect.ImmutableSortedSet;
import com.vdurmont.emoji.EmojiParser;
import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.EmotiParameters;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EmojeParser extends Parser<EmotiParameters> {
    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected EmotiParameters parseLogic(Context e, String[] words) {
        List<EmotiParameters.Emotable<?>> emotable = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        for (String word : words) {
            for (Emote emote : ((ContextMessageReceived) e).e().getMessage().getEmotes()) {
                if (word.contains(emote.getAsMention())) {
                    emotable.add(new EmotiParameters.CustomEmote(counter.incrementAndGet(), emote));
                    word = word.replaceFirst(emote.getAsMention(), "");
                }
            }
            List<String> strings = EmojiParser.extractEmojis(word);
            if (!strings.isEmpty()) {
                strings.forEach(emoji ->
                        emotable.add(new EmotiParameters.CustomEmoji(counter.incrementAndGet(), emoji)));
            }
        }


        SortedSet<EmotiParameters.Emotable<?>> emotables = ImmutableSortedSet.copyOf(Comparator.comparingInt(EmotiParameters.Emotable::position), new HashSet<>(emotable));
        if (emotables.size() > 6) {
            sendError("Can't add more than 6 emotes!", e);
            return null;
        }
        return new

                EmotiParameters(e, emotables);

    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(() -> new ExplanationLineType("Emote|Emoji", "If not emotes are provided, the reactions will be cleared\nEmotes can be either server emotes or emojis.", OptionType.STRING));
    }

}
