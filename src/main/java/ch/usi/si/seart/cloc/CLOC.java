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

    private final CommandLine commandLine;
    private final int timeout;

    private CLOCCommand(CommandLine commandLine, int timeout) {
        this.commandLine = commandLine;
        this.timeout = timeout;
    }

    /**
     * Set the {@link JsonMapper} to use for parsing the output of the command.
     *
     * @param mapper the mapper to use, or {@code null} to revert to the default instance.
     */
    public static void setOutputMapper(@Nullable JsonMapper mapper) {
        OUTPUT_MAPPER = mapper == null ? DEFAULT_MAPPER : mapper;
    }

    /**
     * Obtain a new {@link Builder Builder} for constructing a command.
     *
     * @return a new command builder
     */
    public static Builder create() {
        return new Builder();
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

    /**
     * Facilitates the construction of {@link CLOCCommand} instances.
     * It allows for the step-by-step creation of these objects
     * by providing methods for setting individual attributes.
     * Input validations are performed at each build step.
     */
    public static final class Builder {

        private int timeout = 0;
        private int maxFileSize = 100;
        private int cores = 0;

        /**
         * Set the timeout for executing the command.
         *
         * @param timeout the timeout in seconds, or 0 for no timeout.
         * @return this builder instance.
         * @throws IllegalArgumentException if the timeout is less than 0.
         */
        @Contract(value = "_ -> this")
        public Builder withTimeout(int timeout) {
            if (timeout < 0) throw new IllegalArgumentException("Timeout must be greater than or equal to 0!");
            this.timeout = timeout;
            return this;
        }

        /**
         * Set the number of cores to use for executing the command.
         *
         * @param cores the number of cores to use, or 0 to disable multiprocessing.
         * @return this builder instance.
         * @throws IllegalArgumentException if the number of cores is less than 0.
         */
        @Contract(value = "_ -> this")
        public Builder usingCores(int cores) {
            if (cores < 0) throw new IllegalArgumentException("Number of cores must be greater than or equal to 0!");
            this.cores = cores;
            return this;
        }

        /**
         * Skip files exceeding the specified maximum size in megabytes. Default is 100.
         * <p>
         * The underlying command needs approximately twenty times the largest file's size in memory.
         * Processing files larger than 100 MB on a system with less than 2 GB of RAM may lead to issues.
         *
         * @param size the maximum file size in megabytes.
         * @return this builder instance.
         */
        @Contract(value = "_ -> this")
        public Builder withMaxFileSize(int size) {
            if (size <= 0) throw new IllegalArgumentException("Maximum file size must be greater than 0!");
            this.maxFileSize = size;
            return this;
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
        public @NotNull CLOCCommand targeting(@NotNull Path path) {
            Objects.requireNonNull(path, "Path must not be null!");
            File file = path.toFile();
            if (!file.exists()) throw new IllegalArgumentException("Unable to read: " + path);

            CommandLine commandLine = new CommandLine();
            commandLine.setExecutable(EXECUTABLE);
            commandLine.createArg().setFile(file);
            commandLine.createArg().setValue("--json");
            commandLine.createArg().setValue("--quiet");
            commandLine.createArg().setValue("--processes=" + cores);
            commandLine.createArg().setValue("--max-file-size=" + maxFileSize);
            return new CLOCCommand(commandLine, timeout);
        }
    }

    /**
     * Count the physical lines of source code, reporting results by language.
     *
     * @return A JSON object representation of the command output.
     * @throws CLOCException if an error occurs while executing the command.
     */
    public ObjectNode countByLanguage() throws CLOCException {
        return execute(commandLine, timeout);
    }

    /**
     * Count the physical lines of source code, reporting results by file.
     *
     * @return A JSON object representation of the command output.
     * @throws CLOCException if an error occurs while executing the command.
     */
    public ObjectNode countByFile() throws CLOCException {
        return execute(commandLine.withArgument("--by-file"), timeout);
    }

    /**
     * Count the physical lines of source code, reporting results by file and language.
     *
     * @return A JSON object representation of the command output.
     * @throws CLOCException if an error occurs while executing the command.
     */
    public ObjectNode countByFileAndLanguage() throws CLOCException {
        return execute(commandLine.withArgument("--by-file-by-lang"), timeout);
    }

    /**
     * Count the number of files, reporting results by language.
     *
     * @return A JSON object representation of the command output.
     * @throws CLOCException if an error occurs while executing the command.
     */
    public ObjectNode countFiles() throws CLOCException {
        return execute(commandLine.withArgument("--only-count-files"), timeout);
    }

    private static ObjectNode execute(Commandline commandline, int timeout) throws CLOCException {
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

    private static final class CommandLine extends Commandline {

        /**
         * Creates a copy of the original instance with the specified argument added to the end of the argument list.
         *
         * @param value the value of the argument to add.
         * @return a copy of the current instance containing the specified argument.
         */
        @Contract("_ -> new")
        public @NotNull CommandLine withArgument(String value) {
            CommandLine copy = new CommandLine();
            copy.setShell(getShell());
            copy.setExecutable(getLiteralExecutable());
            copy.setWorkingDirectory(getWorkingDirectory());
            copy.addArguments(getArguments());
            copy.createArg().setValue(value);
            return copy;
        }
    }

    private static final class StringStreamConsumer extends CommandLineUtils.StringStreamConsumer {
    }
}
