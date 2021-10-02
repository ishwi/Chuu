package ish.controllers;

import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import ish.services.CommandService;
import ish.services.dtos.CommandInfo;
import jakarta.inject.Inject;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.List;


@PermitAll
@Controller("/commands")
@CacheConfig("commandinfo")
public class CommandsController {

    @Inject
    private CommandService commandService;

    @CachePut
    @Put("from-file")
    public List<CommandInfo> refreshWithPath(@Body String path) {
        return commandService.getCommandListByFile(path);
    }

    @Get("default")
    @CachePut
    public List<CommandInfo> refreshWithDefault() {
        return commandService.getDefault();
    }

    @Cacheable
    @Get
    public List<CommandInfo> getCommands() {
        return commandService.getCommandinfo();
    }

    @Put("from-json")
    @CachePut
    @CacheInvalidate
    public @Valid List<CommandInfo> upsertCommands(@Body @Valid List<CommandInfo> arr) {
        return arr;
    }

}
