# jcloc &middot; [![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/seart-group/jcloc/deploy.yml)](https://github.com/seart-group/jcloc/actions/workflows/deploy.yml) [![Maven Central](https://img.shields.io/maven-central/v/ch.usi.si.seart/jcloc)](https://central.sonatype.com/artifact/ch.usi.si.seart/jcloc) [![javadoc](https://javadoc.io/badge2/ch.usi.si.seart/jcloc/javadoc.svg)](https://javadoc.io/doc/ch.usi.si.seart/jcloc) [![MIT license](https://img.shields.io/github/license/seart-group/jcloc)](https://github.com/seart-group/jcloc/blob/master/LICENSE)

Java wrapper for the [cloc](https://github.com/AlDanial/cloc) CLI tool. You can use the library by including the
following dependency in your Maven project:

```xml
<dependency>
  <groupId>ch.usi.si.seart</groupId>
  <artifactId>jcloc</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Requirements

This library requires a minimum of Java 8. You don't need to have `cloc` installed on your system, as the JAR comes
bundled with the necessary script. Since it is written in Perl, it should be executable out of the box on most systems
(as the majority of Unix-like systems have it installed by default).

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

For more usage examples, take a look at the [tests](/src/test/java/ch/usi/si/seart/cloc).

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
Since all methods return an `ObjectNode`, you can convert results to a `String`, or map them to a custom POJO.

You can also customise the command output mapper used by the library like so:

```java
import ch.usi.si.seart.cloc.CLOC;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class Main {

    public static void main(String[] args) {
        JsonMapper mapper = JsonMapper.builder()
                .enable(/* FEATURES */)
                .build();
        CLOC.setOutputMapper(mapper);
        // Use the library as usual
    }
}
```

## FAQ

### How can I request a feature or ask a question?

If you have ideas for a feature that you would like to see implemented or if you have any questions, we encourage you to
create a new [discussion](https://github.com/seart-group/jcloc/discussions). By initiating a discussion, you can engage with the community and our
team, and we will respond promptly to address your queries or consider your feature requests.

### How can I report a bug?

To report any issues or bugs you encounter, create a [new issue](https://github.com/seart-group/jcloc/issues). Providing detailed information about
the problem you're facing will help us understand and address it more effectively. Rest assured, we're committed to
promptly reviewing and responding to the issues you raise, working collaboratively to resolve any bugs and improve the
overall user experience.

### How do I contribute to the project?

Refer to [CONTRIBUTING.md](/CONTRIBUTING.md) for more information.
