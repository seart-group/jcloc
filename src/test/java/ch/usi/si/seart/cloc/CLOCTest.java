package ch.usi.si.seart.cloc;

import com.fasterxml.jackson.databind.JsonNode;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class CLOCTest {

    private static final String USER_HOME = System.getProperty("user.home");

    private static final Path RESOURCES = Paths.get("src", "test", "resources");

    @TempDir
    Path empty;

    @BeforeEach
    void setUp() {
        CLOCCommand.setOutputMapper(null);
    }

    @Test
    void testEmptyDirectory() throws CLOCException {
        JsonNode result = CLOCCommand.targeting(empty).byLanguage();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testDirectoryNotExists() {
        Path invalid = RESOURCES.resolve("nonexistant");
        Executable executable = () -> CLOCCommand.targeting(invalid);
        Assertions.assertThrows(IllegalArgumentException.class, executable);
    }

    @Test
    void testTimeout() {
        Path user = Paths.get(USER_HOME);
        CLOCCommand command = CLOCCommand.targeting(user).withTimeout(1);
        Assertions.assertThrows(CLOCException.class, command::byLanguage);
    }

    @Test
    void testByLanguage() throws CLOCException, IOException {
        JsonNode result = CLOCCommand.targeting(RESOURCES).byLanguage();
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertTrue(result.hasNonNull("header"));
        Assertions.assertTrue(result.hasNonNull("SUM"));
        JsonNode header = result.get("header");
        List<File> files = FileUtils.getFiles(RESOURCES.toFile(), null, null);
        Assertions.assertEquals(files.size(), header.get("n_files").asInt());
        Assertions.assertEquals(files.size() + 2, result.size());
    }
}
