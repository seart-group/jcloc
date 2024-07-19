# jcloc &middot; [![MIT license](https://img.shields.io/github/license/seart-group/jcloc)](https://github.com/seart-group/jcloc/blob/master/LICENSE)

Java wrapper for the [cloc](https://github.com/AlDanial/cloc) CLI tool. You can preview the library by including the
following dependency in your Maven project:

```xml
<dependency>
    <groupId>ch.usi.si.seart</groupId>
    <artifactId>jcloc</artifactId>
    <version>PREVIEW-SNAPSHOT</version>
</dependency>
```

> [!WARNING]  
> This library is currently in an alpha stage and should not be used in production.
> Until officially released, expect breaking changes to its APIs.

## Requirements

This library requires a minimum of Java 8. You do not need to have `cloc` installed on your system, as the JAR comes
bundled with the necessary executable. Said script should be executable out of the box on most Unix-like systems (as
it is written in Perl, which is available on the aforementioned systems by default).

## Example

```java
import ch.usi.si.seart.cloc.CLOC;
import ch.usi.si.seart.cloc.CLOCException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws CLOCException {
        Path path = Paths.get("path", "to", "target");
        String result = CLOC.command()
                .timeout(30)
                .target(path)
                .linesByLanguage()
                .toPrettyString();
        System.out.println(result);
    }
}
```

For more usage examples, take a look at the [tests](src/test/java/ch/usi/si/seart/cloc).

## Options

The API currently maps only a subset of the `cloc` command-line options:

```jshelllanguage
import ch.usi.si.seart.cloc.CLOC;

CLOC.command()             // `cloc` equivalent:
    .cores(4)              // --cores
    .docstringAsCode(true) // --docstring-as-code
    .followLinks(true)     // --follow-links
    .maxFileSize(10)       // --max-file-size
    .noRecurse(true)       // --no-recurse
    .readBinaryFiles(true) // --read-binary-files
    .skipUniqueness(true); // --skip-uniqueness
```

Support for other flags and parameters will be added as development progresses.

## Output

Once the command has been built, you can execute it using one of the following methods:

```jshelllanguage
import ch.usi.si.seart.cloc.CLOC;

CLOC cloc = CLOC.command().target(path);

cloc.linesByLanguage();        // Count lines of code by language (default `cloc` behaviour)
cloc.linesByFile();            // Count lines of code by file (equivalent to `cloc --by-file`)
cloc.linesByFileAndLanguage(); // Count lines of code by file and language (equivalent to `cloc --by-file-by-lang`)
cloc.countFiles();             // Count files by language (equivalent to `cloc --only-count-files`)
```

Results returned by all `cloc` command variants are parsed courtesy of [Jackson](https://github.com/FasterXML/jackson).
Since all methods return an `ObjectNode`, you can easily convert results to a `String`, or map them to a custom POJO.

You can also customize the command output mapper used by the library like so:

```java
import ch.usi.si.seart.cloc.CLOC;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class Main {

    public static void main(String[] args) {
        JsonMapper mapper = new JsonMapper();
        // Customize the mapper here...
        CLOC.setOutputMapper(mapper);
        // Use the library as usual...
    }
}
```

## FAQ

### How can I request a feature or ask a question?

If you have ideas for a feature you would like to see implemented or if you have any questions, we encourage you to
create a new [discussion](https://github.com/seart-group/jcloc/discussions/). By initiating a discussion, you can engage
with the community and our team, and we'll respond promptly to address your queries or consider your feature requests.

### How can I report a bug?

To report any issues or bugs you encounter, please create a [new issue](https://github.com/seart-group/jcloc/issues/).
Providing detailed information about the problem you're facing will help us understand and address it more effectively.
Rest assured, we are committed to promptly reviewing and responding to the issues you raise, working collaboratively
to resolve any bugs and improve the overall user experience.

### How do I contribute to the project?

Please refer to [CONTRIBUTING.md](CONTRIBUTING.md) for more information.
