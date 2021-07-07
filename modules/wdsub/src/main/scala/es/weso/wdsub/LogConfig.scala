package es.weso.wdsub
import org.apache.log4j._

object LogConfig {
 
  def configureLogging(): Unit = {
		// Create the appender that will write log messages to the console.
		val consoleAppender: ConsoleAppender = new ConsoleAppender();
		// Define the pattern of log messages.
		// Insert the string "%c{1}:%L" to also show class name and line.
		val pattern: String = "%d{MM-dd HH:mm} %-5p - %m%n";
		consoleAppender.setLayout(new PatternLayout(pattern));
		// Change to Level.ERROR for fewer messages:
		consoleAppender.setThreshold(Level.INFO);
		consoleAppender.activateOptions();
		Logger.getRootLogger().addAppender(consoleAppender);
	}

}