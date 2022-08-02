package core.otherlisteners;

import core.commands.Context;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ButtonValidatorBuilder<T> {
    private UnaryOperator<EmbedBuilder> getLastMessage;
    private Supplier<T> elementFetcher;
    private BiFunction<T, EmbedBuilder, EmbedBuilder> fillBuilder;
    private EmbedBuilder who;
    private Context context;
    private long discordId;
    private Map<String, Reaction<T, ButtonInteractionEvent, ButtonResult>> actionMap;
    private List<ActionRow> actionRows;
    private boolean allowOtherUsers;
    private boolean renderInSameElement;
    private long channelId;
    private long activeSeconds = 30;

    public ButtonValidatorBuilder<T> setGetLastMessage(UnaryOperator<EmbedBuilder> getLastMessage) {
        this.getLastMessage = getLastMessage;
        return this;
    }

    public ButtonValidatorBuilder<T> setElementFetcher(Supplier<T> elementFetcher) {
        this.elementFetcher = elementFetcher;
        return this;
    }

    public ButtonValidatorBuilder<T> setFillBuilder(BiFunction<T, EmbedBuilder, EmbedBuilder> fillBuilder) {
        this.fillBuilder = fillBuilder;
        return this;
    }

    public ButtonValidatorBuilder<T> setWho(EmbedBuilder who) {
        this.who = who;
        return this;
    }

    public ButtonValidatorBuilder<T> setContext(Context context) {
        this.context = context;
        return this;
    }

    public ButtonValidatorBuilder<T> setDiscordId(long discordId) {
        this.discordId = discordId;
        return this;
    }

    public ButtonValidatorBuilder<T> setActionMap(Map<String, Reaction<T, ButtonInteractionEvent, ButtonResult>> actionMap) {
        this.actionMap = actionMap;
        return this;
    }

    public ButtonValidatorBuilder<T> setActionRows(List<ActionRow> actionRows) {
        this.actionRows = actionRows;
        return this;
    }

    public ButtonValidatorBuilder<T> setAllowOtherUsers(boolean allowOtherUsers) {
        this.allowOtherUsers = allowOtherUsers;
        return this;
    }

    public ButtonValidatorBuilder<T> setRenderInSameElement(boolean renderInSameElement) {
        this.renderInSameElement = renderInSameElement;
        return this;
    }

    public ButtonValidatorBuilder<T> setChannelId(long channelId) {
        this.channelId = channelId;
        return this;
    }

    public ButtonValidatorBuilder<T> setActiveSeconds(long activeSeconds) {
        this.activeSeconds = activeSeconds;
        return this;
    }

    public ButtonValidator<T> queue() {
        return new ButtonValidator<>(getLastMessage, elementFetcher, fillBuilder, who, context, discordId, actionMap, actionRows, allowOtherUsers, renderInSameElement, channelId, activeSeconds);
    }
}
