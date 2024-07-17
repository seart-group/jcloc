package ch.usi.si.seart.cloc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Counts blank lines, comment lines, and physical lines of source code in many programming languages.
 *
 * @author Ozren Dabić
 */
public final class CLOCCommand {

    private static final String CMD = "cloc";

    private static final String TMPDIR_PATH = System.getProperty("java.io.tmpdir");

    private static final String EXECUTABLE = getBundledExecutable();

    private static final JsonMapper DEFAULT_MAPPER = new JsonMapper();
    private static volatile JsonMapper OUTPUT_MAPPER = DEFAULT_MAPPER;

    private final Commandline commandline = new Commandline();

    private int timeout = 0;

    /**
     * Set the {@link JsonMapper} to use for parsing the output of the command.
     *
     * @param mapper the mapper to use, or {@code null} to revert to the default instance.
     */
    public static void setOutputMapper(@Nullable JsonMapper mapper) {
        OUTPUT_MAPPER = mapper == null ? DEFAULT_MAPPER : mapper;
    }

    /**
     * Create a new command instance targeting the specified path.
     *
     * @param path the path to target, must not be {@code null}.
     * @return a new command instance targeting the specified path.
     * @throws NullPointerException if the path is {@code null}.
     * @throws IllegalArgumentException if the path does not exist.
     */
    @Contract("_ -> new")
    public static @NotNull CLOCCommand targeting(@NotNull Path path) {
        Objects.requireNonNull(path, "Path must not be null!");
        File target = path.toFile();
        if (!target.exists()) throw new IllegalArgumentException("Unable to read: " + path);
        return new CLOCCommand(target);
    }

    private static String getBundledExecutable() {
        URL url = CLOCCommand.class.getClassLoader().getResource(CMD);
        String protocol = Objects.requireNonNull(url).getProtocol();
        switch (protocol) {
            case "file":
                return url.getPath();
            case "jar":
                try {
                    File tmpdir = new File(TMPDIR_PATH);
                    File script = new File(tmpdir, CMD);
                    FileUtils.copyURLToFile(url, script);
                    boolean ignore = script.setExecutable(true);
                    script.deleteOnExit();
                    return script.getAbsolutePath();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            default:
                throw new UnsupportedOperationException("Unsupported protocol: " + protocol);
        }
    }

    private CLOCCommand(File target) {
        commandline.setExecutable(EXECUTABLE);
        commandline.createArg().setFile(target);
        commandline.createArg().setValue("--json");
        commandline.createArg().setValue("--quiet");
    }

    /**
     * Set the timeout for executing the command.
     *
     * @param timeout the timeout in seconds, or 0 for no timeout.
     * @return this command instance.
     * @throws IllegalArgumentException if the timeout is less than 0.
     */
    @Contract(value = "_ -> this")
    public CLOCCommand withTimeout(int timeout) {
        if (timeout < 0) throw new IllegalArgumentException("Timeout must be greater than or equal to 0!");
        this.timeout = timeout;
        return this;
    }

    /**
     * Count the physical lines of source code, reporting results by language.
     *
     * @return A JSON object representation of the command output.
     * @throws CLOCException if an error occurs while executing the command.
     */
    public ObjectNode byLanguage() throws CLOCException {
        try {
            StringStreamConsumer out = new StringStreamConsumer();
            StringStreamConsumer err = new StringStreamConsumer();
            int code = CommandLineUtils.executeCommandLine(commandline, out, err, timeout);
            if (code != 0) throw new CLOCException(err.getOutput());
            JsonNode json = OUTPUT_MAPPER.readTree(out.getOutput());
            return OUTPUT_MAPPER.convertValue(json, ObjectNode.class);
        } catch (JsonProcessingException ex) {
            throw new CLOCException(ex);
        } catch (CommandLineException ex) {
            throw new CLOCException(ex.getMessage(), ex.getCause());
        } catch (IllegalArgumentException ex) {
            throw new CLOCException("Unexpected output format!", ex);
        }
    }

    private static final class StringStreamConsumer extends CommandLineUtils.StringStreamConsumer {
    }
}
