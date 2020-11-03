package ish.services.dtos;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record DiscordTokenRequest(String client_id, String client_secret, String grant_type, String code,
                                  String redirect_uri, String scope) {
}
