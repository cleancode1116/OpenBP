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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.template.writer.merger.Merger;

/**
 * Template writer.
 *
 * @author Heiko Erhardt
 */
public class TemplateWriter
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Placeholder type: Sorted list */
	public static final int LIST_PLACEHOLDER = Placeholder.LIST_TYPE;

	/** Placeholder type: Hashtable */
	public static final int HASH_PLACEHOLDER = Placeholder.HASH_TYPE;

	/** Merge mode: Undefined */
	public static final int MODE_UNDEFINED = Merger.MODE_UNDEFINED;

	/** Merge mode: No merge action */
	public static final int MODE_OVERWRITE = Merger.MODE_OVERWRITE;

	/** Merge mode: No merge action */
	public static final int MODE_MERGE = Merger.MODE_MERGE;

	/**
	 * Merge mode: Keep section content of input file.
	 * The content of the sections present in the input file will be copied
	 * to the output file. Text outside the section delimiters on the input file
	 * will be overwritten by the generated text.
	 */
	public static final int TYPE_KEEP_SECTIONS = Merger.TYPE_KEEP_SECTIONS;

	/**
	 * Merge mode: Replace section content of input file.
	 * Text outside the section delimiters of the input file will be kept.
	 * The text inside the section delimiters of the input file will be
	 * overwritten by the generated text.
	 */
	public static final int TYPE_REPLACE_SECTIONS = Merger.TYPE_REPLACE_SECTIONS;

	/** Start tag for custom sections */
	public static final String CUSTOM_SECTION_START = "{{";

	/** End tag for custom sections */
	public static final String CUSTOM_SECTION_END = "}}";

	/** Delimiter for custom sections */
	public static final String CUSTOM_SECTION_DELIM = "*";

	/** Escape character */
	public static final char ESCAPE_CHAR = '\\';

	/** Start tag for a placeholder variable */
	public static final String PLACEHOLDER_START = "@[";

	/** End tag for a placeholder variable */
	public static final String PLACEHOLDER_END = "]@";

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** File name */
	protected String fileName;

	/** Table of all placeholders in a template */
	private Map placeholderTable;

	/** Content of the output file */
	protected StringBuffer outputBuffer = new StringBuffer(8192);

	/** Merge tool */
	private Merger merger;

	/** Current indentation level */
	protected int indent;

	/** Indentation string */
	protected String indentStr;

	/** Flag if we are currently at the begin of a line */
	protected boolean isNewLine;

	/** Merge mode requester */
	private MergeModeRequester mergeModeRequester;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Value constructor.
	 *
	 * @param fileName File name
	 */
	public TemplateWriter(String fileName)
		throws IOException
	{
		this();

		this.fileName = fileName;
	}

	/**
	 * Default constructor.
	 */
	public TemplateWriter()
		throws IOException
	{
		merger = new Merger();

		// By default, keep the custom sections
		setMergeMode(MODE_MERGE);
		setMergeType(TYPE_KEEP_SECTIONS);

		// Use tab indentation by default
		indentStr = "\t";
	}

	/**
	 * Gets the merge mode ({@link #MODE_UNDEFINED}/{@link #MODE_MERGE}/{@link #MODE_OVERWRITE}).
	 * @nowarn
	 */
	public int getMergeMode()
	{
		return merger.getMergeMode();
	}

	/**
	 * Sets the merge mode ({@link #MODE_UNDEFINED}/{@link #MODE_MERGE}/{@link #MODE_OVERWRITE}).
	 * @nowarn
	 */
	public void setMergeMode(int mergeMode)
	{
		merger.setMergeMode(mergeMode);
	}

	/**
	 * Sets the merge type ({@link #TYPE_KEEP_SECTIONS}/{@link #TYPE_REPLACE_SECTIONS}).
	 * @nowarn
	 */
	public void setMergeType(int mergeType)
	{
		merger.setMergeType(mergeType);
	}

	/**
	 * Sets the merge mode requester.
	 * @nowarn
	 */
	public void setMergeModeRequester(MergeModeRequester mergeModeRequester)
	{
		this.mergeModeRequester = mergeModeRequester;
	}

	//////////////////////////////////////////////////
	// @@ File operations
	//////////////////////////////////////////////////

	/**
	 * Flushes and closes the output file.
	 * Also performs the merge operation between the an existing output file and a new one.
	 *
	 * @throws IOException On error (e. g. if an existing output file could not be read or
	 * if the output file could not be created/overwritten)
	 * @throws CancelException In case the user cancelled when requesting the merge mode
	 */
	public void close()
		throws IOException, CancelException
	{
		Writer fileWriter = null;
		if (fileName != null)
		{
			try
			{
				File file = new File(fileName);
				if (file.exists())
				{
					if (merger.getMergeMode() == MODE_UNDEFINED)
					{
						// Ask the user for the merge mode
						if (mergeModeRequester != null)
						{
							int mergeMode = mergeModeRequester.determineMergeMode(this);

							if (mergeMode == MODE_UNDEFINED)
							{
								// This means cancel
								throw new CancelException();
							}

							merger.setMergeMode(mergeMode);
						}
					}
				}

				// Merge mode, read the input file
				merger.readInput(fileName);

				File parentDir = file.getParentFile();
				if (!parentDir.exists())
					parentDir.mkdirs();

				fileWriter = new FileWriter(file);
			}
			catch (FileNotFoundException ex)
			{
				throw new IOException(ex.getMessage());
			}
			catch (IOException ex)
			{
				throw ex;
			}
		}

		if (fileWriter == null)
		{
			// Fallback to stdout if no output file specified
			fileWriter = new OutputStreamWriter(System.out);
		}

		PrintWriter printWriter = new PrintWriter(fileWriter);

		List lines = new ArrayList();

		// Substitute output variables (placeholders)
		String out = outputBuffer.toString();
		int startIndex = 0;
		for (;;)
		{
			if (startIndex < 0)
				break;

			int endIndex = out.indexOf('\n', startIndex);

			String line;
			if (endIndex > 0)
			{
				line = out.substring(startIndex, endIndex);
				startIndex = endIndex + 1;
			}
			else
			{
				line = out.substring(startIndex);
				startIndex = -1;
			}

			// Checks if the line is a placeholder we need to replace
			String variable = determinePlaceholderName(line);
			if (variable != null)
			{
				int start = line.indexOf(PLACEHOLDER_START);
				String indentStr = line.substring(0, start);

				// Yes, substitute for the value of the placeholder
				Placeholder placeholder = getPlaceholder(variable);
				if (placeholder == null)
				{
					// Placeholder not defined
					lines.add("// *** Placeholder '" + variable + "' is undefined");
					continue;
				}

				String [] placeholderLines = placeholder.get();
				if (placeholderLines == null)
				{
					// Placeholder is empty, there was no content set
					continue;
				}

				// Replace the placeholder with the content
				for (int i = 0; i < placeholderLines.length; i++)
				{
					String l = indentStr + placeholderLines [i];
					if (l.trim().length() == 0)
						l = "";
					lines.add(l);
				}
				continue;
			}

			// Regular line
			if (line.trim().length() == 0)
				line = "";
			lines.add(line);
		}

		// Merge with input file
		lines = merger.mergeOutput(lines, fileName);

		// Write the output file
		int n = lines.size();
		for (int i = 0; i < n; ++i)
		{
			printWriter.println((String) lines.get(i));
		}

		printWriter.close();

		if (printWriter.checkError())
		{
			String msg = LogUtil.error(getClass(), "Error writing to file $0.", fileName);
			throw new IOException(msg);
		}
	}

	/**
	 * Determines the variable name within a line.
	 *
	 * @param line Line with variable
	 * @return Name of found variable
	 */
	private String determinePlaceholderName(String line)
	{
		int start = line.indexOf(PLACEHOLDER_START);
		int end = line.indexOf(PLACEHOLDER_END);
		if (start == -1 || end == -1)
			return null;
		if (start > 0 && line.charAt(start - 1) == ESCAPE_CHAR)
			return null;

		int lenStart = PLACEHOLDER_START.length();
		return line.substring(start + lenStart, end);
	}

	//////////////////////////////////////////////////
	// @@ Placeholder support
	//////////////////////////////////////////////////

	/**
	 * Defines a placeholder.
	 *
	 * @param name Name of the placeholder
	 * @param type Placeholder type
	 * @return The new placeholder
	 */
	public Placeholder definePlaceholder(String name, int type)
	{
		Placeholder ph = new Placeholder(type);
		if (placeholderTable == null)
			placeholderTable = new HashMap();
		placeholderTable.put(name, ph);
		return ph;
	}

	/**
	 * Adds text to the placeholder.
	 *
	 * @param name Name of the placeholder
	 * @param content Text content to add
	 */
	public void addToPlaceholder(String name, String content)
	{
		Placeholder ph = getPlaceholder(name);
		if (ph == null)
		{
			LogUtil.error(getClass(), "Undefined placeholder $0", name);
			ph = definePlaceholder(name, LIST_PLACEHOLDER);
		}
		ph.add(content);
	}

	/**
	 * Determines if a placeholder contains any text.
	 *
	 * @param name Name
	 * @return
	 *		true	The placeholder is empty.
	 *		false	The placeholder contains text.
	 */
	public boolean isPlaceholderEmpty(String name)
	{
		Placeholder ph = getPlaceholder(name);
		return ph == null || ph.isEmpty();
	}

	/**
	 * Clears the contents of the placeholder.
	 * @param name Name of the placeholder
	 */
	protected void clearPlaceholder(String name)
	{
		Placeholder ph = getPlaceholder(name);
		if (ph != null)
		{
			ph.clear();
		}
	}

	/**
	 * Gets the placeholder by the name.
	 * @param name Name of the placeholder
	 * @return The placeholder or null if no such placeholder exists
	 */
	public Placeholder getPlaceholder(String name)
	{
		return placeholderTable != null ? (Placeholder) placeholderTable.get(name) : null;
	}

	//////////////////////////////////////////////////
	// @@ Print methods
	//////////////////////////////////////////////////

	/**
	 * Prints a newline.
	 *
	 * @throws IOException On error
	 */
	public void println()
		throws IOException
	{
		printToOutput("\n");
	}

	/**
	 * Prints the string representation of an object, followed by a newline.
	 *
	 * @param o Object to print
	 * @throws IOException On error
	 */
	public void println(Object o)
		throws IOException
	{
		if (o != null)
		{
			print(o);
		}
		println();
	}

	/**
	 * Prints the string representation of an object (without newline).
	 *
	 * @param o Object to print
	 * @throws IOException On error
	 */
	public void print(Object o)
		throws IOException
	{
		printToOutput(o != null ? o.toString() : null);
	}

	/**
	 * Prints a string to the output file (without newline).
	 *
	 * @param s String to print
	 * @throws IOException On error
	 */
	protected void printToOutput(String s)
		throws IOException
	{
		basicPrintToOutput(s);
	}

	/**
	 * Prints a string to the output file (without newline).
	 *
	 * @param s String to print
	 * @throws IOException On error
	 */
	protected void basicPrintToOutput(String s)
		throws IOException
	{
		// For the basic writer, simply append the string to the buffer
		if (s != null)
		{
			int n = s.length();
			for (int i = 0; i < n; ++i)
			{
				char c = s.charAt(i);

				if (c == '\n')
				{
					isNewLine = true;
					outputBuffer.append(c);
				}
				else
				{
					if (isNewLine)
					{
						// Add indentation
						for (int iIndent = 0; iIndent < indent; ++iIndent)
						{
							outputBuffer.append(indentStr);
						}
						isNewLine = false;
					}

					outputBuffer.append(c);
				}
			}
		}
	}

	/**
	 * Gets the current indentation level.
	 * @nowarn
	 */
	public int getIndent()
	{
		return indent;
	}

	/**
	 * Sets the current indentation level.
	 * @nowarn
	 */
	public void setIndent(int indent)
	{
		this.indent = indent;
	}

	/**
	 * Increases or decreases the current indentation level.
	 * @nowarn
	 */
	public void addIndent(int indentAdd)
	{
		this.indent += indentAdd;
	}

	/**
	 * Sets the indentation string.
	 * @param indentStr The string to use for the indentation (default: Tab character ("\t"))
	 */
	public void setIndentStr(String indentStr)
	{
		this.indentStr = indentStr;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the file name.
	 * @nowarn
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * Sets the file name.
	 * @nowarn
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
}
