package test.commands.parsers.factories;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nullable;

public record FactoryDeps(JDAImpl jda, @Nullable User user) {


}
