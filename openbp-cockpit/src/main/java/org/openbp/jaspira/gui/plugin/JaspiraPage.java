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
package org.openbp.jaspira.gui.plugin;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.FocusManager;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugin.PluginMgr;

/**
 * A page in a application.
 * A page will be displayed in a {@link JaspiraPageContainer} which may contain one or more pages.
 *
 * @author Jens Ferchland
 */
public abstract class JaspiraPage extends AbstractVisiblePlugin
	implements PluginContainer
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Divider for the contained plugins */
	private PluginDivider divider;

	/**
	 * Used to store the plugin that had the focus last (for {@link #setFocusedPlugin}/{@link #restoreFocus}).
	 * This is a temporary variable only, so we use weak references here in order to prevent memory leaks.
	 */
	protected WeakReference focusedPluginRef;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new Jaspira page.
	 */
	public JaspiraPage()
	{
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		String s = super.toString();

		for (Iterator it = getPlugins().iterator(); it.hasNext();)
		{
			s += "\nContained plugin:" + it.next();
		}

		return s;
	}

	/**
	 */
	protected void initializeComponents()
	{
		setPluginDivider(new PluginDivider());

		layoutDefaultContent();

		setupKeyBindings();
	}

	/**
	 */
	protected void pluginUninstalled()
	{
		for (Iterator it = getPlugins().iterator(); it.hasNext();)
		{
			PluginMgr.getInstance().removeInstance((Plugin) it.next());
		}
	}

	/**
	 * This method will be called to generate the default layout of a Jaspira page.
	 * The normal JaspiraPage has no default layout so this method does nothing.
	 *
	 * If you create a new page which has to look special override this method
	 * and set your layout here.
	 *
	 * This method will be called automatically - you needn't to call it of your own.
	 */
	public void layoutDefaultContent()
	{
	}

	/**
	 * Set the divider of the page.
	 * @param divider Divider to set
	 */
	public void setPluginDivider(PluginDivider divider)
	{
		this.divider = divider;
		getContentPane().removeAll();
		getContentPane().add(divider);
	}

	/**
	 * Returns the divider of the page.
	 * @nowarn
	 */
	public PluginDivider getPluginDivider()
	{
		return divider;
	}

	/**
	 * Returns the JaspiraPageContainer of this page or null.
	 * @nowarn
	 */
	public Window getWindow()
	{
		return SwingUtilities.getWindowAncestor(getPluginComponent());
	}

	////////////////////////////////////////////////////////////
	// @@ PluginContainer implementation
	////////////////////////////////////////////////////////////

	public void addPlugin(VisiblePlugin p)
	{
		divider.addPlugin(p);
	}

	public void removePlugin(VisiblePlugin p)
	{
		divider.removePlugin(p);
		divider.revalidate();
	}

	public List getPlugins()
	{
		return divider.getPlugins();
	}

	public List getVisiblePlugins()
	{
		return divider.getVisiblePlugins();
	}

	public VisiblePlugin getActivePlugin()
	{
		return divider.getActivePlugin();
	}

	/**
	 * A page has no plugin container, so this returns null.
	 */
	public PluginContainer getParentContainer()
	{
		Window w = SwingUtilities.getWindowAncestor(getPluginComponent());
		if (w != null)
		{
			if (w instanceof JaspiraPageContainer)
			{
				return (JaspiraPageContainer) w;
			}
		}
		return null;
	}

	/**
	 */
	public void showPlugin(boolean changePage)
	{
		// Bring the page to the foreground only if we really should
		if (changePage && !getPluginComponent().isShowing())
		{
			Window w = SwingUtilities.getWindowAncestor(getPluginComponent());
			if (w != null)
			{
				if (w instanceof JaspiraPageContainer)
				{
					((JaspiraPageContainer) w).setPageActive(this);
				}
				w.toFront();
			}
		}
	}

	public void sliceContainer(PluginContainer toInsert, PluginContainer currentContainer, String constraint)
	{
		// matches the slice tagOrientation our splitdirection?
		// if so, we simply need to add the slicer directly
		// (if we are horiz. aligned and slice at EAST or WEST or the opposite)

		if (divider.getOrientation() == PluginDivider.HORIZONTAL_SPLIT)
		{
			// We are Horizontal
			if (PluginContainer.SOUTH.equals(constraint))
			{
				// Add to the end
				divider.insertContainerAt(toInsert, -1);
			}
			else if (PluginContainer.NORTH.equals(constraint))
			{
				// Add to the beginning
				divider.insertContainerAt(toInsert, 0);
			}
			else if (PluginContainer.WEST.equals(constraint))
			{
				PluginDivider sub = new PluginDivider(PluginDivider.VERTICAL_SPLIT);
				sub.addClient((Component) toInsert);
				sub.addClient(divider);
				setPluginDivider(sub);
			}
			else if (PluginContainer.EAST.equals(constraint))
			{
				PluginDivider sub2 = new PluginDivider(PluginDivider.VERTICAL_SPLIT);
				sub2.addClient(divider);
				sub2.addClient((Component) toInsert);
				setPluginDivider(sub2);
			}
		}
		else
		{
			// We are Vertical
			if (PluginContainer.EAST.equals(constraint))
			{
				// Add to the end
				divider.insertContainerAt(toInsert, -1);
			}
			else if (PluginContainer.WEST.equals(constraint))
			{
				// Add to the beginning
				divider.insertContainerAt(toInsert, 0);
			}
			else if (PluginContainer.NORTH.equals(constraint))
			{
				PluginDivider sub = new PluginDivider(PluginDivider.HORIZONTAL_SPLIT);
				sub.addClient((Component) toInsert);
				sub.addClient(divider);
				setPluginDivider(sub);
			}
			else if (PluginContainer.SOUTH.equals(constraint))
			{
				PluginDivider sub2 = new PluginDivider(PluginDivider.HORIZONTAL_SPLIT);
				sub2.addClient(divider);
				sub2.addClient((Component) toInsert);
				setPluginDivider(sub2);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Focus support
	//////////////////////////////////////////////////

	/**
	 * Returns the focus component of this plugin, i\.e\. the component
	 * that is to initially receive the focus.
	 * @return The return value defaults to the first component below the content pane of the plugin.
	 * If this component is a scroll pane, the method returns the view component of the pane.
	 * If there is no focusable component, the method returns null.
	 */
	public Component getPluginFocusComponent()
	{
		Component comp = super.getPluginFocusComponent();

		if (comp == null)
		{
			// If we don't have a focusable component ourself, try the component of the currently focused plugin
			if (focusedPluginRef != null)
			{
				VisiblePlugin p = (VisiblePlugin) focusedPluginRef.get();
				if (p != null && p != this)
				{
					comp = p.getPluginFocusComponent();
				}
			}
		}

		return comp;
	}

	/**
	 * Stores the given plugin as last focused component of this page.
	 *
	 * @param plugin The focused plugin
	 */
	public void setFocusedPlugin(VisiblePlugin plugin)
	{
		if (plugin != null)
		{
			if (plugin == this)
			{
				focusedPluginRef = null;

				if (getPluginFocusComponent() == null)
				{
					// We cannot set the focus to ourself, so try the next best plugin
					List plugins = getVisiblePlugins();
					if (plugins != null && plugins.size() > 0)
					{
						plugin = (VisiblePlugin) plugins.get(0);
					}
				}
			}

			if (plugin != null)
			{
				focusedPluginRef = new WeakReference(plugin);
			}
			else
			{
				focusedPluginRef = null;
			}
		}
		else
		{
			focusedPluginRef = null;
		}
	}

	/**
	 * Restores the focus of this page.
	 * Sets the focus to the plugin that had the focus so far or simply tries to determine the component that follows the page component in the focus order.
	 */
	public void restoreFocus()
	{
		boolean hasRestored = false;

		if (focusedPluginRef != null)
		{
			// Try to restore the focus to the stored plugin if it still exists
			VisiblePlugin p = (VisiblePlugin) focusedPluginRef.get();

			if (p != null && p.getPluginHolder() != null)
			{
				// However, don't switch pages.
				p.focusPlugin();
				hasRestored = true;
			}
			else
			{
				// Plugin was garbage collected or removed from the container, kill the reference
				focusedPluginRef = null;
			}
		}

		if (!hasRestored)
		{
			// Unable to restore, choose the next component of the page component instead
			FocusManager.getCurrentManager().focusNextComponent(this.getPluginComponent());
		}
	}

	/**
	 * Advances the focus of this page.
	 *
	 * @param forward
	 *		true	Moves the focus to the next plugin.<br>
	 *		false	Moves the focus to the previous plugin.
	 * @param visibleOnly
	 *		true	Considers currently visible plugins only.<br>
	 *		false	Considers also plugins that are currently hidden
	 *				(i. e. non-active tabs in tabbed plugin containers)
	 */
	public void advanceFocus(boolean forward, boolean visibleOnly)
	{
		int currentIndex = -1;

		// Get a list of all (or all currently visible) plugins of this page
		// We assume the order delivered by the page is also the tab order
		List plugins = visibleOnly ? getVisiblePlugins() : getPlugins();
		if (plugins == null)
			return;
		int n = plugins.size();
		if (n == 0)
			return;

		for (int i = 0; i < n; ++i)
		{
			VisiblePlugin plugin = (VisiblePlugin) plugins.get(i);

			if (plugin.isPluginFocused())
			{
				// Save the index of the currently focused plugin
				currentIndex = i;
			}
		}

		// Advance the focus to the next/previous plugin.
		// This works also if no plugin is currently selected.
		int newIndex = currentIndex;
		if (forward)
		{
			if (++newIndex >= n)
				newIndex = 0;
		}
		else
		{
			if (--newIndex < 0)
				newIndex = n - 1;
		}

		if (newIndex != currentIndex)
		{
			// Set the focus to the determined plugin
			VisiblePlugin vp = (VisiblePlugin) plugins.get(newIndex);
			vp.focusPlugin();
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Keyboard support
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Sets up the key bindings of the page.
	 * Maps CTRL+TAB/SHIFT+CTRL+TAB to cycle the plugins of this page.
	 */
	protected void setupKeyBindings()
	{
		InputMap inputmap = getContentPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionmap = getContentPane().getActionMap();

		// Map F11 to 'Next visible plugin'
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "NextVisiblePlugin");
		actionmap.put("NextVisiblePlugin", new CycleAction(true, true));

		// Map SHIFT+F11 to 'Previous visible plugin'
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, KeyEvent.SHIFT_MASK), "PrevVisiblePlugin");
		actionmap.put("PrevVisiblePlugin", new CycleAction(false, true));

		// Map F12 to 'Next plugin'
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "NextPlugin");
		actionmap.put("NextPlugin", new CycleAction(true, false));

		// Map SHIFT+F12 to 'Previous plugin'
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, KeyEvent.SHIFT_MASK), "PrevPlugin");
		actionmap.put("PrevPlugin", new CycleAction(false, false));
	}

	/**
	 * Advances the plugin focus
	 */
	class CycleAction extends AbstractAction
	{
		/** Direction flag */
		private boolean forward;

		/** Cycle through the visible plugins only */
		private boolean visibleOnly;

		/**
		 * Constructor.
		 *
		 * @param forward
		 *		true	Moves the focus to the next plugin.<br>
		 *		false	Moves the focus to the previous plugin.
		 * @param visibleOnly
		 *		true	Considers currently visible plugins only.<br>
		 *		false	Considers also plugins that are currently hidden
		 *				(i. e. non-active tabs in tabbed plugin containers)
		 */
		public CycleAction(boolean forward, boolean visibleOnly)
		{
			this.forward = forward;
			this.visibleOnly = visibleOnly;
		}

		public void actionPerformed(ActionEvent e)
		{
			advanceFocus(forward, visibleOnly);
		}
	}
}
