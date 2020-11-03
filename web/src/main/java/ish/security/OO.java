package ish.security;

import dao.ChuuService;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import ish.dtos.AccessTokenDiscord;
import ish.services.dtos.DiscordTokenRequest;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;

@PermitAll
@Controller("/discord")
public class OO {
    @Value("${chuu.client_id}")
    String clientId;
    @Value("${chuu.client_secret}")
    String SecretId;

    @Inject
    @Client("/image-queue")
    RxHttpClient client;

    @Inject
    ChuuService chuuService;


    @Get
    @Status(HttpStatus.OK)
    public void index(@Parameter String code, @Parameter String state) {
//        DiscordTokenRequest discordTokenRequest = new DiscordTokenRequest(clientId, SecretId, "authorization_code", code, "/discord", "identify");
//        client.retrieve(HttpRequest.POST("https://discord.com/api/oauth2/token", discordTokenRequest),
//                Argument.of(AccessTokenDiscord.class)).firstOrError().flatMap
//                (x -> client.retrieve(HttpRequest.GET("https://discord.com/api/users/@me").bearerAuth(x.accessToken()), Argument.of(Long.class))
//                        .firstOrError().map(t -> Pair.of(x, t))).map(x -> {
//            chuuService.storeDiscordRefreshToken(x.getLeft().accessToken(), x.getRight(), x.getLeft().expires());
//            return x;
//        }).;
    }


}
