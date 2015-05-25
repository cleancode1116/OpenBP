/*
 *   Copyright 2007 skynamics AG
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.openbp.common.logger;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.MsgFormat;
import org.openbp.common.application.Application;
import org.openbp.common.setting.SettingUtil;
import org.openbp.common.string.StringUtil;

/**
 * Provides utility methods for an implementation-independant and business object-centered logging.
 *
 * Also contains a lot of shortcut methods for invoking the logger.
 * These shortcuts work as follows:
 *
 * Depending on the type of information, use the {@link #debug(Class, String)}, {@link #info(Class, String)}, {@link #warn(Class, String)} or {@link #error(Class, String)} methods.
 * They will take the class name of the calling class as first argument, which denotes the logger to use.
 *
 * There is also a generic logger method named
 * {@link #log(String, Class, String)}method.
 * See the description of this method for details on the other logging methods.
 * Note that in this loggin implementation, log levels are strings, providing the flexibility to define custom log levels.
 *
 * Due to the amount of shortcuts, this class may look rather unsexy, but it is in fact very handy to use as soon as you got the idea:<br>
 * For each possible log level type (debug, info, warn, error, fatal and audit) there is a set of convenience methods, which takes
 * a message and a particular number of message arguments (see the {@link MsgFormat} class for an explanation).<br>
 * These methods take a business object as 2nd argument in addition,
 * providing the business object that caused the logging call to the underlying logger.
 * Note that the business object should have a meaningful toString method in this case.
 */
public final class LogUtil
	implements LogLevel
{
	/** Line separator buffer */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

	/** Flag if trace messages should be logged in info (debug otherwise) */
	private static boolean traceInfo;

	/**
	 * Logger map caches loggers to avoid multiple calls to {@link org.apache.log4j.Logger#getLogger(String)}
	 * for the same log name.
	 */
	private static ConcurrentHashMap<String, org.apache.log4j.Logger> loggerMap = new ConcurrentHashMap<String,Logger>(100);

	/**
	 * There is no name for the root logger, therefore it is not chached
	 * within {@link #loggerMap} but stored herein.
	 */
	private static org.apache.log4j.Logger rootLogger;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private LogUtil()
	{
	}

	/**
	 * Sets the flag if trace messages should be logged in info (debug otherwise).
	 * @nowarn
	 */
	public static void setTraceInfo(boolean traceInfoArg)
	{
		traceInfo = traceInfoArg;
	}

	/**
	 * Updates log settings from property sources.
	 * Updates settings such as "openbp.log.traceInfo".
	 */
	public static void updateSettings()
	{
		traceInfo = SettingUtil.getBooleanSetting("openbp.log.traceInfo", false);
	}

	/**
	 * Initializes the logger from the application's properties file.
	 * @param resourceName Name of the property file containing log4j initialzation properties
	 * @return true if the property resource was found or if the log system has been initialized already
	 */
	public static boolean initializeFromPropertyResource(String resourceName)
	{
		boolean ret = true;

		if (LogManager.getRootLogger().getAllAppenders().hasMoreElements())
		{
			LogUtil.info(LogUtil.class, "log4j already initialized, skipping.");
		}
		else
		{
			try
			{
				InputStream is = LogUtil.class.getClassLoader().getResourceAsStream(resourceName);
				Properties log4jProperties = new Properties();
				log4jProperties.load(is);
				is.close();

				ret = false;
				for (Enumeration e = log4jProperties.elements(); e.hasMoreElements();)
				{
					String key = (String) e.nextElement();
					if (key.startsWith("log4j."))
					{
						ret = true;
						break;
					}
				}
	
				if (ret)
				{
					Properties expandedLog4jProperties = SettingUtil.expandProperties(log4jProperties);
					PropertyConfigurator.configure(expandedLog4jProperties);
					LogUtil.info(LogUtil.class, "log4j initialized.");
				}
			}
			catch (Exception e)
			{
				return false;
			}
		}
		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Logging shortcuts - debug
	//////////////////////////////////////////////////

	/**
	 * Logs a debug message with no arguments.
	 * @nowarn
	 */
	public static String debug(Class cls, String msg)
	{
		return log(LogLevel.DEBUG, cls, msg);
	}

	/**
	 * Logs a debug message with a single argument.
	 * @nowarn
	 */
	public static String debug(Class cls, String msg, Object arg1)
	{
		return log(LogLevel.DEBUG, cls, msg, arg1);
	}

	/**
	 * Logs a debug message with two arguments.
	 * @nowarn
	 */
	public static String debug(Class cls, String msg, Object arg1, Object arg2)
	{
		return log(LogLevel.DEBUG, cls, msg, arg1, arg2);
	}

	/**
	 * Logs a debug message with tree arguments.
	 * @nowarn
	 */
	public static String debug(Class cls, String msg, Object arg1, Object arg2, Object arg3)
	{
		return log(LogLevel.DEBUG, cls, msg, arg1, arg2, arg3);
	}

	/**
	 * Logs a debug message with four arguments.
	 * @nowarn
	 */
	public static String debug(Class cls, String msg, Object arg1, Object arg2, Object arg3, Object arg4)
	{
		return log(LogLevel.DEBUG, cls, msg, arg1, arg2, arg3, arg4);
	}

	/**
	 * Logs a debug message using an argument array.
	 * @nowarn
	 */
	public static String debug(Class cls, String msg, Object [] args)
	{
		return log(LogLevel.DEBUG, cls, msg, args);
	}

	//////////////////////////////////////////////////
	// @@ Logging shortcuts - trace
	//////////////////////////////////////////////////

	/**
	 * Logs a trace message with no arguments.
	 * @nowarn
	 */
	public static String trace(Class cls, String msg)
	{
		return log(LogLevel.TRACE, cls, msg);
	}

	/**
	 * Logs a trace message with a single argument.
	 * @nowarn
	 */
	public static String trace(Class cls, String msg, Object arg1)
	{
		return log(LogLevel.TRACE, cls, msg, arg1);
	}

	/**
	 * Logs a trace message with two arguments.
	 * @nowarn
	 */
	public static String trace(Class cls, String msg, Object arg1, Object arg2)
	{
		return log(LogLevel.TRACE, cls, msg, arg1, arg2);
	}

	/**
	 * Logs a trace message with tree arguments.
	 * @nowarn
	 */
	public static String trace(Class cls, String msg, Object arg1, Object arg2, Object arg3)
	{
		return log(LogLevel.TRACE, cls, msg, arg1, arg2, arg3);
	}

	/**
	 * Logs a trace message with four arguments.
	 * @nowarn
	 */
	public static String trace(Class cls, String msg, Object arg1, Object arg2, Object arg3, Object arg4)
	{
		return log(LogLevel.TRACE, cls, msg, arg1, arg2, arg3, arg4);
	}

	/**
	 * Logs a trace message using an argument array.
	 * @nowarn
	 */
	public static String trace(Class cls, String msg, Object [] args)
	{
		return log(LogLevel.TRACE, cls, msg, args);
	}

	//////////////////////////////////////////////////
	// @@ Logging shortcuts - info
	//////////////////////////////////////////////////

	/**
	 * Logs an informational message with no arguments.
	 * @nowarn
	 */
	public static String info(Class cls, String msg)
	{
		return log(LogLevel.INFO, cls, msg);
	}

	/**
	 * Logs an informational message with a single argument.
	 * @nowarn
	 */
	public static String info(Class cls, String msg, Object arg1)
	{
		return log(LogLevel.INFO, cls, msg, arg1);
	}

	/**
	 * Logs an informational message with two arguments.
	 * @nowarn
	 */
	public static String info(Class cls, String msg, Object arg1, Object arg2)
	{
		return log(LogLevel.INFO, cls, msg, arg1, arg2);
	}

	/**
	 * Logs an informational message with tree arguments.
	 * @nowarn
	 */
	public static String info(Class cls, String msg, Object arg1, Object arg2, Object arg3)
	{
		return log(LogLevel.INFO, cls, msg, arg1, arg2, arg3);
	}

	/**
	 * Logs an informational message with four arguments.
	 * @nowarn
	 */
	public static String info(Class cls, String msg, Object arg1, Object arg2, Object arg3, Object arg4)
	{
		return log(LogLevel.INFO, cls, msg, arg1, arg2, arg3, arg4);
	}

	/**
	 * Logs an informational message using an argument array.
	 * @nowarn
	 */
	public static String info(Class cls, String msg, Object [] args)
	{
		return log(LogLevel.INFO, cls, msg, args);
	}

	//////////////////////////////////////////////////
	// @@ Logging shortcuts - warning
	//////////////////////////////////////////////////

	/**
	 * Logs a warning message with no arguments.
	 * @nowarn
	 */
	public static String warn(Class cls, String msg)
	{
		return log(LogLevel.WARN, cls, msg);
	}

	/**
	 * Logs a warning message with a single argument.
	 * @nowarn
	 */
	public static String warn(Class cls, String msg, Object arg1)
	{
		return log(LogLevel.WARN, cls, msg, arg1);
	}

	/**
	 * Logs a warning message with two arguments.
	 * @nowarn
	 */
	public static String warn(Class cls, String msg, Object arg1, Object arg2)
	{
		return log(LogLevel.WARN, cls, msg, arg1, arg2);
	}

	/**
	 * Logs a warning message with tree arguments.
	 * @nowarn
	 */
	public static String warn(Class cls, String msg, Object arg1, Object arg2, Object arg3)
	{
		return log(LogLevel.WARN, cls, msg, arg1, arg2, arg3);
	}

	/**
	 * Logs a warning message with four arguments.
	 * @nowarn
	 */
	public static String warn(Class cls, String msg, Object arg1, Object arg2, Object arg3, Object arg4)
	{
		return log(LogLevel.WARN, cls, msg, arg1, arg2, arg3, arg4);
	}

	/**
	 * Logs a warning message using an argument array.
	 * @nowarn
	 */
	public static String warn(Class cls, String msg, Object [] args)
	{
		return log(LogLevel.WARN, cls, msg, args);
	}

	//////////////////////////////////////////////////
	// @@ Logging shortcuts - error
	//////////////////////////////////////////////////

	/**
	 * Logs an error message with no arguments.
	 * @nowarn
	 */
	public static String error(Class cls, String msg)
	{
		return log(LogLevel.ERROR, cls, msg);
	}

	/**
	 * Logs an error message with a single argument.
	 * @nowarn
	 */
	public static String error(Class cls, String msg, Object arg1)
	{
		return log(LogLevel.ERROR, cls, msg, arg1);
	}

	/**
	 * Logs an error message with two arguments.
	 * @nowarn
	 */
	public static String error(Class cls, String msg, Object arg1, Object arg2)
	{
		return log(LogLevel.ERROR, cls, msg, arg1, arg2);
	}

	/**
	 * Logs an error message with tree arguments.
	 * @nowarn
	 */
	public static String error(Class cls, String msg, Object arg1, Object arg2, Object arg3)
	{
		return log(LogLevel.ERROR, cls, msg, arg1, arg2, arg3);
	}

	/**
	 * Logs an error message with four arguments.
	 * @nowarn
	 */
	public static String error(Class cls, String msg, Object arg1, Object arg2, Object arg3, Object arg4)
	{
		return log(LogLevel.ERROR, cls, msg, arg1, arg2, arg3, arg4);
	}

	/**
	 * Logs an error message using an argument array.
	 * @nowarn
	 */
	public static String error(Class cls, String msg, Object [] args)
	{
		return log(LogLevel.ERROR, cls, msg, args);
	}

	//////////////////////////////////////////////////
	// @@ Logging shortcuts - fatal
	//////////////////////////////////////////////////

	/**
	 * Logs a fatal message with no arguments.
	 * @nowarn
	 */
	public static String fatal(Class cls, String msg)
	{
		return log(LogLevel.FATAL, cls, msg);
	}

	/**
	 * Logs a fatal message with a single argument.
	 * @nowarn
	 */
	public static String fatal(Class cls, String msg, Object arg1)
	{
		return log(LogLevel.FATAL, cls, msg, arg1);
	}

	/**
	 * Logs a fatal message with two arguments.
	 * @nowarn
	 */
	public static String fatal(Class cls, String msg, Object arg1, Object arg2)
	{
		return log(LogLevel.FATAL, cls, msg, arg1, arg2);
	}

	/**
	 * Logs a fatal message with tree arguments.
	 * @nowarn
	 */
	public static String fatal(Class cls, String msg, Object arg1, Object arg2, Object arg3)
	{
		return log(LogLevel.FATAL, cls, msg, arg1, arg2, arg3);
	}

	/**
	 * Logs a fatal message with four arguments.
	 * @nowarn
	 */
	public static String fatal(Class cls, String msg, Object arg1, Object arg2, Object arg3, Object arg4)
	{
		return log(LogLevel.FATAL, cls, msg, arg1, arg2, arg3, arg4);
	}

	/**
	 * Logs a fatal message using an argument array.
	 * @nowarn
	 */
	public static String fatal(Class cls, String msg, Object [] args)
	{
		return log(LogLevel.FATAL, cls, msg, args);
	}

	//////////////////////////////////////////////////
	// @@ Argument formatting - general
	//////////////////////////////////////////////////

	/**
	 * Logs a message with no arguments.
	 * @nowarn
	 */
	public static String log(String logLevel, Class cls, String msg)
	{
		if (!shouldDoLog(logLevel, cls))
			return null;

		Object [] nullArgs = null; // To prevent compiler warning about unnecessary cast
		return log(logLevel, cls, msg, nullArgs);
	}

	/**
	 * Logs a message with a single argument.
	 * @nowarn
	 */
	public static String log(String logLevel, Class cls, String msg, Object arg1)
	{
		if (!shouldDoLog(logLevel, cls))
			return null;

		return log(logLevel, cls, msg, new Object [] { arg1 });
	}

	/**
	 * Logs a message with two arguments.
	 * @nowarn
	 */
	public static String log(String logLevel, Class cls, String msg, Object arg1, Object arg2)
	{
		if (!shouldDoLog(logLevel, cls))
			return null;

		return log(logLevel, cls, msg, new Object [] { arg1, arg2 });
	}

	/**
	 * Logs a message with tree arguments.
	 * @nowarn
	 */
	public static String log(String logLevel, Class cls, String msg, Object arg1, Object arg2, Object arg3)
	{
		if (!shouldDoLog(logLevel, cls))
			return null;

		return log(logLevel, cls, msg, new Object [] { arg1, arg2, arg3 });
	}

	/**
	 * Logs a message with four arguments.
	 * @nowarn
	 */
	public static String log(String logLevel, Class cls, String msg, Object arg1, Object arg2, Object arg3, Object arg4)
	{
		if (!shouldDoLog(logLevel, cls))
			return null;

		return log(logLevel, cls, msg, new Object [] { arg1, arg2, arg3, arg4 });
	}

	/**
	 * Logs a message using an argument array.
	 * This method formats a string message together with an argument array
	 * using the standard {@link MsgFormat} mechanism, but adds extra
	 * processing for null arguments (which are formatted as [null]), and
	 * handles an extra exception that might be contained as last value
	 * in the argument array.
	 *
	 * @param logLevel Log level (priority)
	 * @param cls Class to be used for logger identification
	 * @param msg The message containing placeholder's like {0} etc.<br>
	 * Special convenience: Arguments that should appear in simple quotes ('\'') do not need to be
	 * provided as "...''{0}''..." in the log message, the shortcut "...$0..." can be used instead.
	 * If the "$" is followed by a non-digit character, it will be printed as it is.<br>
	 * This should eliminate the common error of forgetting a quote, which would confuse MessageFormat.
	 * @param args Array of message arguments
	 * @return The formatted message if log logging is enabled for this log level (or if the level is ERROR or higher) or null otherwise
	 */
	public static String log(String logLevel, Class cls, String msg, Object [] args)
	{
		return doLog(logLevel, cls, msg, args);
	}

	/**
	 * Logs a message using an argument array.
	 * This method formats a string message together with an argument array
	 * using the standard {@link MsgFormat} mechanism, but adds extra
	 * processing for null arguments (which are formatted as [null]), and
	 * handles an extra exception that might be contained as last value
	 * in the argument array.
	 *
	 * @param logLevel Log level (priority)
	 * @param loggerName Logger name to be used for logger identification
	 * @param msg The message containing placeholder's like {0} etc.<br>
	 * Special convenience: Arguments that should appear in simple quotes ('\'') do not need to be
	 * provided as "...''{0}''..." in the log message, the shortcut "...$0..." can be used instead.
	 * If the "$" is followed by a non-digit character, it will be printed as it is.<br>
	 * This should eliminate the common error of forgetting a quote, which would confuse MessageFormat.
	 * @param args Array of message arguments
	 * @return The formatted message if log logging is enabled for this log level (or if the level is ERROR or higher) or null otherwise
	 */
	public static String log(String logLevel, String loggerName, String msg, Object [] args)
	{
		return doLog(logLevel, loggerName, msg, args);
	}

	//////////////////////////////////////////////////
	// @@ Logging implementation
	//////////////////////////////////////////////////

	/**
	 * Checks if we should take a closer look at this logger.
	 * @nowarn
	 */
	private static boolean shouldDoLog(String logLevel, Class cls)
	{
		if (LogLevel.ERROR.equals(logLevel) || LogLevel.FATAL.equals(logLevel))
		{
			// Always look at high log levels - maybe just for producing an error string
			return true;
		}

		Logger logger = mapLogger(cls != null ? cls.getName() : null);
		Priority priority = mapPriority(logLevel);

		if (logger.isEnabledFor(priority))
		{
			return true;
		}

		return false;
	}

	/**
	 * Logs a message using an argument array.
	 * This method formats a string message together with an argument array
	 * using the standard {@link MsgFormat} mechanism, but adds extra
	 * processing for null arguments (which are formatted as [null]), and
	 * handles an extra exception that might be contained as last value
	 * in the argument array.
	 *
	 * @param logLevel Log level (priority)
	 * @param cls Class to be used for logger identification
	 * @param msg The message containing placeholder's like {0} etc.<br>
	 * Special convenience: Arguments that should appear in simple quotes ('\'') do not need to be
	 * provided as "...''{0}''..." in the log message, the shortcut "...$0..." can be used instead.
	 * If the "$" is followed by a non-digit character, it will be printed as it is.<br>
	 * This should eliminate the common error of forgetting a quote, which would confuse MessageFormat.
	 * @param args Array of message arguments<br>
	 * If the last parameter is a java.lang.Throwable, it will not be used as message argument, instead
	 * it will be passed to the underlying logger method as exception argument.
	 * @return The formatted message if log logging is enabled for this log level (or if the level is ERROR or higher) or null otherwise<br>
	 * This message can be used e. g. as exception message if an exception is to be thrown after the log entry.
	 */
	public static String doLog(String logLevel, Class cls, String msg, Object [] args)
	{
		return doLog(logLevel, cls != null ? cls.getName() : null, msg, args);
	}

	/**
	 * Logs a message using an argument array.
	 * This method formats a string message together with an argument array
	 * using the standard {@link MsgFormat} mechanism, but adds extra
	 * processing for null arguments (which are formatted as [null]), and
	 * handles an extra exception that might be contained as last value
	 * in the argument array.
	 *
	 * @param logLevel Log level (priority)
	 * @param loggerName Logger name to be used for logger identification
	 * @param msg The message containing placeholder's like {0} etc.<br>
	 * Special convenience: Arguments that should appear in simple quotes ('\'') do not need to be
	 * provided as "...''{0}''..." in the log message, the shortcut "...$0..." can be used instead.
	 * If the "$" is followed by a non-digit character, it will be printed as it is.<br>
	 * This should eliminate the common error of forgetting a quote, which would confuse MessageFormat.
	 * @param args Array of message arguments<br>
	 * If the last parameter is a java.lang.Throwable, it will not be used as message argument, instead
	 * it will be passed to the underlying logger method as exception argument.
	 * @return The formatted message if log logging is enabled for this log level (or if the level is ERROR or higher) or null otherwise<br>
	 * This message can be used e. g. as exception message if an exception is to be thrown after the log entry.
	 */
	public static String doLog(String logLevel, String loggerName, String msg, Object [] args)
	{
		// TODO This is all Log4J-specific; should map to apache commons logging
		Logger logger = mapLogger(loggerName);
		Priority priority = mapPriority(logLevel);

		boolean enabled = true;
		if (!logger.isEnabledFor(priority))
		{
			// In case of errors, always generate the message; for any other types, exit if type disabled.
			if (logLevel != LogLevel.ERROR)
				return null;
			enabled = false;
		}

		Throwable throwable = null;

		if (args != null && args.length > 0)
		{
			boolean needFormatting = true;

			Object t = args [args.length - 1];
			if (t instanceof Throwable)
			{
				throwable = (Throwable) t;
				if (args.length == 1)
				{
					// The throwable was our only argument
					needFormatting = false;
				}
			}

			if (needFormatting)
			{
				// Replace null arguments with symbolic strings.
				for (int i = 0; i < args.length; i++)
				{
					if (args [i] == null)
					{
						args [i] = "(null)";
					}
				}

				try
				{
					// Format the mesage.
					msg = MsgFormat.format(msg, args);
				}
				catch (Exception e)
				{
					// Build a replacement messsage.
					String fMsg = "Logging error: Can't format message '" + msg + "' with " + args.length + " arguments.\n\tException:\n\t" + ExceptionUtil.getNestedTrace(e);

					// Enforce showing the replacement message (with ERROR level).
					mapLogger(LogUtil.class.getName()).error(fMsg);
				}
			}
		}

		// The message we have processed is identical for the logged and the returned message - so far
		String retMsg = msg;
		String logMsg = msg;

		// Append exception information to the message
		if (throwable != null)
		{
			// Append the exception messages to the returned message
			String exceptionMsg = ExceptionUtil.getNestedMessage(throwable);
			if (exceptionMsg != null)
			{
				retMsg += '\n';
				retMsg += exceptionMsg;
				if (enabled)
				{
					logMsg += '\n';
					logMsg += exceptionMsg;
				}
			}
		}

		// Write the log message if the logger is enabled
		if (enabled)
		{
			// Log4J doesn't convert simple newlines within the message to os-dependent newlines,
			// so we do it here.
			if (!LINE_SEPARATOR.equals("\n"))
			{
				logMsg = StringUtil.substitute(logMsg, "\n", LINE_SEPARATOR);
			}

			if (throwable != null)
			{
				logger.log(priority, logMsg, throwable);
			}
			else
			{
				logger.log(priority, logMsg);
			}
		}

		// Return the return message we have prepared
		return retMsg;
	}

	/**
	 * Maps the given class and log level to a particular logger instance.
	 *
	 * @param loggerName Name of the logger or null
	 * @return The native logger object
	 */
	private static Logger mapLogger(String loggerName)
	{
		Logger logger = null;
		if (loggerName == null)
		{
			if (rootLogger == null)
			{
				rootLogger = org.apache.log4j.Logger.getRootLogger();
			}
			logger = rootLogger;
		}
		else
		{
			logger = loggerMap.get(loggerName);
			if (logger == null)
			{
				logger = org.apache.log4j.Logger.getLogger(loggerName);
				loggerMap.put(loggerName, logger);
			}
		}
		return logger;
	}

	/**
	 * Maps the given log level to a Log4J priority.
	 *
	 * @param logLevel Log level
	 * @return The priority or null if no mappable
	 */
	private static Priority mapPriority(String logLevel)
	{
		Priority priority = null;

		if (LogLevel.DEBUG.equals(logLevel))
		{
			priority = Priority.DEBUG;
		}
		else if (LogLevel.TRACE.equals(logLevel))
		{
			if (traceInfo)
				priority = Priority.INFO;
			else
				priority = Priority.DEBUG;
		}
		else if (LogLevel.INFO.equals(logLevel))
		{
			priority = Priority.INFO;
		}
		else if (LogLevel.WARN.equals(logLevel))
		{
			priority = Priority.WARN;
		}
		else if (LogLevel.ERROR.equals(logLevel))
		{
			priority = Priority.ERROR;
		}
		else if (LogLevel.FATAL.equals(logLevel))
		{
			priority = Priority.FATAL;
		}

		return priority;
	}

	//////////////////////////////////////////////////
	// @@ Code guards
	//////////////////////////////////////////////////

	/**
	 * Checks if debug logging is enabled for the given logger.
	 *
	 * @param cls Class to be used for logger identification
	 * @nowarn
	 */
	public static boolean isDebugEnabled(Class cls)
	{
		return isLoggerEnabled(LogLevel.DEBUG, cls);
	}

	/**
	 * Checks if info logging is enabled for the given logger.
	 *
	 * @param cls Class to be used for logger identification
	 * @nowarn
	 */
	public static boolean isInfoEnabled(Class cls)
	{
		return isLoggerEnabled(LogLevel.INFO, cls);
	}

	/**
	 * Checks if trace logging is enabled for the given logger.
	 *
	 * @param cls Class to be used for logger identification
	 * @nowarn
	 */
	public static boolean isTraceEnabled(Class cls)
	{
		return isLoggerEnabled(LogLevel.TRACE, cls);
	}

	/**
	 * Checks if warn logging is enabled for the given logger.
	 *
	 * @param cls Class to be used for logger identification
	 * @nowarn
	 */
	public static boolean isWarnEnabled(Class cls)
	{
		return isLoggerEnabled(LogLevel.WARN, cls);
	}

	/**
	 * Checks if error logging is enabled for the given logger.
	 *
	 * @param cls Class to be used for logger identification
	 * @nowarn
	 */
	public static boolean isErrorEnabled(Class cls)
	{
		return isLoggerEnabled(LogLevel.ERROR, cls);
	}

	/**
	 * Checks if fatal logging is enabled for the given logger.
	 *
	 * @param cls Class to be used for logger identification
	 * @nowarn
	 */
	public static boolean isFatalEnabled(Class cls)
	{
		return isLoggerEnabled(LogLevel.FATAL, cls);
	}

	/**
	 * Checks if logging is enabled for the given log level for this logger.
	 *
	 * @param logLevel Log level
	 * @param cls Class to be used for logger identification
	 * @nowarn
	 */
	public static boolean isLoggerEnabled(String logLevel, Class cls)
	{
		return isLoggerEnabled(logLevel, cls != null ? cls.getName() : null);
	}

	/**
	 * Checks if logging is enabled for the given log level for this logger.
	 *
	 * @param logLevel Log level
	 * @param loggerName Name of the logger or null
	 * @nowarn
	 */
	public static boolean isLoggerEnabled(String logLevel, String loggerName)
	{
		Logger logger = mapLogger(loggerName);
		Priority priority = mapPriority(logLevel);
		return logger.isEnabledFor(priority);
	}

	//////////////////////////////////////////////////
	// @@ Log file name processing
	//////////////////////////////////////////////////

	/**
	 * This method handles a given log file name in two ways.
	 * First, if the file name is relative, it is extended by the root dir of the application.
	 * Second, the method tries to ensure that the directory the file resides in exists (by creating it).
	 *
	 * @param fileName The name of the log file (relative or absolute)
	 * @return The absolute name of the log file
	 */
	public static String handleLogFileName(String fileName)
	{
		File file = new File(fileName);

		// Check, whether file name is absolute
		if (!file.isAbsolute())
		{
			// Make it absolute by prepending the root dir, if defined
			String rootDir = Application.getRootDir();
			if (rootDir != null)
			{
				fileName = rootDir + StringUtil.FOLDER_SEP + fileName;
				file = new File(fileName);
			}
		}

		// Create necessary directories
		file.getParentFile().mkdirs();

		// Return the absolute file name
		return fileName;
	}
}
