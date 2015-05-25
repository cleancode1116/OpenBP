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
package org.openbp.model.system.io;

import org.openbp.common.logger.LogLevel;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.model.ModelQualifier;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;

// {{*Custom imports*
// }}*Custom imports*

/**
 * Write log message.
 * Implementation of the WriteLogMessage activity handler.
 * Writes a message to the server's log file.
 * 
 * If no logger name has been specified, the logger name will be constructed from "Model."+(qualified name of the executing model), e. g. "Model.AddressBookDemo".
 * 
 * If no log level is given, the log level ERROR will be used.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'LogLevel': Log level
 *     Parameter 'Exception': Exception
 *     Parameter 'LogMessage': Log message
 *     Parameter 'LoggerName': Logger name
 * 
 * Output sockets/parameter:
 *   Socket 'Out'
 */
public class WriteLogMessageHandler
	// {{*Custom extends*
	// }}*Custom extends*
	// {{*Custom interfaces*
	implements Handler
	// }}*Custom interfaces*
{
	/** Parameter LogLevel */
	private static final String PARAM_LOGLEVEL = "LogLevel";

	/** Parameter Exception */
	private static final String PARAM_EXCEPTION = "Exception";

	/** Parameter LogMessage */
	private static final String PARAM_LOGMESSAGE = "LogMessage";

	/** Parameter LoggerName */
	private static final String PARAM_LOGGERNAME = "LoggerName";

	// {{*Custom constants*
	// }}*Custom constants*

	// {{*Custom members*
	// Note: If you define member variables, consider the fact that the same handler instance may be executed
	// by multiple threads in parallel, so you have to make sure that your implementation is thread safe.
	// In general, member variables should be defined for global-like data only.
	// }}*Custom members*

	/**
	 * Executes the handler.
	 *
	 * @param hc Handler context that contains execution parameters
	 * @return true if the handler handled the event, false to apply the default handling to the event
	 * @throws Exception Any exception that may occur during execution of the handler will be
	 * propagated to an exception handler if defined or abort the process execution otherwise.
	 */
	public boolean execute(HandlerContext hc)
		throws Exception
	{
		// {{*Handler implementation*
		String logLevel = (String) hc.getParam(PARAM_LOGLEVEL);
		Object exception = hc.getParam(PARAM_EXCEPTION);
		String logMessage = (String) hc.getParam(PARAM_LOGMESSAGE);
		String loggerName = (String) hc.getParam(PARAM_LOGGERNAME);

		// Determine the logger name and get the logger
		if (loggerName == null)
		{
			loggerName = "Model" + hc.getTokenContext().getExecutingModel().getQualifier().toString().replace(ModelQualifier.PATH_DELIMITER_CHAR, '.');
		}

		if (logLevel == null)
			logLevel = LogLevel.ERROR;

		Object [] args = null;
		if (exception != null)
		{
			args = new Object [] { exception };
			if (logMessage == null && exception instanceof Throwable)
				logMessage = ((Throwable) exception).getMessage();
		}
		if (logMessage != null)
		{
			LogUtil.log(logLevel, loggerName, logMessage, args);
		}

		return true;
		// }}*Handler implementation*
	}

	// {{*Custom methods*
	// }}*Custom methods*
}

