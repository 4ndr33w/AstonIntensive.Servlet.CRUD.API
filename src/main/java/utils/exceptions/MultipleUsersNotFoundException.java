package utils.exceptions;

public class MultipleUsersNotFoundException extends RuntimeException {
    public MultipleUsersNotFoundException(String message) {
        super(message);
    }
        public MultipleUsersNotFoundException(String message, Throwable cause) {super(message, cause);}
}
