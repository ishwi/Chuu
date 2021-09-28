package core.commands.utils;

import dao.ChuuService;
import dao.entities.UsersWrapper;

public record NotOnServerGuard(ChuuService db) {

    public boolean notOnServer(long guildId, long userId) {
        return db.getAll(guildId).stream().mapToLong(UsersWrapper::getDiscordID).noneMatch(w -> w == userId);
    }
}
