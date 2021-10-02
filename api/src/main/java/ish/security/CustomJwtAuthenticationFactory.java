package ish.security;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.RolesFinder;
import io.micronaut.security.token.config.TokenConfiguration;
import io.micronaut.security.token.jwt.validator.DefaultJwtAuthenticationFactory;
import io.micronaut.security.token.jwt.validator.JwtAuthenticationFactory;
import ish.dtos.DiscordDetails;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

@Singleton
@Replaces(bean = DefaultJwtAuthenticationFactory.class)
public class CustomJwtAuthenticationFactory implements JwtAuthenticationFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CustomJwtAuthenticationFactory.class);
    private final TokenConfiguration tokenConfiguration;
    private final RolesFinder rolesFinder;

    public CustomJwtAuthenticationFactory(TokenConfiguration tokenConfiguration, RolesFinder rolesFinder) {
        this.tokenConfiguration = tokenConfiguration;
        this.rolesFinder = rolesFinder;
    }

    @Override
    public Optional<Authentication> createAuthentication(JWT token) {
        try {
            final JWTClaimsSet claimSet = token.getJWTClaimsSet();
            if (claimSet == null) {
                return Optional.empty();
            }
            Map<String, Object> attributes = claimSet.getClaims();
            return usernameForClaims(claimSet).map(username ->
                    DiscordDetails.fromClaims(username, rolesFinder.resolveRoles(attributes),
                            attributes));

        } catch (ParseException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("ParseException creating authentication", e);
            }
        }
        return Optional.empty();
    }

    /**
     * @param claimSet JWT Claims
     * @return the username defined by {@link TokenConfiguration#getNameKey()} ()} or the sub claim.
     * @throws ParseException might be thrown parsing claims
     */
    protected Optional<String> usernameForClaims(JWTClaimsSet claimSet) throws ParseException {
        String username = claimSet.getStringClaim(tokenConfiguration.getNameKey());
        if (username == null) {
            return Optional.ofNullable(claimSet.getSubject());
        }
        return Optional.of(username);
    }
}
