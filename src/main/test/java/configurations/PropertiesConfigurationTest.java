package configurations;

import org.junit.Test;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class PropertiesConfigurationTest {

    @Test
    public void propertiesTest(){
        String expectedResult = "users";
        String actualResult = PropertiesConfiguration.getProperties().getProperty("jdbc.users-table");

        assert expectedResult.equals(actualResult) : "The value is wrong";
    }

    @Test
    public void constantsTest(){
        String expectedResult = "Email is already in use";
        String actualResult = PropertiesConfiguration.getConstants().getProperty("EMAIL_IS_ALREADY_IN_USE_EXCEPTION_MESSAGE");

        assert expectedResult.equals(actualResult) : "The value is wrong";

    }
}
