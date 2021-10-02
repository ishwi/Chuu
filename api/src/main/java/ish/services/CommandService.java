package ish.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.context.annotation.Value;
import ish.services.dtos.CommandInfo;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
