package main.commands.utils;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FunctionalInterface
public interface FieldMatcher {
	Boolean apply(MessageEmbed.Field a, String b, Pattern c, Predicate<Matcher> d);
}
