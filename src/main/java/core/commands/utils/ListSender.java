package core.commands.utils;

import core.commands.Context;
import core.otherlisteners.Reactionary;
import dao.entities.Memoized;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.function.Function;

public record ListSender<T>(Context e, List<T> elements, Function<T, String> mapper, EmbedBuilder eb, boolean memoize) {


    public ListSender(Context e, List<T> elements, Function<T, String> mapper, EmbedBuilder eb) {
        this(e, elements, mapper, eb, false);
    }

    public void doSend() {
        doSend(true);
    }

    public ListSender<T> withMemoize() {
        return new ListSender<>(e, elements, mapper, eb, true);
    }

    public void doSend(boolean numberedEntries) {

        StringBuilder a = new StringBuilder();
        List<?> items;
        if (memoize) {
            List<Memoized<T, String>> memoizeds = elements.stream().map(w -> new Memoized<>(w, mapper)).toList();
            for (int i = 0; i < memoizeds.size() && i < 10; i++) {
                var sb = memoizeds.get(i);
                if (numberedEntries) {
                    a.append(i + 1);
                }
                a.append(sb.toString());
            }
            items = memoizeds;
        } else {
            List<String> s = elements().stream().map(mapper).toList();
            for (int i = 0; i < elements.size() && i < 10; i++) {
                String sb = s.get(i);
                if (numberedEntries) {
                    a.append(i + 1);
                }
                a.append(sb);
            }
            items = s;
        }
        e.sendMessage(eb.setDescription(a).build()).queue(mes ->
                new Reactionary<>(items, mes, eb, numberedEntries));

    }

}
