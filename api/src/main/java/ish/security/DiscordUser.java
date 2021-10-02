package ish.security;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.scribejava.apis.DiscordApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import dao.ChuuService;
import dao.entities.LastFMData;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.rxjava3.http.client.Rx3HttpClient;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import ish.dtos.CodeAndState;
import ish.dtos.DiscordApiUser;
import ish.dtos.DiscordDetails;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

import java.util.concurrent.Future;

@Singleton
public class DiscordUser implements AuthenticationProvider {
    private final ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
    @Value("${chuu.client_id}")
    String clientId;
    @Value("${chuu.client_secret}")
    String SecretId;
    @Inject
    Rx3HttpClient client;
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

                return Flowable.fromFuture(service.executeAsync(request)).map(t -> new ApiResponse(x, t));
            }).map((ApiResponse apiUser) -> {
                DiscordApiUser discordApiUser = mapper.readValue(apiUser.response().getBody(), DiscordApiUser.class);

                OAuth2AccessToken token = apiUser.token();
                chuuService.storeDiscordRefreshToken(token.getAccessToken(), discordApiUser.id(), token.getExpiresIn());

                LastFMData deets = chuuService.findLastFMData(discordApiUser.id());
                return new DiscordDetails(deets.getDiscordId(), deets.getRole(), deets.getName(), discordApiUser.username(), discordApiUser.avatar());
            });
            emitter.onNext(map.blockingFirst());
            emitter.onComplete();

        }, BackpressureStrategy.ERROR);
    }

    private static record ApiResponse(OAuth2AccessToken token, Response response) {
    }
}
