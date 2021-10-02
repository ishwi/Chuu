package core.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.Chuu;
import core.apis.spotify.SpotifySingleton;
import core.commands.abstracts.MyCommand;
import core.commands.config.HelpCommand;
import core.commands.config.PrefixCommand;
import core.commands.discovery.FeaturedCommand;
import core.commands.moderation.AdministrativeCommand;
import core.commands.moderation.EvalCommand;
import core.commands.moderation.TagWithYearCommand;
import dao.ServiceView;
import dao.exceptions.ChuuServiceException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;


public class CommandReportGeneratorTest {

    public static MyCommand<?>[] scanListeners() {
        ServiceView serviceView = new ServiceView(null, null);

        try (ScanResult result = new ClassGraph().acceptPackages("core.commands").scan()) {
            return result.getAllClasses().stream().filter(x -> x.isStandardClass() && !x.isAbstract())
                    .filter(x -> !x.getSimpleName().equals(HelpCommand.class.getSimpleName())
                                 && !x.getSimpleName().equals(AdministrativeCommand.class.getSimpleName())
                                 && !x.getSimpleName().equals(PrefixCommand.class.getSimpleName())
                                 && !x.getSimpleName().equals(TagWithYearCommand.class.getSimpleName())
                                 && !x.getSimpleName().equals(EvalCommand.class.getSimpleName())
                                 && !x.getSimpleName().equals(FeaturedCommand.class.getSimpleName()))
                    .filter(x -> x.extendsSuperclass("core.commands.abstracts.MyCommand"))
                    .map(x -> {
                        try {
                            return (MyCommand<?>) x.loadClass().getConstructor(ServiceView.class).newInstance(serviceView);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            throw new ChuuServiceException(e);
                        }

                    })
                    .toArray(MyCommand<?>[]::new);
        } catch (Exception ex) {
            throw new ChuuServiceException(ex);
        }
    }

    @Test
    public void name() throws JsonProcessingException {

        try (MockedStatic<Chuu> utilities = Mockito.mockStatic(Chuu.class)) {
            try (MockedStatic<SpotifySingleton> spoti = Mockito.mockStatic(SpotifySingleton.class)) {

                spoti.when(SpotifySingleton::getInstance).thenReturn(null);
                utilities.when(Chuu::getDb).thenReturn(null);
                MyCommand<?>[] myCommands = scanListeners();
                new ObjectMapper().writer().writeValueAsString(Arrays.asList(myCommands));
            }
        }
    }
}
