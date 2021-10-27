package core.otherlisteners.util;

import core.commands.Context;
import core.commands.utils.ButtonUtils;
import core.otherlisteners.Reactionary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public record Paginator<T>(Context e, EmbedBuilder eb, List<T> items, int pageSize, boolean numberedEntries,
                           boolean pagingIndicator,
                           long seconds,
                           Function<T, String> mapper,
                           BiFunction<Context, EmbedBuilder, RestAction<Message>> creator,
                           BiFunction<Context, EmbedBuilder, RestAction<Message>> creatorWithButtons,
                           Function<Integer, String> extraText,
                           Function<List<T>, ActionRow> extraRow) {

    public Paginator {
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < pageSize && i < items.size(); i++) {
            if (numberedEntries) {
                a.append(i + 1);
            }
            a.append(mapper.apply(items.get(i)));
        }
        if (pagingIndicator) {
            int totalPageNumber = (int) Math.ceil(items.size() / (float) pageSize);
            if (totalPageNumber > 1) {
                a.append("\n1/%d".formatted(totalPageNumber));
            }
        }
        if (extraText != null) {
            a.append("\n").append(extraText.apply(0));
        }
        eb.setDescription(a);
    }

    public static ActionRow defaultActions() {
        return ActionRow.of(ButtonUtils.getRightButton());
    }

    private Consumer<Message> paginatorConsumer() {
        return message -> new Reactionary<>(items, message, pageSize, eb, numberedEntries, pagingIndicator, seconds, mapper, extraText, extraRow);
    }

    public void queue() {
        queue(null);
    }

    private void queue(@Nullable Consumer<Message> consumer) {
        Consumer<Message> combinedConsumer;
        if (consumer == null) {
            combinedConsumer = paginatorConsumer();
        } else {
            combinedConsumer = paginatorConsumer().andThen(consumer);
        }
        if (this.items.size() < 10) {
            creator.apply(e, eb).queue(consumer);
        } else {
            creatorWithButtons.apply(e, eb).queue(combinedConsumer);
        }
    }


}
