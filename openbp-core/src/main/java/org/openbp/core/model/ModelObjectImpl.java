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
package org.openbp.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openbp.common.CommonUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.generic.taggedvalue.TaggedValue;
import org.openbp.common.string.TextUtil;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.SortingArrayList;
import org.openbp.common.util.ToStringHelper;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.CoreResources;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.modelmgr.ModelMgr;

/**
 * This class is the base class for most OpenBP model objects.
 * A model object is an item or a part of an item
 * (e.g. a node, a link, a socket, a data type or data member etc.).
 *
 * A model object may contain tagged values, which are saved to persistent
 * storage with the model object.
 *
 * @author Heiko Erhardt
 */
public abstract class ModelObjectImpl extends DisplayObjectImpl
	implements ModelObject
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Maximum line length of an info text line in characters */
	public static final int INFO_TEXT_MAX_LINE_LENGTH = 50;

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Design time attribute table */
	protected Map designTimeAttributes = new HashMap();

	/** Tagged value list (contains {@link TaggedValue} objects) */
	private List taggedValueList;

	/** Temporary reference (used by the modeler) */
	private transient Object tmpReference;

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Returns a string represenation of this object.
	 *
	 * @return Debug string containing the most important properties of this object
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "qualifier");
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

		ModelObjectImpl src = (ModelObjectImpl) source;

		if (copyMode == Copyable.COPY_FIRST_LEVEL || copyMode == Copyable.COPY_DEEP)
		{
			// Create deep clones of collection members
			taggedValueList = (List) CopyUtil.copyCollection(src.taggedValueList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES
				: CopyUtil.CLONE_NONE);

			designTimeAttributes = new HashMap();
			if (src.designTimeAttributes != null)
			{
				for (Iterator it = src.designTimeAttributes.entrySet().iterator(); it.hasNext();)
				{
					Map.Entry entry = (Map.Entry) it.next();
					setDesignTimeAttribute((String) entry.getKey(), entry.getValue());
				}
			}
		}
		else
		{
			// Shallow clone
			taggedValueList = src.taggedValueList;
			designTimeAttributes = src.designTimeAttributes;
		}
	}

	/**
	 * Creates a clone of this object.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained members failed
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		// This clone will invoke copyFrom()
		ModelObjectImpl clone = (ModelObjectImpl) super.clone();

		// Make sure all subordinate objects of this object refer to this object
		clone.maintainReferences(0);

		// Don't copy the reference
		clone.tmpReference = null;

		return clone;
	}

	/**
	 * Gets the model the item belongs to.
	 * @nowarn
	 */
	public abstract Model getOwningModel();

	/**
	 * Gets the model manager that loaded this model.
	 * @nowarn
	 */
	public ModelMgr getModelMgr()
	{
		return getOwningModel().getModelMgr();
	}

	/**
	 * Gets the children (direct subordinates) of this object.
	 * By default, the method returns null.
	 *
	 * @return The list of objects or null if the object does not have children.
	 * Note that the list may contain objects of different type.
	 */
	public List getChildren()
	{
		return null;
	}

	/**
	 * Gets the container object (i. e. the parent) of this object.
	 *
	 * @return The container object or null if this object doesn't have a container.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public abstract ModelObject getContainer();

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
		ModelObject container = getContainer();

		if (container != null)
		{
			List children = container.getChildren();
			if (children != null)
				return children.iterator();
		}

		return null;
	}

	/**
	 * Gets the read-only property.
	 * @nowarn
	 */
	public boolean isReadOnly()
	{
		return false;
	}

	/**
	 * Checks if this object can be modified (i\.e\. if itself and its model is not read-only).
	 * @nowarn
	 */
	public boolean isModifiable()
	{
		Model model = getOwningModel();
		if (model != null && model != this && model.isReadOnly())
			return false;

		return ! isReadOnly();
	}

	/**
	 * Gets the printable name of the object.
	 * @return The printable name ("EBusiness.Basket:Order")
	 */
	public String getPrintName()
	{
		return getQualifier().toString();
	}

	//////////////////////////////////////////////////
	// @@ Design time attribute support implementation
	//////////////////////////////////////////////////

	/**
	 * Gets a design time attribute value.
	 *
	 * @param key Name of the attribute
	 * @return The value of the attribute or null if no such attribute exists
	 */
	public Object getDesignTimeAttribute(String key)
	{
		if (designTimeAttributes != null)
			return designTimeAttributes.get(key);
		return null;
	}

	/**
	 * Sets a design time attribute value.
	 *
	 * @param key Name of the attribute
	 * @param value Value of the attribute
	 */
	public void setDesignTimeAttribute(String key, Object value)
	{
		if (designTimeAttributes == null)
		{
			designTimeAttributes = new LinkedHashMap();
		}
		designTimeAttributes.put(key, value);
	}

	/**
	 * Removes a design time attribute.
	 *
	 * @param key Name of the attribute
	 */
	public void removeDesignTimeAttribute(String key)
	{
		if (designTimeAttributes != null)
		{
			designTimeAttributes.remove(key);
			if (designTimeAttributes.isEmpty())
			{
				designTimeAttributes = null;
			}
		}
	}

	/**
	 * Gets the design time attribute list.
	 * @return A list of {@link TaggedValue} objects or null
	 */
	public List getDesignTimeAttributeList()
	{
		List ret = null;
		if (designTimeAttributes != null)
		{
			ret = new SortingArrayList();
			for (Iterator it = designTimeAttributes.entrySet().iterator(); it.hasNext();)
			{
				Map.Entry entry = (Map.Entry) it.next();
				TaggedValue tv = new TaggedValue();
				tv.setName((String) entry.getKey());
				tv.setValue((String) entry.getValue());
				ret.add(tv);
			}
		}
		return ret;
	}

	/**
	 * Adds a design time attribute.
	 * @param designTimeAttribute Attribute to add
	 */
	public void addDesignTimeAttribute(TaggedValue designTimeAttribute)
	{
		if (designTimeAttribute != null)
		{
			setDesignTimeAttribute(designTimeAttribute.getName(), designTimeAttribute.getValue());
		}
	}

	//////////////////////////////////////////////////
	// @@ Tagged values
	//////////////////////////////////////////////////

	/**
	 * Gets the tagged value list.
	 * @return An iterator of {@link TaggedValue} objects
	 */
	public Iterator getTaggedValues()
	{
		if (taggedValueList == null)
			return EmptyIterator.getInstance();
		return taggedValueList.iterator();
	}

	/**
	 * Gets a tagged value by its collection index.
	 *
	 * @param index Collection index
	 * @return The tagged value
	 */
	public TaggedValue getTaggedValue(int index)
	{
		return (TaggedValue) taggedValueList.get(index);
	}

	/**
	 * Adds a tagged value.
	 * @param taggedValue The tagged value to add
	 */
	public void addTaggedValue(TaggedValue taggedValue)
	{
		if (taggedValueList == null)
			taggedValueList = new SortingArrayList();
		taggedValueList.add(taggedValue);
	}

	/**
	 * Clears the tagged value list.
	 */
	public void clearTaggedValues()
	{
		taggedValueList = null;
	}

	/**
	 * Gets the tagged value list.
	 * @return A list of {@link TaggedValue} objects
	 */
	public List getTaggedValueList()
	{
		return taggedValueList;
	}

	/**
	 * Sets the tagged value list.
	 * @param taggedValueList A list of {@link TaggedValue} objects
	 */
	public void setTaggedValueList(List taggedValueList)
	{
		this.taggedValueList = taggedValueList;
	}

	/**
	 * Gets the value of a tagged value.
	 *
	 * @param name Name of the attribute
	 * @return The value or null if no such attribute exists
	 */
	public String getTaggedValue(String name)
	{
		if (taggedValueList != null)
		{
			int n = taggedValueList.size();
			for (int i = 0; i < n; ++i)
			{
				TaggedValue sa = (TaggedValue) taggedValueList.get(i);
				if (CommonUtil.equalsNull(sa.getName(), name))
					return sa.getValue();
			}
		}

		return null;
	}

	/**
	 * Gets the temporary reference (used by the modeler).
	 * This value is not copied by {@link #copyFrom} or the clone method.
	 * @nowarn
	 */
	public Object getTmpReference()
	{
		return tmpReference;
	}

	/**
	 * Sets the temporary reference (used by the modeler).
	 * This value is not copied by {@link #copyFrom} or the clone method.
	 * @nowarn
	 */
	public void setTmpReference(Object tmpReference)
	{
		this.tmpReference = tmpReference;
	}

	//////////////////////////////////////////////////
	// @@ Info text and toString
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
	public String[] getInfoText()
	{
		return createInfoText(getName(), getDisplayText(), getDescriptionText());
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
		return getDescription();
	}

	/**
	 * Gets the technical name of this type of model object.
	 *
	 * @return The name or null
	 */
	public String getModelObjectTypeName()
	{
		return CoreResources.getOptionalString("ModelObjectTypeName." + getClass().getName());
	}

	/**
	 * Creates informational text about the object from three sources.
	 * The text can be used to e. g. display a tool tip that describes the object.
	 *
	 * @param title Headline text
	 * @param displayText Display text
	 * @param description Full object description
	 *
	 * @return An array of strings that make up the information text.<br>
	 * Each array element corresponds to a paragraph that should be displayed.
	 * A paragraph may contain newline ('\n') and tab ('\t') characters that
	 * should be interpreted by the user interface.
	 */
	public String[] createInfoText(String title, String displayText, String description)
	{
		List list = new ArrayList();

		if (displayText != null && displayText.length() > INFO_TEXT_MAX_LINE_LENGTH)
		{
			// Decompose into single lines and trim
			displayText = TextUtil.concatLines(TextUtil.breakIntoLines(displayText, true, INFO_TEXT_MAX_LINE_LENGTH).iterator());
		}

		if (description != null)
		{
			// Decompose into single lines and trim
			description = TextUtil.concatLines(TextUtil.breakIntoLines(description, true, INFO_TEXT_MAX_LINE_LENGTH).iterator());
		}

		String modelObjectTypeName = getModelObjectTypeName();
		if (modelObjectTypeName != null)
		{
			title = title + " (" + modelObjectTypeName + ")";
		}

		addInfoText(list, title);
		addInfoText(list, displayText);
		addInfoText(list, description);

		String[] ret = new String[list.size()];
		return (String[]) list.toArray(ret);
	}

	/**
	 * Adds info text to the info text list if the string is not null
	 * and is not yet contained in the list.
	 *
	 * @param list List of strings
	 * @param s String to add
	 */
	private void addInfoText(List list, String s)
	{
		if (s == null)
			return;

		int n = list.size();
		for (int i = 0; i < n; ++i)
		{
			String ls = (String) list.get(i);
			if (ls.equalsIgnoreCase(s))
				return;
		}

		list.add(s);
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.validate
	 */
	public boolean validate(int flag)
	{
		if (getName() == null)
		{
			getModelMgr().getMsgContainer().addMsg(this, "No object name specified.");
			return false;
		}

		boolean result = ModelQualifier.isValidIdentifier(getName());
		if (! result)
		{
			getModelMgr().getMsgContainer().addMsg(this, "$0 must not contain one of the characters \"" + ModelQualifier.ALL_DELIMITERS + "\".",
				new Object[]
				{
					getName()
				});
		}
		return result;
	}

	/**
	 * Maintains the references from this object to other objects and the hierarchy
	 * links of this object to its parent objects.
	 *
	 * References consist of two parts:<br>
	 * - A name (qualified name) of the referenced object<br>
	 * - A link (reference) to the object
	 *
	 * The method can establish the link according to the reference name or
	 * determine the reference name from an existing link.<br>
	 * The first option should be executed after the object has been loaded
	 * from persistent storage (or received from an RMI connection). In this case,
	 * a validation (see {@link #validate}) will also be performed.<br>
	 * The latter one should be performed before the object is being saved to persistent
	 * storage or sent over an RMI connection.
	 *
	 * The method will also link this object to its parent object.
	 * This should be done after the object has been received over an RMI
	 * connection or to repair the hierarchy links.
	 *
	 * @param flag Flag that determines the operation of the method.
	 * Can be any combination of integer constants of the {@link ModelObject} interface.
	 */
	public void maintainReferences(int flag)
	{
		if ((flag & VALIDATE_BASIC) != 0)
		{
			validate(flag);
		}
	}

	//////////////////////////////////////////////////
	// @@ Associations
	//////////////////////////////////////////////////

	/**
	 * Gets a list of objects associated with this item.
	 * Associations depend on the item type and can be e. g. source files,
	 * xml files, templates items, visual files, icon items, icon files etc.
	 * Each association will be packed in an {@link Association} object.
	 * The method will return the association as specified in the item definition,
	 * e. g. "icon/Abc16.png" for an icon file or "ViewOrder" for a visual item.
	 *
	 * If the association is a file, use the {@link Item#resolveFileRef} method in order
	 * to get a valid path to the file.<br>
	 * If the association is an item itself, use the {@link Item#resolveItemRef} method in order
	 * to get the item.<br>
	 *
	 * If the item supports a particular association, but no concrete association has
	 * been defined, the association object contains an Exception object.
	 * The exception message will give a hint on how to create this association.
	 *
	 * @return A list of {@link Association} objects or null if the item
	 * does not support file associations.<br>
	 * Note that the associated files or items may not exist yet, however they have been
	 * specified by the user in the item definition.
	 */
	public List getAssociations()
	{
		return null;
	}
}
