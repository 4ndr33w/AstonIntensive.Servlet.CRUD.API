package configurations;

import java.util.Properties;

/**
 * Конфигурационный класс для загрузки настроек и списка констант
 * из конфигурационных файлов
 * @author 4ndr33w
 * @version 1.0
 */
public class PropertiesConfiguration {

    private static final Properties properties;
    private static final Properties constants;

    static {
        try {
            properties = new Properties();
            properties.load(PropertiesConfiguration.class.getClassLoader().getResourceAsStream("application.properties"));

            constants = new Properties();
            constants.load(PropertiesConfiguration.class.getClassLoader().getResourceAsStream("constants.properties"));
        }catch (Exception e) {
            throw new RuntimeException("Error while loading application configuration", e);
        }
    }

    /**
     * Получить параметры из конфигурационного файлф
     * @return
     */
    public static Properties getProperties(){
        return properties;
    }

    /**
     * Получить список констант
     * @return
     */
    public static Properties getConstants(){
        return constants;
    }
}
