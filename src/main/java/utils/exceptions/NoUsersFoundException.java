package utils.exceptions;

public class NoUsersFoundException extends RuntimeException {
    public NoUsersFoundException(String message) {
        super(message);
    }
        public NoUsersFoundException(String message, Throwable cause) {super(message, cause);}
}
