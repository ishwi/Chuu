package dao.entities;

import java.time.Instant;
import java.util.Optional;
import java.util.TimeZone;

public record UserListened(long discordId, String lastfmId, TimeZone timeZone, Optional<Instant> moment) {
}
