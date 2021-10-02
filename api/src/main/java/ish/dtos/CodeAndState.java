package ish.dtos;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.security.authentication.AuthenticationRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Introspected
public record CodeAndState(@NotBlank
                           @NotNull
                           String code, String state) implements Serializable, AuthenticationRequest<String, String> {

    @Override
    public String getIdentity() {
        return code;
    }

    /**
     * Returns password conforming to {@link AuthenticationRequest} blueprint.
     *
     * @return secret string.
     */
    @Override
    public String getSecret() {
        return code;
    }


}
