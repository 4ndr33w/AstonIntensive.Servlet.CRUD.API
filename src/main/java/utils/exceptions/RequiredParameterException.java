package utils.exceptions;

public class RequiredParameterException extends RuntimeException {
    public RequiredParameterException(String message) {
        super(message);
    }
    public RequiredParameterException(String message, Throwable cause) {super(message, cause);}
}
