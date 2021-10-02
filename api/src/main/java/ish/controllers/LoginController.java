package ish.controllers;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.Authenticator;
import io.micronaut.security.endpoints.LoginControllerConfigurationProperties;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.handlers.LoginHandler;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.jwt.render.AccessRefreshToken;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import ish.dtos.CodeAndState;
import ish.dtos.DiscordDetails;
import ish.security.DiscordUser;

import javax.annotation.security.PermitAll;
import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Requires(property = LoginControllerConfigurationProperties.PREFIX + ".enabled", notEquals = StringUtils.FALSE, defaultValue = StringUtils.TRUE)
@Requires(beans = LoginHandler.class)
@Requires(beans = Authenticator.class)
@Controller()
@PermitAll
@Replaces(io.micronaut.security.endpoints.LoginController.class)
public class LoginController {
    protected final Map<String, Optional<AccessRefreshToken>> cache = new HashMap<>();
    protected final DiscordUser authenticator;
    protected final LoginHandler loginHandler;
    protected final ApplicationEventPublisher<LoginSuccessfulEvent> eventPublisher;

    /**
     * @param authenticator  {@link Authenticator} collaborator
     * @param loginHandler   A collaborator which helps to build HTTP response depending on success or failure.
     * @param eventPublisher The application event publisher
     */
    public LoginController(DiscordUser authenticator,
                           LoginHandler loginHandler,
                           ApplicationEventPublisher<LoginSuccessfulEvent> eventPublisher) {
        this.authenticator = authenticator;
        this.loginHandler = loginHandler;
        this.eventPublisher = eventPublisher;
    }

    @Get("/principal")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public DiscordDetails login(Principal principal) {
        if (principal instanceof DiscordDetails details) {
            return details;
        }
        throw new IllegalStateException();
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
            MutableHttpResponse<?> mutableHttpResponse = loginHandler.loginSuccess(authenticationResponse, request);
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
