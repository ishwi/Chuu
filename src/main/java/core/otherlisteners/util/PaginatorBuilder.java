package core.otherlisteners.util;

import core.commands.Context;
import core.commands.utils.ButtonUtils;
import dao.entities.Memoized;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PaginatorBuilder<T> {
    private static final Function<?, String> toString = Objects::toString;
    long seconds = 40;
    private Context e;
    private EmbedBuilder eb;
    private List<T> items;
    private boolean numberedEntries = true;
    private int pageSize = 10;
    private Function<T, String> mapper = getDefaultMapper();
    private boolean pagingIndicator = false;
    private Function<Integer, String> extraText = null;
    private BiFunction<Context, EmbedBuilder, RestAction<Message>> creator =
            (context, eb) -> context.sendMessage(eb.build());
    private BiFunction<Context, EmbedBuilder, RestAction<Message>> creatorWithButtons =
            (context, eb) -> context.sendMessage(eb.build(), ActionRow.of(ButtonUtils.getRightButton()));

    public PaginatorBuilder(Context e) {
        this.e = e;
    }

    public PaginatorBuilder(Context e, EmbedBuilder eb, List<T> items) {
        this.e = e;
        this.eb = eb;
        this.items = items;
    }

    @SuppressWarnings("unchecked")
    private static <J> Function<J, String> getDefaultMapper() {
        return (Function<J, String>) toString;
    }

    public PaginatorBuilder<T> e(Context e) {
        this.e = e;
        return this;
    }

    public PaginatorBuilder<T> embedBuilder(EmbedBuilder eb) {
        this.eb = eb;
        return this;
    }

    public PaginatorBuilder<T> items(List<T> items) {
        this.items = items;
        return this;
    }

    public PaginatorBuilder<Memoized<T, String>> memoized(Function<T, String> memoizer) {
        List<Memoized<T, String>> memoizeds = this.items.stream().map(z -> new Memoized<>(z, memoizer)).toList();
        return new PaginatorBuilder<>(e, eb, memoizeds).pageSize(pageSize)
                .numberedEntries(numberedEntries)
                .pagingIndicator(pagingIndicator)
                .seconds(seconds);
    }

    public PaginatorBuilder<T> pageSize(int pageSize) {
        Checks.check(pageSize > 0, "Page size must be greater than 0!");
        this.pageSize = pageSize;
        return this;
    }

    public PaginatorBuilder<T> unnumered() {
        this.numberedEntries = false;
        return this;
    }

    public PaginatorBuilder<T> numberedEntries(boolean numberedEntries) {
        this.numberedEntries = numberedEntries;
        return this;
    }

    public PaginatorBuilder<T> mapper(Function<T, String> mapper) {
        this.mapper = mapper;
        return this;
    }

    public PaginatorBuilder<T> seconds(long seconds) {
        this.seconds = seconds;
        return this;
    }

    public PaginatorBuilder<T> withIndicator() {
        this.pagingIndicator = true;
        return this;
    }

    private PaginatorBuilder<T> pagingIndicator(boolean pagingIndicator) {
        this.pagingIndicator = pagingIndicator;
        return this;
    }

    public PaginatorBuilder<T> creatorWithButtons(BiFunction<Context, EmbedBuilder, RestAction<Message>> creatorWithButtons) {
        this.creatorWithButtons = creatorWithButtons;
        return this;
    }

    public PaginatorBuilder<T> creator(BiFunction<Context, EmbedBuilder, RestAction<Message>> creator) {
        this.creator = creator;
        return this;
    }

    public PaginatorBuilder<T> extraText(Function<Integer, String> extraText) {
        this.extraText = extraText;
        return this;
    }


    @CheckReturnValue
    public Paginator<T> build() {
        return new Paginator<>(e, eb, items, pageSize, numberedEntries, pagingIndicator, seconds, mapper, creator, creatorWithButtons, extraText);
    }
}