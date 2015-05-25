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
package org.openbp.common.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * This is an implementation of a simple hashtable which removes its elements after a given timeout period.
 * The expiration timeout is restarted on each lookup of an object.
 *
 * @author Falk Hartmann
 */
public class ExpirationHashtable
{
	/** The hashtable mapping keys to value descriptors. */
	private Hashtable valueDescriptors = new Hashtable();

	/** The time an object should be kept in the hashtable (milli seconds). */
	private long defaultTimeout = 0;

	/** The disposal listener. */
	private DisposalListener disposalListener;

	/** The cleanup thread. */
	private static CleanupThread cleanupThread;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param defaultTimeout The timeout in milliseconds, after which an object should
	 *                be removed from the hashtable. Each lookup of the object
	 *                restarts the timeout period.
	 */
	public ExpirationHashtable(long defaultTimeout)
	{
		// Hold timeout.
		this.defaultTimeout = defaultTimeout;

		// Create a single cleanup thread for all expiration hashtables
		if (cleanupThread == null)
		{
			cleanupThread = new CleanupThread();
			cleanupThread.start();
		}

		// Register at the cleanup thread.
		cleanupThread.register(this);
	}

	//////////////////////////////////////////////////
	// @@ Object maintainance.
	//////////////////////////////////////////////////

	/**
	 * This method checks, whether the passed object is used as key in this hashtable.
	 *
	 * @param key The key to check for
	 * @return true if the key is a valid key in the hashtable
	 */
	public boolean containsKey(Object key)
	{
		// Delegate to the hashtable itself.
		return valueDescriptors.containsKey(key);
	}

	/**
	 * Returns the entry set of the table.
	 *
	 * @return A set of Map.Entry objects
	 */
	public Set entrySet()
	{
		return valueDescriptors.entrySet();
	}

	/**
	 * Returns the key set of the table.
	 *
	 * @return The set of hash keys
	 */
	public Set keySet()
	{
		return valueDescriptors.keySet();
	}

	/**
	 * Returns the values of the table.
	 *
	 * @return The collection of hash values
	 */
	public Collection values()
	{
		return valueDescriptors.values();
	}

	/**
	 * Returns an enumeration of the values in this hashtable.
	 *
	 * @return an enumeration of the values in this hashtable
	 */
	public Enumeration elements()
	{
		// Delegate to the hashtable itself.
		return new ValueEnumeration(valueDescriptors.elements());
	}

	/**
	 * Returns the value to which the specified key is mapped in this hashtable.
	 *
	 * @param key A key in the hashtable
	 * @return the value to which the key is mapped in this hashtable; null
	 * if the key is not mapped to any value in this hashtable.
	 */
	public Object get(Object key)
	{
		// Try to get the value descriptor with this key.
		ValueDescriptor valueDescriptor = (ValueDescriptor) valueDescriptors.get(key);

		// If we have a descriptor...
		if (valueDescriptor != null)
		{
			// ...return the stored object.
			return valueDescriptor.getValue();
		}

		// Nothing to return...
		return null;
	}

	/**
	 * Returns an enumeration of the keys in this hashtable.
	 *
	 * @return an enumeration of the keys in this hashtable
	 */
	public Enumeration keys()
	{
		// Delegate to the hashtable itself.
		return valueDescriptors.keys();
	}

	/**
	 * Maps the specified key to the specified value in this hashtable.
	 * Neither the key nor the  value can be null. The value can be retrieved by
	 * calling the get method with a key that is equal to the original key.
	 * The timeout value used to construct the expiration hashtable is used as timeout.
	 *
	 * @param key the hashtable key
	 * @param value the value
	 * @return the previous value of the specified key in this hashtable, or null
	 * if it did not have one.
	 */
	public Object put(Object key, Object value)
	{
		return put(key, value, this.defaultTimeout);
	}

	/**
	 * Maps the specified key to the specified value in this hashtable.
	 * Neither the key nor the  value can be null. The value can be retrieved by
	 * calling the get method with a key that is equal to the original key.
	 *
	 * @param key the hashtable key
	 * @param value the value
	 * @param timeout the timeout in milliseconds, after which the value expires; use 0 for no expiration
	 * @return the previous value of the specified key in this hashtable, or null
	 * if it did not have one.
	 */
	public Object put(Object key, Object value, long timeout)
	{
		// Get the old value descriptor.
		ValueDescriptor valueDescriptor = (ValueDescriptor) valueDescriptors.put(key, new ValueDescriptor(value, timeout));

		// If there was an old value descriptor, return its value; otherwise return null.
		return valueDescriptor == null ? null : valueDescriptor.getValue();
	}

	/**
	 * This method removes the object registered with the given key and returns it.
	 *
	 * @param key The key to remove the value for
	 * @return The object removed
	 */
	public Object remove(Object key)
	{
		// Remove value information object.
		ValueDescriptor valueInformation = (ValueDescriptor) valueDescriptors.remove(key);

		// Remove and return value.
		return valueInformation == null ? null : valueInformation.getValue();
	}

	/**
	 * This method sets a disposal listener for this container. The listener
	 * will be invoked for each element that gets removed from the container
	 * due to expiration.
	 *
	 * @param disposalListener The listener to be called
	 */
	public void setDisposalListener(DisposalListener disposalListener)
	{
		// Hold the passed listener.
		this.disposalListener = disposalListener;
	}

	/**
	 * Returns the current size of the hashtable.
	 *
	 * @return The size of the hashtable
	 */
	public int size()
	{
		return valueDescriptors.size();
	}

	//////////////////////////////////////////////////
	// @@ Nested classes.
	//////////////////////////////////////////////////

	/**
	 * Class implementing a thread used to clean up expired hash table entries.
	 */
	private static class CleanupThread extends Thread
	{
		/** This maps the cleanup targets to themselves. */
		private Map targets;

		/**
		 * Default constructor.
		 */
		public CleanupThread()
		{
			super("Expiration table cleanup");

			// This thread must not prevent the VM shutdown.
			setDaemon(true);

			// Create a weak hashmap for the cleanup targets.
			targets = new WeakHashMap();
		}

		/**
		 * This method registers a hashtable to be cleaned up by this thread.
		 *
		 * @param ht The hashtable to be cleaned up
		 */
		public void register(ExpirationHashtable ht)
		{
			targets.put(ht, ht);
		}

		/**
		 * This method implements the run method.
		 */
		public void run()
		{
			// Cleanup is done in an infinite loop.
			for (;;)
			{
				// Iterate over all registered targets.
				Iterator targetIter = targets.keySet().iterator();

				// Cleanup each registered table
				while (targetIter.hasNext())
				{
					ExpirationHashtable ht = (ExpirationHashtable) targetIter.next();
					cleanup(ht);
				}

				// Put this thread to sleep for 10 seconds.
				try
				{
					sleep(10000);
				}
				catch (InterruptedException ie)
				{
					// Do nothing - we're in an infinite loop.
				}
			}
		}

		/**
		 * This method performs the cleanup for the particular target.
		 *
		 * @param ht The hashtable to clean up
		 */
		private void cleanup(ExpirationHashtable ht)
		{
			// Get current time.
			long now = System.currentTimeMillis();

			// Iterate over the last access dates.
			for (Enumeration keys = ht.valueDescriptors.keys(); keys.hasMoreElements();)
			{
				// Get key and value information.
				Object key = keys.nextElement();
				ValueDescriptor valueInformation = (ValueDescriptor) ht.valueDescriptors.get(key);

				// Check for expiration.
				long timeout = valueInformation.getTimeout();
				if (timeout > 0)
				{
					if (now > valueInformation.getLastAccessTime() + timeout)
					{
						// Enry is due for removal.
						// Inform the listener.
						if (ht.disposalListener != null)
						{
							ht.disposalListener.onDispose(key);
						}

						// Remove object from the valueInformation hashtable...
						if (ht.valueDescriptors != null)
						{
							ht.valueDescriptors.remove(key);
						}
					}
				}
			}
		}
	}

	/**
	 * Class representing a tupel of a value object, a last access time and a timeout.
	 */
	private static class ValueDescriptor
	{
		/** This holds the last access time. */
		private long lastAccessTime;

		/** This holds timeout in MS. */
		private long timeout;

		/** This holds the value itself. */
		private Object value;

		/**
		 * This is the constructor.
		 *
		 * @param value The value for which a descriptor should be constructed
		 * @param timeout The timeout in MS after which the value should expire
		 */
		public ValueDescriptor(Object value, long timeout)
		{
			this.value = value;
			this.lastAccessTime = System.currentTimeMillis();
			this.timeout = timeout;
		}

		/**
		 * Returns the time when the value in the descriptor has been touched last.
		 *
		 * @return The time when the value has been touched
		 */
		public long getLastAccessTime()
		{
			return lastAccessTime;
		}

		/**
		 * Return the timeout (in milliseconds) after which the value should expire.
		 *
		 * @return The timeout for the value
		 */
		public long getTimeout()
		{
			return timeout;
		}

		/**
		 * Returns the value. As a sideeffect, this touches the value, i.e., it sets its
		 * last access time to the current system time.
		 *
		 * @return The value
		 */
		public Object getValue()
		{
			// This is an access, so touch the object.
			lastAccessTime = System.currentTimeMillis();

			// Return the value.
			return value;
		}
	}

	/**
	 * This inner class represents an enumeration over values stored in this
	 * hashtable which is based on an enumeration over the value descriptors.
	 */
	private static final class ValueEnumeration
		implements Enumeration
	{
		/** This holds the enumeration over the value descriptors. */
		private Enumeration valueDescriptorEnumeration;

		/**
		 * This creates a value enumeration based on the passed value
		 * descriptor enumeration.
		 *
		 * @param valueDescriptorEnumeration The value descriptor enumeration
		 */
		public ValueEnumeration(Enumeration valueDescriptorEnumeration)
		{
			this.valueDescriptorEnumeration = valueDescriptorEnumeration;
		}

		/**
		 * This checks, whether there are more elements to enumerate. This is
		 * implemented by simple delegation to the underlying value descriptor
		 * enumeration.
		 *
		 * @return true, if there are more elements to be enumerated
		 */
		public boolean hasMoreElements()
		{
			// Just delegate.
			return valueDescriptorEnumeration.hasMoreElements();
		}

		/**
		 * This returns the next value to be enumerated.
		 *
		 * @return The next element
		 */
		public Object nextElement()
		{
			// Get the next value descriptor.
			ValueDescriptor valueDescriptor = (ValueDescriptor) valueDescriptorEnumeration.nextElement();

			// If we have a value descriptor now, return its value; otherwise return null.
			return valueDescriptor == null ? null : valueDescriptor.getValue();
		}
	}
}
