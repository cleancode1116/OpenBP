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
import java.util.List;

import org.openbp.common.CollectionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.common.util.SortingArrayList;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.MimeTypes;
import org.openbp.core.model.Association;
import org.openbp.core.model.AssociationUtil;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * Standard implementation of a parameter.
 *
 * @author Heiko Erhardt
 */
public abstract class ParamImpl extends ProcessObjectImpl
	implements Param
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Type name of the parameter (may not be null) */
	private String typeName;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Data type associated with the parameter */
	private transient DataTypeItem dataType;

	/** Data links that are connected to the parameter (contains {@link DataLink} objects) */
	private transient List dataLinkList;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ParamImpl()
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

		ParamImpl src = (ParamImpl) source;

		typeName = src.typeName;

		if (copyMode == Copyable.COPY_DEEP)
		{
			// We assume we're doing a clone here.
			// References will be rebuilt after the clone,
			// so clear the data link references
			dataLinkList = null;
		}
		else
		{
			dataLinkList = src.dataLinkList;
		}

		dataType = src.dataType;
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
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.maintainReferences
	 */
	public void maintainReferences(int flag)
	{
		super.maintainReferences(flag);

		if (getProcess() != null)
		{
			if ((flag & RESOLVE_GLOBAL_REFS) != 0)
			{
				// Resolve the base type
				try
				{
					dataType = (DataTypeItem) getProcess().resolveItemRef(typeName, ItemTypes.TYPE);
				}
				catch (ModelException e)
				{
					getModelMgr().getMsgContainer().addMsg(this, "Cannot resolve parameter type $0.", new Object [] { typeName, e });
				}
			}

			if ((flag & SYNC_GLOBAL_REFNAMES) != 0)
			{
				if (dataType != null)
				{
					typeName = getProcess().determineItemRef(dataType);
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
	// @@ Associations
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.getAssociations
	 */
	public List getAssociations()
	{
		List associations = null;

		associations = AssociationUtil.addAssociation(associations, -1, "Data Type", dataType, this, new String [] { MimeTypes.ITEM }, Association.NORMAL, "There is no data type associated with this parameter.");

		if (dataType != null)
		{
			associations = AssociationUtil.addAssociations(associations, -1, dataType.getAssociations());
		}

		return associations;
	}

	//////////////////////////////////////////////////
	// @@ Info text
	//////////////////////////////////////////////////

	/**
	 * Gets informational text about the object.
	 * The text can be used to e. g. display a tool tip that describes the object.
	 *
	 * @return An array of strings that make up the information text.<br>
	 * Each array element corresponds to a paragraph that should be displayed.
	 * A paragraph may contain newline ('\n') and tab ('\t') characters that
	 * should be interpreted by the user interface.
	 */
	public String [] getInfoText()
	{
		String headLine = getName();
		if (typeName != null)
		{
			headLine += " : ";
			headLine += typeName;
		}

		String dt = getDisplayText();
		if (dt != null && dt.equals(getName()))
			dt = null;

		return createInfoText(headLine, dt, getDescriptionText());
	}

	/**
	 * Gets text that can be used to display this object.
	 *
	 * @return The display text (should usually not be null)
	 */
	public String getDisplayText()
	{
		String text = super.getDisplayText();
		if (text == null && dataType != null)
		{
			// Use the underlying data type's display text
			text = dataType.getDisplayName();
		}
		if (text == null)
		{
			text = getName();
		}
		return text;
	}

	/**
	 * Gets text that describes the object.
	 * This can be the regular description (getDescription method) of the object
	 * or the description of an underlying object.
	 *
	 * @return The description text or null if there is no description
	 */
	public String getDescriptionText()
	{
		String text = super.getDescription();
		if (text == null && dataType != null)
		{
			// Use the underlying data type's display text
			text = dataType.getDescription();
		}
		return text;
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
	 * Gets the data links that are connected to the parameter.
	 * @return An iterator of {@link DataLink} objects
	 */
	public Iterator getDataLinks()
	{
		if (dataLinkList == null)
			return EmptyIterator.getInstance();
		return dataLinkList.iterator();
	}

	/**
	 * Adds a data link.
	 * @param dataLink The data link to add
	 */
	public void addDataLink(DataLink dataLink)
	{
		if (dataLinkList == null)
		{
			dataLinkList = new SortingArrayList();
			dataLinkList.add(dataLink);
		}
		else
		{
			if (!dataLinkList.contains(dataLink))
			{
				dataLinkList.add(dataLink);
			}
		}

		dataLink.setProcess(getProcess());
	}

	/**
	 * Removes a data link.
	 * @param dataLink The data link to remove
	 */
	public void removeDataLink(DataLink dataLink)
	{
		CollectionUtil.removeReference(dataLinkList, dataLink);
	}

	/**
	 * Gets a data link by its name.
	 *
	 * @param name Name of the dataLink
	 * @return The data link or null if no such data link exists
	 */
	public DataLink getDataLinkByName(String name)
	{
		return dataLinkList != null ? (DataLink) NamedObjectCollectionUtil.getByName(dataLinkList, name) : null;
	}

	/**
	 * Clears the data links that are connected to the parameter.
	 */
	public void clearDataLinks()
	{
		dataLinkList = null;
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
}
