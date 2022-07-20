package core.commands.utils;

import net.dv8tion.jda.api.Permission;

public @interface Command {
    String description();

    CommandCategory category();

    String[] aliases();

    String name();

    Permission[] perms() default {};
}
