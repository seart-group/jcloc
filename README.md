# JCloc

## Requirements

- Perl
- Java 8

Note that you do not need to have `cloc` installed on your system, as the JAR comes bundled with the necessary scripts.

## Examples

```java
import ch.usi.si.seart.cloc.CLOCCommand;
import ch.usi.si.seart.cloc.CLOCException;

public class Main {

    public static void main(String[] args) throws CLOCException {
        Path path = Paths.get("path/to/target");
        String result = CLOCCommand.targeting(path)
                .byLanguage()
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
