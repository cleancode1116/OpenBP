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

import java.util.Iterator;

import org.openbp.common.string.TextUtil;
import org.openbp.core.model.ContextNameUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.activity.ActivityParam;

/**
 * Standard implementation of a node parameter.
 *
 * @author Heiko Erhardt
 */
public class NodeParamImpl extends ParamImpl
	implements NodeParam
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Optional parameter flag */
	private boolean optional;

	/** Flag that controls the data link autoconnector */
	private int autoConnectorMode = AUTOCONNECTOR_DEFAULT;

	/** Parameter visibility flag */
	private boolean visible = true;

	/** Expression to evaluate (suitable for input params only) */
	private String expression;

	/** Geometry information (required by the Modeler) */
	private String geometry;

	/**
	 * Parameter value wizard.
	 * When a node is inserted in a process that contains input parameters having a parameter wizard specification,
	 * the modeler will display the parameter value wizard.
	 * This wizard provides to the user the opportunity to specify constant values for the parameters in question.<br>
	 * A typical example is the 'TypeName' parameters of the database select activities.
	 */
	private String paramValueWizard;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Socket the parameter belongs to (may not be null) */
	private transient NodeSocket socket;

	/** Name of the parameter for parameter value context access ("node.socket.param") */
	private transient String contextName;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public NodeParamImpl()
	{
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		NodeParamImpl src = (NodeParamImpl) source;

		optional = src.optional;
		autoConnectorMode = src.autoConnectorMode;
		visible = src.visible;
		expression = src.expression;
		geometry = src.geometry;
		paramValueWizard = src.paramValueWizard;

		socket = src.socket;
	}

	/**
	 * Copy between node parameter and activity parameter.
	 * Copies all data values that can be mapped between the two types.
	 *
	 * @param activityParam Activity to copy from
	 */
	public void copyFromActivityParam(ActivityParam activityParam)
	{
		// Copy DisplayObject properties
		setName(activityParam.getName());
		setDescription(activityParam.getDescription());
		setDisplayName(activityParam.getDisplayName());
		setParamValueWizard(activityParam.getParamValueWizard());
		setAutoConnectorMode(activityParam.getAutoConnectorMode());
		setVisible(activityParam.isVisible());

		setTypeName(activityParam.getTypeName());
		setExpression(activityParam.getDefaultValue());
		optional = activityParam.isOptional();
		setDataType(activityParam.getDataType());
	}

	/**
	 * Copy between node parameter and activity parameter.
	 * Copies all data values that can be mapped between the two types.
	 *
	 * @param activityParam Activity Param to copy to
	 */
	public void copyToActivityParam(ActivityParam activityParam)
	{
		// Copy DisplayObject properties
		activityParam.setName(getName());
		activityParam.setDescription(getDescription());
		activityParam.setDisplayName(getDisplayName());
		activityParam.setParamValueWizard(getParamValueWizard());
		activityParam.setAutoConnectorMode(getAutoConnectorMode());
		activityParam.setVisible(isVisible());

		activityParam.setTypeName(getTypeName());
		activityParam.setDefaultValue(getExpression());
		activityParam.setOptional(optional);
		activityParam.setDataType(getDataType());
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		return ModelObjectSymbolNames.NODE_PARAM;
	}

	//////////////////////////////////////////////////
	// @@ DisplayObject overrides
	//////////////////////////////////////////////////

	/**
	 * Updates the context name in addition to the name change.
	 * @nowarn
	 */
	public void setName(String name)
	{
		super.setName(name);

		contextName = null;
	}

	/**
	 * Gets the reference to the object.
	 * @return The qualified name
	 */
	public ModelQualifier getQualifier()
	{
		return new ModelQualifier(getSocket().getNode().getProcess(), getSocket().getNode().getName() + ModelQualifier.OBJECT_DELIMITER + getSocket().getName() + ModelQualifier.OBJECT_DELIMITER + getName());
	}

	//////////////////////////////////////////////////
	// @@ ModelObject overrides
	//////////////////////////////////////////////////

	/**
	 * Gets text that can be used to display this object.
	 *
	 * @nowarn
	 */
	public String getDisplayText()
	{
		String text = getDisplayName();
		if (text == null)
		{
			text = getName();
		}

		if (expression != null)
		{
			String ex = expression;
			int l = ex.length();
			if (l > 40)
			{
				ex = ex.substring(0, 30) + "..." + ex.substring(l - 10);
			}
			if (text != null)
				text += "\n(" + ex + ")";
			else
				text = "(" + ex + ")";
		}

		return text != null ? text : "";
	}

	/**
	 * Gets the container object (i. e. the parent) of this object.
	 *
	 * @return The container object or null if this object doesn't have a container.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public ModelObject getContainer()
	{
		return socket;
	}

	/**
	 * Gets an iterator of the children of the container this object belongs to.
	 * This can be used to check on name clashes between objects of this type.
	 * By default, the method returns null.
	 *
	 * @return The iterator if this object is part of a collection or a map.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public Iterator getContainerIterator()
	{
		return socket.getParams();
	}

	//////////////////////////////////////////////////
	// @@ ProcessObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the process the object belongs to.
	 * @nowarn
	 */
	public ProcessItem getProcess()
	{
		if (socket != null && socket.getNode() != null)
		{
			return socket.getNode().getProcess();
		}
		return null;
	}

	/**
	 * Gets the partially qualified name of the object relative to the process.
	 * @nowarn
	 */
	public String getProcessRelativeName()
	{
		return socket != null ? socket.getProcessRelativeName() + ModelQualifier.OBJECT_DELIMITER + getName() : getName();
	}

	/**
	 * Gets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getGeometry()
	{
		return geometry;
	}

	/**
	 * Sets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setGeometry(String geometry)
	{
		this.geometry = geometry;
	}

	/**
	 * Gets the parameter value wizard.
	 * This information is used by the Modeler.
	 * @nowarn
	 */
	public String getParamValueWizard()
	{
		return paramValueWizard;
	}

	/**
	 * Sets the parameter value wizard.
	 * This information is used by the Modeler.
	 * @nowarn
	 */
	public void setParamValueWizard(String paramValueWizard)
	{
		this.paramValueWizard = paramValueWizard;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the optional parameter flag.
	 * @nowarn
	 */
	public boolean isOptional()
	{
		return optional;
	}

	/**
	 * Determines if the optional parameter flag is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasOptional()
	{
		return optional;
	}

	/**
	 * Sets the optional parameter flag.
	 * @nowarn
	 */
	public void setOptional(boolean optional)
	{
		this.optional = optional;
	}

	/**
	 * Gets the flag that controls the data link autoconnector.
	 * @return {@link NodeParam#AUTOCONNECTOR_OFF}/{@link NodeParam#AUTOCONNECTOR_DEFAULT}
	 */
	public int getAutoConnectorMode()
	{
		return autoConnectorMode;
	}

	/**
	 * Sets the flag that controls the data link autoconnector.
	 * @param autoConnectorMode {@link NodeParam#AUTOCONNECTOR_OFF}/{@link NodeParam#AUTOCONNECTOR_DEFAULT}
	 */
	public void setAutoConnectorMode(int autoConnectorMode)
	{
		this.autoConnectorMode = autoConnectorMode;
	}

	/**
	 * Checks the string value of the flag that controls the data link autoconnector.
	 * @nowarn
	 */
	public boolean hasAutoConnectorModeString()
	{
		return autoConnectorMode != AUTOCONNECTOR_DEFAULT;
	}

	/**
	 * Gets the string value of the flag that controls the data link autoconnector.
	 * @nowarn
	 */
	public String getAutoConnectorModeString()
	{
		if (autoConnectorMode == AUTOCONNECTOR_DEFAULT)
			return null;
		return "off";
	}

	/**
	 * Sets the string value of the flag that controls the data link autoconnector.
	 * @nowarn
	 */
	public void setAutoConnectorModeString(String autoConnectorModeString)
	{
		if ("off".equals(autoConnectorModeString))
		{
			autoConnectorMode = AUTOCONNECTOR_OFF;
		}
		else
		{
			autoConnectorMode = AUTOCONNECTOR_DEFAULT;
		}
	}

	/**
	 * Checks if the parameter visibility flag is present.
	 * @nowarn
	 */
	public boolean hasVisible()
	{
		return !visible;
	}

	/**
	 * Gets the parameter visibility flag.
	 * @nowarn
	 */
	public boolean isVisible()
	{
		return visible;
	}

	/**
	 * Sets the parameter visibility flag.
	 * @nowarn
	 */
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

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
	public String getExpression()
	{
		return expression;
	}

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
	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	/**
	 * Gets the escape representation of the expression to evaluate.
	 * In the escape representation, a newline escape ("\n") has been added before any newline character.
	 * Tab characters are replaced by the tab escape ("\t").
	 * (we need this in order to remove spaces inserted by Castor when serializing
	 * multi-line text content)
	 * @nowarn
	 */
	public String getExpressionEscape()
	{
		return TextUtil.encodeMultiLineString(expression);
	}

	/**
	 * Sets the escape representation of the expression to evaluate.
	 * In the escape representation, a newline escape ("\n") has been added before any newline character.
	 * Tab characters are replaced by the tab escape ("\t").
	 * (we need this in order to remove spaces inserted by Castor when serializing
	 * multi-line text content)
	 * @nowarn
	 */
	public void setExpressionEscape(String expression)
	{
		this.expression = TextUtil.decodeMultiLineString(expression);
	}

	/**
	 * Gets the socket the parameter belongs to.
	 * @nowarn
	 */
	public NodeSocket getSocket()
	{
		return socket;
	}

	/**
	 * Sets the socket the parameter belongs to.
	 * @nowarn
	 */
	public void setSocket(NodeSocket socket)
	{
		this.socket = socket;

		contextName = null;
	}

	/**
	 * Gets the name of the parameter for parameter value context access ("node.socket.param").
	 * @nowarn
	 */
	public String getContextName()
	{
		if (contextName == null)
		{
			contextName = ContextNameUtil.constructContextName(this);
		}

		return contextName;
	}
}
