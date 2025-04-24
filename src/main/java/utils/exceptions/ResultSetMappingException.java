package utils.exceptions;

public class ResultSetMappingException extends RuntimeException {
    public ResultSetMappingException(String message) {
        super(message);
    }
    public ResultSetMappingException(String message, Throwable cause) {}
}
