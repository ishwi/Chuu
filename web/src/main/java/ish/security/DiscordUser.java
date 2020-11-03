package ish.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.github.scribejava.apis.DiscordApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import dao.ChuuService;
import dao.entities.LastFMData;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.security.authentication.*;
import io.micronaut.security.oauth2.client.OauthClient;
import io.micronaut.security.oauth2.client.OpenIdProviderMetadata;
import io.micronaut.security.oauth2.configuration.OauthClientConfiguration;
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdTokenResponse;
import io.micronaut.security.oauth2.endpoint.token.response.validation.DefaultOpenIdTokenResponseValidator;
import io.micronaut.security.oauth2.endpoint.token.response.validation.OpenIdTokenResponseValidator;
import io.micronaut.security.token.jwt.generator.claims.JWTClaimsSetGenerator;
import io.micronaut.security.token.jwt.validator.JwtValidator;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import ish.dtos.AccessTokenDiscord;
import ish.dtos.CodeAndState;
import ish.dtos.DiscordDetails;
import ish.dtos.UserDetails;
import ish.services.dtos.DiscordTokenRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Future;

@Singleton
public class DiscordUser implements AuthenticationProvider {
    @Value("${chuu.client_id}")
    String clientId;
    @Value("${chuu.client_secret}")
    String SecretId;
    @Inject
    RxHttpClient client;
    @Inject
    ChuuService chuuService;

    @Override
    public Publisher<AuthenticationResponse> authenticate(HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {

        return Flowable.create(emitter -> {

            CodeAndState codeAndState = (CodeAndState) authenticationRequest;
            final OAuth20Service service = new ServiceBuilder(clientId)
                    .apiSecret(SecretId)
                    .defaultScope("identify") // replace with desired scope
                    .callback("http://localhost:8080/login")
                    .userAgent("ScribeJava")
                    .build(DiscordApi.instance());
            Future<OAuth2AccessToken> accessTokenAsync = service.getAccessTokenAsync(codeAndState.code());


            Flowable<DiscordDetails> map = Flowable.fromFuture(accessTokenAsync).flatMap(x -> {
                OAuthRequest request = new OAuthRequest(Verb.GET, "https://discordapp.com/api/users/@me");
                service.signRequest(x.getAccessToken(), request);
                return Flowable.fromFuture(service.executeAsync(request)).map(t -> Pair.of(x, t));
            }).map(x -> {
                String body = x.getRight().getBody();
                UserDetails userDetails = new ObjectMapper().readValue(body, UserDetails.class);
                chuuService.storeDiscordRefreshToken(x.getLeft().getAccessToken(), userDetails.id(), x.getLeft().getExpiresIn());
                return chuuService.findLastFMData(userDetails.id());
            }).map(x -> new DiscordDetails(x.getDiscordId(), x.getRole(), x.getName()));
            emitter.onNext(map.blockingFirst());
            emitter.onComplete();

        }, BackpressureStrategy.ERROR);
    }
}
