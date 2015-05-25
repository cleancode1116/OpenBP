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

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.common.generic.propertybrowser.PropertyDescriptor;
import org.openbp.common.util.ToStringHelper;
import org.openbp.jaspira.propertybrowser.PropertyBrowserModel;
import org.openbp.jaspira.propertybrowser.editor.PropertyEditor;
import org.openbp.jaspira.propertybrowser.editor.PropertyEditorMgr;
import org.openbp.jaspira.propertybrowser.editor.PropertyEditorOwner;
import org.openbp.jaspira.propertybrowser.editor.PropertyValidator;
import org.openbp.swing.components.treetable.JTreeTable;

/**
 * A property descriptor node represents a single non-collection property of an object.
 *
 * @author Erich Lauterbach
 */
public class PropertyNode extends AbstractNode
	implements PropertyEditorOwner
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Property descriptor of this node */
	private PropertyDescriptor propertyDescriptor;

	/** Property editor associated with the property descriptor */
	private PropertyEditor propertyEditor;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default Contructor
	 *
	 * @param propertyDescriptor Property descriptor represented by this node
	 */
	public PropertyNode(PropertyDescriptor propertyDescriptor)
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
		return ToStringHelper.toString(this, "propertyDescriptor.name");
	}

	/**
	 * Reloads the content of this node.
	 */
	public void reload()
	{
		// Assign object and property browser references
		super.reload();

		initPropertyEditor();
		if (propertyEditor != null)
		{
			propertyEditor.loadProperty();
		}

		if (propertyBrowser != null)
		{
			propertyBrowser.saveCurrentPosition();

			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					if (propertyBrowser != null)
					{
						PropertyBrowserModel model = (PropertyBrowserModel) propertyBrowser.getModel();
						if (model != null)
						{
							model.fireNodeChanged(PropertyNode.this);
							SwingUtilities.invokeLater(new Runnable()
							{
								public void run()
								{
									if (propertyBrowser != null)
									{
										propertyBrowser.restoreCurrentPosition();
									}
								}
							});
						}
					}
				}
			});
		}
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
	 * Gets the the propertyEditor associated with the property descriptor.
	 * @nowarn
	 */
	public PropertyEditor getPropertyEditor()
	{
		return propertyEditor;
	}

	/**
	 * Sets the the propertyEditor associated with the property descriptor.
	 * @nowarn
	 */
	public void setPropertyEditor(PropertyEditor propertyEditor)
	{
		this.propertyEditor = propertyEditor;
	}

	/**
	 * Gets the propertyDescriptor for this node..
	 * @nowarn
	 */
	public PropertyDescriptor getPropertyDescriptor()
	{
		return propertyDescriptor;
	}

	/**
	 * Sets the propertyDescriptor for this node..
	 * @nowarn
	 */
	public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor)
	{
		this.propertyDescriptor = propertyDescriptor;
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

		PropertyNode src = (PropertyNode) source;

		propertyDescriptor = src.propertyDescriptor;

		propertyEditor = src.propertyEditor != null ? (PropertyEditor) src.propertyEditor.clone() : null;
		if (propertyEditor != null)
		{
			propertyEditor.setOwner(this);
		}
	}

	//////////////////////////////////////////////////
	// @@ PropertyEditorOwner implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the the property validator used by this property editor..
	 * @nowarn
	 */
	public PropertyValidator getValidator()
	{
		// In order to validate this property, get the validator from the {@link ObjectNode}
		// that is the direct or indirect parent of this node

		ObjectNode on = getObjectNode();
		if (on != null)
			return on.getValidator();

		// Not found, so no validation
		return null;
	}

	/**
	 * Handles a key event that is not consumed by the property editor.
	 *
	 * @param e The key event
	 */
	public void handleKeyEvent(KeyEvent e)
	{
		// Forward to the property browser
		if (propertyBrowser != null)
		{
			propertyBrowser.handleKeyEvent(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ TreeTableNode overrides
	//////////////////////////////////////////////////

	/**
	 * @copy TreeTableNode.getNodeText()
	 */
	public String getNodeText()
	{
		return (propertyDescriptor != null) ? propertyDescriptor.getDisplayName() : null;
	}

	/**
	 * Gets the preferred height of the node in the tree.
	 *
	 * @return The height as an int in pixels
	 */
	public Dimension getPreferredSize()
	{
		initPropertyEditor();

		if (propertyEditor == null)
		{
			return new Dimension(0, 0);
		}

		int componentHeight = 0;
		JComponent component = propertyEditor.getPropertyComponent();
		if (component != null)
		{
			componentHeight = component.getPreferredSize().height;
		}
		return new Dimension(0, componentHeight);
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
			return propertyDescriptor;

		case 1:
			initPropertyEditor();
			return propertyEditor;

		case 2:
			return JTreeTable.createDescriptionCellValue(propertyDescriptor.getDescription());

		default:
			return null;
		}
	}

	//////////////////////////////////////////////////
	// @@ AbstractNode overrides
	//////////////////////////////////////////////////

	/**
	 * Sets the object containing the values for the porperty.
	 *
	 * @param object The object to be set
	 */
	public void setObject(Object object)
	{
		super.setObject(object);
		if (propertyEditor != null)
		{
			propertyEditor.setObject(object);
		}
	}

	/**
	 * Returns true if the propertyEditor is read only.
	 * @nowarn
	 */
	public boolean isReadOnly()
	{
		return propertyDescriptor.isReadOnly();
	}

	/**
	 * Checks if the node node should be expanded on initial display.
	 *
	 * @return
	 *		true	If the node should be expanded<br>
	 *		false	If the node should be collapsed
	 */
	public boolean shouldExpand()
	{
		return propertyDescriptor.isExpanded();
	}

	//////////////////////////////////////////////////
	// @@ Protected methods
	//////////////////////////////////////////////////

	/**
	 * Gets the property editor for this node.
	 * The method loads and caches the editor.
	 */
	private void initPropertyEditor()
	{
		if (propertyEditor != null || propertyDescriptor == null)
			return;

		try
		{
			// Determine the property editor class
			Class cls = propertyDescriptor.getEditorClass();
			if (cls == null)
			{
				if (propertyDescriptor.getEditorClassName() == null)
				{
					return;
				}

				cls = PropertyEditorMgr.getInstance().findPropertyEditor(propertyDescriptor.getEditorClassName());
				if (cls == null)
					return;

				propertyDescriptor.setEditorClass(cls);
			}

			// Instantiate and initialize the editor
			PropertyEditor pe = (PropertyEditor) ReflectUtil.instantiate(cls, PropertyEditor.class, "property editor");

			pe.initialize(propertyDescriptor.getEditorParamString());
			pe.setPropertyName(propertyDescriptor.getName());
			pe.setReadonly(propertyDescriptor.isReadOnly());
			pe.setPropertyBrowser(propertyBrowser);

			// Instantiate the property validator if desired
			if (propertyDescriptor.getValidatorClassName() != null)
			{
				cls = propertyDescriptor.getValidatorClass();
				if (cls == null)
				{
					cls = PropertyEditorMgr.getInstance().findPropertyValidator(propertyDescriptor.getValidatorClassName());
					if (cls != null)
					{
						propertyDescriptor.setValidatorClass(cls);
					}
				}

				if (cls != null)
				{
					PropertyValidator pv = (PropertyValidator) ReflectUtil.instantiate(cls, PropertyValidator.class, "property validator");
					pe.setValidator(pv);
				}
			}

			// Provide the object to edit
			if (object != null)
			{
				pe.setObject(object);
			}

			// Make it belong to this node
			pe.setOwner(this);

			propertyEditor = pe;
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
		}
	}
}
