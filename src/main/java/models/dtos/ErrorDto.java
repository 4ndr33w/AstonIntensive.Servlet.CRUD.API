package models.dtos;

import java.util.Date;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ErrorDto {

    private Date timestamp;
    private int statusCode;
    private String path;
    private String message;

    public ErrorDto() {
        this.timestamp = new Date();
    }
    public ErrorDto(int statusCode, String path, String message) {

        this.timestamp = new Date();
        this.statusCode = statusCode;
        this.path = path;
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public int getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ErrorDto{" +
                "timestamp=" + timestamp +
                ", statusCode=" + statusCode +
                ", path='" + path + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
