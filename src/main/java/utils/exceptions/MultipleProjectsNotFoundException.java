package utils.exceptions;

public class MultipleProjectsNotFoundException extends RuntimeException {
    public MultipleProjectsNotFoundException(String message) {
        super(message);
    }
    public MultipleProjectsNotFoundException(String message, Throwable cause) {super(message, cause);}
}
