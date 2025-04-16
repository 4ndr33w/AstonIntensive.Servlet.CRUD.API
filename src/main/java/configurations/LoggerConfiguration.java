package configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class LoggerConfiguration {
    public static final Logger logger = LoggerFactory.getLogger(LoggerConfiguration.class);

    static {
        configureLogger();
    }

    public LoggerConfiguration() {
        // Логирование инициализации
        logger.info("Logger configuration initialized");
    }

    private static void configureLogger() {
        // 1. Настройка моста JUL-to-SLF4J
        configureJULBridge();

        // 2. Дополнительные настройки (можно расширить)
        configureLoggingLevels();
    }

    private static void configureJULBridge() {
        try {
            // Удаляем стандартные обработчики JUL
            LogManager.getLogManager().reset();

            // Устанавливаем мост для перенаправления JUL -> SLF4J
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            // Устанавливаем уровень логирования для корневого логгера JUL
            java.util.logging.Logger.getLogger("").setLevel(Level.FINEST);

            logger.debug("JUL to SLF4J bridge configured successfully");
        } catch (Exception e) {
            logger.error("Failed to configure JUL bridge", e);
        }
    }

    private static void configureLoggingLevels() {
        // Можно добавить специфичные настройки уровней для разных пакетов
        // Пример:
        // ch.qos.logback.classic.Logger root =
        //     (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        // root.setLevel(Level.INFO);
        // ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger("com.myapp")).setLevel(Level.DEBUG);
    }

    /**
     * Дополнительный метод для проверки конфигурации логирования
     */
    public static void logConfigurationTest() {
        logger.trace("This is a TRACE message");
        logger.debug("This is a DEBUG message");
        logger.info("This is an INFO message");
        logger.warn("This is a WARN message");
        logger.error("This is an ERROR message");

        // Тест JUL логирования
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(LoggerConfiguration.class.getName());
        julLogger.finest("JUL FINEST message");
        julLogger.finer("JUL FINER message");
        julLogger.fine("JUL FINE message");
        julLogger.info("JUL INFO message");
        julLogger.warning("JUL WARNING message");
        julLogger.severe("JUL SEVERE message");
    }
}
