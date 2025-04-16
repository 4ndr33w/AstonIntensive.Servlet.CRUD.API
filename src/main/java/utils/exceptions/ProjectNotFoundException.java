package utils.exceptions;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String message) {
        super(message);
    }
    public ProjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
