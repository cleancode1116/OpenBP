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
package org.openbp.common.template.writer;

import java.io.IOException;

import org.openbp.common.string.StringUtil;

/**
 * Template writer for Java source files.
 *
 * @author Heiko Erhardt
 */
public class JavaTemplateWriter extends TemplateWriter
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Package placeholder name */
	public static final String PACKAGE_PLACEHOLDER = "package";

	/** Import placeholder name */
	public static final String IMPORTS_PLACEHOLDER = "imports";

	/** Flag for {@link #printComment}: Short form (//) */
	public static final int COMMENT_SHORT = 0;

	/** Flag for {@link #printComment}: Single line */
	public static final int COMMENT_SINGLE = 1;

	/** Flag for {@link #printComment}: Multi line */
	public static final int COMMENT_MULTI = 2;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Unqualified class name of the class to create */
	protected String className;

	/** Name of the package */
	protected String packageName;

	/** Auto indentation feature */
	protected boolean autoIndent = true;

	/** Content of the output file */
	protected StringBuffer formatBuffer = new StringBuffer(256);

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param srcDir Source directory
	 * @param qualifiedName Fully qualified name of the class to create
	 */
	public JavaTemplateWriter(String srcDir, String qualifiedName)
		throws IOException
	{
		super();

		setFileName(constructFileName(srcDir, qualifiedName));

		definePlaceholder(PACKAGE_PLACEHOLDER, Placeholder.LIST_TYPE);
		definePlaceholder(IMPORTS_PLACEHOLDER, Placeholder.LIST_TYPE);

		setPackageAndClassInfo(qualifiedName);
	}

	/**
	 * Sets the package and class name information.
	 *
	 * @param qualifiedName Fully qualified class name
	 * @throws IOException On error
	 */
	protected void setPackageAndClassInfo(String qualifiedName)
		throws IOException
	{
		if (qualifiedName == null)
		{
			throw new IOException("Package is null");
		}

		int pos = qualifiedName.lastIndexOf('.');
		if (pos > 0)
		{
			packageName = qualifiedName.substring(0, pos);
			className = qualifiedName.substring(pos + 1);
		}
		else
		{
			className = qualifiedName;
		}

		if (packageName != null)
		{
			addToPlaceholder(PACKAGE_PLACEHOLDER, "package " + packageName + ";");
		}
	}

	//////////////////////////////////////////////////
	// @@ Import handling
	//////////////////////////////////////////////////

	/**
	 * Adds an import statement to the import list if not already present.
	 *
	 * @param clsName Class name
	 */
	public void addImport(String clsName)
	{
		if (clsName == null)
		{
			return;
		}

		if (clsName.startsWith("java.lang."))
		{
			// Don't import anything in the java.lang package
			return;
		}

		int pos = clsName.lastIndexOf(".");
		if (pos < 0)
		{
			// No package specification, nothing to import
			return;
		}

		if (packageName != null && packageName.equals(clsName.substring(0, pos)))
		{
			return;
		}

		Placeholder importPlaceholder = getPlaceholder(IMPORTS_PLACEHOLDER);

		String line = "import " + clsName + ";";

		// Check if this import is already present
		if (importPlaceholder.contains(line))
		{
			// Don't import twice
			return;
		}

		importPlaceholder.add(line);
	}

	//////////////////////////////////////////////////
	// @@ Printing
	//////////////////////////////////////////////////

	/** Parsing status */
	protected int status = TEXT;

	/** Parser status: Regular text */
	protected static final int TEXT = 0;

	/** Parser status: Char constant */
	protected static final int CHARCONST = 1;

	/** Parser status: int constant */
	protected static final int STRINGCONST = 2;

	/** Parser status: '//' Comment */
	protected static final int LINECOMMENT = 3;

	/** Parser status: '/*' Comment */
	protected static final int BLOCKCOMMENT = 4;

	/** Indent to add before printing */
	protected int addBefore;

	/** Indent to add after printing */
	protected int addAfter;

	/**
	 * Prints a string to the output file.
	 * if the {@link #setAutoIndent} option is set, indentation will be controlled automatically
	 * by opening and closing braces ("{"/"}").<br>
	 * Also adds a space before block comment lines:
	 * @code 3
	 * / **
	 * * Comment
	 * * /
	 * @code
	 * Will be printed as
	 * @code 3
	 * / **
	 *  * Comment
	 *  * /
	 * @code
	 *
	 * @param s String to print
	 * @throws IOException On error
	 */
	protected void printToOutput(String s)
		throws IOException
	{
		if (s == null)
			return;

		if (!autoIndent)
		{
			// If auto-indent is off, do nothing special
			basicPrintToOutput(s);
			return;
		}

		int n = s.length();
		for (int i = 0; i < n; ++i)
		{
			char c = s.charAt(i);
			char cNext = i + 1 < n ? s.charAt(i + 1) : ' ';
			char cLast = i > 0 ? s.charAt(i - 1) : ' ';

			formatBuffer.append(c);

			if (c == '\'')
			{
				if (status == TEXT)
					status = CHARCONST;
				else if (status == CHARCONST)
				{
					if (cLast != '\\')
					{
						status = TEXT;
					}
				}
				continue;
			}

			if (c == '\"')
			{
				if (status == TEXT)
				{
					status = STRINGCONST;
				}
				else if (status == STRINGCONST)
				{
					status = TEXT;
					if (cLast != '\\')
					{
						status = TEXT;
					}
				}
				continue;
			}

			if (c == '/' && cNext == '/')
			{
				if (status == TEXT)
				{
					status = LINECOMMENT;
				}
				continue;
			}

			if (c == '/' && cNext == '*')
			{
				if (status == TEXT)
				{
					status = BLOCKCOMMENT;
				}
				continue;
			}

			if (c == '*' && cNext == '/')
			{
				if (status == BLOCKCOMMENT)
				{
					status = TEXT;
				}
				continue;
			}

			if (c == '{')
			{
				if (status == TEXT)
				{
					// There is an opening brace, indent after printing the text
					++addAfter;
				}
				continue;
			}

			if (c == '}')
			{
				if (status == TEXT)
				{
					// There is a closing brace, de-indent before printing the text
					--addBefore;
				}
				continue;
			}

			if (c == '\n')
			{
				if (status == CHARCONST || status == STRINGCONST || status == LINECOMMENT)
				{
					status = TEXT;
				}

				// Add indent before printing
				if (addBefore != 0)
				{
					addIndent(addBefore);
					addBefore = 0;
				}

				// Print it to the output with the current indent.
				basicPrintToOutput(formatBuffer.toString());

				// Add indent after printing
				if (addAfter != 0)
				{
					addIndent(addAfter);
					addAfter = 0;
				}

				// Start a new line
				formatBuffer.setLength(0);

				if (status == BLOCKCOMMENT)
				{
					// Add a space before the block comment
					formatBuffer.append(' ');
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Comment printing
	//////////////////////////////////////////////////

	/**
	 * Prints a Javadoc comment.
	 * The method will detect any return charactes, where it will break the line
	 * and add the '*' with the nessesary spacing.
	 *
	 * @param s Comment to print
	 * @param flag {@link #COMMENT_SHORT}/{@link #COMMENT_SINGLE}/{@link #COMMENT_MULTI}
	 * @throws IOException On error
	 */
	public void printComment(String s, int flag)
		throws IOException
	{
		if (s == null)
			return;

		s = s.trim();

		// Determining if multiline java doc
		if (s.indexOf('\n') != -1)
		{
			flag = COMMENT_MULTI;
		}

		if (flag == COMMENT_SHORT)
		{
			print("// ");
		}
		else if (flag == COMMENT_MULTI)
		{
			println("/**");
			print("* ");
		}
		else
		{
			print("/** ");
		}

		int l = s.length();
		int pos = 0;

		while (pos < l)
		{
			int prevPos = pos;

			pos = s.indexOf('\n', prevPos);
			if (pos >= 0)
			{
				println(s.substring(prevPos, pos));
				print("* ");
				pos++;
				continue;
			}

			print(s.substring(prevPos, l));
			break;
		}

		if (flag == COMMENT_MULTI || flag == COMMENT_SHORT)
		{
			println();
		}
		if (flag == COMMENT_MULTI || flag == COMMENT_SINGLE)
		{
			println("*/");
		}
	}

	//////////////////////////////////////////////////
	// @@ Output file name construction
	//////////////////////////////////////////////////

	/**
	 * Constructs the file name from a source directory and the class name.
	 *
	 * @param srcDir Source directory
	 * @param qualifiedName Fully qualified name of the class to create
	 * @return The file name
	 */
	protected String constructFileName(String srcDir, String qualifiedName)
	{
		StringBuffer sb = new StringBuffer();
		if (srcDir != null)
		{
			sb.append(StringUtil.normalizePathName(srcDir));
			sb.append(StringUtil.FOLDER_SEP);
		}
		sb.append(qualifiedName.replace('.', StringUtil.FOLDER_SEP_CHAR));
		sb.append(".java");
		return sb.toString();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the package.
	 * @nowarn
	 */
	public String getPackageName()
	{
		return packageName;
	}

	/**
	 * Gets the unqualified class name of the class to create.
	 * @nowarn
	 */
	public String getClassName()
	{
		return className;
	}

	/**
	 * Gets the fully qualified class name of the class to create.
	 * @nowarn
	 */
	public String getQualifiedClassName()
	{
		return packageName + "." + className;
	}

	/**
	 * Gets the auto indentation feature.
	 * @nowarn
	 */
	public boolean isAutoIndent()
	{
		return autoIndent;
	}

	/**
	 * Sets the auto indentation feature.
	 * @nowarn
	 */
	public void setAutoIndent(boolean autoIndent)
	{
		this.autoIndent = autoIndent;
	}
}
