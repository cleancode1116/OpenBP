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

/**
 * Value interfaces that defines standard log levels.
 * Note that log levels are strings, providing the flexibility to define custom log levels.
 *
 * @author Heiko Erhardt
 */
public interface LogLevel
{
	/**
	 * Log level 'Debug'.
	 */
	public static final String DEBUG = "Debug";

	/**
	 * Log level 'Trace'.
	 */
	public static final String TRACE = "Trace";

	/**
	 * Log level 'Info'.
	 */
	public static final String INFO = "Info";

	/**
	 * Log level 'Warn'.
	 */
	public static final String WARN = "Warn";

	/**
	 * Log level 'Error'.
	 */
	public static final String ERROR = "Error";

	/**
	 * Log level 'Fatal'.
	 */
	public static final String FATAL = "Fatal";
}
