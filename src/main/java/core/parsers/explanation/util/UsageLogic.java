package core.parsers.explanation.util;

import core.parsers.utils.OptionalEntity;
import org.apache.commons.text.WordUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record UsageLogic(String commandName, List<Explanation> explanations, Set<OptionalEntity> optionals) {
    private static final Pattern words = Pattern.compile("[\\w\\d]+\\s+[\\w\\d]+");

    private static boolean checkString(String str) {
        return str != null && !str.isBlank();
    }

    public String getUsage() {
        StringBuilder a = new StringBuilder();
        for (OptionalEntity opt : optionals) {
            if (!opt.isEnabledByDefault()) {
                a.append(opt.getDescription());
            }
        }
        String explanations = "";
        if (!this.explanations.isEmpty()) {
            explanations = "*" + this.explanations.stream().map(Explanation::explanation)
                    .map(Interactible::header).filter(UsageLogic::checkString).map(this::mapHeader).collect(Collectors.joining(" ")) + "*";
        }

        String headerLine = "**%s** %s".formatted(commandName, explanations);
        String body = this.explanations.stream().map(Explanation::explanation).map(Interactible::usage).filter(UsageLogic::checkString).map(
                str -> {
                    String trimmed = str.trim();
                    if (!trimmed.endsWith(".")) {
                        trimmed += ".";
                    }
                    return "\t " + trimmed.replaceAll("\n", "\n\t");
                }).collect(Collectors.joining("\n"));
        if (!body.isBlank()) {
            body += "\n";
        }
        return headerLine + "\n" + body + a;
    }

    @Nonnull
    private String mapHeader(String t) {
        if (words.matcher(t).matches()) return "**[" + WordUtils.capitalizeFully(t.replaceAll("\\s+", "-")) + "]**";
        return "**" + t + "**";
    }


}
