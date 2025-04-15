package utils;

import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class Utils {

    public boolean validateId(String id) throws IllegalArgumentException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
