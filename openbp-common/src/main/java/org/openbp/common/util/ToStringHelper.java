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
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.openbp.common.generic.PrintNameProvider;
import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.common.generic.description.DisplayObject;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.property.PropertyAccessUtil;

/**
 * Helper class for convenient implementation of toString methods.
 *
 * @author Erich Lauterbach
 */
public final class ToStringHelper
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Flag for toString: Display qualified classname */
	public static final int TOSTRING_QUALIFIED_CLASSNAME = (1 << 0);

	/** Flag for toString: Display abbreviated classname */
	public static final int TOSTRING_ABBREVIATED_CLASSNAME = (1 << 1);

	/** Flag for toString: Display unqualified classname */
	public static final int TOSTRING_UNQUALIFIED_CLASSNAME = (1 << 2);

	/** Flag for toString: Display hashcode */
	public static final int HASHCODE = (1 << 3);

	//////////////////////////////////////////////////
	// @@ Static members
	//////////////////////////////////////////////////

	/** {@link #toString()} operation mode */
	private static int toStringFlags = TOSTRING_UNQUALIFIED_CLASSNAME;

	/** Prints sizes of collections and maps instead of content for property values */
	private static boolean shortInfo = true;

	private static String [] classNamePrefixes = { "org.openbp.", "org.openbp.common.", "java.lang.", "java.util.", };

	/**
	 * Private constructor prevents instantiation.
	 */
	private ToStringHelper()
	{
	}

	//////////////////////////////////////////////////
	// @@ toString Support
	//////////////////////////////////////////////////

	/**
	 * Returns a string representation of the given object.
	 * Convencience method for stateless objects.
	 * @seem toString (Object o, String [] properties)
	 * @nowarn
	 */
	public static String toString(Object o)
	{
		return toString(o, (String []) null);
	}

	/**
	 * Returns a string representation of the given object.
	 * Convencience method for {@link #toString(Object,String [])}
	 * @nowarn
	 */
	public static String toString(Object o, String property1)
	{
		return toString(o, new String [] { property1 });
	}

	/**
	 * Returns a string representation of the given object.
	 * Convencience method for {@link #toString(Object, String [])}
	 * @nowarn
	 */
	public static String toString(Object o, String property1, String property2)
	{
		return toString(o, new String [] { property1, property2 });
	}

	/**
	 * Returns a string representation of the given object.
	 * Convencience method for {@link #toString()}
	 * @nowarn
	 */
	public static String toString(Object o, String property1, String property2, String property3)
	{
		return toString(o, new String [] { property1, property2, property3 });
	}

	/**
	 * Returns a string representation of the given object.
	 * Convencience method for {@link #toString(Object, String [])}
	 * @nowarn
	 */
	public static String toString(Object o, String property1, String property2, String property3, String property4)
	{
		return toString(o, new String [] { property1, property2, property3, property4 });
	}

	/**
	 * Returns a string representation of the given object.
	 * Convencience method for {@link #toString(Object, String [])}
	 * @nowarn
	 */
	public static String toString(Object o, String property1, String property2, String property3, String property4, String property5)
	{
		return toString(o, new String [] { property1, property2, property3, property4, property5 });
	}

	/**
	 * Returns a string representation of the given object.
	 * Convencience method for {@link #toString(Object, String [])}
	 * @nowarn
	 */
	public static String toString(Object o, String property1, String property2, String property3, String property4, String property5, String property6)
	{
		return toString(o, new String [] { property1, property2, property3, property4, property5, property6 });
	}

	/**
	 * Returns the basic object info of an object.
	 * @param o Object to display
	 * @return Either "(null)" if 'o' is null or
	 * the class name of the object (either qualified or unqualified, dependent on the value of
	 * toStringFlags), followed by the hash code of the object as known
	 * from the standard implementation of Object.toString. Example:<br>
	 * "java.lang.Object@83AF"<br>
	 * or<br>
	 * "Object@83AF"
	 */
	private static StringBuffer buildObjectInfo(Object o)
	{
		StringBuffer sb = new StringBuffer();

		if (o == null)
		{
			// Null object
			sb.append("(null)");
			return sb;
		}

		// Handle collections
		// Note: Don't ask for an iterator here to prevent emptying the iterator.
		if (o instanceof Collection)
		{
			int r = 0;
			for (Iterator it = ((Collection) o).iterator(); it.hasNext();)
			{
				Object v = it.next();

				sb.append("[");
				sb.append(r++);
				sb.append("] = ");
				appendToStringOutput(sb, v);
			}
			return sb;
		}

		// Get the class name
		String className = o.getClass().getName();

		if ((toStringFlags & TOSTRING_ABBREVIATED_CLASSNAME) != 0)
		{
			// Display abbreviated classname
			for (int i = 0; i < classNamePrefixes.length; ++i)
			{
				if (className.startsWith(classNamePrefixes [i]))
				{
					className = className.substring(classNamePrefixes [i].length());
					break;
				}
			}
		}
		else if ((toStringFlags & TOSTRING_UNQUALIFIED_CLASSNAME) != 0)
		{
			// Display unqualified classname
			int i = className.lastIndexOf('.');
			if (i > 0)
				className = className.substring(i + 1);
		}

		sb.append(className);

		if ((toStringFlags & HASHCODE) != 0)
		{
			sb.append('@');
			sb.append(Integer.toHexString(o.hashCode()));
		}

		String name = null;
		if (o instanceof PrintNameProvider)
		{
			name = ((PrintNameProvider) o).getPrintName();
		}
		else if (o instanceof DescriptionObject)
		{
			name = ((DescriptionObject) o).getName();
		}
		if (name != null)
		{
			sb.append("(");
			sb.append(name);
			sb.append(")");
		}

		return sb;
	}

	/**
	 * Returns a string representation of the given object.
	 * This method is a standard implemention of the toString method.
	 * It will generate a string containing basic object information (see below) and
	 * the values of the given properties, which are queried using reflection.
	 *
	 * @param o Object to display
	 * @param properties Table of object properties to display
	 * @return Either "(null)" if 'o' is null or
	 * the class name of the object (either qualified or unqualified, dependent on the value of
	 * toStringFlags), followed by the hash code of the object as known
	 * from the standard implementation of Object.toString.<br>
	 * <br>
	 * Example:<br>
	 * <br>
	 * "java.lang.Object@83AF"<br>
	 * or<br>
	 * "Object@83AF"<br>
	 * <br>
	 * if 'properties' is not null, the given properties will be appended, separated by a new line
	 * and indented by two spaces.<br>
	 * Given the properties { "Name", "DisplayName", "Description" } and a {@link DisplayObject} the
	 * return value may look like:<br>
	 * <br>
	 * "DisplayObject@83AF"<br>
	 * "  Name = TestObject"<br>
	 * "  DisplayName = Test object"<br>
	 * "  Description = This is a test object"
	 * <br>
	 * If the property inspection causes an exception, "*ERROR*" will be printed as property value.
	 */
	public static String toString(Object o, String [] properties)
	{
		StringBuffer sb = buildObjectInfo(o);

		if (o != null && properties != null)
		{
			for (int i = 0; i < properties.length; ++i)
			{
				try
				{
					Object value = PropertyAccessUtil.getProperty(o, properties[i]);
					if (value != null)
					{
						if (i == 0)
							sb.append(":");
						else
							sb.append(",");
						sb.append(" ");
						sb.append(properties[i]);
						sb.append(" = ");

						if (shortInfo)
						{
							if (value instanceof Collection)
							{
								value = ((Collection) value).iterator();
							}
						}
						else
						{
							if (value instanceof Collection)
							{
								value = "[collection size: " + ((Collection) value).size() + "]";
							}
							else if (value instanceof Map)
							{
								value = "[map size: " + ((Map) value).size() + "]";
							}
						}

						if (value instanceof Iterator)
						{
							int r = 0;
							Iterator it = (Iterator) value;
							while (it.hasNext())
							{
								Object v = it.next();

								sb.append("[");
								sb.append(r++);
								sb.append("] = ");
								appendToStringOutput(sb, v);
							}
						}
						else
						{
							appendToStringOutput(sb, value);
						}
					}
				}
				catch (Exception e)
				{
					LogUtil.warn(ToStringHelper.class, "Logging error: Error printing log object argument $0 of object of type $1.", properties[i], o.getClass().getName(), e);

					if (i == 0)
						sb.append(":");
					else
						sb.append(",");
					sb.append(" ");
					sb.append(properties[i]);
					sb.append(" = ");
					sb.append("<ERROR>");
				}
			}
		}

		return sb.toString();
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Appends the toString output of an object to the given string buffer.
	 *
	 * @param sb Buffer to append to
	 * @param o Object to print
	 */
	private static void appendToStringOutput(StringBuffer sb, Object o)
	{
		String s = o.toString();
		if (s.indexOf('\n') >= 0)
		{
			// Simple value
			sb.append(s);
		}
		else
		{
			// toString value of a complex object, insert new lines and indentation
			boolean first = true;
			StringTokenizer st = new StringTokenizer(s, "\n");
			while (st.hasMoreTokens())
			{
				if (!first)
					sb.append("\n    ");
				else
					first = false;

				sb.append(st.nextToken());
			}
		}
	}
}
