package utils.exceptions;

public class ProjectUserNotFoundException extends RuntimeException {
    public ProjectUserNotFoundException(String message) {
        super(message);
    }
    public ProjectUserNotFoundException(String message, Throwable cause) { super(message, cause);}
}
