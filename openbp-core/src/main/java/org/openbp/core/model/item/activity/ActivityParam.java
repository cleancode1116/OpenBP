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
package org.openbp.core.model.item.activity;

import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * An activity parameter defines a parameter of an activity socket.
 *
 * @author Heiko Erhardt
 */
public interface ActivityParam
	extends ModelObject
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the type name of the parameter.
	 * @nowarn
	 */
	public String getTypeName();

	/**
	 * Sets the type name of the parameter.
	 * @nowarn
	 */
	public void setTypeName(String typeName);

	/**
	 * Gets the default value of the parameter.
	 * @nowarn
	 */
	public String getDefaultValue();

	/**
	 * Sets the default value of the parameter.
	 * @nowarn
	 */
	public void setDefaultValue(String defaultValue);

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

	/**
	 * Gets the flag that controls the data link autoconnector.
	 * @return {@link NodeParam#AUTOCONNECTOR_OFF}/{@link NodeParam#AUTOCONNECTOR_DEFAULT}
	 */
	public int getAutoConnectorMode();

	/**
	 * Sets the flag that controls the data link autoconnector.
	 * @param autoConnectorMode {@link NodeParam#AUTOCONNECTOR_OFF}/{@link NodeParam#AUTOCONNECTOR_DEFAULT}
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
	 * Gets the data type associated with the parameter.
	 * @nowarn
	 */
	public DataTypeItem getDataType();

	/**
	 * Sets the data type associated with the parameter.
	 * @nowarn
	 */
	public void setDataType(DataTypeItem dataType);

	/**
	 * Gets the socket the parameter belongs to (may not be null).
	 * @nowarn
	 */
	public ActivitySocket getSocket();

	/**
	 * Sets the socket the parameter belongs to (may not be null).
	 * @nowarn
	 */
	public void setSocket(ActivitySocket socket);
}
