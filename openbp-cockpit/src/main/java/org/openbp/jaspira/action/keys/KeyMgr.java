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
package org.openbp.jaspira.action.keys;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventMgr;
import org.openbp.jaspira.event.KeySequenceEvent;
import org.openbp.jaspira.gui.plugin.ApplicationBase;
import org.openbp.jaspira.gui.plugin.PluginFocusMgr;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugins.statusbar.StatusBarTextEvent;

/**
 * The key manager is the central instance for handling keysequences. It transforms
 * those sequences into key sequence events that are passed through the plugin hierarchy.
 * In order to prevent unnecessesary events, it transforms only those sequences that have
 * previously been registered with the manager.
 * In order to make a toplevel component key aware, use the install method.
 *
 * @author Stephan Moritz
 */
public final class KeyMgr
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Static data
	/////////////////////////////////////////////////////////////////////////

	/** Escape key for input/action maps */
	public static final String ESCAPE = "escape";

	/** The singleton instance. */
	private static KeyMgr singleton;

	/**
	 * Table that maps key names (strings) to key codes (Integer objects).
	 * This table contains all keys defined in the java.awt.event.KeyEvent class.
	 * For VK_* keys, the "VK_" prefix is removed from the name.
	 */
	private static final Map keyCodes;

	/**
	 * Table that maps key codes (Integer objects) to key names (strings).
	 * This table contains all keys defined in the java.awt.event.KeyEvent class.
	 * For VK_* keys, the "VK_" prefix is removed from the name.
	 */
	private static final Map keyStrings;

	static
	{
		keyCodes = new HashMap();
		keyStrings = new HashMap();

		// We generate a map between keynames and keycodes... Tiresome but necessary
		try
		{
			Field [] fields = KeyEvent.class.getFields();

			for (int i = 0; i < fields.length; i++)
			{
				Field field = fields [i];

				if (field.getName().startsWith("VK_"))
				{
					String name = field.getName().substring(3);
					Integer value = Integer.valueOf(field.getInt(null));

					keyCodes.put(name, value);
					keyStrings.put(value, name);
				}
			}
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Root of the key tree */
	private KeyTree rootTree;

	/** The currently active key tree */
	private KeyTree currentTree;

	/** Action that is used to reset the current state of the key manager */
	private Action resetAction;

	/** Timer to set the input map back to the root map */
	private Timer resetTimer;

	/** Map with JComponent - MultiInputMap pairs (installed components and their rootmaps) */
	private List clients;

	/** Flag that the {@link #resetClients} method should not be called when adding key sequences */
	private int updateSuspendLevel;

	/** Perform update after {@link #resumeUpdate} */
	private boolean performUpdate;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * No public constructor, use getInstance () instead.
	 */
	private KeyMgr()
	{
		super();

		rootTree = new KeyTree();
		currentTree = rootTree;

		clients = new ArrayList(4);

		resetAction = new AbstractAction()
		{
			/**
			 * Changes the input maps of all clients back to the root map.
			 */
			public void actionPerformed(ActionEvent e)
			{
				resetTimer.stop();

				JaspiraEventMgr.fireGlobalEvent(new StatusBarTextEvent(null, null));

				currentTree = rootTree;
				resetClients();
			}
		};

		resetTimer = new Timer(3000, resetAction);
	}

	//////////////////////////////////////////////////
	// @@ Static utitlity methods
	//////////////////////////////////////////////////

	/**
	 * Converts the given string to a keycode, as defined in the java\.awt\.event\.KeyEvent class.
	 * Note that the "VK_" must not be prepended to the string.
	 * @param keyName Key name to translate
	 * @return The key code or -1 if no such key exists
	 */
	public static int stringToKeyCode(String keyName)
	{
		Integer value = (Integer) keyCodes.get(keyName.toUpperCase());
		return (value != null) ? value.intValue() : -1;
	}

	/**
	 * Converts the given key code to a string, as defined in the java\.awt\.event\.KeyEvent class.
	 * Note that the "VK_" must not be prepended to the string.
	 * @param keyCode Key code to translate
	 * @return The key code or -1 if no such key exists
	 */
	public static String keyCodeToString(int keyCode)
	{
		String result = (String) keyStrings.get(Integer.valueOf(keyCode));
		return (result != null) ? result.toUpperCase() : null;
	}

	/**
	 * Returns the singleton instance of the key manager, creating it if
	 * necessary.
	 * @nowarn
	 */
	public static synchronized KeyMgr getInstance()
	{
		if (singleton == null)
		{
			singleton = new KeyMgr();
		}
		return singleton;
	}

	/**
	 * Installs the manager for the given JComponent. This should usually be
	 * the content pane of a toplevel container. Note that installing the manager
	 * to ANY component of a toplevel container activates the manager for the
	 * whole container. DO NOT call this method more than once for any component
	 * of the same tlc.
	 *
	 * @param c Component to install the manager to
	 */
	public void install(JComponent c)
	{
		clients.add(c);

		new MultiInputMap(c);
		new MultiActionMap(c);
	}

	/**
	 * Uninstalls the manager from the given JComponent.
	 * Resets the input and action map of the container and removes the container from the manager's client list.
	 *
	 * @param c Component to uninstall the manager from
	 */
	public void uninstall(JComponent c)
	{
		clients.remove(c);

		((MultiActionMap) c.getActionMap()).uninstall(c);
		((MultiInputMap) c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)).uninstall();
	}

	/**
	 * Suspends updates of the clients of this key manager.
	 * This can be called before adding new key sequences to the manager.
	 */
	public void suspendUpdate()
	{
		++updateSuspendLevel;
	}

	/**
	 * Resumes updates of the clients of this key manager.
	 * This must be called after adding new key sequences to the manager
	 * when the {@link #suspendUpdate} method has been called before.
	 */
	public void resumeUpdate()
	{
		if (--updateSuspendLevel <= 0)
		{
			if (performUpdate)
			{
				resetClients();
				performUpdate = false;
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Methods
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Adds the given sequences to the list of recognized sequences.
	 * @param sequences Iterator of {@link KeySequence} objects to add
	 */
	public void addSequences(Iterator sequences)
	{
		while (sequences.hasNext())
		{
			KeySequence ks = (KeySequence) sequences.next();
			rootTree.addSequence(ks);
		}
		resetClients();
	}

	/**
	 * Removes the given sequencess to the list of recognized sequences.
	 * @param sequences Iterator of {@link KeySequence} objects to remove
	 */
	public void removeSequences(Iterator sequences)
	{
		while (sequences.hasNext())
		{
			KeySequence ks = (KeySequence) sequences.next();
			rootTree.removeSequence(ks);
		}
		resetClients();
	}

	/**
	 * Adds the given sequence to the list of recognized sequences.
	 * @param sequence Sequence to add
	 */
	public void addSequence(KeySequence sequence)
	{
		rootTree.addSequence(sequence);
		resetClients();
	}

	/**
	 * Removes the given sequences to the list of recognized sequences.
	 * @param sequence Sequence to remove
	 */
	public void removeSequence(KeySequence sequence)
	{
		rootTree.removeSequence(sequence);
		resetClients();
	}

	/**
	 * Resets the input maps of all clients.
	 * Needs to be called if sequences have been added or removed or if the
	 * current sequence tree has changed.
	 */
	void resetClients()
	{
		if (updateSuspendLevel <= 0)
		{
			for (Iterator it = clients.iterator(); it.hasNext();)
			{
				JComponent next = (JComponent) it.next();

				next.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, next.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW));
			}
		}
		else
		{
			performUpdate = true;
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Inner classes
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Updates the actualKey sequence of the manager, throws the event and restores the
	 * manager if no more combinations are possible.
	 */
	public class KeyAction extends AbstractAction
	{
		private KeyTree tree;

		public KeyAction(KeyTree tree)
		{
			this.tree = tree;
		}

		public void actionPerformed(ActionEvent e)
		{
			currentTree = tree;

			KeySequence sequence = tree.getSequence();

			Plugin plugin = PluginFocusMgr.getInstance().getFocusedPlugin();
			if (plugin == null)
			{
				plugin = ApplicationBase.getInstance();
			}

			// First, fire the key event bottom up starting from the current plugin
			KeySequenceEvent event = new KeySequenceEvent(plugin, sequence, JaspiraEvent.TYPE_BOTTOM_UP);
			plugin.fireEvent(event);

			if (!event.isConsumed())
			{
				// None cared, broadcast the event application-wide
				event = new KeySequenceEvent(plugin, sequence, JaspiraEvent.TYPE_FLOOD);
				plugin.fireEvent(event);
			}

			if (currentTree.hasChildren())
			{
				// Set up the time that reset the current key sequence
				if (resetTimer.isRunning())
				{
					resetTimer.restart();
				}
				else
				{
					resetTimer.start();
				}

				// Display the sequence entered so far in the status bar
				JaspiraEventMgr.fireGlobalEvent(new StatusBarTextEvent(null, sequence.toString()));

				resetClients();
			}
			else
			{
				// There are no possible further keys, reset the manager's state
				resetAction.actionPerformed(e);
			}
		}
	}

	/**
	 * Input Map that supports two different parent map.
	 * Allows one parent map to be shared between components.
	 * Adding and removal of entries is delegated to the true parent.
	 */
	public class MultiInputMap extends ComponentInputMap
	{
		/** The originql map, cached for perfomance. */
		private ComponentInputMap parent;

		/////////////////////////////////////////////////////////////////////////
		// @@ Construction
		/////////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new MultiInputMap to a given component and shared parentmap.
		 * @param component Component the map should belong to
		 */
		public MultiInputMap(JComponent component)
		{
			super(component);

			parent = (ComponentInputMap) component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			if (parent == null)
			{
				parent = new ComponentInputMap(component);
			}

			component.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, this);
		}

		/**
		 * Uninstalls the multi inout map from the given component and restores the old input map.
		 * Does nothing if the component has not been assigned a multi input map.
		 */
		public void uninstall()
		{
			JComponent component = getComponent();

			InputMap im = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			if (im instanceof MultiInputMap)
			{
				component.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, ((MultiInputMap) im).parent);
			}
		}

		/**
		 * Returns the binding for given key, messaging the
		 * parent InputMap if the binding is not locally defined.
		 *
		 * @param keyStroke Key to get the binding for
		 * @return The binding or null if no binding exists for this key
		 */
		public Object get(KeyStroke keyStroke)
		{
			KeyTree value = currentTree.getSubTree(keyStroke);
			if (value != null)
			{
				return value;
			}

			if (keyStroke.equals(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)))
			{
				return ESCAPE;
			}

			return parent.get(keyStroke);
		}

		/**
		 * Returns an array of the KeyStrokes defined in this
		 * InputMap and its parent. This differs from keys() in that
		 * this method includes the keys defined in the parent.
		 * @nowarn
		 */
		public KeyStroke [] allKeys()
		{
			if (parent.size() == 0)
			{
				return currentTree.keys();
			}

			if (!currentTree.hasChildren())
			{
				return parent.keys();
			}

			Set set = new HashSet();

			KeyStroke [] keys = currentTree.keys();
			for (int i = keys.length - 1; i >= 0; i--)
			{
				set.add(keys [i]);
			}

			KeyStroke [] parentKeys = parent.keys();
			for (int i = parentKeys.length - 1; i >= 0; i--)
			{
				set.add(parentKeys [i]);
			}

			if (currentTree != rootTree)
			{
				// We are somewhere in the tree, add esc
				set.add(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
			}

			KeyStroke [] allKeys = new KeyStroke [set.size()];
			return (KeyStroke []) set.toArray(allKeys);
		}

		/////////////////////////////////////////////////////////////////////////
		// @@ Delegation to parent
		/////////////////////////////////////////////////////////////////////////

		/**
		 * @see javax.swing.ComponentInputMap#clear()
		 */
		public void clear()
		{
			parent.clear();
		}

		/**
		 * @see javax.swing.ComponentInputMap#getComponent()
		 */
		public JComponent getComponent()
		{
			return parent.getComponent();
		}

		/**
		 * @see javax.swing.InputMap#getParent()
		 */
		public InputMap getParent()
		{
			return parent.getParent();
		}

		/**
		 * @see javax.swing.InputMap#keys()
		 */
		public KeyStroke [] keys()
		{
			return parent.keys();
		}

		/**
		 * @see javax.swing.ComponentInputMap#put(javax.swing.KeyStroke, java.lang.Object)
		 */
		public void put(KeyStroke keyStroke, Object actionMapKey)
		{
			parent.put(keyStroke, actionMapKey);
		}

		/**
		 * @see javax.swing.ComponentInputMap#remove(javax.swing.KeyStroke)
		 */
		public void remove(KeyStroke key)
		{
			parent.remove(key);
		}

		/**
		 * @see javax.swing.ComponentInputMap#setParent(javax.swing.InputMap)
		 */
		public void setParent(InputMap map)
		{
			parent.setParent(map);
		}

		/**
		 * @see javax.swing.InputMap#size()
		 */
		public int size()
		{
			return parent.size();
		}
	}

	/**
	 * Action Map that creates new Actions for pressed keys "on the fly".
	 * Adding and removal of entries is delegated to the true parent.
	 */
	public class MultiActionMap extends ActionMap
	{
		/** The original map, cached for perfomance. */
		private ActionMap parent;

		/////////////////////////////////////////////////////////////////////////
		// @@ Construction
		/////////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new MultiActionMap for the given Component.
		 * @param component Component the map should belong to
		 */
		public MultiActionMap(JComponent component)
		{
			super();

			parent = component.getActionMap();
			if (parent != null)
			{
				parent = new ActionMap();
			}

			component.setActionMap(this);
		}

		/**
		 * Uninstalls the multi inout map from the given component and restores the old input map.
		 * Does nothing if the component has not been assigned a multi input map.
		 * @nowarn
		 */
		public void uninstall(JComponent component)
		{
			ActionMap am = component.getActionMap();
			if (am instanceof MultiActionMap)
			{
				component.setActionMap(((MultiActionMap) am).parent);
			}
		}

		/**
		 * Returns the binding for given key, messaging the
		 * parent ActionMap if the binding is not locally defined.
		 *
		 * @param key Key to get the binding for
		 * @return The binding or null if no binding exists for this key
		 */
		public Action get(Object key)
		{
			if (key instanceof KeyTree)
			{
				return new KeyAction((KeyTree) key);
			}

			if (ESCAPE.equals(key))
			{
				return resetAction;
			}

			return parent.get(key);
		}

		/**
		 * Returns an array of the keys defined in this <code>ActionMap</code> and
		 * its parent. This method differs from <code>keys()</code> in that
		 * this method includes the keys defined in the parent.
		 * @nowarn
		 */
		public Object [] allKeys()
		{
			if (parent.size() == 0)
			{
				return currentTree.subTrees();
			}

			if (!currentTree.hasChildren())
			{
				return keys();
			}

			Set set = new HashSet();

			Object [] keys = currentTree.subTrees();
			for (int i = keys.length - 1; i >= 0; i--)
			{
				set.add(keys [i]);
			}

			Object [] parentKeys = parent.keys();
			for (int i = parentKeys.length - 1; i >= 0; i--)
			{
				set.add(parentKeys [i]);
			}

			set.add(ESCAPE);

			Object [] allKeys = new Object [set.size()];
			return set.toArray(allKeys);
		}

		/////////////////////////////////////////////////////////////////////////
		// @@ Delegation to parent
		/////////////////////////////////////////////////////////////////////////

		/**
		 * @see javax.swing.ActionMap#clear()
		 */
		public void clear()
		{
			parent.clear();
		}

		/**
		 * @see javax.swing.ActionMap#getParent()
		 */
		public ActionMap getParent()
		{
			return parent.getParent();
		}

		/**
		 * @see javax.swing.ActionMap#keys()
		 */
		public Object [] keys()
		{
			return parent.keys();
		}

		/**
		 * @see javax.swing.ActionMap#put(java.lang.Object, javax.swing.Action)
		 */
		public void put(Object key, Action action)
		{
			parent.put(key, action);
		}

		/**
		 * @see javax.swing.ActionMap#remove(java.lang.Object)
		 */
		public void remove(Object key)
		{
			parent.remove(key);
		}

		/**
		 * @see javax.swing.ActionMap#setParent(javax.swing.ActionMap)
		 */
		public void setParent(ActionMap map)
		{
			parent.setParent(map);
		}

		/**
		 * @see javax.swing.ActionMap#size()
		 */
		public int size()
		{
			return parent.size();
		}
	}
}
