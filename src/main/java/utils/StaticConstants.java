package utils;

import configurations.PropertiesConfiguration;

/**
 * Утильный класс для загрузки и хранения текстовых значений констант
 * из файла constants.properties
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class StaticConstants {

    public static final String BAD_REQUEST_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("BAD_REQUEST_EXCEPTION_MESSAGE");

    public static final String PARAMETER_IS_NULL_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("PARAMETER_IS_NULL_EXCEPTION_MESSAGE");
    public static final String FIND_BY_ID_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("FIND_BY_ID_EXCEPTION_MESSAGE");
    public static final String FIND_ALL_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("FIND_ALL_EXCEPTION_MESSAGE");


    public static final String ERROR_FETCHING_RESULT_SET_METADATA_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("ERROR_FETCHING_RESULT_SET_METADATA_EXCEPTION_MESSAGE");

    public static final String INVALID_ID_FORMAT_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("INVALID_ID_FORMAT_EXCEPTION_MESSAGE");
    public static final String REQUEST_COMPLETER_SUCCESSFULLY_MESSAGE = PropertiesConfiguration.getConstants().getProperty("REQUEST_COMPLETER_SUCCESSFULLY_MESSAGE");
    public static final String ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE = PropertiesConfiguration.getConstants().getProperty("ID_REQUIRED_AD_PARAMETER_ERROR_MESSAGE");
    public static final String OPERATION_FAILED_ERROR_MESSAGE = PropertiesConfiguration.getConstants().getProperty("OPERATION_FAILED_ERROR_MESSAGE");

    public static final String UNEXPECTED_ERROR_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("UNEXPECTED_ERROR_EXCEPTION_MESSAGE");
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = PropertiesConfiguration.getConstants().getProperty("INTERNAL_SERVER_ERROR_MESSAGE");
    public static final String STATIC_TEST_STRING = PropertiesConfiguration.getConstants().getProperty("STATIC_TEST_STRING");
    public static final String REQUEST_VALIDATION_ERROR_MESSAGE = PropertiesConfiguration.getConstants().getProperty("REQUEST_VALIDATION_ERROR_MESSAGE");
    public static final String ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE");

    // Project
    public static final String PROJECT_NOT_FOUND_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("PROJECT_NOT_FOUND_EXCEPTION_MESSAGE");
    public static final String NO_PROJECTS_FOUND_BY_USER_ID_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("NO_PROJECTS_FOUND_BY_USER_ID_EXCEPTION_MESSAGE");
    public static final String PROJECTS_NOT_FOUND_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("PROJECTS_NOT_FOUND_EXCEPTION_MESSAGE");
    public static final String NO_PROJECTS_FOUND_BY_ADMIN_ID_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("PROJECTS_NOT_FOUND_BY_USER_ID_EXCEPTION_MESSAGE");
    public static final String ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE");
    public static final String FAILED_TO_UPDATE_PROJECT_USERS_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("FAILED_TO_UPDATE_PROJECT_USERS_EXCEPTION_MESSAGE");

    // User
    public static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("USER_NOT_FOUND_EXCEPTION_MESSAGE");
    public static final String USERS_NOT_FOUND_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("USERS_NOT_FOUND_EXCEPTION_MESSAGE");
    public static final String FAILED_TO_CREATE_NEW_USER_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("FAILED_TO_CREATE_NEW_USER_EXCEPTION_MESSAGE");
    public static final String FAILED_TO_UPDATE_USER_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("FAILED_TO_UPDATE_USER_EXCEPTION_MESSAGE");
    public static final String USER_ALREADY_EXISTS_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("USER_ALREADY_EXISTS_EXCEPTION_MESSAGE");

    // Database
    public static final String DATABASE_OPERATION_NO_ROWS_AFFECTED_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("DATABASE_OPERATION_NO_ROWS_AFFECTED_EXCEPTION_MESSAGE");
    public static final String DATA_NOT_FOUND_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("DATA_NOT_FOUND_EXCEPTION_MESSAGE");
    public static final String ERROR_DURING_SAVING_DATA_INTO_DATABASE_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("ERROR_DURING_SAVING_DATA_INTO_DATABASE_EXCEPTION_MESSAGE");
    public static final String DATABASE_ACCESS_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("DATABASE_ACCESS_EXCEPTION_MESSAGE");
    public static final String FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE");
    public static final String UNABLE_TO_LOAD_DB_DRIVER = PropertiesConfiguration.getConstants().getProperty("UNABLE_TO_LOAD_DB_DRIVER");
    public static final String PROJECT_USER_NOT_FOUND_EXCEPTION_MESSAGE = PropertiesConfiguration.getConstants().getProperty("PROJECT_USER_NOT_FOUND_EXCEPTION_MESSAGE");

}