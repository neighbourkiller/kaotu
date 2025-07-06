package com.kaotu.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局日志工具类
 * <p>
 * 封装了 logback.xml 中定义的 logger，提供静态方法直接调用。
 */
public final class LogUtils {

    // 根据 logback.xml 中定义的 logger name 获取 Logger 实例
    // 用于记录到 browse.log
    private static final Logger BROWSE_LOGGER = LoggerFactory.getLogger("browse");

    // 用于记录到 other.log
    private static final Logger OTHER_OP_LOGGER = LoggerFactory.getLogger("other");

    private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("error");

    // 通用 logger，由 root logger 处理，主要输出到控制台
    private static final Logger CONSOLE_LOGGER = LoggerFactory.getLogger(LogUtils.class);

    /**
     * 私有构造函数，防止该类被实例化
     */
    private LogUtils() {
    }

    /**
     * 记录 "browse" 日志 (INFO 级别)
     *
     * @param format    日志消息模板，例如 "用户ID: {}, 书籍ID: {}"
     * @param arguments 模板参数
     */
    public static void browse(String format, Object... arguments) {
        BROWSE_LOGGER.info(format, arguments);
    }

    /**
     * 记录 "otherOperation" 日志 (INFO 级别)
     *
     * @param format    日志消息模板
     * @param arguments 模板参数
     */
    public static void other(String format, Object... arguments) {
        OTHER_OP_LOGGER.info(format, arguments);
    }

    /**
     * 记录通用 INFO 级别日志 (由 root logger 处理，默认输出到控制台)
     *
     * @param format    日志消息模板
     * @param arguments 模板参数
     */
    public static void info(String format, Object... arguments) {
        CONSOLE_LOGGER.info(format, arguments);
    }

    /**
     * 记录通用 WARN 级别日志 (由 root logger 处理，默认输出到控制台)
     *
     * @param format    日志消息模板
     * @param arguments 模板参数
     */
    public static void warn(String format, Object... arguments) {
        CONSOLE_LOGGER.warn(format, arguments);
    }

    /**
     * 记录通用 ERROR 级别日志 (由 root logger 处理，默认输出到控制台)
     *
     * @param format    日志消息模板
     * @param arguments 模板参数
     */
    public static void error(String format, Object... arguments) {
        CONSOLE_LOGGER.error(format, arguments);
        ERROR_LOGGER.error(format, arguments);
    }

    /**
     * 记录通用 ERROR 级别日志，并附带异常信息
     *
     * @param message 日志消息
     * @param t       异常对象
     */
    public static void error(String message, Throwable t) {
        CONSOLE_LOGGER.error(message, t);
    }
}