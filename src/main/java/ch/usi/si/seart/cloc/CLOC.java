package ch.usi.si.seart.cloc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.SystemUtils;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Counts blank lines, comment lines, and physical lines of source code in many programming languages.
 *
 * @author Ozren Dabić
 */
public final class CLOC {

    private static final String CMD = "cloc";

    private static final String EXECUTABLE = getBundledExecutable();

    private static final JsonMapper DEFAULT_MAPPER = new JsonMapper();
    private static volatile JsonMapper OUTPUT_MAPPER = DEFAULT_MAPPER;

    private final CommandLine commandLine;
    private final int timeout;

    private CLOC(CommandLine commandLine, int timeout) {
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
     * @return a new command builder instance.
     */
    public static Builder command() {
        return new Builder();
    }

    private static String getBundledExecutable() {
        String extension = SystemUtils.IS_OS_WINDOWS ? "exe" : "pl";
        URL url = CLOC.class.getClassLoader().getResource(CMD + "." + extension);
        String protocol = Objects.requireNonNull(url).getProtocol();
        switch (protocol) {
            case "file":
                return new File(url.getFile()).getPath();
            case "jar":
                try {
                    File script = new File(SystemUtils.JAVA_IO_TMPDIR, CMD);
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
     * Facilitates the construction of {@link CLOC} command instances.
     * It allows for the step-by-step creation of these objects
     * by providing methods for setting individual attributes.
     * Input validations are performed at each build step.
     */
    public static final class Builder {

        private int timeout = 0;

        private final Set<String> flags = Stream.of("json", "quiet")
                .collect(Collectors.toCollection(LinkedHashSet::new));

        private final Map<String, String> parameters = new LinkedHashMap<>();

        /**
         * Set the timeout for executing the command.
         *
         * @param value the timeout in seconds, or 0 for no timeout.
         * @return this builder instance.
         * @throws IllegalArgumentException if the timeout is less than 0.
         */
        @Contract(value = "_ -> this")
        public Builder timeout(int value) {
            if (value < 0) throw new IllegalArgumentException("Timeout must be greater than or equal to 0!");
            timeout = value;
            return this;
        }

        /**
         * Set the number of cores to use for executing the command.
         *
         * @param value the number of cores to use, or 0 to disable multiprocessing.
         * @return this builder instance.
         * @throws IllegalArgumentException if the number of cores is lower than 0.
         */
        @Contract(value = "_ -> this")
        public Builder cores(int value) {
            if (value < 0) throw new IllegalArgumentException("Number of cores must be greater than or equal to 0!");
            if (value > 1) parameters.put("processes", String.valueOf(value));
            else parameters.remove("processes");
            return this;
        }

        /**
         * Controls whether {@code cloc} treats docstrings as code.
         * By default, docstrings are seen as comments,
         * but they may also be regular strings in assignments or function arguments.
         *
         * @param value true to treat docstrings as code, false to treat them as comments.
         * @return this builder instance.
         */
        @Contract(value = "_ -> this")
        public Builder docstringAsCode(boolean value) {
            Consumer<String> action = value ? flags::add : flags::remove;
            action.accept("docstring-as-code");
            return this;
        }

        /**
         * Follow symbolic links to directories. Symbolic links to files are always followed. Only applies to Unix-like
         * systems.
         *
         * @param value whether symbolic links should be followed.
         * @return this builder instance.
         */
        @Contract(value = "_ -> this")
        public Builder followLinks(boolean value) {
            Consumer<String> action = value ? flags::add : flags::remove;
            action.accept("follow-links");
            return this;
        }

        /**
         * Skip files exceeding the specified maximum size in megabytes. Default is 100.
         * <p>
         * The underlying command needs approximately twenty times the largest file's size in memory.
         * Processing files larger than 100 MB on a system with less than 2 GB of RAM may lead to issues.
         *
         * @param value the maximum file size in megabytes.
         * @return this builder instance.
         */
        @Contract(value = "_ -> this")
        public Builder maxFileSize(int value) {
            if (value <= 0) throw new IllegalArgumentException("Maximum file size must be greater than 0!");
            parameters.put("max-file-size", String.valueOf(value));
            return this;
        }

        /**
         * Count files in the given directories without recursively descending into their subdirectories.
         *
         * @param value whether to prevent recursion into subdirectories.
         * @return this builder instance.
         */
        @Contract(value = "_ -> this")
        public Builder noRecurse(boolean value) {
            Consumer<String> action = value ? flags::add : flags::remove;
            action.accept("no-recurse");
            return this;
        }

        /**
         * Process binary files in addition to text files.
         * <p>
         * This is usually a bad idea and should only be attempted with text files that have embedded binary data.
         *
         * @param value whether to process binary files.
         * @return this builder instance.
         */
        @Contract(value = "_ -> this")
        public Builder readBinaryFiles(boolean value) {
            Consumer<String> action = value ? flags::add : flags::remove;
            action.accept("read-binary-files");
            return this;
        }

        /**
         * Skip the file uniqueness check.
         * <p>
         * This will give a performance boost at the expense of counting files with identical contents multiple times
         * (if such duplicates exist).
         *
         * @param value whether to skip the file uniqueness check.
         * @return this builder instance.
         */
        @Contract(value = "_ -> this")
        public Builder skipUniqueness(boolean value) {
            Consumer<String> action = value ? flags::add : flags::remove;
            action.accept("skip-uniqueness");
            return this;
        }

        /**
         * Create a new command instance targeting the specified path.
         *
         * @param path the path to target, mustn't be {@code null}.
         * @return a new command instance targeting the specified path.
         * @throws NullPointerException if the path is {@code null}.
         * @throws IllegalArgumentException if the path doesn't exist.
         */
        @Contract("_ -> new")
        public @NotNull CLOC target(@NotNull Path path) {
            Objects.requireNonNull(path, "Path must not be null!");
            File file = path.toFile();
            if (!file.exists()) throw new IllegalArgumentException("Unable to read: " + path);

            CommandLine commandLine = new CommandLine();
            commandLine.setExecutable(EXECUTABLE);
            commandLine.createArg().setFile(file);
            flags.stream()
                    .map(flag -> "--" + flag)
                    .forEach(commandLine::createArg);
            parameters.entrySet().stream()
                    .map(entry -> "--" + entry.getKey() + "=" + entry.getValue())
                    .forEach(commandLine::createArg);
            return new CLOC(commandLine, timeout);
        }
    }

    /**
     * Count the physical lines of source code, reporting results by language.
     *
     * @return A JSON object representation of the command output.
     * @throws CLOCException if an error occurs while executing the command.
     */
    public ObjectNode linesByLanguage() throws CLOCException {
        return execute(commandLine.clone(), timeout);
    }

    /**
     * Count the physical lines of source code, reporting results by file.
     *
     * @return A JSON object representation of the command output.
     * @throws CLOCException if an error occurs while executing the command.
     */
    public ObjectNode linesByFile() throws CLOCException {
        return execute(commandLine.withArgument("--by-file"), timeout);
    }

    /**
     * Count the physical lines of source code, reporting results by file and language.
     *
     * @return A JSON object representation of the command output.
     * @throws CLOCException if an error occurs while executing the command.
     */
    public ObjectNode linesByFileAndLanguage() throws CLOCException {
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

        @Override
        public CommandLine clone() {
            CommandLine clone = new CommandLine();
            clone.setShell(getShell());
            clone.setExecutable(getLiteralExecutable());
            clone.setWorkingDirectory(getWorkingDirectory());
            clone.addArguments(getArguments());
            return clone;
        }

        /**
         * Creates a copy of the original instance with the specified argument added to the end of the argument list.
         *
         * @param value the value of the argument to add.
         * @return a copy of the current instance containing the specified argument.
         */
        @Contract("_ -> new")
        public @NotNull CommandLine withArgument(String value) {
            CommandLine clone = clone();
            clone.createArg(value);
            return clone;
        }


        /**
         * Creates an argument object and sets its value.
         *
         * @param value the value of the argument to add.
         */
        public void createArg(String value) {
            createArg().setValue(value);
        }
    }

    private static final class StringStreamConsumer extends CommandLineUtils.StringStreamConsumer {
    }
}
