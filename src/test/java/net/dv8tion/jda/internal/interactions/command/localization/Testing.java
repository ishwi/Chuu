package net.dv8tion.jda.internal.interactions.command.localization;


import core.interactions.InteractionBuilder;
import integration.IntegrationTest;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.restaction.CommandListUpdateActionImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvWriter;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvWriterSettings;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static test.commands.parsers.MessageGenerator.mockJDA;

public class Testing implements IntegrationTest {

    private static String map(TransKey z) {
        return switch (z) {
            case CKeyDescription cKeyDescription -> cKeyDescription.description;
            case CKeyName cKeyName -> cKeyName.name;
            case OKeyDescription oKeyDescription -> oKeyDescription.description;
            case OKeyName oKeyName -> oKeyName.name;
            case ChKey chKey -> chKey.name;
        };
    }

    private static String map2(TransKey z) {
        return switch (z) {
            case CKeyDescription cKeyDescription -> cKeyDescription.description;
            case CKeyName cKeyName -> cKeyName.name;
            case OKeyDescription oKeyDescription -> oKeyDescription.command;
            case OKeyName oKeyName -> oKeyName.command;
            case ChKey chKey -> chKey.name;
        };
    }

    @Test
    void name() throws IOException {
        List<SlashCommandData> data = new ArrayList<>();
        JDAImpl api = mockJDA();
        CommandListUpdateAction commandListUpdateAction = new CommandListUpdateActionImpl(api, null, null) {
            @NotNull
            @Override
            public CommandListUpdateAction addCommands(@NotNull Collection<? extends CommandData> commands) {
                data.addAll(commands.stream().map(w -> (SlashCommandData) w).toList());
                return super.addCommands(commands);
            }
        };
        CommandListUpdateAction clua = InteractionBuilder.fillAction(api, commandListUpdateAction);
        Set<String> keys = new HashSet<>();
        LocalizationFunction fn = localizationKey -> {
            keys.add(localizationKey);

            return Collections.emptyMap();
        };
        data.forEach(commandData -> commandData.setLocalizationFunction(fn));


        data.forEach(SerializableData::toData);


        Map<String, SlashCommandData> collect = data.stream().collect(Collectors.toMap(z -> z.getName().toLowerCase(), z -> z));
        List<TransKey> filledData = getData(keys, collect);

        Map<MapKey, List<TransKey>> grouped = filledData.stream().collect(Collectors.groupingBy(x -> new MapKey(x.getClass(), x.part())));


        var distincts = grouped.entrySet().stream()
                .filter(x ->
                        x.getValue().stream().map(z -> map(z)).distinct().count() > 1)
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().stream().collect(Collectors.groupingBy(j -> map(j)))));

        List<Map.Entry<MapKey, List<TransKey>>> filtered = grouped.entrySet().stream().filter(x -> distincts.get(x.getKey()) == null).toList();

        List<Map.Entry<MapKey, List<TransKey>>> replacements = new ArrayList<>();
        for (var a : distincts.entrySet()) {
            Map<String, List<TransKey>> value = a.getValue();
            MapKey mapKey = a.getKey();
            for (var b : value.entrySet()) {
                replacements.add(Map.entry(new MapKey(mapKey.clazz, map2(b.getValue().get(0)) + ">>>" + mapKey.name()), b.getValue()));
            }
        }
        List<Map.Entry<MapKey, List<TransKey>>> recomposed = Stream.concat(filtered.stream(), replacements.stream()).toList();
        List<Map.Entry<MapKey, List<TransKey>>> sorted = recomposed.stream().sorted(Comparator.comparing(z -> {
            MapKey key = z.getKey();
            List<TransKey> value = z.getValue();
            TransKey first = value.get(0);
            if (first instanceof Spec b) {

                return first.def() + "." + key.name() + "." + b.spec();
            }
            throw new IllegalStateException();
        }, String.CASE_INSENSITIVE_ORDER)).toList();

        var sb = new StringBuilder();
        for (var mapKeyListEntry : sorted) {
            MapKey key = mapKeyListEntry.getKey();
            List<TransKey> value = mapKeyListEntry.getValue();
            TransKey first = value.get(0);
            if (first instanceof Spec b) {
                sb.append(first.def()).append(".").append(key.name()).append(".").append(b.spec()).append(" => ");
            }

            sb.append(value.stream().map(x -> "\"" + x.key() + "\"").collect(Collectors.joining(", ")));
            sb.append("\n");
        }

        StringBuilder d = new StringBuilder();

        StringWriter out = new StringWriter();
        CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
        csvWriterSettings.setQuoteAllFields(true);

        CsvWriter csvWriter = new CsvWriter(new PrintWriter(out), csvWriterSettings);
        csvWriter.writeHeaders("key", "default", "es");

        for (var mapKeyListEntry : sorted) {
            MapKey key = mapKeyListEntry.getKey();
            List<TransKey> value = mapKeyListEntry.getValue();
            TransKey first = value.get(0);
            if (first instanceof Spec b) {
                csvWriter.writeRow(first.def() + "." + key.name + "." + b.spec(), map(first), null);
            }

        }
        csvWriter.flush();
        System.out.println("finished");
    }

    void generateBundles() {

    }


    @NotNull
    private List<TransKey> getData(Set<String> keys, Map<String, SlashCommandData> collect) {
        List<TransKey> filledData = keys.stream().map(key -> {
            return getData(collect, key);

        }).toList();
        return filledData;
    }

    private TransKey getData(Map<String, SlashCommandData> collect, String key) {
        if (key.contains("...")) {
            return new ChKey(key, ".", ".");
        }

        String[] split = key.split("\\.");
        String slashName = split[0];
        String second = split[1];
        SlashCommandData slashCommandData = collect.get(slashName);

        if (split.length == 2) {
            return switch (second) {
                case "name" -> new CKeyName(key, slashName, slashCommandData.getName());
                case "description" -> new CKeyDescription(key, slashName, slashCommandData.getDescription());
                default -> throw new RuntimeException();
            };
        }
        Map<String, SubcommandData> subs = Optional.ofNullable(slashCommandData).map(s -> s
                .getSubcommands().stream().collect(Collectors.toMap(z -> z.getName().toLowerCase().replaceAll("\\s+", "_"), z -> z))).orElse(Collections.emptyMap());
        SubcommandData subcommandData = subs.get(second);
        if (split.length == 3) {
            assert subcommandData != null;
            String third = split[2];
            return switch (third) {
                case "name" -> new CKeyName(key, slashName + "." + second, subcommandData.getName());
                case "description" ->
                        new CKeyDescription(key, slashName + "." + second, subcommandData.getDescription());
                default -> throw new RuntimeException();
            };
        }
        if (split.length == 4 || split.length == 7) {
            assert split[1].equals("options");
            assert subcommandData == null;
            String option = split[2];
            String forth = split[3];
            return handleOptions(key, slashCommandData.getOptions(), split, 2, slashName);

        }
        if (split.length == 5 || split.length == 8) {
            assert split[2].equals("options");
            assert subcommandData != null;
            String option = split[3];
            String fifth = split[4];
            return handleOptions(key, subcommandData.getOptions(), split, 3, second);

        }
        throw new IllegalStateException(key);
    }

    public TransKey handleOptions(String key, List<OptionData> data, String[] split, int optionIndex, String slashPart) {
        String third = split[optionIndex];
        Map<String, OptionData> opts = data.stream().collect(Collectors.toMap(z -> z.getName().toLowerCase().replaceAll("\\s+", "_"), z -> z));
        OptionData optionData = opts.get(third);
        String forth = split[optionIndex + 1];


        return switch (forth) {
            case "name" -> new OKeyName(key, slashPart, third, optionData.getName());
            case "description" -> new OKeyDescription(key, slashPart, third, optionData.getDescription());
            case "choices" -> {
                var choices = optionData.getChoices().stream().collect(Collectors.toMap(z -> z.getName().toLowerCase().replaceAll("\\s+", "_"), z -> z));
                String options = split[optionIndex + 2];
                assert options.equals("options");
                String name = split[optionIndex + 3];
                Command.Choice choice = choices.get(name);
                yield switch (split[optionIndex + 4]) {
                    case "name" -> new ChKey(key, name, choice.getName());
                    default -> throw new IllegalStateException("Unexpected value: " + split[optionIndex + 4]);
                };
            }
            default -> throw new IllegalStateException("Unexpected value: " + forth);
        };
    }

    sealed interface TransKey {

        String key();

        String part();

        String def();

    }


    sealed interface CKey extends TransKey {
        @Override
        default String def() {
            return "Command";
        }

    }

    sealed interface OKey extends TransKey {
        @Override
        default String def() {
            return "Option";
        }
    }

    sealed interface NameSpec extends Spec {
        default String spec() {
            return "name";
        }
    }

    sealed interface Spec {
        String spec();
    }

    sealed interface DescSpec extends Spec {

        default String spec() {
            return "name";
        }
    }

    record MapKey(Class<?> clazz, String name) {

    }


    record CKeyName(String key, String part, String name) implements NameSpec, CKey {
        @Override
        public String spec() {
            return "name";
        }
    }

    record CKeyDescription(String key, String part, String description) implements DescSpec, CKey {
        @Override
        public String spec() {
            return "desc";
        }
    }

    record OKeyName(String key, String command, String part, String name) implements NameSpec, OKey {
        @Override
        public String spec() {
            return "name";
        }
    }

    record OKeyDescription(String key, String command, String part, String description) implements DescSpec, OKey {
        @Override
        public String spec() {
            return "desc";
        }
    }

    record ChKey(String key, String part, String name) implements TransKey, NameSpec {
        public String def() {
            return "Choice";
        }

        @Override
        public String spec() {
            return "name";
        }
    }

}
