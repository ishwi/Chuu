package ish.controllers;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class CommandsControllerTest {

    @Value("${chuu.path_to_commands}")
    String path;
    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/commands")
    HttpClient client;

    private String getOneItem() {
        return "[{\"name\":\"Artists ordered by listening time\",\"category\":\"CHARTS\",\"aliases\":[\"timeartist\",\"tart\",\"tar\",\"ta\"],\"instructions\":\"**timeartist *[d,w,m,q,s,y,a]* *sizeXsize*  *Username* ** \\n\\tIf time is not specified defaults to week\\n\\tIf username is not specified defaults to authors account \\n\\tIf Size not specified it defaults to 5x5\\n\"}]";
    }

    private String getTwoItems() {
        String oneItem = getOneItem();
        return oneItem.substring(0, oneItem.length() - 1) + "," + oneItem.substring(1);
    }

    @Test
    void testCacheAndDefault() throws IOException {
        boolean deleted = false;
        File file = new File(path);
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".json");
        try {

            // If it exists we delete it and copy to force a empty file
            if (file.exists() && file.canRead() && file.length() > 0) {
                Files.copy(file.toPath().toAbsolutePath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                assertTrue(file.delete());
                deleted = true;
            }
            String response = client.toBlocking().retrieve(HttpRequest.GET(""));
            // Empty
            assertEquals("[]", response);
            response = client.toBlocking().retrieve(HttpRequest.GET("/default"));
            assertEquals("[]", response);

        } catch (Exception e) {
            fail();
        } finally {
            if (deleted) {
                Files.copy(tempFile.toPath().toAbsolutePath(), file.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                // File didnt exist
                writeStringTOFile(file, getOneItem());
            }
        }
        // File exists and should not be cached
        String response = client.toBlocking().retrieve(HttpRequest.GET(""));
        assertEquals("[]", response);

        response = client.toBlocking().retrieve(HttpRequest.GET("/default"));
        assertEquals(getOneItem(), response);

        response = client.toBlocking().retrieve(HttpRequest.GET(""));
        assertEquals(getOneItem(), response);

    }

    @Test
    void testPuts() throws IOException {
//        File file = new File(path);
//        if (!file.exists()) {
//            assertTrue(file.createNewFile());
//        }
//        writeStringTOFile(file, getOneItem());
//        String response = client.toBlocking().retrieve(HttpRequest.GET("/default"));
//        assertEquals(getOneItem(), response);
//
//        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".json");
//        writeStringTOFile(tempFile,getTwoItems());
//        response = client.toBlocking().retrieve(HttpRequest.PUT("/from-file", tempFile.getAbsolutePath()));
//        assertEquals(getTwoItems(), response);
//        response = client.toBlocking().retrieve(HttpRequest.GET("/"));
//        assertEquals(getTwoItems(), response);
//        response = client.toBlocking().retrieve(HttpRequest.PUT("from-json", getTwoItems()));
//        assertEquals(getTwoItems(), response);








    }

    private void writeStringTOFile(File file, String value) {
        try (FileWriter myWrite = new FileWriter(file)) {
            myWrite.write(value);
        } catch (IOException e) {
            fail();
            throw new RuntimeException();
        }
    }
}
