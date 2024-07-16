package ch.usi.si.seart.cloc;

/**
 * Base class for all exceptions representing {@code cloc} errors.
 *
 * @author Ozren Dabić
 */
public class CLOCException extends Exception {

    public CLOCException(String message) {
        super(message);
    }

    public CLOCException(Throwable cause) {
        super(cause);
    }

    public CLOCException(String message, Throwable cause) {
        super(message, cause);
    }
}
