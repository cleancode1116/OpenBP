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
 * The sub process node calls another process as sub process.
 *
 * @author Heiko Erhardt
 */
public interface SubprocessNode
	extends MultiSocketNode
{
	//////////////////////////////////////////////////
	// @@ Property access: Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the associated process.
	 * @nowarn
	 */
	public String getSubprocessName();

	/**
	 * Sets the name of the associated process.
	 * @nowarn
	 */
	public void setSubprocessName(String subprocessName);

	/**
	 * Gets the underlying process.
	 * @nowarn
	 */
	public ProcessItem getSubprocess();

	/**
	 * Sets the underlying process.
	 * @nowarn
	 */
	public void setSubprocess(ProcessItem subprocess);
}
