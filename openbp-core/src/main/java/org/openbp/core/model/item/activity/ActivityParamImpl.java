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

import java.util.Iterator;

import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelObjectImpl;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * Implementation of an activity parameter.
 *
 * @author Heiko Erhardt
 */
public class ActivityParamImpl extends ModelObjectImpl
	implements ActivityParam
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Type name of the parameter */
	private String typeName;

	/** Default value of the parameter */
	private String defaultValue;

	/** Optional parameter flag */
	private boolean optional;

	/**
	 * Parameter value wizard.
	 * When a node is inserted in a process that contains input parameters having a parameter wizard specification,
	 * the modeler will display the parameter value wizard.
	 * This wizard provides the user the opportunity to specify constant values for the parameters in question.<br>
	 * A typical example is the 'TypeName' parameters of the database select activities.
	 */
	private String paramValueWizard;

	/** Flag that controls the data link autoconnector */
	private int autoConnectorMode = NodeParam.AUTOCONNECTOR_DEFAULT;

	/** Parameter visibility flag */
	private boolean visible = true;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Socket the parameter belongs to (may not be null) */
	private transient ActivitySocket socket;

	/** Data type associated with the parameter */
	private transient DataTypeItem dataType;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ActivityParamImpl()
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

		ActivityParamImpl src = (ActivityParamImpl) source;

		typeName = src.typeName;
		defaultValue = src.defaultValue;
		optional = src.optional;
		paramValueWizard = src.paramValueWizard;
		autoConnectorMode = src.autoConnectorMode;
		visible = src.visible;

		socket = src.socket;
		dataType = src.dataType;
	}

	//////////////////////////////////////////////////
	// @@ ModelObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the model the item belongs to.
	 * @nowarn
	 */
	public Model getOwningModel()
	{
		return socket != null ? socket.getOwningModel() : null;
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

	/**
	 * Gets the reference to the object.
	 * @return The qualified name
	 */
	public ModelQualifier getQualifier()
	{
		return new ModelQualifier(getSocket().getActivity(), getSocket().getName() + ModelQualifier.OBJECT_DELIMITER + getName());
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.maintainReferences
	 */
	public void maintainReferences(int flag)
	{
		super.maintainReferences(flag);

		if (getSocket() != null)
		{
			if ((flag & RESOLVE_GLOBAL_REFS) != 0)
			{
				// Resolve the base type
				try
				{
					dataType = (DataTypeItem) getSocket().getActivity().resolveItemRef(typeName, ItemTypes.TYPE);
				}
				catch (ModelException e)
				{
					getModelMgr().getMsgContainer().addMsg(this, "Cannot resolve parameter type $0.", new Object[]
					{
						typeName, e
					});
				}
			}

			if ((flag & SYNC_GLOBAL_REFNAMES) != 0)
			{
				if (dataType != null)
				{
					typeName = getSocket().getActivity().determineItemRef(dataType);
				}
			}
		}
	}

	/**
	 * @copy ModelObject.validate
	 */
	public boolean validate(int flag)
	{
		// Check for an object name first
		boolean success = super.validate(flag);

		if (typeName == null)
		{
			getModelMgr().getMsgContainer().addMsg(this, "No type name of the parameter specified.");
			success = false;
		}

		return success;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the type name of the parameter.
	 * @nowarn
	 */
	public String getTypeName()
	{
		return typeName;
	}

	/**
	 * Sets the type name of the parameter.
	 * @nowarn
	 */
	public void setTypeName(String typeName)
	{
		this.typeName = typeName;
	}

	/**
	 * Gets the default value of the parameter.
	 * @nowarn
	 */
	public String getDefaultValue()
	{
		return defaultValue;
	}

	/**
	 * Sets the default value of the parameter.
	 * @nowarn
	 */
	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

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
		return autoConnectorMode != NodeParam.AUTOCONNECTOR_DEFAULT;
	}

	/**
	 * Gets the string value of the flag that controls the data link autoconnector.
	 * @nowarn
	 */
	public String getAutoConnectorModeString()
	{
		if (autoConnectorMode == NodeParam.AUTOCONNECTOR_DEFAULT)
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
			autoConnectorMode = NodeParam.AUTOCONNECTOR_OFF;
		}
		else
		{
			autoConnectorMode = NodeParam.AUTOCONNECTOR_DEFAULT;
		}
	}

	/**
	 * Checks if the parameter visibility flag is present.
	 * @nowarn
	 */
	public boolean hasVisible()
	{
		return ! visible;
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
	 * Gets the data type associated with the parameter.
	 * @nowarn
	 */
	public DataTypeItem getDataType()
	{
		return dataType;
	}

	/**
	 * Sets the data type associated with the parameter.
	 * @nowarn
	 */
	public void setDataType(DataTypeItem dataType)
	{
		this.dataType = dataType;
	}

	/**
	 * Gets the socket the parameter belongs to (may not be null).
	 * @nowarn
	 */
	public ActivitySocket getSocket()
	{
		return socket;
	}

	/**
	 * Sets the socket the parameter belongs to (may not be null).
	 * @nowarn
	 */
	public void setSocket(ActivitySocket socket)
	{
		this.socket = socket;
	}
}
