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

import java.util.LinkedList;
import java.util.List;

/**
 * This class implements a queue that is restricted in size. If too much objects
 * are queued in, the oldest values get disposed.
 *
 * @author Falk Hartmann
 */
public class SizeRestrictedQueue
{
	/** This list holds the elements of the queue. */
	private List objects;

	/** This holds the maximum size of the queue. */
	private int maxSize;

	/** This holds the disposal listener. */
	private DisposalListener disposalListener;

	/**
	 * This is the constructor.
	 *
	 * @param maxSize The maximum size of the queue
	 */
	public SizeRestrictedQueue(int maxSize)
	{
		this.maxSize = maxSize;
		this.objects = new LinkedList();
	}

	/**
	 * Gets the current size of the queue.
	 * @nowarn
	 */
	public int size()
	{
		return objects.size();
	}

	/**
	 * This method adds an element to a queue.
	 *
	 * @param obj The object to be enqueued
	 * @param removeExisting
	 *		true	If the queue already contains the object, the object will be moved to the begin of the queue.<br>
	 *		false	Adds the object regardless if it is already present in the queue.
	 */
	public void enqueue(Object obj, boolean removeExisting)
	{
		if (removeExisting)
		{
			objects.remove(obj);
		}

		// Add the element.
		objects.add(obj);

		// Ensure the maximum size.
		ensureSizeRestriction();
	}

	/**
	 * Checks if the queue contains the given element.
	 * @nowarn
	 */
	public boolean contains(Object obj)
	{
		return objects.contains(obj);
	}

	/**
	 * This method ensures that the queue size doesn't exceed the adjusted size.
	 */
	private void ensureSizeRestriction()
	{
		// Only on thread at a time.
		synchronized (objects)
		{
			// If many thread operate on the queue, there might be more than one element to much!
			while (objects.size() > maxSize)
			{
				// Get the element to be disposed.
				Object disposedObject = objects.remove(0);

				// If we have a disposal listener...
				if (disposalListener != null)
				{
					// ...notify it.
					disposalListener.onDispose(disposedObject);
				}
			}
		}
	}

	/**
	 * This method sets a disposal listener which gets called when
	 * an element gets removed from the queue.
	 *
	 * @param disposalListener The disposal listener
	 */
	public void setDisposalListener(DisposalListener disposalListener)
	{
		// Hold the passed listener.
		this.disposalListener = disposalListener;
	}
}
