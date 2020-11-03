package ish.controllers;

import dao.entities.ImageQueueResponse;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.runtime.context.scope.Refreshable;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import ish.services.CommandService;
import ish.services.dtos.AcceptedImageDTO;
import ish.services.dtos.CommandInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.validation.Valid;
import java.time.Instant;
import java.util.List;


@PermitAll
@Controller("/commands")
@CacheConfig("commandinfo")
public class CommandsController {

    @Inject
    private CommandService commandService;

    @CachePut
    @Put("from-file")
    public List<CommandInfo> postFile(@Body String path) {
        return commandService.getCommandListByFile(path);
    }

    @Get("default")
    @CachePut
    public List<CommandInfo> defaultFile() {
        return commandService.getDefault();
    }

    @Cacheable
    @Get
    public List<CommandInfo> index() {
        return commandService.getCommandinfo();
    }

    @Put("from-json")
    @CachePut
    @CacheInvalidate
    public @Valid List<CommandInfo> accept(@Body @Valid List<CommandInfo> arr) {
        return arr;
    }

}
