package core.otherlisteners.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public record ConfirmatorItem(String reaction, UnaryOperator<EmbedBuilder> builder, Consumer<Message> callback) {
}
