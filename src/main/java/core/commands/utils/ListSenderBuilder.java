package core.commands.utils;

import core.commands.Context;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.function.Function;

public class ListSenderBuilder<T> {
    private final Context e;
    private Function<T, String> mapper;
    private List<T> elements;
    private EmbedBuilder embedBuilder;

    public ListSenderBuilder(Context e, Class<T> clazz) {
        this.e = e;
    }

    public ListSenderBuilder(Context e, List<T> elements) {
        this.e = e;
    }


    public ListSenderBuilder<T> setMapper(Function<T, String> mapper) {
        this.mapper = mapper;
        return this;
    }

    public ListSenderBuilder<T> setElements(List<T> elements) {
        this.elements = elements;
        return this;

    }

    public ListSenderBuilder<T> setEmbedBuilder(EmbedBuilder eb) {
        this.embedBuilder = eb;
        return this;

    }

    public ListSender<T> build() {
        return new ListSender<>(e, elements, mapper, embedBuilder);
    }
}
