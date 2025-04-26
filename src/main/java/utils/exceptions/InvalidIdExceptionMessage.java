package utils.exceptions;

public class InvalidIdExceptionMessage extends RuntimeException {
    public InvalidIdExceptionMessage(String message) {
        super(message);
    }
    public InvalidIdExceptionMessage(String message, Throwable cause) { super(message, cause);}
}
