package ish;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SecurityScheme(name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "jwt")
@OpenAPIDefinition(
        info = @Info(
                title = "Chuu API",
                version = "0.0",
                description = "Api for the discord bot Chuubot",
                license = @License(name = "Apache 2.0", url = "https://foo.bar"),
                contact = @Contact(url = "https://gigantic-server.com", name = "Ish", email = "ishwi6@gmail.com")
        )
)
@SecurityRequirement(name = "BearerAuth")
public class Application {

    public static void main(String[] args) {

        Micronaut.run(Application.class, args);
    }
}
