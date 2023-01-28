package test.commands.parsers.mock;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.internal.requests.DeferredRestAction;
import test.commands.parsers.factories.FactoryDeps;

public class GuildMock extends GuildImpl {
    public GuildMock(FactoryDeps deps) {
        super(deps.jda(), -1);
    }

    @Override
    public CacheRestAction<Member> retrieveMemberById(long id) {
        return new DeferredRestAction<>(getJDA(), () -> new CompletedRestAction<>(getJDA(), null));
    }
}
