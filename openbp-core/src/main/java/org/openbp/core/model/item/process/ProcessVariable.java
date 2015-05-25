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
package org.openbp.core.model.item.process;

/**
 * Process variable.
 *
 * @author Heiko Erhardt
 */
public interface ProcessVariable
	extends Param
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/**
	 * Variable scope: Process-local
     * The process variable will be removed from the token context automatically when the process defining them returns
	 * using a final node (as long as the process has not been called as a subprocess from within itself).&lt;/li&gt;
	 */
	public static final int SCOPE_PROCESS = 1;

	/**
	 * Variable scope: Context
     * The process variable is not removed from the token context.
	 */
	public static final int SCOPE_CONTEXT = 2;

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the scope of the process variable.
	 * @return {@link #SCOPE_PROCESS}/{@link #SCOPE_CONTEXT}
	 */
	public int getScope();

	/**
	 * Sets the scope of the process variable.
	 * @param scope {@link #SCOPE_PROCESS}/{@link #SCOPE_CONTEXT}
	 */
	public void setScope(int scope);

	/**
	 * Gets the automatical assignment flag.
	 * @nowarn
	 */
	public boolean isAutoAssign();

	/**
	 * Sets the automatical assignment flag.
	 * @nowarn
	 */
	public void setAutoAssign(boolean autoAssign);

	/**
	 * Gets the root context flag.
	 * @nowarn
	 */
	public boolean isRootContextVariable();

	/**
	 * Sets the root context flag.
	 * @nowarn
	 */
	public void setRootContextVariable(boolean rootContextVariable);

	/**
	 * Gets the persistent variable property.
	 * @nowarn
	 */
	public boolean isPersistentVariable();

	/**
	 * Sets the persistent variable property.
	 * @nowarn
	 */
	public void setPersistentVariable(boolean persistentVariable);

	/**
	 * Gets the process the parameter belongs to (may not be null).
	 * @nowarn
	 */
	public ProcessItem getProcess();

	/**
	 * Sets the process the parameter belongs to (may not be null).
	 * @nowarn
	 */
	public void setProcess(ProcessItem process);
}
