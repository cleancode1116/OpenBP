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
package org.openbp.jaspira.propertybrowser.editor;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.property.PropertyAccessUtil;
import org.openbp.common.property.PropertyException;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.jaspira.propertybrowser.PropertyBrowserEvent;

/**
 * The abstract property editor provides some of the generic methods
 * needed by any property editor implementing the property editor
 * interface.
 *
 * @author Erich Lauterbach
 */
public abstract class AbstractPropertyEditor
	implements PropertyEditor, KeyListener, FocusListener
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Property name */
	protected String propertyName;

	/** Editor parameter values */
	protected String params;

	/** Determines if the property is read only */
	protected boolean readonly;

	/** The property validator used by this property editor. */
	protected PropertyValidator validator;

	/** Editor listener */
	protected PropertyEditorListener listener;

	/** The owner of the editor. This can be a component or a tree node if the pe is used by the property browser. */
	protected PropertyEditorOwner owner;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Display component */
	protected JComponent component;

	/** Parameter parser */
	protected EditorParameterParser parameterParser;

	/** property browser instance if this property is used within the context of an property browser */
	protected PropertyBrowser propertyBrowser;

	/** The object to edit (object that owns the property) */
	protected Object object;

	/** The actual property value */
	protected Object value;

	/** Flag set when the value has been loaded from the property */
	protected boolean valueLoaded;

	/** Flag set when the value has been edited */
	protected boolean valueChanged;

	/** Status flag: The saveProperty method is being performed */
	private boolean performingSaveProperty;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Sets the editor parameters and initializes the editor.
	 *
	 * @param params Editor parameters from the property descriptor or null
	 */
	public void initialize(String params)
	{
		this.params = params;
	}

	/**
	 * Returns a clone of this.
	 *
	 * The clone method is defined by default to be protected (who did this?!?).
	 * However, we define it to be public in order to be able to invoke it directly
	 * from outside the class and not needing to use reflections to call it.
	 *
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		PropertyEditor clone = (PropertyEditor) super.clone();

		// Perform a deep copy
		clone.copyFrom(this, Copyable.COPY_DEEP);

		return clone;
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

		AbstractPropertyEditor src = (AbstractPropertyEditor) source;

		validator = src.validator;
		readonly = src.readonly;
		propertyName = src.propertyName;
		params = src.params;

		component = null;
		object = null;
		valueChanged = false;
	}

	/**
	 * Gets the parameter parser.
	 * @nowarn
	 */
	public EditorParameterParser getParameterParser()
	{
		return parameterParser;
	}

	//////////////////////////////////////////////////
	// @@ Methods to be implemented by the actual editor
	//////////////////////////////////////////////////

	/**
	 * Sets the display component value from the associated property.
	 */
	protected abstract void setComponentValue();

	/**
	 * Gets the current editor component value.
	 *
	 * @return The current value of the component (can be null)<br>
	 * Note that this value might be different from the actual property value
	 * if the property hasn't been saved yet.
	 */
	protected abstract Object getComponentValue();

	/**
	 * Creates the editor component of the property editor.
	 */
	protected abstract void createComponent();

	/**
	 * Highlights the content of the component.
	 * @param on
	 *		true	Turns the highlight on if the component has the focus<br>
	 *		false	Turns the highlight off
	 */
	protected void highlight(boolean on)
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * When a change in the property value has occurred, then this method is to be called.
	 */
	public void propertyChanged()
	{
		value = getComponentValue();
		valueChanged = true;

		// Validate the incomplete property value
		if (!validateProperty(false))
			return;

		if (listener != null)
		{
			listener.componentCreated(this);
		}

		propertyBrowser.firePropertyBrowserEvent(new PropertyBrowserEvent(PropertyBrowserEvent.PROPERTY_CHANGED, this));
	}

	/**
	 * Loads the property value from the object and displays it in the property component.
	 */
	public void loadProperty()
	{
		try
		{
			value = PropertyAccessUtil.getProperty(object, propertyName);
			valueLoaded = true;
			valueChanged = false;

			setComponentValue();
		}
		catch (PropertyException e)
		{
			// Cannot access property; returning null will display a blank space
			ExceptionUtil.printTrace(e);
		}
	}

	/**
	 * Saves the changes made to the property value if the property was modified.
	 * The method will will validate the modified value, if a {@link PropertyValidator} has been set and then
	 * on success save the property value.
	 *
	 * @return
	 *		true	The property value has been successfully validated and saved.<br>
	 *		false	Validation or saving failed.
	 */
	public boolean saveProperty()
	{
		if (performingSaveProperty)
		{
			// This may be caused if an error message is displayed in a message box while
			// validating the property. Return false in this case to prevent any further
			// processing that might lead to an infinite loop
			return false;
		}

		// saveProperty might be called deferred by focusLost using invokeLater.
		// In some situations, the reference to the object might not exist any more,
		// so we have to check if the object is null.
		// This might mean we actually loose the changed property value, but we have
		// to accept this here or else the validators wouldn't be able to show message boxes.
		if (valueChanged && object != null)
		{
			performingSaveProperty = true;

			try
			{
				// Validate the final property value
				if (!validateProperty(true))
					return false;

				try
				{
					valueChanged = false;
					PropertyAccessUtil.setProperty(object, propertyName, value);

					propertyBrowser.firePropertyBrowserEvent(new PropertyBrowserEvent(PropertyBrowserEvent.PROPERTY_UPDATED, this));
				}
				catch (PropertyException e)
				{
					// Cannot access property; returning null will display a blank space
					ExceptionUtil.printTrace(e);
					return false;
				}
			}
			finally
			{
				performingSaveProperty = false;
			}
		}

		return true;
	}

	/**
	 * Resets the property to its original value.
	 */
	public void resetProperty()
	{
		loadProperty();
		highlight(true);
	}

	/**
	 * Validates the edited value for this property, if a
	 * validator has been specified in the property descriptor file.
	 * This method is called by the {@link #saveProperty} method.
	 *
	 * @param complete
	 *		true	The value has been completely entered. This is the case if the user wishes to leave the field.<br>
	 *		false	The value is being typed/edited.
	 * @return
	 *		true	If the validation succeeded and the edited value is valid.<br>
	 *		false	If the value is not valid.
	 */
	protected boolean validateProperty(boolean complete)
	{
		if (valueChanged)
		{
			if (validator != null)
			{
				if (!validator.validateProperty(propertyName, value, object, this, complete))
					return false;
			}

			if (owner != null)
			{
				// Check if the owner of the pe can provide a validator
				PropertyValidator ownerValidator = owner.getValidator();
				if (ownerValidator != null)
				{
					if (!ownerValidator.validateProperty(propertyName, value, object, this, complete))
						return false;
				}
			}
		}

		return true;
	}

	/**
	 * Get the editor for the property.
	 * This will also initialize the component (if not done yet) and load the current property value
	 * from the object to be displayed.
	 *
	 * @return The property editor or null if the property cannot be accessed
	 */
	public JComponent getPropertyComponent()
	{
		if (object != null)
		{
			initializeComponent();

			if (!valueLoaded)
			{
				loadProperty();
			}
		}

		return component;
	}

	/**
	 * Get the editor for the property.
	 * This will also initialize the component (if not done yet), however it will
	 * \bnot\b load the current property value from the object to be displayed.
	 *
	 * @return The property editor or null if the property cannot be accessed
	 */
	public JComponent getComponent()
	{
		if (object != null)
		{
			initializeComponent();
		}

		return component;
	}

	/**
	 * Initializes the editor component.
	 */
	protected void initializeComponent()
	{
		if (component == null)
		{
			if (getParams() != null)
			{
				// Parse the parameters of the editor
				parameterParser = new EditorParameterParser(this);

				String className = parameterParser.getString("listener");
				if (className != null)
				{
					try
					{
						listener = (PropertyEditorListener) ReflectUtil.instantiate(className, PropertyEditorListener.class, "property editor listener");
					}
					catch (Exception e)
					{
						ExceptionUtil.printTrace(e);
					}
				}

				// Parse the editor parameters from the property descriptor
				parseParams(parameterParser);
			}

			if (listener != null)
			{
				listener.initialized(this);
			}

			// Let the implementation create the component
			createComponent();

			if (listener != null)
			{
				listener.componentCreated(this);
			}
		}
	}

	/**
	 * Resets the display of the editor.
	 * Shows the editor component in an 'unfocused' state.
	 */
	public void resetComponentDisplay()
	{
		highlight(false);
	}

	/**
	 * Parses the editor parameters specified in the property descriptor.
	 * @param parser Parameter parser
	 */
	protected void parseParams(EditorParameterParser parser)
	{
	}

	//////////////////////////////////////////////////
	// @@ Listener implementations
	//////////////////////////////////////////////////

	/**
	 * Invoked when a key has been released.
	 * @nowarn
	 */
	public void keyReleased(KeyEvent e)
	{
	}

	/**
	 * Invoked when a key has been pressed.
	 * @nowarn
	 */
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();

		if (keyCode == KeyEvent.VK_ESCAPE)
		{
			if (valueChanged)
			{
				// The property has been modified, reset and return
				loadProperty();
				highlight(true);

				e.consume();
				return;
			}
		}

		if (keyCode == KeyEvent.VK_TAB || keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_HOME || keyCode == KeyEvent.VK_END || keyCode == KeyEvent.VK_PAGE_UP || keyCode == KeyEvent.VK_PAGE_DOWN || keyCode == KeyEvent.VK_ESCAPE)
		{
			forwardKeyEvent(e);
		}
	}

	/**
	 * Invoked when a key has been typed.
	 * @nowarn
	 */
	public void keyTyped(KeyEvent e)
	{
	}

	/**
	 * Invoked when a component gains the keyboard focus.
	 * @nowarn
	 */
	public void focusGained(FocusEvent e)
	{
		propertyBrowser.firePropertyBrowserEvent(new PropertyBrowserEvent(PropertyBrowserEvent.FOCUS_GAINED, this));
		highlight(true);
	}

	/**
	 * Invoked when a component loses the keyboard focus.
	 * @nowarn
	 */
	public void focusLost(FocusEvent e)
	{
		highlight(false);

		// Executing the focus lost action right now might create problems if some validator shows a message box or so...
		propertyBrowser.firePropertyBrowserEvent(new PropertyBrowserEvent(PropertyBrowserEvent.FOCUS_LOST, AbstractPropertyEditor.this));
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Sets the name of the property that should be edited by this editor instance.
	 *
	 * @param propertyName The property name
	 */
	public void setPropertyName(String propertyName)
	{
		this.propertyName = propertyName;
		valueLoaded = false;
	}

	/**
	 * Gets the property name.
	 * @nowarn
	 */
	public String getPropertyName()
	{
		return propertyName;
	}

	/**
	 * Sets the object that holds the property specified by {@link #setPropertyName}
	 *
	 * @param object The object to edit
	 */
	public void setObject(Object object)
	{
		this.object = object;
		valueLoaded = false;
	}

	/**
	 * Gets the the object to edit (object that owns the property).
	 * @nowarn
	 */
	public Object getObject()
	{
		return object;
	}

	/**
	 * Gets the the actual property value.
	 * @nowarn
	 */
	public Object getValue()
	{
		return value;
	}

	/**
	 * Sets the the actual property value.
	 * @nowarn
	 */
	public void setValue(Object value)
	{
		this.value = value;
	}

	/**
	 * Gets the read only flag.
	 * @nowarn
	 */
	public boolean isReadonly()
	{
		return readonly;
	}

	/**
	 * Sets the read only flag.
	 * @nowarn
	 */
	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}

	/**
	 * Gets the editor parameter values.
	 * @nowarn
	 */
	public String getParams()
	{
		return params;
	}

	/**
	 * Sets the editor parameter values.
	 * @nowarn
	 */
	public void setParams(String params)
	{
		this.params = params;
	}

	/**
	 * Gets the the property validator used by this property editor.
	 * @nowarn
	 */
	public PropertyValidator getValidator()
	{
		return validator;
	}

	/**
	 * Sets the the property validator used by this property editor.
	 * @nowarn
	 */
	public void setValidator(PropertyValidator validator)
	{
		this.validator = validator;
	}

	/**
	 * Gets the the owner of the editor.
	 * This can be a component or a tree node if the pe is used by the property browser.
	 * @nowarn
	 */
	public PropertyEditorOwner getOwner()
	{
		return owner;
	}

	/**
	 * Sets the the owner of the editor.
	 * This can be a component or a tree node if the pe is used by the property browser.
	 * @nowarn
	 */
	public void setOwner(PropertyEditorOwner owner)
	{
		this.owner = owner;
	}

	/**
	 * Gets the property browser instance if this property is used within the context of an property browser.
	 * @nowarn
	 */
	public PropertyBrowser getPropertyBrowser()
	{
		return propertyBrowser;
	}

	/**
	 * Sets the property browser instance if this property is used within the context of an property browser.
	 * @nowarn
	 */
	public void setPropertyBrowser(PropertyBrowser propertyBrowser)
	{
		this.propertyBrowser = propertyBrowser;
	}

	//////////////////////////////////////////////////
	// @@ Static helpers
	//////////////////////////////////////////////////

	/**
	 * Gets the string representation of the specified value.
	 *
	 * @param value Value or null
	 * @return The toString value or null
	 */
	protected String getSafeString(Object value)
	{
		return value != null ? value.toString() : null;
	}

	/**
	 * Forwards the given key event to the parent of the property editor component.
	 *
	 * @param e Event<br>
	 * The event will be consumed by this method.
	 */
	protected void forwardKeyEvent(KeyEvent e)
	{
		if (owner != null)
		{
			owner.handleKeyEvent(e);
		}

		e.consume();
	}
}
