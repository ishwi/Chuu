package core.translations;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;

public class ChuuResourceBundle extends ResourceBundle {
    private final Map<String, String> translations;

    public ChuuResourceBundle(Map<String, String> translations) {
        this.translations = translations;
    }

    @Override
    protected Object handleGetObject(@NotNull String key) {
        String s = translations.get(key);
        if (s != null) {
            return StringUtils.abbreviate(s, 100);
        }
        return null;
    }

    @Override
    public boolean containsKey(@NotNull String key) {
        return translations.containsKey(key);
    }

    @NotNull
    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(translations.keySet());
    }
}
