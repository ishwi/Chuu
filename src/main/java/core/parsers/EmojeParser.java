package core.parsers;

import com.google.common.collect.ImmutableSortedSet;
import com.vdurmont.emoji.EmojiParser;
import core.Chuu;
import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.ContextSlashReceived;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.EmotiParameters;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EmojeParser extends Parser<EmotiParameters> {
    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    public EmotiParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        CommandInteraction e = ctx.e();
        var option = Optional.ofNullable(e.getOption("emote-emoji")).map(OptionMapping::getAsString).map(z -> z.split("\\s+")).orElse(new String[]{});
        return parseWords(ctx, option);

    }

    @Override
    protected EmotiParameters parseLogic(Context e, String[] words) {
        return parseWords(e, words);
    }

    @Nullable
    private EmotiParameters parseWords(Context e, String[] words) {
        List<EmotiParameters.Emotable<?>> emotable = new ArrayList<>();

        AtomicInteger counter = new AtomicInteger(0);
        if (e instanceof ContextSlashReceived cts) {
            Pattern compile = Pattern.compile("<a?:(?:\\w+:)?(\\d+)>");
            Map<Boolean, List<String>> collect = Arrays.stream(words).collect(Collectors.partitioningBy(z -> compile.matcher(z).matches()));
            collect.get(true).forEach(z -> {
                Matcher matcher = compile.matcher(z);
                if (matcher.matches()) {
                    Emote emote = Chuu.getShardManager().getEmoteById(matcher.group(1));
                    emotable.add(new EmotiParameters.CustomEmote(counter.incrementAndGet(), emote));
                }
            });
            words = collect.get(false).toArray(String[]::new);
        }
        for (String word : words) {
            if (e instanceof ContextMessageReceived ctm) {
                for (Emote emote : ctm.e().getMessage().getEmotes()) {
                    if (word.contains(emote.getAsMention())) {
                        emotable.add(new EmotiParameters.CustomEmote(counter.incrementAndGet(), emote));
                        word = word.replaceFirst(emote.getAsMention(), "");
                    }
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

        return List.of(() -> new ExplanationLineType("emote-emoji", "If not emotes are provided, the reactions will be cleared\nEmotes can be either server emotes or emojis.", OptionType.STRING));
    }

}
