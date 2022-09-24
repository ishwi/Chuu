package core.parsers.params;

import core.commands.Context;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.SortedSet;

public class EmotiParameters extends CommandParameters {

    private final SortedSet<Emotable<?>> getEmojis;

    public EmotiParameters(Context e, SortedSet<Emotable<?>> getEmojis) {
        super(e);
        this.getEmojis = getEmojis;
    }

    public SortedSet<Emotable<?>> getEmotis() {
        return getEmojis;
    }

    public boolean hasEmotes() {
        return getEmojis.stream().anyMatch(t -> t instanceof DiscordEmote);
    }

    public List<String> getEmojis() {
        return getEmojis.stream().filter(t -> t instanceof UnicodeEmote).map(t -> ((UnicodeEmote) t).entity().getName()).toList();
    }

    public List<RichCustomEmoji> getEmotes() {
        return getEmojis.stream().filter(t -> t instanceof DiscordEmote).map(t -> ((DiscordEmote) t).entity()).toList();
    }

    public boolean hasEmojis() {
        return getEmojis.stream().anyMatch(t -> t instanceof UnicodeEmote);

    }

    public interface Emotable<T extends Emoji> extends Comparable<Emotable<T>> {
        static String toDisplay(String title) {
            if (title.matches("a:.*\\d+")) {
                return "<" + title + ">";
            } else if (title.matches(".*\\d+")) {
                return "<:" + title + ">";
            }
            return title;
        }

        int position();

        T entity();

        String getContent();

        @Override
        default int compareTo(@NotNull EmotiParameters.Emotable<T> o) {
            return Integer.compare(position(), o.position());
        }
    }

    public record DiscordEmote(int position, RichCustomEmoji entity) implements Emotable<RichCustomEmoji> {

        @Override
        public String getContent() {
            return (entity.isAnimated() ? "a:" : "") + entity.getName() + ":" + entity.getId();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DiscordEmote that = (DiscordEmote) o;

            return entity.equals(that.entity);
        }

        @Override
        public int hashCode() {
            return entity.hashCode();
        }
    }

    public record UnicodeEmote(int position, UnicodeEmoji entity) implements Emotable<UnicodeEmoji> {
        @Override
        public String getContent() {
            return entity.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UnicodeEmote that = (UnicodeEmote) o;

            return entity.equals(that.entity);
        }

        @Override
        public int hashCode() {
            return entity.hashCode();
        }
    }
}
