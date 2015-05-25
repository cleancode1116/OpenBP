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

import org.openbp.core.model.item.activity.ActivityParam;

/**
 * Param of a node socket.
 * The parameter contains the same data as an activity parameter, but may have a data link connected to it.
 *
 * @author Heiko Erhardt
 */
public interface NodeParam
	extends Param
{
	//////////////////////////////////////////////////
	// @@ Flags
	//////////////////////////////////////////////////

	/** Autoconnector mode: Off */
	public static final int AUTOCONNECTOR_OFF = 0;

	/** Autoconnector mode: Default */
	public static final int AUTOCONNECTOR_DEFAULT = 1;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Copy between node parameter and activity parameter.
	 * Copies all data values that can be mapped between the two types.
	 *
	 * @param activityParam Activity to copy from
	 */
	public void copyFromActivityParam(ActivityParam activityParam);

	/**
	 * Copy between node parameter and activity parameter.
	 * Copies all data values that can be mapped between the two types.
	 *
	 * @param activityParam Activity Param to copy to
	 */
	public void copyToActivityParam(ActivityParam activityParam);

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the optional parameter flag.
	 * @nowarn
	 */
	public boolean isOptional();

	/**
	 * Sets the optional parameter flag.
	 * @nowarn
	 */
	public void setOptional(boolean optional);

	/**
	 * Gets the flag that controls the data link autoconnector.
	 * @return {@link #AUTOCONNECTOR_OFF}/{@link #AUTOCONNECTOR_DEFAULT}
	 */
	public int getAutoConnectorMode();

	/**
	 * Sets the flag that controls the data link autoconnector.
	 * @param autoConnectorMode {@link #AUTOCONNECTOR_OFF}/{@link #AUTOCONNECTOR_DEFAULT}
	 */
	public void setAutoConnectorMode(int autoConnectorMode);

	/**
	 * Gets the parameter visibility flag.
	 * @nowarn
	 */
	public boolean isVisible();

	/**
	 * Sets the parameter visibility flag.
	 * @nowarn
	 */
	public void setVisible(boolean visible);

	/**
	 * Gets the expression to evaluate.
	 * The expression of a node input parameter will be evaluated before the node
	 * is being executed, the expression of a node output parameter will be evaluated
	 * after the node has been executed.
	 * The expression usually contains constants, but may also
	 * contain references to the current node parameters or other node parameters
	 * (so the expression context is the set of node parameters of the current node socket).
	 *
	 * @nowarn
	 */
	public String getExpression();

	/**
	 * Gets the expression to evaluate.
	 * The expression of a node input parameter will be evaluated before the node
	 * is being executed, the expression of a node output parameter will be evaluated
	 * after the node has been executed.
	 * The expression usually contains constants, but may also
	 * contain references to the current node parameters or other node parameters
	 * (so the expression context is the set of node parameters of the current node socket).
	 *
	 * @nowarn
	 */
	public void setExpression(String expression);

	/**
	 * Gets the socket the parameter belongs to.
	 * @nowarn
	 */
	public NodeSocket getSocket();

	/**
	 * Sets the socket the parameter belongs to.
	 * @nowarn
	 */
	public void setSocket(NodeSocket socket);

	/**
	 * Gets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getGeometry();

	/**
	 * Sets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setGeometry(String geometry);

	/**
	 * Gets the parameter value wizard.
	 * This information is used by the Modeler.
	 * @nowarn
	 */
	public String getParamValueWizard();

	/**
	 * Sets the parameter value wizard.
	 * This information is used by the Modeler.
	 * @nowarn
	 */
	public void setParamValueWizard(String paramValueWizard);
}
