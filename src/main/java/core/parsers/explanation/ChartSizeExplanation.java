package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineAutoComplete;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChartSizeExplanation implements Explanation {
    public static final String NAME = "chart-size";
    private static final List<OptionData> optionData;
    private static final Pattern SIZE_MATCHER = Pattern.compile("(\\d+)?(\\s*[xX]?\\s*|\\s+)?(\\d+)?");
    private static final List<Command.Choice> CHOICES;

    static {
        optionData = List.of(new OptionData(OptionType.STRING, "chart-size", "Size of the chart"));
        CHOICES = genDefault();
    }

    public static List<Command.Choice> genDefault() {
        List<Command.Choice> choices = new ArrayList<>();

        for (int i = 5; i > 0; i--) {
            String s = i + "x" + i;
            choices.add(new Command.Choice(s, s));
        }
        choices.add(new Command.Choice("4x2", "4x2"));
        choices.add(new Command.Choice("5x2", "5x2"));
        for (int i = 5; i <= 20; i++) {
            String s = i + "x" + i;
            choices.add(new Command.Choice(s, s));
        }
        return choices;
    }

    public static List<Command.Choice> defaultChoices() {
        return CHOICES;
    }

    @Override
    public Interactible explanation() {
        return new ExplanationLineAutoComplete("sizeXsize", "If the size is not specified it defaults to 5x5", optionData, this::generateChoices);
    }

    public List<Command.Choice> generateChoices(CommandAutoCompleteInteractionEvent e) {
        String input = e.getFocusedOption().getValue();
        if (StringUtils.isBlank(input)) {
            return defaultChoices();
        } else {
            Matcher matcher = SIZE_MATCHER.matcher(input);
            if (matcher.find()) {
                String leftSide = matcher.group(1);
                if (leftSide != null) {
                    long l = Long.parseLong(leftSide);
                    if (l < 0 || l > 20) {
                        return defaultChoices();
                    } else {
                        String separator = matcher.group(2);
                        if (separator != null) {
                            String rightSide = matcher.group(3);
                            if (rightSide != null) {
                                long r = Long.parseLong(rightSide);
                                if (r < 0 || r > 20) {
                                    return choicesFixesLeftSide(l);
                                }
                                return List.of(new Command.Choice(leftSide + "x" + rightSide, leftSide + "x" + rightSide));
                            } else {
                                return choicesFixesLeftSide(l);
                            }
                        } else {
                            return choicesFixesLeftSide(l);
                        }
                    }
                } else {
                    return defaultChoices();
                }
            } else {
                return defaultChoices();
            }
        }
    }

    public List<Command.Choice> choicesFixesLeftSide(long leftSide) {
        List<Command.Choice> choices = new ArrayList<>();
        String name = leftSide + "x" + leftSide;
        choices.add(new Command.Choice(name, name));

        for (int i = 1; i <= 20; i++) {
            if (i == leftSide) {
                continue;
            }
            String s = leftSide + "x" + i;
            choices.add(new Command.Choice(s, s));
        }
        return choices;
    }
}
