package core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.Chuu;
import core.commands.MyCommand;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CommandReportGenerator {
    private final List<MyCommand<?>> myCommands;

    public CommandReportGenerator(List<MyCommand<?>> myCommands) {
        this.myCommands = myCommands;
    }

    public void generateReport() {
        try {
            File file = new File("commands.json");
            new ObjectMapper().writeValue(file, myCommands);
        } catch (
                IOException ex) {
            Chuu.getLogger().warn(ex.getMessage(), ex);
        }
    }
}
