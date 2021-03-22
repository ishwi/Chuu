package core.parsers.params;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.SortedSet;

public class EmotiParameters extends CommandParameters {

    private final SortedSet<Emotable<?>> getEmojis;

    public EmotiParameters(MessageReceivedEvent e, SortedSet<Emotable<?>> getEmojis) {
        super(e);
        this.getEmojis = getEmojis;
    }

    public SortedSet<Emotable<?>> getEmotis() {
        return getEmojis;
    }

    public boolean hasEmotes() {
        return getEmojis.stream().anyMatch(t -> t instanceof CustomEmote);
    }

    public List<String> getEmojis() {
        return getEmojis.stream().filter(t -> t instanceof CustomEmoji).map(t -> ((CustomEmoji) t).entity()).toList();
    }

    public List<Emote> getEmotes() {
        return getEmojis.stream().filter(t -> t instanceof CustomEmote).map(t -> ((CustomEmote) t).entity()).toList();
    }

    public boolean hasEmojis() {
        return getEmojis.stream().anyMatch(t -> t instanceof CustomEmoji);

    }

    public interface Emotable<T> {
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
    }

    public record CustomEmote(int position, Emote entity) implements Emotable<Emote> {

        @Override
        public String getContent() {
            return (entity.isAnimated() ? "a:" : "") + entity.getName() + ":" + entity.getId();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CustomEmote that = (CustomEmote) o;

            return entity.equals(that.entity);
        }

        @Override
        public int hashCode() {
            return entity.hashCode();
        }
    }

    public record CustomEmoji(int position, String entity) implements Emotable<String> {
        @Override
        public String getContent() {
            return entity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CustomEmoji that = (CustomEmoji) o;

            return entity.equals(that.entity);
        }

        @Override
        public int hashCode() {
            return entity.hashCode();
        }
    }
}
