package utils.exceptions.handlers;

import models.dtos.ErrorDto;
import utils.StaticConstants;
import utils.exceptions.*;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ExceptionHandler {

    private Exception exception;

    public ExceptionHandler(Exception exception) {
        this.exception = exception;
    }

    public ErrorDto handleException(Exception exception) {

        var error = new ErrorDto();
        if(exception instanceof DatabaseOperationException ||
                exception instanceof DataParsingException ||
                exception instanceof NullPointerException ||
                exception instanceof ResultSetMappingException ||
                exception instanceof SQLException ||
                exception instanceof ProjectUpdateException) {

            error.setMessage(exception.getMessage());
            error.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            error.setTimestamp(new Date());
        }

        if(exception instanceof MultipleUsersNotFoundException ||
                exception instanceof MultipleProjectsNotFoundException ||
                exception instanceof ProjectNotFoundException ||
                exception instanceof UserNotFoundException) {

            error.setMessage(exception.getMessage());
            error.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
            error.setTimestamp(new Date());
        }
        return error;
    }

}
