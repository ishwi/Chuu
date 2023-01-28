package core.translations;

import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class TranslationManager {

    public static String m(Messages messages, Object... fragments) {
        return m(messages, null, fragments);
    }

    public static String m(Messages messages, DiscordLocale discordLocale, Object... fragments) {
        ResourceBundle bundle;
        Locale locale = null;
        if (discordLocale == null) {
            bundle = ResourceBundle.getBundle("messages");
        } else {
            locale = Locale.forLanguageTag(discordLocale.getLocale());
            bundle = ResourceBundle.getBundle("messages", locale);
        }
        String string = bundle.getString(messages.key());
        if (fragments.length > 0) {
            MessageFormat messageFormat = new MessageFormat(string, locale);
            return messageFormat.format(fragments);
        }
        return string;
    }
}
