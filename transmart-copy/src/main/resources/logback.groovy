import ch.qos.logback.classic.filter.ThresholdFilter

appender("stderr", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d [%level] %msg%n"
    }
    /* from https://stackoverflow.com/questions/5998419/how-do-i-use-logback-groovy-file-to-log-trace-level-to-file-and-info-to-console */
    filter(ThresholdFilter) {
        level = INFO
    }
    target = 'System.err'
}
logger('com.zaxxer.hikari', WARN)

root(INFO, ["stderr"])
