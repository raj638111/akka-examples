package a10_akka_typed.utils

import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.{ConfigurationBuilder, ConfigurationBuilderFactory}
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration
import org.apache.logging.log4j.{Level, LogManager, Logger}


object CustomLogger {

  val pattern = "%p | %d{yy/MM/dd HH:mm:ss} | %c | %M() | %L | %m%n%throwable"
  val consoleAppenderName = "Stdout"
  val logLevel = System.getProperty("loglevel") match {
    case "debug" => Level.DEBUG
    case "info" => Level.INFO
    case _ => Level.INFO
  }
  setLogger()

  def setLogger(): Unit = {
    val builder = createBuilder()
    addConsoleAppender(builder)
    initializeLogger(builder)
  }

  def getLogger(category: String): Logger = {
    LogManager.getLogger(category)
  }

  private def createBuilder(): ConfigurationBuilder[BuiltConfiguration] = {
    ConfigurationBuilderFactory.newConfigurationBuilder().setConfigurationName("BuilderTest")
  }

  private def addConsoleAppender(builder: ConfigurationBuilder[BuiltConfiguration]): Unit = {
    val appenderBuilder = builder
      .newAppender(consoleAppenderName, "CONSOLE")
      .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)  match {
      case appenderBuilder => appenderBuilder
        .add(builder
          .newLayout("PatternLayout")
          .addAttribute("pattern", pattern))
        .add(builder
          .newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
          .addAttribute("marker", "FLOW"));
    }
    builder.add(appenderBuilder)
  }

  private def initializeLogger(builder: ConfigurationBuilder[BuiltConfiguration]): Unit = {
    // Set root logger
    builder.add(builder.newRootLogger(logLevel).add(builder.newAppenderRef(consoleAppenderName)))
    // Initialize logging context
    Configurator.initialize(builder.build());
  }


}
