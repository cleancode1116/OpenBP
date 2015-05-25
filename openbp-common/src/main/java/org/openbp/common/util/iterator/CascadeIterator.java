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
package org.openbp.common.util.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class provides an iterator over a treelike structure. It does this by using the
 * Composite-pattern: a component (node) can either be composite (map or a collection)
 * or a leaf (everything else). A composite contains components itself (again either
 * comosites or leaves) and so on, forming a treelike structure with leaves at the ends.<br>
 *
 * This class creates an iterator over all LEAVES of such a structure. An optional
 * second parameter (keyOrValue) specifies whether the iterator should return keys or values
 * of maps.<br>
 *
 * KeyOrValue can either be KEY, VALUE, or a positive integer.<br>
 * KEY:<br>
 * The iterator traverses all keys of maps (which usually means it does not go
 * beyond the first map/collection encountered)<br>
 * VALUE (Default):<br>
 * the iterator traverses all leaves of the structure (theoretically to any depth)\n<br>
 * positive Integer n:<br>
 * This is somewhat tricky. The class traverses the first n levels of the structure for values,
 * and if an element on the n+1'th level is a map/collection, it returns it's keys (see example).
 *
 * Example:
 * @code 3
 * Let's consider a structure of three chained map/collection-levels:
 *                           top
 *                            |
 *   -------------------------------------------------------------------------
 *   |                                      |                                |
 *  key1                                   key2                             key3
 *  hash1                                  hash2                            hash3
 *   |                                      |                                |
 * ------------------        ---------------------------------------         ---------------
 * |                |        |                  |                  |            |          |
 * k11             k12      k21                k22                k23          k31        k32
 * h11             h12      h21                h22                h23          h31        h32
 *  |               |        |                  |                  |            |          |
 * ------------     |    -----------    ----------------       ----     ------------       |
 * |          |     |    |         |    |      |       |       |        |          |       |
 * k111     k112  k121  k211      k212 k221   k222    k223    k231     k311      k312     k321
 * v111     v112  v121  v211      v212 v221   v222    h223    v231     v311      v312     v321
 *                                                      |
 *                                                ---------------------
 *                                                |                   |
 *                                              k2231               k2232
 *                                              v2231               v2232
 *
 * (key/k means a String, hash/h a map/collection and v a value (an object), entries are written
 * as KEY over VALUE)
 * Same results (with varying keyOrValue's, syntax: new CascadeIterator (top, koV):
 * VALUE: (v111, v112, v121, v211, v212, v221, v222, v2231, v2232, v231, v311, v312, v321)
 * KEY: (key1, key2, key3)
 * 1: (k11, k12, k21, k22, k23, k31, k32)
 * 2: (k111, k112, k121, k211, k212, k221, k222, k223, k231, k311, k312, k321)
 * 3: (v111, v112, v121, v211, v212, v221, v222, k2231, k2232, v231, v311, v312, v321)
 * >3: same as values
 * @code
 *
 * As you see, using the positive-integer syntax with a non uniform structure can lead to quite
 * unexpected (but predictable nonetheless) results.<br>
 *
 * @author Stephan Moritz
 */
public class CascadeIterator
	implements Iterator
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Iterate through keys. */
	public static final int KEY = 0;

	/** Iterate through values. */
	public static final int VALUE = -1;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Contains the firstlevel iterator. */
	private Iterator basis;

	/** Contains the second level iterator (over subobjects). */
	private Iterator actual = EmptyIterator.getInstance();

	/** Contains the modus of operation (return either keys or values of hastables). */
	private int keyOrValue = VALUE;

	/** Internally used to determine whether the end of the iterator is reached. */
	private boolean hasMore = true;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor
	 */
	public CascadeIterator()
	{
		super();
	}

	/**
	 * Creates an iterator over the given object.
	 * (Target CAN be a single leaf, in that case a singleton iterator is returned)
	 * This traverses VALUES of maps.<br>
	 *
	 * @param target The object to iterate
	 * @return The iterator
	 */
	protected CascadeIterator init(Object target)
	{
		return init(target, VALUE);
	}

	/**
	 * Creates an iterator over the given object.
	 * (Target CAN be a single leaf, in that case a singleton iterator is returned)
	 * This traverses VALUES or KEYS of maps.<br>
	 *
	 * @param target The object to iterate
	 * @param keyOrValue Whether keys or values of maps should be traversed
	 * @return The iterator
	 */
	protected CascadeIterator init(Object target, int keyOrValue)
	{
		// we set the basis iterator over the actual object
		this.basis = getIterator(target, keyOrValue);
		this.keyOrValue = keyOrValue;
		stepAhead();
		return this;
	}

	/**
	 * Creates an iterator over an array of Objects, chaining their values together.
	 * This constructor returns values in maps (default)
	 *
	 * @param targets The object-array
	 * @return The iterator
	 */
	protected CascadeIterator init(Object [] targets)
	{
		return init(targets, VALUE);
	}

	/**
	 * Creates an iterator over an array of Objects, chaining their values together.
	 * This constructor returns either values or keys in maps.
	 *
	 * @param targets the object-array
	 * @param keyOrValue Return keys or values in hastables
	 * @return The iterator
	 */
	protected CascadeIterator init(Object [] targets, int keyOrValue)
	{
		this.keyOrValue = keyOrValue;
		this.basis = new ArrayIterator(targets);
		stepAhead();
		return this;
	}

	//////////////////////////////////////////////////
	// @@ implementation of java.util.Iterator interface
	//////////////////////////////////////////////////

	/**
	 * Returns true if there are elements left.
	 *
	 * @return
	 *    true: There are objects left.<br>
	 *    false: Otherwise.<br>
	 */
	public boolean hasNext()
	{
		return hasMore;
	}

	/**
	 * Returns the next Element of the iterator and advances the internal counter.
	 *
	 * @return The next element
	 */
	public Object next()
	{
		if (!hasMore)
		{
			// we don't have anymore items
			throw new NoSuchElementException("No more Elements");
		}

		// actual returns all elements of lower levels...
		Object result = actual.next();
		stepAhead();
		return result;
	}

	/**
	 * Always throws an UnsupportedOperationException: removal is not supported by this iterator.
	 */
	public void remove()
	{
		throw new UnsupportedOperationException("remove operation not supported by CascadeIterator");
	}

	//////////////////////////////////////////////////
	// @@ Internal methods
	//////////////////////////////////////////////////

	/**
	 * Internally used method to advance the pointer to the next element.
	 */
	protected void stepAhead()
	{
		while (!actual.hasNext())
		{
			// this sub iterator hast finished, we move on to the next element of basis
			if (basis.hasNext())
			{
				// we have more objects in the basis-iterator
				actual = iterator(basis.next(), (keyOrValue > KEY ? keyOrValue - 1 : keyOrValue));
			}
			else
			{
				hasMore = false;
				break;
			}
		}
	}

	/**
	 * This static method returns an iterator over any given object.
	 * if the object is either a hastable or a collection it returns a CascadeIterator,
	 * else a singleton iterator.<br>
	 *
	 * Do not overwrite this method in order to integrate custom types, overwrite
	 * getCustomIterator () instead.
	 *
	 * @param next The object to iterator
	 * @param modus Return keys (KEY) or values (VALUE) in maps
	 * @return The iterator
	 */
	protected Iterator getIterator(Object next, int modus)
	{
		Iterator result;
		if ((result = getCustomIterator(next, modus)) != null)
		{
			return result;
		}
		// if it is anything else, we treat it as single object using a singleton iterator
		return new SingleIterator(next);
	}

	/**
	 * This method checks if the object is of a known type that can be iterated.
	 * In order to add custom types, overwrite this method, what should look like:
	 * @code 3
	 *
	 * protected Iterator getCustomIterator (Object obj, int modus)
	 * {
	 *	Iterator result = super.getCustomIterator (obj, modus);
	 *	if (result != null)
	 *	{
	 *		return result;
	 *	}
	 *	[your checks, for example]
	 *	if (obj instanceof myObject)
	 *	{
	 *		return ((myObject) obj).iteratorMethod ();
	 *	}
	 *	return null;
	 * }
	 * @code
	 *
	 * @param obj The object to iterate
	 * @param modus The modus (keyOrValue for maps)
	 * @return The iterator
	 */
	protected Iterator getCustomIterator(Object obj, int modus)
	{
		if (obj instanceof Map)
		{
			// if the next Element is a hastable, we return either the keys or
			// values, according to keyOrValue
			return modus == KEY ? ((Map) obj).keySet().iterator() : ((Map) obj).values().iterator();
		}
		if (obj instanceof Collection)
		{
			// if the next element is a collection, we return the elements
			return ((Collection) obj).iterator();
		}
		if (obj instanceof Iterator)
		{
			return (Iterator) obj;
		}

		return null;
	}

	/**
	 * Convenience method for iterating values (as opposed to keys).
	 *
	 * @param obj The object to iterate
	 * @return The iterator
	 */
	public Iterator iterator(Object obj)
	{
		return iterator(obj, VALUE);
	}

	/**
	 * Iterate over a given object.
	 *
	 * @param obj The object to iterate
	 * @param modus The modus (KEY, VALUE or a positive integer, see above)
	 * @return The iterator
	 */
	public Iterator iterator(Object obj, int modus)
	{
		if (getCustomIterator(obj, modus) == null)
		{
			// Single object
			return new SingleIterator(obj);
		}

		try
		{
			return ((CascadeIterator) this.getClass().newInstance()).init(obj, modus);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Iterate over a given object-array.
	 *
	 * @param objs The object-array to iterate
	 * @param modus The modus (KEY, VALUE or a positive integer, see above)
	 * @return The iterator
	 */
	public Iterator iterator(Object [] objs, int modus)
	{
		if (objs.length == 1)
		{
			// Single object
			return iterator(objs [0], modus);
		}

		try
		{
			return ((CascadeIterator) this.getClass().newInstance()).init(objs, modus);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Main method for test.
	 *
	 * @param args Argument vector; No arguments needed
	 */
	public static void main(String [] args)
	{
		List test = new ArrayList();
		test.add("t1");
		test.add("null");

		List v = new ArrayList();

		v.add("v1");
		v.add("null");
		v.add("v3");
		v.add("v4");

		test.add(v);

		test.add("t3");

		for (Iterator en = new CascadeIterator().iterator(test); en.hasNext();)
		{
			System.out.println(en.next());
			System.out.println("-----------------------------");
		}
	}
}
