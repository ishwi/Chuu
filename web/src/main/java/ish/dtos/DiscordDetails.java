package ish.dtos;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import dao.entities.Role;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record DiscordDetails(long discordId, Role role, String lastfmid) implements AuthenticationResponse {


    @Override
    public boolean isAuthenticated() {
        return role == Role.ADMIN;
    }

    @Override
    public Optional<UserDetails> getUserDetails() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getMessage() {
        return Optional.empty();
    }


}
