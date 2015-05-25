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
package org.openbp.common.template.writer.merger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbp.common.io.IOUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.string.TextUtil;
import org.openbp.common.template.writer.TemplateWriter;

/**
 * Merge tool.
 *
 * @author Heiko Erhardt
 */
public class Merger
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Merge mode: Undefined */
	public static final int MODE_UNDEFINED = -1;

	/** Merge mode: No merge action */
	public static final int MODE_OVERWRITE = 0;

	/** Merge mode: No merge action */
	public static final int MODE_MERGE = 1;

	/**
	 * Merge mode: Keep section content of input file.
	 * The content of the sections present in the input file will be copied
	 * to the output file. Text outside the section delimiters on the input file
	 * will be overwritten by the generated text.
	 */
	public static final int TYPE_KEEP_SECTIONS = 1;

	/**
	 * Merge mode: Replace section content of input file.
	 * Text outside the section delimiters of the input file will be kept.
	 * The text inside the section delimiters of the input file will be
	 * overwritten by the generated text.
	 */
	public static final int TYPE_REPLACE_SECTIONS = 2;

	/** Section begin string */
	private static final String SECTION_BEGIN = TemplateWriter.CUSTOM_SECTION_START + TemplateWriter.CUSTOM_SECTION_DELIM;

	/** Length of section begin string */
	private static final int SECTION_BEGIN_LEN = SECTION_BEGIN.length();

	/** Section end string */
	private static final String SECTION_END = TemplateWriter.CUSTOM_SECTION_END + TemplateWriter.CUSTOM_SECTION_DELIM;

	/** Length of section end string */
	private static final int SECTION_END_LEN = SECTION_END.length();

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/**
	 * Input content (contains {@link ContentBlock} objects).
	 * This list contains the content of the input file.
	 * The content blocks appear in the order they have been encountered.
	 */
	private List inputContent = new ArrayList();

	/**
	 * Output content (contains {@link ContentBlock} objects).
	 * This list contains the content of the generated output file.
	 * The content blocks appear in the order they have been encountered.
	 */
	private List outputContent = new ArrayList();

	/**
	 * Input file sections.
	 * Maps sections names to {@link SectionContentBlock} objects.
	 */
	private Map inputSections = new HashMap();

	/**
	 * output file sections.
	 * Maps sections names to {@link SectionContentBlock} objects.
	 */
	private Map outputSections = new HashMap();

	/**
	 * Section filter.
	 * Maps the names of sections to replace to Boolean.TRUE objects.
	 * If the section filter is not null, only the sections contained in this map
	 * will be replaced. The others will be left unchanged.
	 */
	private Map sectionFilter;

	/** Merge mode ({@link #MODE_UNDEFINED}/{@link #MODE_MERGE}/{@link #MODE_OVERWRITE}) */
	private int mergeMode;

	/** Merge type ({@link #TYPE_KEEP_SECTIONS}/{@link #TYPE_REPLACE_SECTIONS}) */
	private int mergeType;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public Merger()
	{
	}

	/**
	 * Gets the merge mode ({@link #MODE_UNDEFINED}/{@link #MODE_MERGE}/{@link #MODE_OVERWRITE}).
	 * @nowarn
	 */
	public int getMergeMode()
	{
		return mergeMode;
	}

	/**
	 * Sets the merge mode ({@link #MODE_UNDEFINED}/{@link #MODE_MERGE}/{@link #MODE_OVERWRITE}).
	 * @nowarn
	 */
	public void setMergeMode(int mergeMode)
	{
		this.mergeMode = mergeMode;
	}

	/**
	 * Gets the merge type ({@link #TYPE_KEEP_SECTIONS}/{@link #TYPE_REPLACE_SECTIONS}).
	 * @nowarn
	 */
	public int getMergeType()
	{
		return mergeType;
	}

	/**
	 * Sets the merge type ({@link #TYPE_KEEP_SECTIONS}/{@link #TYPE_REPLACE_SECTIONS}).
	 * @nowarn
	 */
	public void setMergeType(int mergeType)
	{
		this.mergeType = mergeType;
	}

	/**
	 * Adds a section name to the section filter.
	 * If names have been added to the section filter,
	 * only the sections contained in the filter will be replaced.
	 * The others will be left unchanged.<br>
	 * Otherwise, all sections will be replaced.
	 *
	 * @param name Name of the section to replace
	 */
	public void addToSectionFilter(String name)
	{
		if (sectionFilter == null)
			sectionFilter = new HashMap();
		sectionFilter.put(name, Boolean.TRUE);
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Reads the input file if present and scans for custom sections.
	 *
	 * @param inputFileName Input file name
	 * @throws IOException On i/o error
	 */
	public void readInput(String inputFileName)
		throws IOException
	{
		File file = new File(inputFileName);

		if (mergeMode == MODE_OVERWRITE)
		{
			// No merge action
			return;
		}

		try
		{
			// Decompose into single lines, do not trim
			List lines = TextUtil.breakIntoLines(IOUtil.readTextFile(file), false, -1);

			// Decompose the input text into content blocks
			parseLines(lines, inputContent, inputSections, file.getName());
		}
		catch (FileNotFoundException e)
		{
			LogUtil.debug(getClass(), "Merge: Creating new output file $0.", file.getName());
			return;
		}
		catch (IOException e)
		{
			String msg = LogUtil.error(getClass(), "Merge: Error reading input file $0.", file.getName(), e);
			throw new IOException(msg);
		}
	}

	/**
	 * Merges the lines of the output file with any existing custom sections of the input file
	 * scanned by {@link #readInput}.
	 *
	 * @param lines Lines of the output file
	 * @param outputFileName Name of the output file (for error msgs)
	 * @return The merged result
	 */
	public List mergeOutput(List lines, String outputFileName)
	{
		if (mergeMode == MODE_OVERWRITE)
		{
			// No merge, return the argument unchanged
			return lines;
		}

		// Decompose the generated output into content blocks
		parseLines(lines, outputContent, outputSections, outputFileName);

		List fromContent;
		Map primarySectionTable = null;
		Map secondarySectionTable = null;

		if (mergeType == TYPE_REPLACE_SECTIONS)
		{
			// Replace the sections of the input file by the generated sections

			if (inputContent.size() > 0)
			{
				// We will keep the original file content in this case
				fromContent = inputContent;

				// Take the section content from the generated output
				primarySectionTable = outputSections;

				// If not present, use the existing section content
				secondarySectionTable = inputSections;
			}
			else
			{
				// No input file

				// Use the generated output only
				fromContent = outputContent;
				primarySectionTable = outputSections;
			}
		}
		else
		{
			// Replace the text content of the input file by the generated text

			// Overwrite the original file content in this case
			fromContent = outputContent;

			// Take the section content from the existing input file sections
			primarySectionTable = inputSections;

			// If not present, use the generted sections
			secondarySectionTable = outputSections;
		}

		// Build the result text
		List resultLines = new ArrayList();

		int n = fromContent.size();
		for (int i = 0; i < n; ++i)
		{
			ContentBlock block = (ContentBlock) fromContent.get(i);

			if (block.isSection())
			{
				String name = ((SectionContentBlock) block).getName();

				// Search for this section in the primary section table
				SectionContentBlock resultBlock = null;
				if (sectionFilter == null || sectionFilter.containsKey(name))
				{
					// Replace this section if there is no section filter or the section
					// has been added to the filter.
					if (primarySectionTable != null)
					{
						resultBlock = (SectionContentBlock) primarySectionTable.get(name);
					}
				}

				if (resultBlock == null)
				{
					// Fallback to the secondary one
					if (secondarySectionTable != null)
					{
						resultBlock = (SectionContentBlock) secondarySectionTable.get(name);
					}
				}

				if (resultBlock != null)
				{
					// If found, use it instead of the original block;
					// if not, we will leave the text unchanged
					block = resultBlock;
				}
			}

			// Merge any section definition with the existing input

			// Copy the lines of the current block to the result
			block.copyToResult(resultLines);
		}

		return resultLines;
	}

	//////////////////////////////////////////////////
	// @@ Static helpers
	//////////////////////////////////////////////////

	/**
	 * Decompose the the lines of a file into content blocks and stores them in the supplied content list.
	 *
	 * @param lines Lines
	 * @param contentList Content list (contains {@link ContentBlock} objects).
	 * This list will contain the content of the file after the method has finished.
	 * The content blocks appear in the order they have been encountered.
	 * @param sectionTable Table of encountered content sections.
	 * Each section will be entered in this table.
	 * Maps sections names to {@link SectionContentBlock} objects.
	 * @param fileName File name for error msgs
	 */
	private static void parseLines(List lines, List contentList, Map sectionTable, String fileName)
	{
		ContentBlock currentBlock = null;

		// Iterate all lines
		int n = lines.size();
		for (int i = 0; i < n; ++i)
		{
			String line = (String) lines.get(i);

			String name = checkSectionStart(line, currentBlock, fileName);
			if (name != null)
			{
				// Start of a section, create new section block
				currentBlock = new SectionContentBlock(name);

				contentList.add(currentBlock);
				sectionTable.put(name, currentBlock);

				// Add section begin line to section block
				currentBlock.addLine(line);
				continue;
			}

			name = checkSectionEnd(line, currentBlock, fileName);
			if (name != null)
			{
				// End of a section

				// Add section end line to section block
				if (currentBlock != null)
					currentBlock.addLine(line);

				currentBlock = null;
				continue;
			}

			if (currentBlock == null)
			{
				// Start a new text block
				currentBlock = new TextContentBlock();
				contentList.add(currentBlock);
			}

			// Add line to current block
			currentBlock.addLine(line);
		}
	}

	/**
	 * Checks if this line contains a section start indicator.
	 *
	 * @param line Line to parse
	 * @param currentBlock Current content block
	 * @param fileName File name for error msgs
	 * @return The name of the new section or null
	 * if the line does not contain a section start indicator.
	 */
	private static String checkSectionStart(String line, ContentBlock currentBlock, String fileName)
	{
		int index = line.indexOf(SECTION_BEGIN);
		if (index < 0)
			return null;

		index += SECTION_BEGIN_LEN;
		String name = extractSectionName(line, index, fileName);

		if (name != null)
		{
			if (currentBlock != null && currentBlock.isSection())
			{
				// Unclosed section
				LogUtil.error(Merger.class, "Merge: Unclosed section block $0 in output file $1.", ((SectionContentBlock) currentBlock).getName(), fileName);
			}
		}

		return name;
	}

	/**
	 * Checks if this line contains a section start indicator.
	 *
	 * @param line Line to parse
	 * @param currentBlock Current content block
	 * @param fileName File name for error msgs
	 * @return The name of the new section or null
	 * if the line does not contain a section start indicator.
	 */
	private static String checkSectionEnd(String line, ContentBlock currentBlock, String fileName)
	{
		int index = line.indexOf(SECTION_END);
		if (index < 0)
			return null;

		index += SECTION_END_LEN;
		String name = extractSectionName(line, index, fileName);

		if (name != null)
		{
			if (currentBlock == null)
			{
				// Unclosed section
				LogUtil.error(Merger.class, "Merge: Encountered end of section $0 without section begin in output file $1.", name, fileName);
			}
			else if (currentBlock.isSection())
			{
				String currentName = ((SectionContentBlock) currentBlock).getName();
				if (!name.equals(currentName))
				{
					// Unclosed section
					LogUtil.error(Merger.class, "Merge: End of section $0 does not match begin of section $1 in output file $2.", name, currentName, fileName);
				}
			}
		}

		return name;
	}

	/**
	 * Determines the customer section name
	 * @param s Section mark line
	 * @param startPosition Position to begin
	 * @param fileName File name for error msgs
	 * @return Section name
	 */
	public static String extractSectionName(String s, int startPosition, String fileName)
	{
		int posEnd = s.indexOf(TemplateWriter.CUSTOM_SECTION_DELIM, startPosition);
		if (posEnd == -1)
		{
			LogUtil.error(Merger.class, "Section line not closed with $0 in file $1.", TemplateWriter.CUSTOM_SECTION_DELIM, fileName);
			posEnd = s.length();
		}

		String name = s.substring(startPosition, posEnd);
		if (name.length() == 0)
		{
			LogUtil.error(Merger.class, "Unnamed section in file $0.", fileName);
			return null;
		}

		return name;
	}

	/**
	 * Verifies if section names match.
	 * Issues a message if not.
	 *
	 * @param givenSection Name that was found in the source
	 * @param expectedSection Expected section name
	 */
	public static void verifySectionName(String expectedSection, String givenSection)
	{
		if (!expectedSection.equals(givenSection))
		{
			LogUtil.error(Merger.class, "Expected end of section $0, but found section $1.", expectedSection, givenSection);
		}
	}

	/**
	 * Determines the correct begin mark for the customer section.
	 * @param markName Name of the section
	 * @return The correct begin mark
	 */
	public static String sectionBeginText(String markName)
	{
		return TemplateWriter.CUSTOM_SECTION_START + TemplateWriter.CUSTOM_SECTION_DELIM + markName + TemplateWriter.CUSTOM_SECTION_DELIM;
	}

	/**
	 * Determines the correct end mark for the customer section.
	 * @param markName Name of the section
	 * @return The correct end mark
	 */
	public static String sectionEndText(String markName)
	{
		return TemplateWriter.CUSTOM_SECTION_END + TemplateWriter.CUSTOM_SECTION_DELIM + markName + TemplateWriter.CUSTOM_SECTION_DELIM;
	}
}
