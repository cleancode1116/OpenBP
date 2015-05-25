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

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenuItem;

import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;
import org.openbp.jaspira.action.keys.KeySequence;
import org.openbp.swing.AdvancedAccelerator;

/**
 * Menu item that refers to a Jaspira action and supports multi icons.
 *
 * @author Stephan Moritz
 */
public class JaspiraMenuItem extends JMenuItem
	implements FlexibleSize, PropertyChangeListener, HierarchyListener, AdvancedAccelerator
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Properties
	/////////////////////////////////////////////////////////////////////////

	/** Action represented by this menu item */
	private JaspiraAction action;

	/** Menu icon */
	private MultiIcon icon;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param action Action this menu represents
	 */
	public JaspiraMenuItem(JaspiraAction action)
	{
		super();

		this.action = action;
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

		// The actionPerformed method of this class will forward the menu activiation to the action
		addActionListener(action);

		icon = action.getIcon();
		if (icon != null)
		{
			// Actually, we should clone the icon here; however, since we use the same icon size everywhere, this is not necessary.
			setIcon(icon.getIcon(FlexibleSize.SMALL));
		}
	}

	/**
	 * Copies the values from the action to the menu item.
	 *
	 * @param propertyName Name of the property that has changed and needs to copied.
	 * If this parameter is null, the method will copy all parameters of the action.
	 * Otherwise, it will only copy the given property, if applicable.
	 */
	private void copyFromAction(String propertyName)
	{
		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_NAME))
		{
			setActionCommand(action.getName());
		}

		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_DISPLAY_NAME))
		{
			setText(action.getDisplayName());
		}

		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_DESCRIPTION))
		{
			setToolTipText(action.getDescription());
		}

		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_ENABLED))
		{
			setEnabled(action.isEnabled());
		}

		if (propertyName == null || propertyName.equals(JaspiraAction.PROPERTY_SELECTED))
		{
			setSelected(action.isSelected());
		}

		if (propertyName == null && action.isMnemonic())
		{
			setMnemonic(action.getMnemonicChar());
			setDisplayedMnemonicIndex(action.getMnemonicPos());
		}
	}

	/**
	 * @see org.openbp.swing.AdvancedAccelerator#getAcceleratorString()
	 */
	public String getAcceleratorString()
	{
		KeySequence [] ks = action.getKeySequences();

		if (ks != null && ks.length > 0)
		{
			// Use the first key sequence only (e. g. for popup display)
			return ks [0].toString();
		}
		return null;
	}

	/**
	 * Gets the action represented by this menu item.
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

	/////////////////////////////////////////////////////////////////////////
	// @@ FlexibleSize implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.common.icon.FlexibleSize#getIconSize()
	 */
	public int getIconSize()
	{
		if (icon == null)
		{
			return SMALL;
		}
		return icon.getIconSize();
	}

	/**
	 * @see org.openbp.common.icon.FlexibleSize#setIconSize(int)
	 */
	public void setIconSize(int size)
	{
		if (icon != null)
		{
			icon.setIconSize(size);

			revalidate();
			repaint();
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Property Change Listener
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		copyFromAction(evt.getPropertyName());
	}
}
