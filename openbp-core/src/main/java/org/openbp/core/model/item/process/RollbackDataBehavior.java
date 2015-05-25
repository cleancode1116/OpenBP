/*
 *   Copyright 2009 skynamics AG
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
package org.openbp.core.model.item.process;

/**
 * Enumeration class for rollback data behavior specification.
 *
 * @author Heiko Erhardt
 */
public final class RollbackDataBehavior
{
	/** Update process variables */
	public static final int UPDATE_VARIABLES = 0;

	/** Add new variables|add-variables */
	public static final int ADD_VARIABLES = 1;

	/** Restore process variables */
	public static final int RESTORE_VARIABLES= 2;

	/**
	 * Private constructor prevents instantiation.
	 */
	private RollbackDataBehavior()
	{
	}
}
