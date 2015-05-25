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
package org.openbp.jaspira.propertybrowser.nodes;

import java.util.Iterator;
import java.util.Vector;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.common.generic.description.DisplayObject;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.generic.description.Displayable;
import org.openbp.common.generic.propertybrowser.ObjectDescriptor;
import org.openbp.common.generic.propertybrowser.ObjectDescriptorMgr;
import org.openbp.common.generic.propertybrowser.PropertyDescriptor;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.property.PropertyAccessUtil;
import org.openbp.common.property.PropertyException;
import org.openbp.common.util.ToStringHelper;
import org.openbp.jaspira.propertybrowser.ObjectValidator;
import org.openbp.jaspira.propertybrowser.PropertyBrowserModel;
import org.openbp.swing.components.treetable.JTreeTable;

/**
 * An object descriptor node is either the root node of an property browser tree,
 * represents an object contained in a collection (see {@link CollectionNode}) or the value of a complex type property.
 *
 * @author Erich Lauterbach
 */
public class ObjectNode extends AbstractNode
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Object descriptor used by this node. */
	private ObjectDescriptor objectDescriptor;

	/** Property descritpor in case this object descriptor specifies a complex type property */
	private PropertyDescriptor propertyDescriptor;

	/** Object validator that is associated with the object descriptor of this node */
	private ObjectValidator validator;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor for regular object nodes.
	 *
	 * @param objectDescriptor Object descriptor represented by this node
	 */
	public ObjectNode(ObjectDescriptor objectDescriptor)
	{
		super();

		this.objectDescriptor = objectDescriptor;

		initialize();
	}

	/**
	 * Constructor for object nodes describing a complex property.
	 *
	 * @param propertyDescriptor Property descriptor in case this object node specifies a complex type property
	 */
	public ObjectNode(PropertyDescriptor propertyDescriptor)
	{
		super();

		this.propertyDescriptor = propertyDescriptor;
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		if (propertyDescriptor != null)
		{
			return ToStringHelper.toString(this, "propertyDescriptor.name", "objectDescriptor.objectClassName");
		}
		return ToStringHelper.toString(this, "objectDescriptor.objectClassName");
	}

	/**
	 * Reloads the content of this node.
	 */
	public void reload()
	{
		children = null;
		initialize();

		// Assign object and property browser references
		super.reload();
	}

	/**
	 * Checks if this node represents the given property.
	 *
	 * @param propertyName Name of the property to check
	 * @nowarn
	 */
	public boolean representsProperty(String propertyName)
	{
		return propertyDescriptor != null && propertyDescriptor.getName().equals(propertyName);
	}

	/**
	 * Initializes the node.
	 */
	public void initialize()
	{
		if (children == null)
		{
			if (objectDescriptor != null)
			{
				children = new Vector();

				for (Iterator it = objectDescriptor.getProperties(); it.hasNext();)
				{
					addProperty((PropertyDescriptor) it.next());
				}

				createValidator();
			}
		}
	}

	/**
	 * Creates an object validator if there is one specified in the object descriptor.
	 */
	private void createValidator()
	{
		if (validator != null)
		{
			// Validator already exists
			return;
		}

		// Instantiate the object validator if given
		String validatorClassName = objectDescriptor.getValidatorClassName();
		if (validatorClassName != null)
		{
			Class cls = objectDescriptor.getValidatorClass();
			if (cls == null)
			{
				cls = ReflectUtil.loadClass(validatorClassName);
				if (cls != null)
				{
					if (ObjectValidator.class.isAssignableFrom(cls))
					{
						objectDescriptor.setValidatorClass(cls);
					}
					else
					{
						System.err.println("Object validator '" + validatorClassName + "' does not implement the ObjectValidator interface");
						cls = null;
					}
				}
				else
				{
					System.err.println("Object validator '" + validatorClassName + "' not found");
				}
			}

			try
			{
				validator = (ObjectValidator) ReflectUtil.instantiate(cls, ObjectValidator.class, "object validator");
			}
			catch (Exception e)
			{
				// Something went wrong, warn
				ExceptionUtil.printTrace(e);
			}
		}
	}

	/**
	 * Gets the property descritpor in case this object descriptor specifies a complex type property.
	 * @nowarn
	 */
	public PropertyDescriptor getPropertyDescriptor()
	{
		return propertyDescriptor;
	}

	//////////////////////////////////////////////////
	// @@ Copyable implementation
	//////////////////////////////////////////////////

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

		ObjectNode src = (ObjectNode) source;

		objectDescriptor = src.objectDescriptor;
		propertyDescriptor = src.propertyDescriptor;
		validator = src.validator;
	}

	//////////////////////////////////////////////////
	// @@ TreeTableNode overrides
	//////////////////////////////////////////////////

	/**
	 * @copy TreeTableNode.getNodeText()
	 */
	public String getNodeText()
	{
		if (propertyDescriptor != null)
		{
			// For complex type properties, forward the node text responsibility to the property descriptor
			return propertyDescriptor.getDisplayName();
		}

		// TODO Feature 4: Property browser should support role display mode
		if (object != null)
		{
			if (object instanceof DisplayObjectImpl)
			{
				String value = ((DisplayObjectImpl) object).getDisplayText();
				return value;
			}
			if (object instanceof DisplayObject)
			{
				String value = ((DisplayObject) object).getDisplayName();
				if (value == null)
					value = ((DisplayObject) object).getName();
				return value;
			}
			if (object instanceof Displayable)
			{
				String value = ((Displayable) object).getDisplayText();
				return value;
			}
		}

		if (objectDescriptor != null)
			return objectDescriptor.getDisplayName();

		return null;
	}

	/**
	 * Returns the column value for specified column index.
	 *
	 * @param columnIndex The of the column object to be returned
	 * @return The column value as an object
	 */
	public Object getColumnValue(int columnIndex)
	{
		switch (columnIndex)
		{
		case 0:
			if (propertyDescriptor != null)
			{
				return propertyDescriptor;
			}

			if (object != null)
			{
				return object;
			}

			if (objectDescriptor != null)
				return objectDescriptor;

			return null;

		case 1:
			if (propertyDescriptor != null)
			{
				// For complex type properties, forward the description text responsibility to the property descriptor
				return JTreeTable.createDescriptionCellValue(propertyDescriptor.getDescription());
			}

			if (object != null)
			{
				if (object instanceof DisplayObject)
				{
					return JTreeTable.createDescriptionCellValue(((DisplayObject) object).getDescriptionText());
				}
				return object.toString();
			}

			return null;

		default:
			return null;
		}
	}

	/**
	 * @copy AbstractNode.isLeaf
	 */
	public boolean isLeaf()
	{
		return false;
	}

	//////////////////////////////////////////////////
	// @@ AbstractNode implementation
	//////////////////////////////////////////////////

	/**
	 * Checks if the node node should be expanded on initial display.
	 *
	 * @return
	 *		true	If the node should be expanded<br>
	 *		false	If the node should be collapsed
	 */
	public boolean shouldExpand()
	{
		if (propertyDescriptor != null)
		{
			// For complex type properties, forward the expansion flag responsibility to the property descriptor
			return super.shouldExpand();
		}

		// Always expand top-level nodes
		return parentNode == null;
	}

	//////////////////////////////////////////////////
	// @@ Object accessors
	//////////////////////////////////////////////////

	/**
	 * Sets the object containing the values for the porperty.
	 *
	 * @param object The object to be set
	 */
	public void setObject(Object object)
	{
		if (propertyDescriptor != null && object != null)
		{
			// This object descriptor node refers to a complex type property.
			// Retrieve the object using the given property name and use it as current object
			// for this node and its child nodes.
			try
			{
				object = PropertyAccessUtil.getProperty(object, propertyDescriptor.getName());
			}
			catch (PropertyException e)
			{
				ExceptionUtil.printTrace(e);
				return;
			}

			if (object == null)
			{
				// No object to display, so remove this node
				AbstractNode parent = (AbstractNode) getParent();
				if (parent.children != null)
				{
					parent.children.remove(this);
					if (propertyBrowser != null)
					{
						((PropertyBrowserModel) propertyBrowser.getModel()).fireNodeRemoved(parent);
					}
				}
				return;
			}

			// Determine the object descriptor according to the actual type of the object
			Class cls = object.getClass();
			try
			{
				objectDescriptor = ObjectDescriptorMgr.getInstance().getDescriptor(cls, ObjectDescriptorMgr.ODM_THROW_ERROR);

				// Initialize our child nodes according to the object descriptor we just got
				initialize();

				// Make the child nodes aware of the property browser we belong to
				setPropertyBrowser(propertyBrowser);
			}
			catch (XMLDriverException ex)
			{
				ExceptionUtil.printTrace(ex);
				return;
			}
		}
		else
		{
			// Make sure we are initialized
			initialize();
		}

		// Make the object we are working on the current object and also pass it to the child nodes
		super.setObject(object);
	}

	//////////////////////////////////////////////////
	// @@ ObjectDescriptor Accessors
	//////////////////////////////////////////////////

	/**
	 * Returns the objectDescriptor.
	 * @return ObjectDescriptor
	 */
	public ObjectDescriptor getObjectDescriptor()
	{
		return objectDescriptor;
	}

	/**
	 * Sets the objectDescriptor.
	 * @param objectDescriptor The objectDescriptor to set
	 */
	public void setObjectDescriptor(ObjectDescriptor objectDescriptor)
	{
		this.objectDescriptor = objectDescriptor;
	}

	/**
	 * Gets the object validator that is associated with the object descriptor of this node.
	 * @nowarn
	 */
	public ObjectValidator getValidator()
	{
		return validator;
	}

	/**
	 * Sets the object validator that is associated with the object descriptor of this node.
	 * @nowarn
	 */
	public void setValidator(ObjectValidator validator)
	{
		this.validator = validator;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Adds a property descriptor as a {@link PropertyNode} to a specified
	 * {@link GroupNode} according to the group name. If the {@link GroupNode} should not exist,
	 * then a new {@link GroupNode} will be created.
	 *
	 * @param pd to be added
	 */
	protected void addProperty(PropertyDescriptor pd)
	{
		String groupName = pd.getGroup();

		if (groupName == null)
		{
			addPropertyNode(pd, this);
		}
		else
		{
			for (int i = 0; i < children.size(); i++)
			{
				Object node = children.get(i);

				if (node instanceof GroupNode)
				{
					GroupNode groupNode = (GroupNode) node;
					if (groupNode.getGroupName().equals(groupName))
					{
						addPropertyNode(pd, groupNode);
						return;
					}
				}
			}

			GroupNode groupNode = new GroupNode(groupName);
			addPropertyNode(pd, groupNode);
			add(groupNode);
		}
	}

	/**
	 * Adds a new {@link PropertyNode} with the specified {@link PropertyDescriptor} to
	 * the defined {@link AbstractNode}
	 *
	 * @param pd The {@link PropertyDescriptor} required to build the new {@link PropertyNode}
	 * @param parent The {@link AbstractNode} to which the {@link PropertyNode} is to be added to
	 */
	private static void addPropertyNode(PropertyDescriptor pd, AbstractNode parent)
	{
		AbstractNode node;

		if (pd.getCollectionDescriptor() != null)
		{
			node = new CollectionNode(pd);
		}
		else if (pd.getComplexProperty() != null)
		{
			node = new ObjectNode(pd);
		}
		else
		{
			node = new PropertyNode(pd);
		}

		parent.add(node);
	}
}
