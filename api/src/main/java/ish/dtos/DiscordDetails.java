package ish.dtos;

import dao.entities.Role;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationResponse;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record DiscordDetails(long discordId, Role role,
                             String lastfmid, String discordName,
                             String avatarUrl) implements AuthenticationResponse, Authentication {

    public static DiscordDetails fromClaims(String lastfmid, List<String> roles, Map<String, Object> claims) {
        return new DiscordDetails((long) claims.get("id"), roles.stream().findFirst().map(Role::valueOf).orElse(Role.USER), lastfmid,
                (String) claims.get("discordName"), (String) claims.get("avatarUrl"));
    }

    @Override
    public boolean isAuthenticated() {
        return role == Role.ADMIN;
    }

    @Override
    public Optional<Authentication> getAuthentication() {
        return Optional.of(Authentication.build(lastfmid, Set.of(role.name()), getAttributes()));
    }


    @Override
    public Optional<String> getMessage() {
        return Optional.empty();
    }

    @Override
    public @NotNull List<String> getRoles() {
        return List.of(role.name());
    }

    @Override
    public @NotNull Map<String, Object> getAttributes() {
        return Map.of("id", discordId, "lastfmId", lastfmid, "discordName", discordName, "avatarUrl", avatarUrl);
    }

    @Override
    public String getName() {
        return lastfmid;
    }
}
