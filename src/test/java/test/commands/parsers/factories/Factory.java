package test.commands.parsers.factories;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.UserImpl;

import java.util.function.Function;

public record Factory(Function<FactoryDeps, User> userFn, Function<FactoryDeps, GuildImpl> guildFn) {

    public static Factory def() {
        return new Factory(
                (deps) -> {
                    UserImpl user = new UserImpl(-1, deps.jda());
                    user.setName("testing");
                    return user;
                },
                (deps) -> new GuildImpl(deps.jda(), -1)
        );
    }

    public Factory withUser(Function<FactoryDeps, User> uf) {
        return new Factory(uf, guildFn);
    }

    public Factory withGuild(Function<FactoryDeps, GuildImpl> gF) {
        return new Factory(userFn, gF);
    }
}
