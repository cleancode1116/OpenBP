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
package org.openbp.common.dump;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.ReflectUtil;

/**
 * Generic class for dumping objects.
 * Used for debugging and logging purposes.<br>
 * The dump methods gather information about the object to dump using Java reflections.
 * By default, each data member of a class and its superclasses will be dumped. If you
 * want to override this behaviour, the object must implement the {@link Dumpable} interface.<br>
 * The class keeps track of objects already dumped to prevent endless recursions.
 *
 * @author Heiko Erhardt
 */
public class Dumper
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Maximum size of a set to be dumped */
	private int maxSetSize = 10;

	/** Flag that determines if to skip static data members */
	private boolean skipStatic = true;

	/** Flag that determines if to skip null values */
	private boolean skipNull = true;

	/** Table of objects already dumped. Key and value is identical. */
	private Map dumpedObjects = new HashMap();

	/** Line separator for text files */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Do not instantiate this class!
	 */
	public Dumper()
	{
	}

	//////////////////////////////////////////////////
	// @@ Dump methods
	//////////////////////////////////////////////////

	/**
	 * Dumps an object to standard output.
	 * Shorthand for dump (object, new PrintWriter (System.out, true)).
	 *
	 * @param object The object to dump
	 */
	public void dump(Object object)
	{
		dump(null, object, new PrintWriter(System.out, true), 0);
	}

	/**
	 * Dumps an object to standard output with a title.
	 * Shorthand for dump (title, object, new PrintWriter (System.out, true), 0).
	 *
	 * @param title The title is prepended to the actual object dump
	 * @param o The object to dump
	 * Each indent level will translate to a tab character.
	 */
	public void dump(String title, Object o)
	{
		dump(title, o, new PrintWriter(System.out, true), 0);
	}

	/**
	 * Dumps an object to an output.
	 * Shorthand for dump (object, writer, 0).<br>
	 * If the object implements Dumpable, the class name of the object will be
	 * written on one line, followed by the output of the object's {@link #dump(Object)} method
	 * on the subsequent lines, indented by 1.<br>
	 * Otherwise, the toString representation of the object will be output and the
	 * line terminated.
	 *
	 * @param object The object to dump
	 * @param writer Writer object to use for output generation
	 */
	public void dump(Object object, Writer writer)
	{
		dump(null, object, writer, 0);
	}

	/**
	 * Dumps an object to an output.
	 * If the object implements Dumpable, the class name of the object will be
	 * written on one line, followed by the output of the object's {@link #dump(Object)} method
	 * on the subsequent lines, indented by 1.<br>
	 * Otherwise, the toString representation of the object will be output and the
	 * line terminated.
	 *
	 * @param object The object to dump
	 * @param writer Writer object to use for output generation
	 * @param indent Indentation level (0 = no indentation)<br>
	 * Each indent level will translate to a tab character.
	 */
	public void dump(Object object, Writer writer, int indent)
	{
		dump(null, object, writer, indent);
	}

	/**
	 * Dumps an object to an output with a title.
	 * If the object implements Dumpable, the class name of the object will be
	 * written on one line, followed by the output of the object's {@link #dump(Object)} method
	 * on the subsequent lines, indented by 1.<br>
	 * Otherwise, the toString representation of the object will be output and the
	 * line terminated.
	 *
	 * @param title The title is prepended to the actual object dump
	 * @param o The object to dump
	 * @param writer Writer object to use for output generation
	 * @param indent Indentation level (0 = no indentation)<br>
	 * Each indent level will translate to a tab character.
	 */
	public void dump(String title, Object o, Writer writer, int indent)
	{
		write(writer, indent);

		try
		{
			// Write the title
			if (title != null)
			{
				write(writer, title);
				write(writer, " ");
			}

			if (o == null)
			{
				writeln(writer, "null");
				return;
			}

			Class cls = o.getClass();

			if (cls.isPrimitive() || cls.equals(Boolean.class) || cls.equals(Character.class) || cls.equals(Byte.class) || cls.equals(Short.class) || cls.equals(Integer.class) || cls.equals(Long.class) || cls.equals(Float.class) || cls.equals(Double.class) || cls.equals(Void.class) || cls.equals(String.class))
			{
				// Simple primitive type
				writeln(writer, o.toString());
				return;
			}

			// Write the class name
			write(writer, ReflectUtil.getPrintableClassName(cls));
			writeln(writer, ":");

			if (dumpedObjects.get(o) != null)
			{
				// Object already dumped
				writeln(writer, "(dumped) " + o.toString(), indent + 1);
				return;
			}

			// Mark object as dumped
			dumpedObjects.put(o, o);

			if (o instanceof Dumpable)
			{
				// The object will dump itself
				((Dumpable) o).dump(writer, indent + 1);
				return;
			}

			if (o instanceof Collection)
			{
				// Dump collection object
				Collection collection = (Collection) o;
				Iterator it = collection.iterator();
				int size = collection.size();

				int i;
				for (i = 0; i < size; ++i)
				{
					if (i >= maxSetSize)
					{
						writeln(writer, "[...]", indent);
						break;
					}

					Object tmp = it.next();
					dump("[" + i + "]", tmp, writer, indent + 1);
				}
				if (i > 0)
					writeln(writer);
				return;
			}

			if (o instanceof Iterator)
			{
				// Dump iterator object
				Iterator it = (Iterator) o;

				int i;
				for (i = 0; it.hasNext(); ++i)
				{
					if (i >= maxSetSize)
					{
						writeln(writer, "[...]", indent);
						break;
					}

					Object tmp = it.next();
					dump("[" + i + "]", tmp, writer, indent + 1);
				}
				if (i > 0)
					writeln(writer);
				return;
			}

			if (cls.isArray())
			{
				// Dump array
				int size = Array.getLength(o);

				int i;
				for (i = 0; i < size; ++i)
				{
					if (i >= maxSetSize)
					{
						writeln(writer, "[...]", indent);
						break;
					}

					Object tmp = Array.get(o, i);
					dump("[" + i + "]", tmp, writer, indent + 1);
				}
				if (i > 0)
					writeln(writer);
				return;
			}

			// Dump map objects
			if (o instanceof Map)
			{
				Map map = (Map) o;

				int i = 0;
				for (Iterator it = map.keySet().iterator(); it.hasNext();)
				{
					if (i++ >= maxSetSize)
					{
						writeln(writer, "[...]", indent);
						break;
					}

					Object key = it.next();
					Object value = map.get(key);
					dump("[" + key.toString() + "]", value, writer, indent + 1);
				}
				if (i > 0)
					writeln(writer);
				return;
			}

			// A regular object. Dump it's members
			dumpMembers(o, o.getClass(), writer, indent + 1);
		}
		catch (IOException ioe)
		{
			// Ignore
		}
	}

	/**
	 * Dump the members of an object.
	 * Also dumps the fields of the superclass of the object.
	 * @param o The object to dump
	 * @param cls Class object to use for the inspection of the fields of the object
	 * @param writer Writer object to use for output generation
	 * @param indent Indentation level (0 = no indentation)<br>
	 * Each indent level will translate to a tab character.
	 */
	public void dumpMembers(Object o, Class cls, Writer writer, int indent)
	{
		if (o == null || cls == Object.class)
		{
			// End of recursive iteration
			return;
		}

		// Dump the field values
		try
		{
			// Get all fields of the objects class
			Field [] fields = cls.getDeclaredFields();

			// Make them accessible using reflections, i. e. Field.get
			// This may throw a security exception of there is a Java security manager
			// installed (this is usually no the case in development environments)
			AccessibleObject.setAccessible(fields, true);

			Field field;
			String name;
			Object value;
			for (int i = 0; i < fields.length; ++i)
			{
				field = fields [i];

				// Skip final and static fields
				int mod = field.getModifiers();
				if (skipStatic && Modifier.isStatic(mod))
				{
					// Ignore static members if desired
					continue;
				}
				if (Modifier.isStatic(mod) && Modifier.isFinal(mod))
				{
					// Always ignore constants
					continue;
				}
				if (Modifier.isTransient(mod))
				{
					// Ignore transient fields
					continue;
				}

				name = field.getName();
				try
				{
					value = field.get(o);
					if (skipNull && value == null)
					{
						// Ignore null values if desired
						continue;
					}

					if (o instanceof Class)
						continue;

					dump(name + " =", value, writer, indent);
				}
				catch (IllegalArgumentException e)
				{
					// Report, but ignore
					System.err.println("Dumper: IllegalArgumentException accessing field " + name);
					ExceptionUtil.printTrace(e);
				}
				catch (IllegalAccessException e)
				{
					// Report, but ignore
					System.err.println("Dumper: IllegalArgumentException accessing field " + name);
					ExceptionUtil.printTrace(e);
				}
			}
		}
		catch (SecurityException e)
		{
			// Ignore
			System.err.println("Dumper: IllegalArgumentException accessing field list of " + ReflectUtil.getPrintableClassName(cls));
			ExceptionUtil.printTrace(e);
		}

		// Dump the superclass members
		dumpMembers(o, cls.getSuperclass(), writer, indent);
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Writes the indent to the output writer.
	 *
	 * @param writer Writer object to use for output generation
	 * @param indent Indentation level (0 = no indentation)<br>
	 * Each indent level will translate to a tab character.
	 */
	public void write(Writer writer, int indent)
	{
		try
		{
			for (int i = 0; i < indent; ++i)
			{
				writer.write('\t');
			}
		}
		catch (IOException ioe)
		{
			// Ignore
		}
	}

	/**
	 * Writes a string to the output writer.
	 *
	 * @param writer Writer object to use for output generation
	 * @param s The string to write
	 * @throws IOException On i/o error
	 */
	protected void write(Writer writer, String s)
		throws IOException
	{
		writer.write(s);
	}

	/**
	 * Writes a string to the output writer.
	 *
	 * @param writer Writer object to use for output generation
	 * @param s The string to write
	 * @param indent Indentation level
	 * @throws IOException On i/o error
	 */
	protected void write(Writer writer, String s, int indent)
		throws IOException
	{
		write(writer, indent);
		write(writer, s);
	}

	/**
	 * Writes a newline to the output writer.
	 *
	 * @param writer Writer object to use for output generation
	 * @throws IOException On i/o error
	 */
	protected void writeln(Writer writer)
		throws IOException
	{
		writeln(writer, null);
	}

	/**
	 * Writes a string and a newline to the output writer.
	 *
	 * @param writer Writer object to use for output generation
	 * @param s The string to write
	 * @throws IOException On i/o error
	 */
	protected void writeln(Writer writer, String s)
		throws IOException
	{
		if (s != null)
			writer.write(s);
		writer.write(LINE_SEPARATOR);
		writer.flush();
	}

	/**
	 * Writes a string to the output writer.
	 *
	 * @param writer Writer object to use for output generation
	 * @param s The string to write
	 * @param indent Indentation level
	 * @throws IOException On i/o error
	 */
	protected void writeln(Writer writer, String s, int indent)
		throws IOException
	{
		write(writer, indent);
		writeln(writer, s);
	}

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/**
	 * Gets the maximum size of a set to be dumped.
	 * Default: 10 elements
	 * @nowarn
	 */
	public int getMaxSetSize()
	{
		return maxSetSize;
	}

	/**
	 * Sets the maximum size of a set to be dumped.
	 * Default: 10 elements
	 * @nowarn
	 */
	public void setMaxSetSize(int maxSetSizeArg)
	{
		maxSetSize = maxSetSizeArg;
	}

	/**
	 * Gets the flag that determines if to skip static data members.
	 * Default: true
	 * @nowarn
	 */
	public boolean getSkipStatic()
	{
		return skipStatic;
	}

	/**
	 * Sets the flag that determines if to skip static data members.
	 * Default: true
	 * @nowarn
	 */
	public void setSkipStatic(boolean skipStaticArg)
	{
		skipStatic = skipStaticArg;
	}

	/**
	 * Gets the flag that determines if to skip null values.
	 * Default: true
	 * @nowarn
	 */
	public boolean getSkipNull()
	{
		return skipNull;
	}

	/**
	 * Sets the flag that determines if to skip null values.
	 * Default: true
	 * @nowarn
	 */
	public void setSkipNull(boolean skipNullArg)
	{
		skipNull = skipNullArg;
	}
}
