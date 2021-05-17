package core.commands.utils;

import core.commands.Context;
import core.services.ColorService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class ChuuEmbedBuilder extends EmbedBuilder {
    private static final Pattern linkMatcher = Pattern.compile("\\[([^\\[\\]]*)]\\(.*?\\)");

    public ChuuEmbedBuilder(Context e) {
        setColor(ColorService.computeColor(e));
    }

    public ChuuEmbedBuilder(boolean acknowledgeNoImages) {
        setColor(CommandUtil.pastelColor());
    }


    @NotNull
    @Override
    public MessageEmbed build() {
        if (this.getDescriptionBuilder().length() > MessageEmbed.TEXT_MAX_LENGTH) {

            this.setDescription(linkMatcher.matcher(this.getDescriptionBuilder().toString()).replaceAll("$1"));
        }
        return super.build();
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
