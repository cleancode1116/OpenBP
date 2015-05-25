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
package org.openbp.jaspira.gui.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.common.listener.ListenerSupport;

/**
 * The clipboard manager handles all clipboard activity of a Jaspira application.
 * It supports a multi-entry clipboard that can be connected to a
 * clipboard toolbox for easy access.
 *
 * @author Stephan Moritz
 */
public final class ClipboardMgr
	implements ClipboardOwner
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Number of clipboard entries.
	 * Currently, we don't support more than one clipboard entry in order not to preven gc.
	 */
	public static final int DEFAULT_CAPACITY = 1;

	/////////////////////////////////////////////////////////////////////////
	// @@ Data members
	/////////////////////////////////////////////////////////////////////////

	/** List containing the clipboard entries (contains Transferable objects). */
	private LinkedList entries;

	/** The current capacity of the clipboard. */
	private int capacity = DEFAULT_CAPACITY;

	/** The system clipboard beeing used. If null, only internal mechanisms are used. */
	private Clipboard systemClipboard;

	/** Singleton instance of the manager */
	private static ClipboardMgr instance;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction/Instantiation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the singleton instance of the clipboard manager.
	 * @nowarn
	 */
	public static synchronized ClipboardMgr getInstance()
	{
		if (instance == null)
		{
			instance = new ClipboardMgr();
		}
		return instance;
	}

	/**
	 * Private constructor, use getInstance ().
	 */
	private ClipboardMgr()
	{
		entries = new LinkedList();

		// We try to obtain the system clipboard
		// Currently, we don't support copying the contents to the system clipboard.
		// systemClipboard = Toolkit.getDefaultToolkit ().getSystemClipboard ();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Methods
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the number of entries currently in the clipboard.
	 * @nowarn
	 */
	public int getNumberOfEntries()
	{
		return entries.size();
	}

	/**
	 * Adds an entry to the clipboard. If the clipboard is already full, the
	 * last entry is removed.
	 *
	 * @param entry The entry to add
	 */
	public void addEntry(Transferable entry)
	{
		if (entry == null)
			return;

		// We try to remove the entry. That way, add would cause a revival of the
		// entry (i.e. moving it to the front)
		entries.remove(entry);

		entries.addFirst(entry);

		// Trim the clipboard to its capacity
		while (entries.size() > capacity)
		{
			entries.removeLast();
		}

		// We put the entry into the system clipboard as well...
		if (systemClipboard != null)
		{
			try
			{
				systemClipboard.setContents(entry, this);
			}
			catch (IllegalStateException e)
			{
				// No access to system clipboard, we ignore
			}
		}

		fireClipboardChanged();
	}

	/**
	 * Returns the current capacity of the clipboard.
	 * @nowarn
	 */
	public int getCapacity()
	{
		return capacity;
	}

	/**
	 * Sets the capacity of the clipboard. If capacity is less than the current
	 * number of elements, the last elements are discarded in order to match the
	 * new capacity.
	 * @nowarn
	 */
	public void setCapacity(int capacity)
	{
		this.capacity = capacity;

		// Trim the clipboard to its capacity
		boolean removed = false;
		while (entries.size() > capacity)
		{
			entries.removeLast();
			removed = true;
		}

		if (removed)
		{
			fireClipboardChanged();
		}
	}

	/**
	 * Returns the current entry of the clipboard. This the first entry in the
	 * list. If there currently are no entries in the board, returns null.
	 * @nowarn
	 */
	public Transferable getCurrentEntry()
	{
		if (entries.isEmpty())
		{
			return null;
		}

		return (Transferable) entries.getFirst();
	}

	/**
	 * Returns the entry with the given index.
	 * @nowarn
	 */
	public Transferable getEntryAt(int index)
	{
		return (Transferable) entries.get(index);
	}

	/**
	 * Returns all current entries.
	 * @nowarn
	 */
	public List getEntries()
	{
		return entries;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Clipboard listener support
	/////////////////////////////////////////////////////////////////////////

	/** Listener support object holding the listeners */
	private ListenerSupport listenerSupport;

	/**
	 * Fires a 'clipboard change' event to all registered clipboard change listeners.
	 */
	protected void fireClipboardChanged()
	{
		if (listenerSupport != null && listenerSupport.containsListeners(ChangeListener.class))
		{
			ChangeEvent e = null;
			for (Iterator it = listenerSupport.getListenerIterator(ChangeListener.class); it.hasNext();)
			{
				if (e == null)
					e = new ChangeEvent(this);
				((ChangeListener) it.next()).stateChanged(e);
			}
		}
	}

	/**
	 * Adds a property change listener to the listener list.
	 * A ChangeEvent will get fired in response to setting a bound property.
	 * The listener is registered for all properties as a WEAK listener, i. e. it may
	 * be garbage-collected if not referenced otherwise.<br>
	 * ATTENTION: Never add an automatic class (i. e new ChangeListener () { ... }) or an inner
	 * class that is not referenced otherwise as a weak listener to the list. These objects
	 * will be cleared by the garbage collector during the next gc run!
	 *
	 * @param listener The listener to be added
	 */
	public synchronized void addClipboardListener(ChangeListener listener)
	{
		if (listenerSupport == null)
		{
			listenerSupport = new ListenerSupport();
		}
		listenerSupport.addWeakListener(ChangeListener.class, listener);
	}

	/**
	 * Removes a property change listener from the listener list.
	 *
	 * @param listener The listener to be removed
	 */
	public synchronized void removeClipboardListener(ChangeListener listener)
	{
		if (listenerSupport != null)
		{
			listenerSupport.removeListener(ChangeListener.class, listener);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Clipboard owner
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Is called when the clipboard manager is no longer owner of the content of the system clipboard.
	 * This happens when an external application enters content into the clipboard.
	 *
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		addEntry(clipboard.getContents(null));
	}
}
