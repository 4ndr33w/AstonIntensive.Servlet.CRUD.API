package utils;

import configurations.PropertiesConfiguration;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class StaticConstants {
    public static final String EMAIL_IS_ALREADY_IN_USE_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("EMAIL_IS_ALREADY_IN_USE_EXCEPTION_MESSAGE");
    public static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("USER_NOT_FOUND_EXCEPTION_MESSAGE");
    public static final String CANNOT_CREATE_NEW_USER_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("CANNOT_CREATE_NEW_USER_EXCEPTION_MESSAGE");
    public static final String INVALID_USERNAME_OR_PASSWORD_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("INVALID_USERNAME_OR_PASSWORD_EXCEPTION_MESSAGE");
    public static final String USERNAME_OR_PASSWORD_IS_NULL_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("USERNAME_OR_PASSWORD_IS_NULL_EXCEPTION_MESSAGE");
    public static final String BASIC_AUTHORIZATION_HEADER_IS_MISSING_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("BASIC_AUTHORIZATION_HEADER_IS_MISSING_EXCEPTION_MESSAGE");
    public static final String DATABASE_ACCESS_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("DATABASE_ACCESS_EXCEPTION_MESSAGE");
    public static final String BAD_REQUEST_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("BAD_REQUEST_EXCEPTION_MESSAGE");
    public static final String NO_PROJECTS_FOUND_BY_ADMIN_ID_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("NO_PROJECTS_FOUND_BY_ADMIN_ID_EXCEPTION_MESSAGE");
    public static final String PARAMETER_IS_NULL_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("PARAMETER_IS_NULL_EXCEPTION_MESSAGE");
    public static final String FIND_BY_ID_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("FIND_BY_ID_EXCEPTION_MESSAGE");
    public static final String FIND_ALL_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("FIND_ALL_EXCEPTION_MESSAGE");
    public static final String ERROR_DURING_SAVING_DATA_INTO_DATABASE_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("ERROR_DURING_SAVING_DATA_INTO_DATABASE_EXCEPTION_MESSAGE");
    public static final String FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE");
    public static final String ERROR_FETCHING_RESULT_SET_METADATA_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("ERROR_FETCHING_RESULT_SET_METADATA_EXCEPTION_MESSAGE");
}