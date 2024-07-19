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

## Usage

```java
import ch.usi.si.seart.cloc.CLOC;
import ch.usi.si.seart.cloc.CLOCException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws CLOCException {
        Path path = Paths.get("path", "to", "target");
        String result = CLOC.command()
                .cores(4)
                .timeout(60)
                .maxFileSize(10)
                .target(path)
                .countByLanguage()
                .toPrettyString();
        System.out.println(result);
    }
}
```

For more usage examples, take a look at the [tests](src/test/java/ch/usi/si/seart/cloc).

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
