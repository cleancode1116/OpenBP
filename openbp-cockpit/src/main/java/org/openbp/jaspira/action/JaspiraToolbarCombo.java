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
package org.openbp.jaspira.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.border.EmptyBorder;

import org.openbp.jaspira.action.keys.KeySequence;
import org.openbp.jaspira.gui.plugin.PluginPanel;
import org.openbp.jaspira.gui.plugin.VisiblePlugin;
import org.openbp.swing.AdvancedAccelerator;
import org.openbp.swing.components.popupfield.JSelectionField;

/**
 * Tool bar button that refers to a Jaspira action and supports multi icons.
 * Prior to executing the action, the focus will be set to the plugin that owns the Jaspira
 * toolbar this button belongs to (if any). This will ensure that Jaspria events generated
 * by the Jaspira action of the button will be distributed starting with the plugin the
 * button usually refers to.
 *
 * @author Heiko Erhardt
 */
public class JaspiraToolbarCombo extends JSelectionField
	implements ActionListener, PropertyChangeListener, HierarchyListener, AdvancedAccelerator
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Resource/property Constants
	/////////////////////////////////////////////////////////////////////////

	/** Property id for normal icon */
	public static final String PROPERTY_ICON = "icon";

	/** Property id for editable */
	public static final String PROPERTY_EDITABLE = "editable";

	/** Property id for the selection value */
	public static final String PROPERTY_SELECTION_VALUE = "selectionvalue";

	/** Property id for the selection text */
	public static final String PROPERTY_SELECTION_TEXT = "selectiontext";

	/** Property id for the selected item */
	public static final String PROPERTY_SELECTEDITEM = "selectedItem";

	/** Property id for the text */
	public static final String PROPERTY_TEXT = "text";

	/////////////////////////////////////////////////////////////////////////
	// @@ Properties
	/////////////////////////////////////////////////////////////////////////

	/** Action represented by this button */
	private JaspiraAction action;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param action Action this button represents
	 */
	public JaspiraToolbarCombo(JaspiraAction action)
	{
		super();

		this.action = action;

		// Add a 2 pixel offset to the top and bottom
		setBorder(new EmptyBorder(2, 0, 2, 0));

		copyFromAction(null);

		// Add ourself as property listener to the action so we are notified of state changes
		// like enabled, selected, text etc.
		// The link to the listener will be a weak link, so we don't need to care about removing
		// ourself from the action's listener list (however, we do it anyway using a hierarchy listener
		// in order to prevent zombie component updates when there is no garbage collection).
		action.addPropertyChangeListener(this);

		// The hierarchy listener will remove the property change listener
		// when the component is being removed from its parent
		// TODO Optimize 6: Remove property change listener when Jaspira component is not in use any more addHierarchyListener (this);

		// The actionPerformed method of this class will forward the button activiation to the action
		addActionListener(this);
	}

	/**
	 * Copies the values from the action into the button.
	 *
	 * @param propertyName Name of the property that has changed and needs to copied.
	 * If this parameter is null, the method will copy all parameters of the action.
	 * Otherwise, it will only copy the given property, if applicable.
	 */
	private void copyFromAction(String propertyName)
	{
		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_DISPLAY_NAME))
		{
			setLabelText(action.getDisplayName());
		}

		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_DESCRIPTION))
		{
			setToolTipText(action.getDescription());
		}

		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_ENABLED))
		{
			setEnabled(action.isEnabled());
		}

		if (propertyName == null || propertyName.equals(PROPERTY_EDITABLE))
		{
			// Load the editable option
			setEditable(action.getActionPropertyBoolean(PROPERTY_EDITABLE, false));
		}

		if (propertyName == null)
		{
			// Load the values (selectionvalue0...selectionvalue1 and selectiontext0...selectiontextn)
			// Get the values either from the property map of the action or the resource file
			clearItems();
			for (int i = 0;; ++i)
			{
				String key = PROPERTY_SELECTION_VALUE + i;
				Object value = action.getActionProperty(key);
				if (value == null)
					break;

				key = PROPERTY_SELECTION_TEXT + i;
				String text = action.getActionPropertyString(key);

				addItem(text, value);
			}

			adjustPreferredSize();
		}

		boolean set = false;

		if (propertyName == null || propertyName.equals(PROPERTY_SELECTEDITEM))
		{
			Object value = action.getActionProperty(PROPERTY_SELECTEDITEM);
			if (value != null || propertyName != null)
			{
				setSelectedItem(value);
				set = true;
			}
		}

		if (propertyName == null || propertyName.equals(PROPERTY_TEXT))
		{
			String value = action.getActionPropertyString(PROPERTY_TEXT);
			if (value != null || propertyName != null)
			{
				setText(value);
				set = true;
			}
		}

		if (propertyName == null && !set)
		{
			setText(null);
		}
	}

	/**
	 * @see org.openbp.swing.AdvancedAccelerator#getAcceleratorString()
	 */
	public String getAcceleratorString()
	{
		KeySequence [] ks = action.getKeySequences();

		if (ks != null && ks.length > 0)
			return ks [0].toString();
		return null;
	}

	/**
	 * Gets the action represented by this button.
	 * @nowarn
	 */
	public JaspiraAction getJaspiraAction()
	{
		return action;
	}

	//////////////////////////////////////////////////
	// @@ HierarchyListener implementation
	//////////////////////////////////////////////////

	/**
	 * Called when the parent of the component changes.
	 * Removes or adds this component as property change listener from its action.
	 * @nowarn
	 */
	public void hierarchyChanged(HierarchyEvent e)
	{
		if (!isShowing())
		{
			action.removePropertyChangeListener(this);
		}
		else
		{
			action.addPropertyChangeListener(this);
		}
	}

	//////////////////////////////////////////////////
	// @@ ActionListener implementation
	//////////////////////////////////////////////////

	/**
	 * Action execution proxy method.
	 * Tries to set the focus to the plugin that owns the toolbar before executing the actual action.
	 *
	 * @param event Event
	 */
	public void actionPerformed(ActionEvent event)
	{
		// Search the component hierarchy backwards for the plugin panel
		// that holds the Jaspira toolbar this button belongs to
		for (Component c = this; c != null; c = c.getParent())
		{
			if (c instanceof PluginPanel)
			{
				PluginPanel pluginPanel = (PluginPanel) c;
				VisiblePlugin plugin = pluginPanel.getPlugin();
				if (plugin != null)
				{
					// Set the focus to the plugin prior to executing the action
					plugin.focusPlugin();
				}
			}
		}

		// Save the selected text value to the action object
		String text = getText();
		action.putValue(PROPERTY_TEXT, text);

		action.actionPerformed(event);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Property change listener implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		copyFromAction(evt.getPropertyName());
	}
}
