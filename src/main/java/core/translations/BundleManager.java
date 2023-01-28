package core.translations;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class BundleManager {

    private static final String MAPPINGFILES = "/mappings.txt";
    private static final String TRANSLATIONS = "/translations.csv";


    public Map<DiscordLocale, ChuuResourceBundle> generate() {
        Map<DiscordLocale, ChuuResourceBundle> bundles = new HashMap<>();

        Map<String, List<CharSequence>> mappings = new HashMap<>();
        try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(MAPPINGFILES)))) {
            while (inputStream.ready()) {
                String s = inputStream.readLine();
                String[] split = s.split(" => ");
                String key = split[0];
                String values = split[1];
                List<CharSequence> elements = Arrays.stream(values.split(", ")).map(x -> x.subSequence(1, x.length() - 1)).toList();
                mappings.put(key, elements);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, Map<String, String>> translations = new HashMap<>();
        try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(TRANSLATIONS)))) {
            CSVParser reader = new CSVParser(inputStream, CSVFormat.Builder.create().setQuoteMode(QuoteMode.ALL).build());

            int i = 0;
            List<String> languages = new ArrayList<>();
            for (CSVRecord strings : reader) {
                int j = 0;
                if (i++ == 0) {
                    for (String string : strings) {
                        if (j++ >= 2) {
                            languages.add(string);
                            translations.put(string, new HashMap<>());
                        }
                    }
                    continue;
                }
                List<String> cols = strings.toList();
                for (int i1 = 2; i1 < cols.size(); i1++) {
                    String value = cols.get(i1);
                    if (value != null && !value.isBlank()) {
                        Map<String, String> currentMap = translations.get(languages.get(i1 - 2));
                        String mappingKey = cols.get(0);
                        List<CharSequence> allMapped = mappings.get(mappingKey);
                        for (CharSequence key : allMapped) {
                            currentMap.put(key.toString(), value);

                        }
                    }
                }
            }
            for (Map.Entry<String, Map<String, String>> tran : translations.entrySet()) {
                if (!tran.getValue().isEmpty()) {
                    bundles.put(DiscordLocale.from(tran.getKey()), new ChuuResourceBundle(tran.getValue()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bundles;


    }
}
