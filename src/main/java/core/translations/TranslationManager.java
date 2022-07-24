package core.translations;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class TranslationManager {

    public static String m(Messages messages, Object... fragments) {
        return m(messages, null, fragments);
    }

    public static String m(Messages messages, Locale locale, Object... fragments) {
        ResourceBundle bundle;
        if (locale == null) {
            bundle = ResourceBundle.getBundle("messages");
        } else {
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
