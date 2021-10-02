package ish.security;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;


@Filter(value = {"/oauth/login", "/oauth/login/*"})
public class OAuthCsrfFilter extends OncePerRequestHttpServerFilter {

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        System.out.println('A');
//        String requestParameter = request.getParameters().get("_csrf");
//        String cookieValue = request.getCookies().findCookie("_csrf").map(Cookie::getValue).orElse(null);
//
//        if (cookieValue == null || !cookieValue.equals(requestParameter)) {
//            return Publishers.just(HttpResponse.status(HttpStatus.FORBIDDEN));
//        }

        return chain.proceed(request);
    }
}
