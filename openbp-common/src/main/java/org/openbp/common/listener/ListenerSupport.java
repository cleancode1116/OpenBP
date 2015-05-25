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
package org.openbp.common.listener;

import java.util.EventListener;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openbp.common.util.WeakArrayList;
import org.openbp.common.util.iterator.EmptyIterator;

/**
 * This class holds a list of event listeners.
 * It can be used to implement generic listener functionality.
 * It combines listeners of various type into a single 'listener manager' class.
 * Listeners can be added as regular 'hard' references ({@link #addListener}) or as 'weak' references
 * ({@link #addWeakListener}), which may be garbage-collected if not referenced otherwise.
 * The add-methods also ensure that the same listener will not be registered twice
 * (comparing the listener objects using the == operation).
 *
 * Listeners that have not been removed from the object they have been registered with
 * are a common cause for memory leaks. By adding them as weak listeners, you may not just
 * add and forget them. They will be automatically garbage-collected if not referenced otherwise
 * and automatically (i. e. after a call to the {@link #getListenerIterator} method) removed from
 * the listener list.<br>
 *
 * ATTENTION: Never add an automatic class (i. e new FocusListener () { ... }) or an inner
 * class that is not referenced otherwise as a weak listener to the list. These objects
 * will be cleared by the garbage collector during the next gc run!
 *
 * The listeners and the corresponding listener classes will be stored in a single array list
 * of type {@link WeakArrayList} the listener classes using hard links and the listeners as either
 * hard or weak references. The {@link WeakArrayList} class is allocated lazily. This minimizes the
 * footprint of the ListenerSupport class as long as no listeners have been added.
 *
 * For access to the listeners, you may use the {@link #getListenerIterator} method.
 * It will return a static instance of {@link EmptyIterator} if no listeners of the desired type
 * have been registered, minimizing allocation overhead. However, if matching listeners have
 * been created, a small-footprint iterator class will be constructed, but this should be
 * tolerable.
 *
 * @author Heiko Erhardt
 */
public class ListenerSupport
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/**
	 * List of listeners.
	 * The list will always be of even size; the first element being the listener class
	 * and the second element being the actual listener as hard or weak reference.
	 */
	private WeakArrayList list;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ListenerSupport()
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Adds a listener to the list (using a regular hard reference).
	 * The method ensures that the same listener will not be added twice
	 * (comparing the listener objects using the == operation).
	 *
	 * @param listenerClass Listener class of the listener
	 * @param listener Listener to add
	 */
	public void addListener(Class listenerClass, EventListener listener)
	{
		if (list == null)
		{
			list = new WeakArrayList();
		}
		else
		{
			// First, trim the list
			trim();

			if (containsListener(listenerClass, listener))
				return;
		}

		// Add the class always as hard reference
		list.addHardReference(listenerClass);

		// Add the listener also as hard reference
		list.addHardReference(listener);
	}

	/**
	 * Adds a listener to the list (using a weak reference).
	 * The method ensures that the same listener will not be added twice
	 * (comparing the listener objects using the == operation).<br>
	 * Weak references may be garbage-collected if not referenced otherwise.<br>
	 * ATTENTION: Never add an automatic class (i. e new FocusListener () { ... }) or an inner
	 * class that is not referenced otherwise as a weak listener to the list. These objects
	 * will be cleared by the garbage collector during the next gc run!
	 *
	 * @param listenerClass Listener class of the listener
	 * @param listener Listener to add
	 */
	public void addWeakListener(Class listenerClass, EventListener listener)
	{
		if (list == null)
		{
			list = new WeakArrayList();
		}
		else
		{
			// First, trim the list
			trim();

			if (containsListener(listenerClass, listener))
				return;
		}

		// Add the class always as hard reference
		list.addHardReference(listenerClass);

		// Add the listener also as weak reference
		list.add(listener);
	}

	/**
	 * Removes a listener from the list.
	 *
	 * @param listenerClass Listener class of the listener<br>
	 * If this parameter is null, all listener objects (regardless of the type)
	 * of the specified listener instance will be removed.
	 * @param listener Listener to remove<br>
	 * The listener may be either a hard or a weak listener.<br>
	 * If this parameter is null, all listeners of the specified class will be removed.<br>
	 * If both parameters are null, the method will clear the listener list.
	 */
	public void removeListener(Class listenerClass, EventListener listener)
	{
		if (list == null)
			return;

		boolean doTrim = false;

		int n = list.size();
		for (int i = 0; i < n; i += 2)
		{
			if (listenerClass != null)
			{
				Class cls = (Class) list.get(i);
				if (cls != listenerClass)
					continue;
			}

			Object o = list.get(i + 1);
			if (o != null)
			{
				if (listener == null || listener == o)
				{
					// Null out the listener data; the space will be removed by compacting the list
					list.set(i, null);
					list.set(i + 1, null);
					doTrim = true;
				}
			}
			else
			{
				// A listener has been garbage-collected, we should compact the list
				// Null out the listener class
				list.set(i, null);
				doTrim = true;
			}
		}

		if (doTrim)
		{
			list.trim();
		}
	}

	/**
	 * Removes all listeners from the list.
	 *
	 * @param listenerClass Class of the listeners to remove.<br>
	 * If this parameter is null, the method will clear the listener list.
	 */
	public void removeAllListeners(Class listenerClass)
	{
		removeListener(listenerClass, null);
	}

	/**
	 * Removes 'dead' (i\.e\. garbage-collected) weak listeners from the list.
	 */
	public void trim()
	{
		if (list != null)
		{
			boolean doTrim = false;

			int n = list.size();
			for (int i = 0; i < n; i += 2)
			{
				Object o = list.get(i + 1);
				if (o == null)
				{
					// A listener has been garbage-collected, we should compact the list
					// Null out the listener class
					list.set(i, null);
					list.set(i + 1, null);
					doTrim = true;
				}
			}

			if (doTrim)
			{
				list.trim();
			}
		}
	}

	/**
	 * Checks if the list contains listeners of a particular type.
	 * The method will consider only listeners that have not been garbage-collected.
	 *
	 * @param listenerClass Listener class to search for<br>
	 * If this parameter is null, the search will accept listeners of any class.
	 * @return
	 *		true	At least one matching listener has been found.<br>
	 *		false	The list contains no matching listeners or the listeners have been gc'ed.
	 */
	public boolean containsListeners(Class listenerClass)
	{
		return containsListener(listenerClass, null);
	}

	/**
	 * Checks if the list contains a particular listener or listeners of a particular type.
	 * The method will consider only listeners that have not been garbage-collected.
	 *
	 * @param listenerClass Listener class to search for<br>
	 * If this parameter is null, the search will accept listeners of any class.
	 * @param listener Listener instance to search for<br>
	 * If this parameter is null, the search will consider the listener class only.
	 * If both parameters are null, the method will merely check if the list contains
	 * at least one valid listener reference.
	 * @return
	 *		true	At least one matching listener has been found.<br>
	 *		false	The list contains no matching listeners or the listeners have been gc'ed.
	 */
	public boolean containsListener(Class listenerClass, EventListener listener)
	{
		if (list != null)
		{
			int n = list.size();
			for (int i = 0; i < n; i += 2)
			{
				if (listenerClass != null)
				{
					Class cls = (Class) list.get(i);
					if (cls != listenerClass)
						continue;
				}

				// Get the listener
				Object o = list.get(i + 1);
				if (o != null)
				{
					if (listener != null)
					{
						if (listener == o)
						{
							// We found the desired listener
							return true;
						}
					}
					else
					{
						// We found one valid listener
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Gets the number of listeners of the specified type.
	 *
	 * @param listenerClass Listener class
	 * @return The number of registered listeners of this class
	 */
	public int getListenerCount(Class listenerClass)
	{
		int count = 0;

		if (list != null)
		{
			int n = list.size();
			for (int i = 0; i < n; i += 2)
			{
				Class cls = (Class) list.get(i);
				if (cls == listenerClass)
				{
					++count;
				}
			}
		}

		return count;
	}

	/**
	 * Gets an iterator over listeners of the specified type.
	 *
	 * @param listenerClass Listener class
	 * @return An iterator of registered listeners of this class. The Iterator.next method
	 * of the returned iterator will return objects of type java.util.EventListener.<br>
	 * The method returns a static instance of {@link EmptyIterator} if no listeners of the desired type
	 * have been registered, minimizing allocation overhead. However, if matching listeners have
	 * been created, a small-footprint iterator class will be constructed, but this should be
	 * tolerable.<br>
	 * You should not call one of the methods {@link #addListener}, {@link #addWeakListener} or {@link #trim}
	 * while iterating the listener list.
	 * The returned iterator will automatically perform a {@link #trim} operation after the Iterator.hasNext
	 * method returns false (i. e. the end of the list has been reached) and 'dead' listener
	 * references have been detected in order to keep the listener list clean.<br>
	 * Note that the returned iterator does not support the Iterator.remove method.
	 */
	public Iterator getListenerIterator(Class listenerClass)
	{
		if (getListenerCount(listenerClass) == 0)
		{
			return EmptyIterator.getInstance();
		}

		return new ListenerIterator(listenerClass);
	}

	//////////////////////////////////////////////////
	// @@ ListenerIterator
	//////////////////////////////////////////////////

	/**
	 * Convenience class that can serve as an empty Iterator.
	 */
	public class ListenerIterator
		implements Iterator
	{
		/** Listener class */
		private Class listenerClass;

		/** Current list index. Will always point to the listener class object. */
		private int index;

		/** Flag that the list needs to be trimmed */
		private boolean doTrim;

		/**
		 * Constructor.
		 *
		 * @param listenerClass Listener class
		 */
		public ListenerIterator(Class listenerClass)
		{
			this.listenerClass = listenerClass;
		}

		/**
		 * Implementation of Iterator interface.
		 * @return Always false
		 */
		public boolean hasNext()
		{
			int n = list.size();
			for (; index < n; index += 2)
			{
				// Get the listener
				Object o = list.get(index + 1);
				if (o == null)
				{
					// Dead listener, we should trim
					doTrim = true;

					// Skip this listener
					continue;
				}

				Class cls = (Class) list.get(index);
				if (cls == listenerClass)
				{
					// We found one, stop here
					return true;
				}
			}

			if (doTrim)
			{
				// Dead listeners have been found, compact the list
				trim();
			}

			// No listener of the desired class found
			return false;
		}

		/**
		 * Implementation of the Iterator interface.
		 * @return Nothing, throws a NoSuchElementException instead
		 */
		public Object next()
		{
			if (index < list.size())
			{
				// Get the listener
				Object o = list.get(index + 1);

				// Advance the current index
				index += 2;

				// Return the listener
				return o;
			}
			throw new NoSuchElementException("Iterator reached end of ListenerSupport list");
		}

		/**
		 * Implementation of the Iterator interface.
		 * Always throws an IllegalStateException.
		 */
		public void remove()
		{
			throw new IllegalStateException("Remove operation not supported by ListenerSupport iterator");
		}
	}
}
