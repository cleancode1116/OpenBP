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
package org.openbp.server;

/**
 * Server-specific constants.
 *
 * @author Heiko Erhardt
 */
public interface ServerConstants
{
	//////////////////////////////////////////////////
	// @@ Sytem properties
	//////////////////////////////////////////////////

	/* System property: Flag that determines if accessing an undefined process variable should cause an exception */
	public static final String SYSPROP_PROCESSVARIABLEHANDLING_STRICT = "openbp.processVariableHandling.strict";

	/* System property: Timeout of the server session in sec.
	 * Determines when a token context is removed from the {@link SessionRegistry} 0 for no timeout (default: 0)
	 */
	public static final String SYSPROP_SERVERSESSION_TIMEOUT = "openbp.serverSession.timeout";

	/* System property: Timeout of the client session in sec.
	 * Determines when the connection between the OpenBP engine and the OpenBP modeler will be closed. 0 for no timeout (default: 24 hours)
	 */
	public static final String SYSPROP_CLIENTSESSION_TIMEOUT = "openbp.clientSession.timeout";

	/* System property: Specifies the behaviour in case of an RMI binding error at the server side.
	 * RMI is being used for the connection between the OpenBP engine and the OpenBP Cockpit.
	 *
	 * ignore: Binding errors will be silently ignored.
	 * This mode will be used when the engine is being run in a production system that does not allow RMI access to the engine.<br>
	 * output: Prints an error message to System.err<br>
	 * All other values or en empty value will cause an exception to be thrown on binding errors.
	 */
	public static final String SYSPROP_RMIBINDINGERRORHANDLING = "openbp.rmiBindingErrorHandling";

	/** System for test cases: Name of the OpenBP.spring.xml file */
	public static final String SYSPROP_SPRING_CONFIG_FILE = "openbp.springConfigFile";

	/** System for test cases: Wait for modeler to connect to server */
	public static final String SYSPROP_WAIT_FOR_MODELER_PROPERTY = "openbp.waitForModeler";

	/** System for test cases: Stop at first process step */
	public static final String SYSPROP_DEBUGGER_CONTROL = "openbp.debuggerControl";
}
