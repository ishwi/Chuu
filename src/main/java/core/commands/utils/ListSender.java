package core.commands.utils;

import core.commands.Context;
import core.otherlisteners.Reactionary;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.function.Function;

public record ListSender<T>(Context e, List<T> elements, Function<T, String> mapper, EmbedBuilder eb) {
    public void doSend() {

        StringBuilder a = new StringBuilder();
        List<String> s = elements().stream().map(mapper).toList();
        for (int i = 0; i < elements.size() && i < 10; i++) {
            String sb = s.get(i);
            a.append(i + 1).append(sb);
        }
        e.sendMessage(eb.setDescription(a).build()).queue(mes ->
                new Reactionary<>(s, mes, eb));

    }
}
