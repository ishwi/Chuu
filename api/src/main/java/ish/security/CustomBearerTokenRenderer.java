package ish.security;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpHeaderValues;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.jwt.render.AccessRefreshToken;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.security.token.jwt.render.BearerTokenRenderer;
import ish.dtos.DiscordDetails;

import java.util.Collection;

@Replaces(bean = BearerTokenRenderer.class)
public class CustomBearerTokenRenderer extends BearerTokenRenderer {

    private final String authorizationPrefixBearer = HttpHeaderValues.AUTHORIZATION_PREFIX_BEARER;

    @Override
    public AccessRefreshToken render(Authentication userDetails, Integer expiresIn, String accessToken, String refreshToken) {
        if (userDetails instanceof DiscordDetails du) {
            return new CustomBearerAccessRefreshToken(du, userDetails.getRoles(), expiresIn, accessToken, refreshToken);
        }
        return new BearerAccessRefreshToken(userDetails.getName(), userDetails.getRoles(), expiresIn, accessToken, refreshToken, authorizationPrefixBearer);
    }


    private class CustomBearerAccessRefreshToken extends BearerAccessRefreshToken {
        private final DiscordDetails discordDetails;

        public CustomBearerAccessRefreshToken(DiscordDetails discordDetails, Collection<String> roles, Integer expiresIn, String accessToken, String refreshToken) {
            super(discordDetails.discordName(), roles, expiresIn, accessToken, refreshToken, authorizationPrefixBearer);
            this.discordDetails = discordDetails;
        }

        public DiscordDetails getDiscordDetails() {
            return discordDetails;
        }
    }
}
