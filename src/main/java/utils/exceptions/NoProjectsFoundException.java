package utils.exceptions;

public class NoProjectsFoundException extends RuntimeException {
    public NoProjectsFoundException(String message) {
        super(message);
    }
    public NoProjectsFoundException(String message, Throwable cause) {super(message, cause);}
}
