/*
 *   Copyright 2008 skynamics AG
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
package org.openbp.server.persistence;

/**
 * Persistent object not found exception.
 *
 * @author Heiko Erhardt
 */
public class PersistentObjectNotFoundException
	extends PersistenceException
{
	/**
	 * Constructor.
	 * @param msg Exception message
	 */
	public PersistentObjectNotFoundException(String msg)
	{
		super(msg);
	}

	/**
	 * Constructor.
	 * @param exception Nested exception<br>
	 * This might be e. g. a java.sql.SQLException
	 */
	public PersistentObjectNotFoundException(Throwable exception)
	{
		super(exception);
	}

	/**
	 * Constructor.
	 * @param msg Exception message
	 * @param exception Nested exception
	 * This might be e. g. a java.sql.SQLException
	 */
	public PersistentObjectNotFoundException(String msg, Throwable exception)
	{
		super(msg, exception);
	}
}
