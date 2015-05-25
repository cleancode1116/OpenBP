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
package org.openbp.cockpit.template.standard;

import org.openbp.common.generic.PrintNameProvider;
import org.openbp.common.generic.description.DisplayObject;
import org.openbp.common.string.NameUtil;
import org.openbp.common.string.StringUtil;
import org.openbp.common.template.TemplateBase;
import org.openbp.common.template.writer.JavaTemplateWriter;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * Base class for Java source code templates.
 * Input: Visual items.
 * Note that we do not define the generate method (which creates the JavaTemplateWriter),
 * we just provide methods to call from the generate method.
 *
 * @author Heiko Erhardt
 */
public abstract class JavaSrcTemplate extends TemplateBase
{
	//////////////////////////////////////////////////
	// @@ Standard methods
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public JavaSrcTemplate()
	{
	}

	//////////////////////////////////////////////////
	// @@ File header
	//////////////////////////////////////////////////

	/**
	 * Prints the file header of the source code file.
	 * The header contains the file name, the creator, the creation date of the file and a copyright notice.
	 * The method also prints the placeholders for package name ("@[package]@") and imports ("@[imports]@") and
	 * defines the placeholders.
	 *
	 * @param w Source file writer
	 * @throws Exception On error
	 */
	protected void printFileHeader(JavaTemplateWriter w)
		throws Exception
	{
		//
		// File header generation
		//

		w.println("@[package]@");
		w.println();

		w.println("@[imports]@");
		w.println();

		w.println("// {{*Custom imports*");
		w.println("// }}*Custom imports*");
		w.println();
	}

	//////////////////////////////////////////////////
	// @@ Comments
	//////////////////////////////////////////////////

	/**
	 * Creates a comment for the given object.
	 * The comments contains - if defined - the display name and the description of the item
	 * and the supplied text.
	 * If no display name and description are given, the method will try to guess a display
	 * name from the object name.
	 *
	 * @param o Object to describe
	 * @param text Text to print in the line after the display name or null
	 * @return The generated comment contains regular text only and no comment characters
	 * (start of comment, leading starts, end of comment etc.).
	 * The last character of the comment is always a newline.
	 */
	protected String createComment(DisplayObject o, String text)
	{
		String displayName = o.getDisplayName();
		if (displayName != null)
			displayName = StringUtil.capitalize(displayName);
		if (displayName != null && !displayName.endsWith("."))
			displayName += ".";

		String description = o.getDescription();
		if (description != null)
			description = StringUtil.capitalize(description);
		if (description != null && !description.endsWith("."))
			description += ".";

		StringBuffer comment = new StringBuffer();
		if (displayName != null && description != null)
		{
			comment.append(displayName);
			if (text != null)
			{
				comment.append("\n");
				comment.append(text);
			}
			comment.append("\n");
			comment.append(description);
		}
		else if (displayName != null)
		{
			comment.append(displayName);
			if (text != null)
			{
				comment.append("\n");
				comment.append(text);
			}
		}
		else if (description != null)
		{
			comment.append(description);
			if (text != null)
			{
				comment.append("\n");
				comment.append(text);
			}
		}
		else
		{
			comment.append(NameUtil.makeDisplayName(o.getName()) + ".");
			if (text != null)
			{
				comment.append("\n");
				comment.append(text);
			}
		}
		comment.append("\n");

		return comment.toString();
	}

	/**
	 * Creates a comment for the given object.
	 * The comments contains - if defined - the display name and the description of the item.
	 * If no display name and description are given, the method will try to guess a display
	 * name from the object name.
	 *
	 * @param o Object to describe
	 * @return The generated comment contains regular text only and no comment characters
	 * (start of comment, leading starts, end of comment etc.).
	 * The last character of the comment is always a newline.
	 */
	protected String createComment(DisplayObject o)
	{
		return createComment(o, null);
	}

	//////////////////////////////////////////////////
	// @@ Type name support
	//////////////////////////////////////////////////

	/**
	 * Determines the Java type of the given data type item.
	 * Reduces fully qualified type names to primitive type names if possible.
	 * Otherwise returns the unqualified type name after registering the type name as import.
	 *
	 * @param w Source file writer
	 * @param dataType Data type
	 * @return The type name to use for the member variable declaration.
	 */
	protected String determineJavaType(JavaTemplateWriter w, DataTypeItem dataType)
	{
		String type = dataType.getClassName();
		if (type == null)
		{
			// No class name, obviously we are referencing a draft bean (a bean that does not
			// have a class name assigned). Use a generic Object for this.
			type = "Object";
		}

		// Check for primitive types
		if (type.equals("java.lang.Boolean"))
			type = "boolean";
		else if (type.equals("java.lang.Byte"))
			type = "byte";
		else if (type.equals("java.lang.Character"))
			type = "char";
		else if (type.equals("java.lang.Double"))
			type = "double";
		else if (type.equals("java.lang.Float"))
			type = "float";
		else if (type.equals("java.lang.Integer"))
			type = "int";
		else if (type.equals("java.lang.Long"))
			type = "long";
		else if (type.equals("java.lang.Short"))
			type = "short";
		else if (type.equals("[B"))
			type = "byte[]";
		else
		{
			// Use the given type class name directly
			type = extractUnqualifiedName(w, type);
		}
		return type;
	}

	/**
	 * Extracts the unqualified name from the given class name.
	 * The method will automatically add the class name to the import list of the writer
	 * if it is fully qualified.
	 *
	 * @param w Source file writer
	 * @param qualifiedName Qualified or unqualified name
	 * @return The unqualified name
	 */
	protected String extractUnqualifiedName(JavaTemplateWriter w, String qualifiedName)
	{
		if (qualifiedName == null)
			return null;
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot >= 0)
		{
			String unqualifiedName = qualifiedName.substring(lastDot + 1, qualifiedName.length());
			String currentClass = w.getClassName();
			if (!unqualifiedName.equals(currentClass))
			{
				if (!qualifiedName.startsWith("java.lang."))
					w.addImport(qualifiedName);
				return unqualifiedName;
			}
		}

		return qualifiedName;
	}

	//////////////////////////////////////////////////
	// @@ Generic English text utilities
	//////////////////////////////////////////////////

	/** Names of collection names */
	private static final String [] collectionNames = new String [] { "list", "map", "table", };

	/**
	 * Returns the singular name of the given type name description string.
	 * The following forms are recognized:<br>
	 * "list/map/table of 'type'" returns "'type'"<br>
	 * "'type' list/map/table" returns "'type'"<br>
	 * "'type's" returns "'type'"<br>
	 * otherwise returns 's'
	 *
	 * @param s String to inspect
	 * @return The singular type name
	 */
	protected String singular(String s)
	{
		if (s == null)
			return null;

		int sl = s.length();

		for (int i = 0; i < collectionNames.length; ++i)
		{
			String text = collectionNames [i] + " of ";
			if (s.startsWith(text))
				return s.substring(text.length());
		}

		for (int i = 0; i < collectionNames.length; ++i)
		{
			String text = collectionNames [i];
			if (s.endsWith(text))
				return s.substring(0, sl - text.length()).trim();
		}

		char lastChar = s.charAt(sl - 1);
		if (lastChar == 's')
			return s.substring(0, sl - 1);
		return s;
	}

	/**
	 * Removes a trailing dot if present.
	 *
	 * @param s String to inspect
	 * @return The result string
	 */
	protected String removeDot(String s)
	{
		if (s != null && s.endsWith("."))
		{
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	/**
	 * Returns an engish adverb for the given substantive.
	 *
	 * @param s The substantive
	 * @return 'an' or 'a' dependant if the substantive starts with a vocal
	 */
	protected String adverb(String s)
	{
		char [] vowl = { 'a', 'e', 'i', 'o', 'u' };
		char firstChar = s.charAt(0);
		for (int i = 0; i < vowl.length; i++)
		{
			if (vowl [i] == firstChar)
				return "an";
		}
		return "a";
	}

	//////////////////////////////////////////////////
	// @@ Error reporting
	//////////////////////////////////////////////////

	/**
	 * Displays an error message to stdout, preceeded by the fully qualified name of the supplied object.
	 *
	 * @param o Display object or null for the current item
	 * @param msg Error messsage
	 */
	protected void errMsg(PrintNameProvider o, String msg)
	{
		if (o == null)
			o = (Item) getProperty("item");
		System.err.println("" + o.getPrintName() + ": " + msg);
	}

	/**
	 * Displays an error message to stdout, preceeded by the fully qualified name of the current item.
	 *
	 * @param msg Error messsage
	 */
	protected void errMsg(String msg)
	{
		errMsg(null, msg);
	}
}
