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
package org.openbp.jaspira.event;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;

import javax.swing.FocusManager;
import javax.swing.JComponent;

import org.openbp.common.ExceptionUtil;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraPopupMenu;
import org.openbp.jaspira.action.JaspiraToolbar;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Event that is sent before a popup menu is shown.
 * Provides methods for plugins to add their own entries.
 *
 * @author Stephan Moritz
 */
public class InteractionEvent extends JaspiraEvent
	implements Transferable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Event base name for interaction events */
	public static final String EVENTBASE = "global.interaction.";

	/** Event name for popupevents */
	public static final String POPUP = "popup";

	/** Event name for menu events */
	public static final String MENU = "menu";

	/** Event name for toolbar events */
	public static final String TOOLBAR = "toolbar";

	//////////////////////////////////////////////////
	// @@ members
	//////////////////////////////////////////////////

	/** The transferable encapsulated inside of this event. */
	private Transferable transferable;

	/** Root action used to collect Jaspira actions that have been added by event handlers */
	private JaspiraAction virtualRoot;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @param source Source plugin
	 * @param type {@link #POPUP}/{@link #MENU}/{@link #TOOLBAR}
	 * @param transferable Transferable argument
	 */
	public InteractionEvent(Plugin source, String type, Transferable transferable)
	{
		super(source, EVENTBASE + type, null, TYPE_FLOOD, type.equals(POPUP) ? Plugin.LEVEL_APPLICATION : Plugin.LEVEL_PAGE, JaspiraEvent.UNCONSUMABLE);

		if (transferable == null && POPUP.equals(type))
		{
			throw new NullPointerException("Transferable must not be null for Popups!");
		}

		this.transferable = transferable;
	}

	/**
	 * Constructor.
	 * @param source Source plugin
	 * @param type {@link #POPUP}/{@link #MENU}/{@link #TOOLBAR}
	 * @param o Arbitrary event argument
	 */
	public InteractionEvent(Plugin source, String type, Object o)
	{
		super(source, EVENTBASE + type, o, TYPE_FLOOD, type.equals(POPUP) ? Plugin.LEVEL_APPLICATION : Plugin.LEVEL_PAGE, JaspiraEvent.UNCONSUMABLE);

		if (o == null && POPUP.equals(type))
		{
			throw new NullPointerException("Argument must not be null for Popups!");
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Delegation of Transferable
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see java.awt.datatransfer.Transferable#getTransferData(DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException
	{
		if (transferable == null)
			throw new UnsupportedFlavorException(flavor);
		return transferable.getTransferData(flavor);
	}

	/**
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor [] getTransferDataFlavors()
	{
		return transferable != null ? transferable.getTransferDataFlavors() : new DataFlavor [0];
	}

	/**
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return transferable != null ? transferable.isDataFlavorSupported(flavor) : false;
	}

	/**
	 * Gets the transfer data in the desired format (convenience method that supresses exceptions).
	 * You should call this method only if have previously checked if the flavor is supported
	 * by calling {@link #isDataFlavorSupported}.
	 *
	 * @param flavor Flavor to get
	 * @return The desired object or null if the flavor is not supported or an i/o error has occurred.
	 * In the latter case, the method prints a stack trace to stderr.
	 */
	public Object getSafeTransferData(DataFlavor flavor)
	{
		try
		{
			if (transferable != null)
			{
				return transferable.getTransferData(flavor);
			}
		}
		catch (UnsupportedFlavorException e)
		{
			// Silently ignore
		}
		catch (IOException e)
		{
			ExceptionUtil.printTrace(e);
		}
		return null;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Action management
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a Jaspria action (which can be an actual action, a group or a submenu) to the popup menu.
	 * @param action Action to add
	 */
	public void add(JaspiraAction action)
	{
		if (virtualRoot == null)
		{
			virtualRoot = new JaspiraAction("root", null, null, null, null, 1, JaspiraAction.TYPE_MENU);
		}

		virtualRoot.addMenuChild(action);
		virtualRoot.addToolbarChild(action);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Popup menu and toolbar creation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Generates a menu from of the given entries.
	 * @return The menu or null if no entries have been added to the interaction event
	 */
	public JaspiraPopupMenu createPopupMenu()
	{
		if (virtualRoot == null)
		{
			// We have no children, so we do not want the menu to be shown
			return null;
		}

		// we are a menu with subentries - create the menu and return it

		JaspiraPopupMenu menu = new JaspiraPopupMenu();

		SortedSet menuchildren = virtualRoot.getMenuchildren();
		if (menuchildren == null)
			return null;

		for (Iterator it = menuchildren.iterator(); it.hasNext();)
		{
			JaspiraAction next = (JaspiraAction) it.next();

			if (next.getType().equals(JaspiraAction.TYPE_GROUP))
			{
				if (next.getMenuchildren() == null)
				{
					// The group is empty, ignore it.
					continue;
				}

				for (Iterator it2 = next.getMenuchildren().iterator(); it2.hasNext();)
				{
					menu.add((JaspiraAction) it2.next());
				}
			}
			else
			{
				menu.add(next);
			}

			if (it.hasNext())
			{
				menu.addSeparator();
			}
		}

		Component focusOwner = FocusManager.getCurrentManager().getFocusOwner();
		menu.setInvoker(focusOwner);

		return menu;
	}

	/**
	 * Generates a toolbar from of the given entries.
	 * @return The toolbar or null if no entries have been added to the interaction event
	 */
	public JaspiraToolbar createToolbar()
	{
		if (virtualRoot == null)
		{
			// We have no children, so we do not want the toolbar to be shown
			return null;
		}

		JaspiraToolbar toolbar = new JaspiraToolbar();

		if (virtualRoot.getToolbarchildren() != null)
		{
			for (Iterator it = virtualRoot.getToolbarchildren().iterator(); it.hasNext();)
			{
				JaspiraAction action = (JaspiraAction) it.next();

				JComponent item = action.toToolBarComponent();
				if (item != null)
				{
					toolbar.add(item);
					toolbar.addSeparator();
				}
			}
		}

		return toolbar;
	}
}
