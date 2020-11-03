package ish.controllers;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.Authenticator;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.endpoints.LoginControllerConfigurationProperties;
import io.micronaut.security.event.LoginFailedEvent;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.handlers.LoginHandler;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.jwt.render.AccessRefreshToken;
import io.micronaut.validation.Validated;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.internal.operators.flowable.FlowableAllSingle;
import ish.dtos.CodeAndState;
import ish.dtos.DiscordDetails;
import ish.security.DiscordUser;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import java.net.URI;
import java.util.*;

@Requires(property = LoginControllerConfigurationProperties.PREFIX + ".enabled", notEquals = StringUtils.FALSE, defaultValue = StringUtils.TRUE)
@Requires(beans = LoginHandler.class)
@Requires(beans = Authenticator.class)
@Controller("/")
@PermitAll
@Replaces(io.micronaut.security.endpoints.LoginController.class)
public class LoginController {
    protected final Map<String, Optional<AccessRefreshToken>> cache = new HashMap<>();
    protected final DiscordUser authenticator;
    protected final LoginHandler loginHandler;
    protected final ApplicationEventPublisher eventPublisher;

    /**
     * @param authenticator  {@link Authenticator} collaborator
     * @param loginHandler   A collaborator which helps to build HTTP response depending on success or failure.
     * @param eventPublisher The application event publisher
     */
    public LoginController(DiscordUser authenticator,
                           LoginHandler loginHandler,
                           ApplicationEventPublisher eventPublisher) {
        this.authenticator = authenticator;
        this.loginHandler = loginHandler;
        this.eventPublisher = eventPublisher;
    }

    /**
     * @param request The {@link HttpRequest} being executed
     * @return An AccessRefreshToken encapsulated in the HttpResponse or a failure indicated by the HTTP status
     */
    @Get("/login")
    @PermitAll
    public Single<? extends MutableHttpResponse<?>> login(@QueryValue String code, @QueryValue @Nullable String state, HttpRequest<?> request) {
        Flowable<AuthenticationResponse> authenticationResponseFlowable = Flowable.fromPublisher(authenticator.authenticate(request, new CodeAndState(code, state)));
        return authenticationResponseFlowable.map(authenticationResponse1 -> {
            DiscordDetails authenticationResponse = (DiscordDetails) authenticationResponse1;
            eventPublisher.publishEvent(new LoginSuccessfulEvent(authenticationResponse));
            MutableHttpResponse<?> mutableHttpResponse = loginHandler.loginSuccess(new UserDetails(authenticationResponse.lastfmid(), Collections.singleton(authenticationResponse.role().toString())), request);
            String key = UUID.randomUUID().toString();
            Optional<AccessRefreshToken> body = mutableHttpResponse.getBody(AccessRefreshToken.class);
            cache.put(key, body);
            return HttpResponse.redirect(URI.create(state + "?uuid=" + key)).body(mutableHttpResponse.body());
        }).first(HttpResponse.status(HttpStatus.UNAUTHORIZED));
    }

    @Get("/discord/login")
    @PermitAll
    public AccessRefreshToken login(@QueryValue String uuid) {
        return Optional.of(cache.get(uuid)).map(Optional::get).orElseThrow();
    }
}
