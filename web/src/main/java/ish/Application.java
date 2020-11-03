package ish;

import dao.ChuuService;
import io.micronaut.context.annotation.Factory;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

import javax.inject.Singleton;


@OpenAPIDefinition(
        info = @Info(
                title = "Hello World",
                version = "0.0",
                description = "My API",
                license = @License(name = "Apache 2.0", url = "https://foo.bar"),
                contact = @Contact(url = "https://gigantic-server.com", name = "Fred", email = "Fred@gigagantic-server.com")
        )
)
public class Application {
    @Factory
    @Singleton
    protected ChuuService chuuService() {
        return new ChuuService();
    }

    public static void main(String[] args) {

        Micronaut.run(Application.class, args);
    }
}
