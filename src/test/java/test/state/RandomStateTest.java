package test.state;

import core.parsers.RandomAlbumParser;
import core.parsers.params.UrlParameters;
import dao.entities.RandomUrlEntity;
import dao.exceptions.InstanceNotFoundException;
import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test.commands.utils.TestResources;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TestResources.class)
public class RandomStateTest extends ExecutionContext implements RandomState {


    private static final String[] validAndInvalidUrls = new String[]{
            "https://open.spotify.com/album/3DcOkGnGL9cZgq9G1R75HE?si=ZxAlBG36TMK0rR5FZDtl9w",
            "https://open.spotify.com/album/1uhwfCCEWi4q8Yzv9QBJ0w?si=pU_tTcm9R5CJT7m2HrWFiA",
            "https://open.spotify.com/album/4svcCm5yRZtKE0tXn4n1cn?si=X9t0Awi2Tw-OJz8tGUPCPQ",
            "spotify:album:4svcCm5yRZtKE0tXn4n1cn",
            "https://open.spotify.com/album/5K4YFkTizFoMOyN5Khfp7G?si=PAOKMCQnTY2eWj9MKaNAQg",
            "https://www.youtube.com/watch?v=1FH-q0I1fJY",
            "https://www.youtube.com/watch?v=B6Y-WsgpzlQ",
            "https://www.youtube.com/watch?v=TGwmFIphNcg",
            "https://www.deezer.com/en/album/61394462",
            "https://github.com/GraphWalker/graphwalker-project/wiki/Test-data-and-GraphWalker",
            "https://graveface.bandcamp.com/album/crystallize",
            "http://frontierer.bandcamp.com/album/unloved",
            "https://moodle.udc.es/",
            "https://open.spotify.com/album/1sJzod7aGgZwu2ShYec8GQ",
            "https://open.spotify.com/album/1I8mUtgebG39rNBApC7clb?si=WnsSEdamQ0m7cd0s0sU9ag",
            "https://open.spotify.com/album/5gxoKngxrfPdm8BPlsnB5m",
            "https://open.spotify.com/album/5pLqFeIJbDaAlrY5XYbcrj?si=mocdlVvDTXaESZQWlFawhQ",
            "https://open.spotixasdascytausgfy.com/album/5pLqFeIJbDaAlrY5XYbcrj?si=mocdlVvDTXaESZQWlFawhQ",
            "http://localhost:9090/studio.html",
            "http://twitter.com/",
            "https://open.spotify.com/album/1UwxlKflEKIzogMmfC2eKl?si=tL0Xzua2Tp-ueAS_CMUm8Q",
            "https://open.spotify.NET/album/6B2RRiDJFXHojfPxKja5Mx?si=av9GSsiLRIOXdpOp5NXAXA",
            "https://open.spotify.coms/album/0HMsmYvoT1h2x1C4di5faf?si=i5buwaloR1ytkiUkCxDxhA",
            "https://closed.spotify.com/album/2uDTi1PlpSpvAv7IRAoAEU?si=MaoZ6ywtQEyq_YZmcyPFzA",
            "httxcas://asdopen.spotify.com/album/01U2G02pbK0IcIOMW0j3Dn?si=r7Y1qtqUQM6in0xrKNs42w",
            "https://open.spotify.com/albumsada/4CGanXs6KlVuXXdNrf82qE?si=o18KmqSpSD-zYF8TLeUeDA",
            "https://open.spotify.com/albumasdzcx/53VKICyqCf91sVkTdFrzKXasdzxc?si=-Ii5weRuTDa1LTmk8HKqnQ"};
    private final Queue<String> formatQueue = new ArrayDeque<>();
    private final Queue<String> repeatedQueue = new ArrayDeque<>();
    private final Guard messageValidGuard = new Guard("requiresIsMessageValid();");
    private final Guard messageInValidGuard = new Guard("!requiresIsMessageValid();");
    private final Guard urlUniqueGuard = new Guard("requiresIsUrlUnique();");
    private final Guard notUrlUniqueGuard = new Guard("!requiresIsUrlUnique();");
    private final Random random = new Random();
    private boolean isMessageValid = true;
    private boolean isUrlUnique = false;
    private int additions = 0;

    public boolean requiresIsMessageValid() {
        return isMessageValid;
    }

    public boolean requiresIsUrlUnique() {
        return (isUrlUnique);
    }

    @BeforeEach
    @AfterEach
    public void setUp() {
        TestResources.dao.truncateRandomPool();
    }


    @Test
    public void success() {

        Vertex v_EmptyPool = new Vertex().setName("v_EmptyPool").setId("9e0f72e0-191d-11ea-b049-210ffb124d1c");
        Vertex v_NonEmptyPool = new Vertex().setName("v_NonEmptyPool").setId("c6592b60-191d-11ea-b049-210ffb124d1c");
        Vertex v_ProcessingFirstMessage = new Vertex().setName("v_ProcessingFirstMessage")
                .setId("47a63690-191e-11ea-b049-210ffb124d1c");
        Vertex v_CheckingRepeated = new Vertex().setName("v_CheckingRepeated")
                .setId("10fce070-191f-11ea-b049-210ffb124d1c");
        Vertex v_ProcessingMessageGeneral = new Vertex().setName("v_ProcessingMessageGeneral")
                .setId("b7dfcec0-191f-11ea-b049-210ffb124d1c");

        Model model = new Model();
        model.addEdge(new Edge().setSourceVertex(v_EmptyPool).setTargetVertex(v_ProcessingFirstMessage)
                .setName("e_sendMessage").setId("61e348e0-191e-11ea-b049-210ffb124d1c"));

        model.addEdge(new Edge().setSourceVertex(v_ProcessingFirstMessage).setTargetVertex(v_EmptyPool)
                .setName("e_InvalidFormat").setId("3d8490c0-191f-11ea-b049-210ffb124d1c")
                .setGuard(messageInValidGuard));

        model.addEdge(new Edge().setSourceVertex(v_CheckingRepeated).setTargetVertex(v_NonEmptyPool)
                .setName("e_RepeatedUrl").setId("4d6c3880-191f-11ea-b049-210ffb124d1c").setGuard(notUrlUniqueGuard));

        model.addEdge(new Edge().setSourceVertex(v_ProcessingFirstMessage).setTargetVertex(v_NonEmptyPool)
                .setName("e_addUrlFromValid").setId("b0017eb0-191f-11ea-b049-210ffb124d1c")
                .setGuard(messageValidGuard));

        model.addEdge(new Edge().setSourceVertex(v_NonEmptyPool).setTargetVertex(v_ProcessingMessageGeneral)
                .setName("e_sendMessage").setId("c29078b0-191f-11ea-b049-210ffb124d1c"));

        model.addEdge(new Edge().setSourceVertex(v_ProcessingMessageGeneral).setTargetVertex(v_CheckingRepeated)
                .setName("e_checkRepeated").setId("cc691220-191f-11ea-b049-210ffb124d1c").setGuard(messageValidGuard));
        model.addEdge(new Edge().setSourceVertex(v_ProcessingMessageGeneral).setTargetVertex(v_NonEmptyPool)
                .setName("e_InvalidFormat").setId("eff43120-191f-11ea-b049-210ffb124d1c")
                .setGuard(messageInValidGuard));
        model.addEdge(new Edge().setSourceVertex(v_CheckingRepeated).setTargetVertex(v_NonEmptyPool)
                .setName("e_addUrlFromUnique").setId("fa2f35e0-191f-11ea-b049-210ffb124d1c")
                .setGuard(urlUniqueGuard));
        model.addEdge(new Edge().setSourceVertex(v_NonEmptyPool).setTargetVertex(v_EmptyPool).setName("e_DeleteAll")
                .setId("740b7fe0-1920-11ea-b049-210ffb124d1c"));

        Context context = new RandomStateTest();
        context.setModel(model.build()).setPathGenerator(new RandomPath(new EdgeCoverage(100)));
        context.setNextElement(context.getModel().findElements("v_EmptyPool").get(0));
        Machine machine = new SimpleMachine(context);
        while (machine.hasNextStep()) {
            machine.getNextStep();
			/*if (nextStep.getCurrentElement() instanceof Vertex.RuntimeVertex) {
				Vertex.RuntimeVertex currentElement = (Vertex.RuntimeVertex) nextStep.getCurrentElement();

				Method method = vertexMethods.get(currentElement.getName());
				try {
					method.invoke(this);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}*/
            System.out.println(context.getCurrentElement().getName());
        }
    }


    @Override
    public void v_EmptyPool() {
        assertThat(0).isEqualTo(TestResources.dao.randomCount(null));
    }

    @Override
    public void v_ProcessingFirstMessage() {
        assertThat(formatQueue).hasSize(1);
        String peek = formatQueue.peek();

        RandomAlbumParser randomAlbumParser = new RandomAlbumParser(null);
        try {
            UrlParameters received = randomAlbumParser.parseLogic(null, new String[]{peek});
            formatQueue.poll();
            formatQueue.add(received.getUrl());
            isMessageValid = true;
        }
        //A error message was tried to be sent so it means an error occured because we are sending null as MessageReceivedEvent
        catch (NullPointerException | InstanceNotFoundException ex) {
            isMessageValid = false;
        }
    }

    @Override
    public void v_NonEmptyPool() {
        assertThat(additions).isEqualTo(TestResources.dao.randomCount(null));
    }

    @Override
    public void v_ProcessingMessageGeneral() {
        v_ProcessingFirstMessage();
    }

    @Override
    public void v_CheckingRepeated() {
        assertThat(1).isEqualTo(repeatedQueue.size());
        String peek = repeatedQueue.peek();
        isUrlUnique = !TestResources.dao.randomUrlExists(peek);
    }

    @Override
    public void e_InvalidFormat() {
        formatQueue.poll();
        assertThat(isMessageValid).isFalse();

    }

    @Override
    public void e_sendMessage() {
        formatQueue.add(getRandomUrl());
    }

    private String getRandomUrl() {
        return validAndInvalidUrls[random.nextInt(validAndInvalidUrls.length)];
    }

    @Override
    public void e_addUrlFromUnique() {
        String url = repeatedQueue.poll();
        RandomUrlEntity e = new RandomUrlEntity(url, TestResources.developerId);
        assertThat(TestResources.dao.addToRandomPool(e)).isTrue();
        additions++;
    }

    @Override
    public void e_addUrlFromValid() {
        String url = formatQueue.poll();
        RandomUrlEntity e = new RandomUrlEntity(url, 1L);
        assertThat(TestResources.dao.addToRandomPool(e)).isTrue();
        additions++;
    }

    @Override
    public void e_DeleteAll() {
        TestResources.dao.truncateRandomPool();
        additions = 0;
    }

    @Override
    public void e_RepeatedUrl() {
        repeatedQueue.poll();
        assertThat(isUrlUnique).isFalse();
    }

    @Override
    public void e_checkRepeated() {
        String validUrl = formatQueue.poll();
        repeatedQueue.add(validUrl);
    }
}
