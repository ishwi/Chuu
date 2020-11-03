package ish.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.micronaut.cache.CacheInfo;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.scheduling.annotation.Async;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import ish.services.dtos.CommandInfo;
import org.checkerframework.checker.regex.qual.Regex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
@CacheConfig("commandinfo")
public class CommandService {
    private static final Logger LOG = LoggerFactory.getLogger(CommandService.class);

    @Value("${chuu.path_to_commands}")
    String pathToCommands;

    List<CommandInfo> commandinfo;

    public CommandService(@Value("${chuu.path_to_commands}")
                                  String pathToCommands) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(pathToCommands), StandardCharsets.UTF_8))) {
            String json = bufferedReader.lines().collect(Collectors.joining());
            CommandInfo[] infoes = new ObjectMapper().readValue(json, CommandInfo[].class);
            commandinfo = List.of(infoes);

        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
            commandinfo = new ArrayList<>();
        }
    }

    public List<CommandInfo> getCommandinfo() {
        return commandinfo;
    }

    public List<CommandInfo> getCommandList(String jsonObject) {
        try {
            CommandInfo[] infoes = new ObjectMapper().readValue(jsonObject, CommandInfo[].class);
            return List.of(infoes);
        } catch (JsonProcessingException e) {
            LOG.info(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<CommandInfo> getCommandListByFile(String file) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            return getCommandList(bufferedReader.lines().collect(Collectors.joining()));
        } catch (IOException e) {
            return new ArrayList<>();
        }

    }


    public List<CommandInfo> getDefault() {
        return getCommandListByFile(pathToCommands);
    }



}
