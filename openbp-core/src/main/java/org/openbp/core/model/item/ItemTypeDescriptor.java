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
package org.openbp.core.model.item;

import java.io.Serializable;

import org.openbp.common.ReflectException;
import org.openbp.common.ReflectUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.ModelException;

/**
 * Class containing constants for item types.
 *
 * For an explanation of the item object container flag, see the description of the
 * {@link org.openbp.core.model.item.ItemContainer} class.
 *
 * @see org.openbp.core.model.item.ItemTypes
 *
 * @author Heiko Erhardt
 */
public class ItemTypeDescriptor
	implements Serializable, Comparable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Name of the item type */
	private String itemType;

	/** Class name of the item's interface class */
	private String itemInterfaceName;

	/** Class name of the item's implementation class */
	private String itemClassName;

	/** Class name of the item factory that creates new items */
	private String itemFactoryClassName;

	/** Sequence that determines in which order the item types will appear in lists, toolbars etc. */
	protected int sequence;

	/** Flag if the item is displayed in item lists */
	private boolean visible;

	/** Flag if the item is wrapped by the {@link ItemContainer} class in its xml file */
	private boolean containedItem;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Item interface class */
	private transient Class itemInterface;

	/** Item implementation class */
	private transient Class itemClass;

	/** Item factory instance */
	private transient ItemFactory itemFactory;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ItemTypeDescriptor()
	{
		visible = true;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the item type.
	 * @nowarn
	 */
	public String getItemType()
	{
		return itemType;
	}

	/**
	 * Sets the name of the item type.
	 * @nowarn
	 */
	public void setItemType(String itemType)
	{
		this.itemType = itemType;
	}

	/**
	 * Gets the class name of the item's interface class.
	 * @nowarn
	 */
	public String getItemInterfaceName()
	{
		return itemInterfaceName;
	}

	/**
	 * Sets the class name of the item's interface class.
	 * @nowarn
	 */
	public void setItemInterfaceName(String itemInterfaceName)
	{
		this.itemInterfaceName = itemInterfaceName;
	}

	/**
	 * Gets the class name of the item's implementation class.
	 * @nowarn
	 */
	public String getItemClassName()
	{
		return itemClassName;
	}

	/**
	 * Sets the class name of the item's implementation class.
	 * @nowarn
	 */
	public void setItemClassName(String itemClassName)
	{
		this.itemClassName = itemClassName;
	}

	/**
	 * Gets the class name of the item factory that creates new items.
	 * @nowarn
	 */
	public String getItemFactoryClassName()
	{
		return itemFactoryClassName;
	}

	/**
	 * Sets the class name of the item factory that creates new items.
	 * @nowarn
	 */
	public void setItemFactoryClassName(String itemFactoryClassName)
	{
		this.itemFactoryClassName = itemFactoryClassName;
	}

	/**
	 * Gets the sequence that determines in which order the item types will appear in lists, toolbars etc..
	 * @nowarn
	 */
	public int getSequence()
	{
		return sequence;
	}

	/**
	 * Determines if the sequence number is set.
	 * @nowarn
	 */
	public boolean hasSequence()
	{
		return sequence != 0;
	}

	/**
	 * Sets the sequence that determines in which order the item types will appear in lists, toolbars etc..
	 * @nowarn
	 */
	public void setSequence(int sequence)
	{
		this.sequence = sequence;
	}

	/**
	 * Gets the flag if the item is displayed in item lists.
	 * @nowarn
	 */
	public boolean isVisible()
	{
		return visible;
	}

	/**
	 * Sets the flag if the item is displayed in item lists.
	 * @nowarn
	 */
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	/**
	 * Gets the flag if the item is wrapped by the {@link ItemContainer} class in its xml file.
	 * @nowarn
	 */
	public boolean isContainedItem()
	{
		return containedItem;
	}

	/**
	 * Determines if the flag if the item is wrapped by the {@link ItemContainer} class in its xml file is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasContainedItem()
	{
		return containedItem;
	}

	/**
	 * Sets the flag if the item is wrapped by the {@link ItemContainer} class in its xml file.
	 * @nowarn
	 */
	public void setContainedItem(boolean containedItem)
	{
		this.containedItem = containedItem;
	}

	/**
	 * Gets the item interface class.
	 * @nowarn
	 */
	public Class getItemInterface()
	{
		return itemInterface;
	}

	/**
	 * Gets the item implementation class.
	 * @nowarn
	 */
	public Class getItemClass()
	{
		return itemClass;
	}

	/**
	 * Gets the item factory instance.
	 * @nowarn
	 */
	public ItemFactory getItemFactory()
	{
		return itemFactory;
	}

	//////////////////////////////////////////////////
	// @@ Object descriptor access
	//////////////////////////////////////////////////

	/**
	 * Loads the various classes and the object descriptor associated with this item type.
	 * @throws OpenBPException On error
	 */
	public void initialize()
	{
		if (itemInterface == null)
		{
			try
			{
				itemInterface = Class.forName(itemInterfaceName);
			}
			catch (Exception e)
			{
				throw new ModelException("Initialization", "Component interface '" + itemInterfaceName + "' not found.");
			}
		}

		if (itemClass == null)
		{
			try
			{
				itemClass = Class.forName(itemClassName);
			}
			catch (Exception e)
			{
				throw new ModelException("Initialization", "Component class '" + itemClassName + "' not found.");
			}
		}

		if (itemFactory == null)
		{
			if (itemFactoryClassName != null)
			{
				try
				{
					itemFactory = (ItemFactory) ReflectUtil.instantiate(itemFactoryClassName, ItemFactory.class, "component factory");
				}
				catch (ReflectException e)
				{
					throw new ModelException("Initialization", "Error instantiating component factory.", e);
				}
			}
			else
			{
				StandardItemFactory stdFactory = new StandardItemFactory();
				stdFactory.setItemTypeDescr(this);
				itemFactory = stdFactory;
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Comparable implementation
	//////////////////////////////////////////////////

	/**
	 * Compares this object to another Object.
	 * Compares by the {@link #setSequence} property.
	 *
	 * @param o Object to be compared
	 * @return  The value 0 if the argument is a string lexicographically equal to this object;<br>
	 * a value less than 0 if the argument is a string lexicographically greater than this object;<br>
	 * and a value greater than 0 if the argument is a string lexicographically less than this object.
	 * @throws ClassCastException if the argument is not a WorkflowTask.
	 */
	public int compareTo(Object o)
	{
		int result = getSequence() - ((ItemTypeDescriptor) o).getSequence();
		return result;
	}
}
