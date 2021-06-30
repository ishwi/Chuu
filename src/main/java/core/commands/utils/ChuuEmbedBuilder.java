package core.commands.utils;

import core.commands.Context;
import core.services.ColorService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.utils.Checks;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.regex.Pattern;

public class ChuuEmbedBuilder extends EmbedBuilder {
    public static final Pattern linkMatcher = Pattern.compile("\\[([^\\[\\]]*)]\\(.*?\\)");

    public ChuuEmbedBuilder(Context e) {
        setColor(ColorService.computeColor(e));
    }

    public ChuuEmbedBuilder(boolean acknowledgeNoImages) {
        setColor(CommandUtil.pastelColor());
    }

    @NotNull
    @Override
    public EmbedBuilder setColor(@Nullable Color color) {
        if (color != null && color.equals(Color.white)) {
            return super.setColor(new Color(254, 255, 255));
        }
        return super.setColor(color);
    }

    @NotNull
    @Override
    public MessageEmbed build() {
        if (this.getDescriptionBuilder().length() > MessageEmbed.TEXT_MAX_LENGTH) {

            this.setDescription(linkMatcher.matcher(this.getDescriptionBuilder().toString()).replaceAll("$1"));
        }
        return super.build();
    }

    @NotNull
    @Override
    public EmbedBuilder setFooter(@Nullable String text, @Nullable String iconUrl) {
        if (text != null) {
            return super.setFooter(CommandUtil.stripEscapedMarkdown(text), iconUrl);
        }
        return super.setFooter(null, iconUrl);
    }

    @NotNull
    @Override
    public EmbedBuilder setAuthor(@Nullable String name, @Nullable String url, @Nullable String iconUrl) {
        if (name != null) {
            return super.setAuthor(CommandUtil.stripEscapedMarkdown(name), url, iconUrl);
        }
        return super.setAuthor(null, url, iconUrl);
    }

    @NotNull
    @Override
    public EmbedBuilder setImage(@Nullable String url) {
        if (StringUtils.isBlank(url)) {
            return super.setImage(null);
        }
        return super.setImage(url);
    }

    @Nonnull
    public EmbedBuilder appendDescription(@Nonnull CharSequence description) {
        Checks.notNull(description, "description");
        if (this.getDescriptionBuilder().length() + description.length() > MessageEmbed.TEXT_MAX_LENGTH) {
            return super.appendDescription(linkMatcher.matcher(description.toString()).replaceAll("$1"));
        }
        return super.appendDescription(description);
    }
}
