package ch.usi.si.seart.cloc;

/**
 * Base class for all exceptions representing {@code cloc} errors.
 *
 * @author Ozren Dabić
 */
public class CLOCException extends Exception {

    CLOCException(String message) {
        super(message);
    }

    CLOCException(Throwable cause) {
        super(cause);
    }

    CLOCException(String message, Throwable cause) {
        super(message, cause);
    }
}
