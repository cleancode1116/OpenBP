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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.openbp.common.CommonUtil;
import org.openbp.common.generic.description.DescriptionObject;

/**
 * Static utilitiy methods for managing collections of NamedObject objects.
 *
 * @author Erich Lauterbach
 */
public final class NamedObjectCollectionUtil
{
	/**
	 * Private constructor prevents instantiation.
	 */
	private NamedObjectCollectionUtil()
	{
	}

	/**
	 * Searches a collection for an object with a particular name.
	 * The object must implement the {@link DescriptionObject} interface
	 * in order to be found. Objects that do not implement this interface
	 * will be skipped.
	 *
	 * @param c Collection to search or null
	 * @param name Object name to search for or null
	 * @return The found object or null if no such object exists
	 */
	public static DescriptionObject getByName(Collection c, String name)
	{
		if (c != null)
		{
			if (c instanceof ArrayList)
			{
				// Due to performance reasons, we try to avoid using iterators when traversing array lists
				ArrayList list = (ArrayList) c;
				int n = list.size();
				for (int i = 0; i < n; ++i)
				{
					Object o = list.get(i);
					if (o instanceof DescriptionObject)
					{
						DescriptionObject desc = (DescriptionObject) o;
						if (CommonUtil.equalsNull(desc.getName(), name))
							return desc;
					}
				}
			}
			else
			{
				// For any other collection use an iterator to traverse it
				for (Iterator it = c.iterator(); it.hasNext();)
				{
					Object o = it.next();
					if (o instanceof DescriptionObject)
					{
						DescriptionObject desc = (DescriptionObject) o;
						if (CommonUtil.equalsNull(desc.getName(), name))
							return desc;
					}
				}
			}
		}

		// Not found
		return null;
	}

	/**
	 * Creates a unique id among objects in a collection beginning
	 * with the specified type name.
	 * The method ensures that the id is unique within the collection
	 * by appending a number at the end of the type name.
	 *
	 * @param c Collection containing {@link DescriptionObject} items
	 * @param typeName The type name to use as base name
	 * @return The generated names will start with the sequence number 1.<br>
	 * E. g. for the typeName "ident" the method will return names
	 * like "ident", "ident2", "ident3", ...
	 */
	public static String createUniqueId(Collection c, String typeName)
	{
		// Build a unique name
		for (int i = 1;; ++i)
		{
			String n = i == 1 ? typeName : typeName + i;
			DescriptionObject o = getByName(c, n);
			if (o == null)
			{
				// We found an id that is not in use yet
				return n;
			}
		}
	}

	/**
	 * Creates a unique id among objects in two collections. The returned name
	 * is either the original, if that name is not yet present in the second (base)
	 * collection or a variant of the name that is not present in EITHER
	 * collection.
	 * The method ensures that the id is unique within the collection
	 * by appending a number at the end of the type name.
	 * Use this method if you want to insert a number of elements from one collection into
	 * another.
	 *
	 * @param toInsert Collection containing {@link DescriptionObject} items that shall be inserted into the target
	 * @param inserted Collection containing {@link DescriptionObject} items that have been inserted into the target
	 * (but are not present yet in the base collection)
	 * @param base Collection containing {@link DescriptionObject} items already present in the target
	 * @param typeName The type name to use as base name, should be a name present in insert
	 * @return The generated names will start with the sequence number 1.<br>
	 * E. g. for the typeName "ident" the method will return names
	 * like "ident", "ident2", "ident3", ...
	 */
	public static String createUniqueId(Collection toInsert, Collection inserted, Collection base, String typeName)
	{
		// Is the original name usable?
		if (getByName(base, typeName) == null)
		{
			return typeName;
		}

		// We need to rename, look for a name that is not present in either collection
		for (int i = 2;; ++i)
		{
			String n = typeName + i;

			if (getByName(inserted, n) == null && getByName(base, n) == null)
			{
				// We found an id that is not in use yet
				return n;
			}
		}
	}

	/**
	 * Renames the elements of the first collection in such a way,
	 * that the element names do not conflict with the elements of the given base collection.
	 * If a name does not qualify, a number is appended in order to cope.
	 * Note that the contents of the second collection are NOT changed during this process.
	 * @param toInsert Changeable collection of {@link DescriptionObject} items
	 * @param base Collection of {@link DescriptionObject} items
	 */
	public static void createUniqueNames(Collection toInsert, Collection base)
	{
		if (toInsert == null || base == null)
			return;

		ArrayList inserted = new ArrayList();

		for (Iterator it = toInsert.iterator(); it.hasNext();)
		{
			DescriptionObject element = (DescriptionObject) it.next();

			String n = element.getName();
			n = cutTrailingDigits(n);
			n = createUniqueId(toInsert, inserted, base, n);

			element.setName(n);

			inserted.add(element);
		}
	}

	/**
	 * Renames the given element in such a way, that the element name doesn't conflict with an element of the given collection.
	 * If a name does not qualify, a number is appended in order to cope.
	 * Note that the contents of the second collection are NOT changed during this process.
	 * @param element Element to rename
	 * @param base Collection of {@link DescriptionObject} items
	 */
	public static void createUniqueName(DescriptionObject element, Collection base)
	{
		if (element == null || base == null)
			return;

		ArrayList toInsert = new ArrayList();
		toInsert.add(element);

		String n = element.getName();
		n = cutTrailingDigits(n);
		n = createUniqueId(toInsert, null, base, n);

		element.setName(n);
	}

	/**
	 * Cuts trailing digits from a string.
	 *
	 * @param s String to process
	 * @return Result
	 */
	private static String cutTrailingDigits(String s)
	{
		StringBuffer sb = new StringBuffer(s);

		int n = sb.length();
		for (int i = n - 1; i > 0; --i)
		{
			if (Character.isDigit(sb.charAt(i)))
				sb.setLength(i);
		}

		return sb.toString();
	}
}
