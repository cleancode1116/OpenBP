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
package org.openbp.swing.components.wizard;

import java.util.HashMap;
import java.util.Map;

import org.openbp.common.util.CopyUtil;

/**
 * Sequence manager implementation.
 *
 * @author Heiko Erhardt
 */
public class SequenceManagerImpl
	implements SequenceManager, Cloneable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** First element name */
	private String first;

	/** Current element name */
	private String current;

	/** Map of successors (maps element names) */
	private Map next;

	/** Map of predecessors (maps element names) */
	private Map prev;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SequenceManagerImpl()
	{
		next = new HashMap();
		prev = new HashMap();
	}

	/**
	 * Creates a clone of this object.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained members failed
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		SequenceManagerImpl clone = (SequenceManagerImpl) super.clone();

		clone.next = CopyUtil.copyMap(next, CopyUtil.CLONE_NONE);
		clone.prev = CopyUtil.copyMap(prev, CopyUtil.CLONE_NONE);

		return clone;
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the first element.
	 * @nowarn
	 */
	public String getFirst()
	{
		return first;
	}

	/**
	 * Sets the name of the first element.
	 * @nowarn
	 */
	public void setFirst(String first)
	{
		this.first = first;
	}

	/**
	 * Gets the name of the current element.
	 * @nowarn
	 */
	public String getCurrent()
	{
		return current;
	}

	/**
	 * Sets the name of the current element.
	 * @nowarn
	 */
	public void setCurrent(String current)
	{
		this.current = current;
	}

	/**
	 * Gets the name of the element that succeeds the current element.
	 *
	 * @return The next element or null if this element does not have a successor
	 */
	public String getNext()
	{
		return getNext(null);
	}

	/**
	 * Gets the name of the element that preceeds the current element.
	 *
	 * @return The next element or null if this element does not have a prdecessor
	 */
	public String getPrevious()
	{
		return getPrevious(null);
	}

	/**
	 * Gets the name of the element that succeeds the specified element.
	 *
	 * @param name Name of the element or null for the current element
	 * @return The next element or null if this element does not have a successor
	 */
	public String getNext(String name)
	{
		if (name == null)
			name = current;
		return (String) next.get(name);
	}

	/**
	 * Gets the name of the element that preceeds the specified element.
	 *
	 * @param name Name of the element or null for the current element
	 * @return The next element or null if this element does not have a prdecessor
	 */
	public String getPrevious(String name)
	{
		if (name == null)
			name = current;
		return (String) prev.get(name);
	}

	/**
	 * Chains the current element with the specified element.
	 *
	 * @param link Name of the successor or null if this element is the last element
	 */
	public void chain(String link)
	{
		chain(null, link);
	}

	/**
	 * Chains the specified elements.
	 *
	 * @param name Name of the element or null for the current element
	 * @param link Name of the successor or null if this element is the last element
	 */
	public void chain(String name, String link)
	{
		if (name != null)
		{
			if (link != null)
			{
				next.put(name, link);
				prev.put(link, name);
			}
			else
			{
				next.remove(name);
			}
		}
		else
		{
			first = link;
		}
	}

	/**
	 * Sets the name of the element that succeeds the current element.
	 *
	 * @param link Name of the successor or null if this element is the last element
	 */
	public void setNext(String link)
	{
		setNext(current, link);
	}

	/**
	 * Sets the name of the element that succeeds the specified element.
	 *
	 * @param name Name of the element or null for the current element
	 * @param link Name of the successor or null if this element is the last element
	 */
	public void setNext(String name, String link)
	{
		if (name != null)
		{
			if (link != null)
			{
				next.put(name, link);
			}
			else
			{
				next.remove(name);
			}
		}
	}

	/**
	 * Sets the name of the element that preceeds the current element.
	 *
	 * @param link Name of the predecessor or null if this element is the first element
	 */
	public void setPrevious(String link)
	{
		setPrevious(current, link);
	}

	/**
	 * Sets the name of the element that preceeds the specified element.
	 *
	 * @param name Name of the element or null for the current element
	 * @param link Name of the predecessor or null if this element is the first element
	 */
	public void setPrevious(String name, String link)
	{
		if (name != null)
		{
			if (link != null)
			{
				prev.put(name, link);
			}
			else
			{
				prev.remove(name);
			}
		}
	}

	/**
	 * Removes an element from the sequence.
	 * The predecessor and the successor of the element will be linked with each other, if any.
	 *
	 * @param name Name of the page to remove
	 */
	public void remove(String name)
	{
		String p = (String) prev.remove(name);
		String n = (String) next.remove(name);

		if (p != null)
		{
			if (n != null)
				next.put(p, n);
			else
				next.remove(p);
		}

		if (n != null)
		{
			if (p != null)
				prev.put(n, p);
			else
				prev.remove(n);
		}

		if (name.equals(first))
		{
			first = n;
		}

		if (name.equals(current))
		{
			current = n != null ? n : p;
		}
	}

	/**
	 * Clears all sequence manager information.
	 * Keeps the first and current references.
	 */
	public void clearSequence()
	{
		next.clear();
		prev.clear();
	}

	/**
	 * Clears all sequence manager information, including the
	 * first and current references.
	 */
	public void clear()
	{
		next.clear();
		prev.clear();
		first = current = null;
	}
}
