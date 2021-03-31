package core.parsers.explanation.util;

import core.parsers.OptionalEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record UsageLogic(String commandName, List<Explanation> explanations, Set<OptionalEntity> optionals) {
    private static boolean checkString(String str) {
        return str != null && !str.isBlank();
    }

    public String getUsage() {
        StringBuilder a = new StringBuilder();
        for (OptionalEntity opt : optionals) {
            if (!opt.isEnabledByDefault()) {
                a.append(opt.getDefinition());
            }
        }
        String headerLine = "**%s** *%s*".formatted(commandName, explanations.stream().map(Explanation::explanation)
                .map(ExplanationLine::header).filter(UsageLogic::checkString).map(t -> "**" + t + "**").collect(Collectors.joining(" ")));
        String body = explanations.stream().map(Explanation::explanation).map(ExplanationLine::usage).filter(UsageLogic::checkString).map(
                str -> {
                    String trimmed = str.trim();
                    if (!str.endsWith(".")) {
                        str += ".";
                    }
                    return "\t " + str.replaceAll("\n", "\n\t");
                }).collect(Collectors.joining("\n"));
        if (!body.isBlank()) {
            body += "\n";
        }
        return headerLine + "\n" + body + a;
    }


}
