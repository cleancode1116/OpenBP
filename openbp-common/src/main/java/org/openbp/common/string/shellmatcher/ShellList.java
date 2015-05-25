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
package org.openbp.common.string.shellmatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openbp.common.CollectionUtil;
import org.openbp.common.string.StringUtil;

/**
 * The ShellList class can be used to generate a list of files from a
 * UNIX-shell-compatible search pattern.
 * The search pattern may span several directory levels (e. g. "/ usr / src / * / lib* / *.java"
 * - the spaces in the string are there to prevent the compiler from considering this
 * an end of comment, do not insert them in the path in real life).
 *
 * @seec ShellMatcher
 * @seec ShellFilenameFilter
 *
 * @author Heiko Erhardt
 */
public class ShellList
{
	/** List of files found in the tree */
	private List fileList = null;

	/**
	 * Constructor.
	 */
	public ShellList()
	{
		fileList = new ArrayList();
	}

	/**
	 * Expands a pattern to a list of files.
	 * The files are added to the current file list.
	 *
	 * @param pattern The pattern to match the file names against.<br>
	 * For a description of the meta characters that can be used in the pattern, see
	 * the {@link ShellMatcher#setPattern} method.
	 */
	public void expandPattern(String pattern)
	{
		expandPattern(pattern, true);
	}

	/**
	 * Clears the current file list.
	 */
	public void clearFiles()
	{
		fileList.clear();
	}

	/**
	 * Gets the current file list.
	 *
	 * @return An array of file names or null if there were no files found
	 */
	public String [] getFiles()
	{
		return CollectionUtil.toStringArray(fileList);
	}

	/**
	 * Gets the number of elements of the current file list.
	 *
	 * @return The file count
	 */
	public int getFileCount()
	{
		return fileList.size();
	}

	/**
	 * Expands a pattern to a list of files.
	 * The files are added to the current file list.
	 *
	 * @param fileName The pattern to match the file names against.<br>
	 * For a description of the meta characters that can be used in the pattern, see
	 * the {@link ShellMatcher#setPattern} method.
	 * @param first
	 *		true	Determines that this call of the method examines the first level
	 *				of the directory tree.<br>
	 *		false	We're examining a lower level of the directory tree
	 */
	private void expandPattern(String fileName, boolean first)
	{
		fileName =  StringUtil.normalizePathName(fileName);

		StringBuffer dirBuf = new StringBuffer();
		StringBuffer patternBuf = new StringBuffer();
		StringBuffer remainderBuf = new StringBuffer();

		// Split the supplied file name in dir, pattern and remainder
		splitPath(fileName, dirBuf, patternBuf, remainderBuf);

		String dir = dirBuf.length() == 0 ? null : dirBuf.toString();
		String pattern = patternBuf.length() == 0 ? null : patternBuf.toString();
		String remainder = remainderBuf.length() == 0 ? null : remainderBuf.toString();

		if (dir != null)
		{
			File dirFile = new File(dir);
			boolean dirExists = dirFile.exists();

			if (pattern == null && (first || dirExists))
			{
				// No remaining pattern to search this directory, so add directory to list
				fileList.add(dir);
			}

			if (!dirExists)
			{
				// Path does not exist
				return;
			}
		}

		if (pattern == null)
		{
			// File name did not contain meta characters, can't go deeper
			return;
		}

		String [] dirFiles = getFileList(dir, pattern);
		if (dirFiles == null)
		{
			// No files found
			return;
		}

		// Iterate the entries of this directory
		for (int i = 0; i < dirFiles.length; ++i)
		{
			String s = dirFiles [i];
			String path;
			if (dir != null)
				path = dir + StringUtil.FOLDER_SEP_CHAR + s;
			else
				path = s;

			if (remainder == null)
			{
				fileList.add(path);
				continue;
			}

			File pathFile = new File(path);
			if (pathFile.isDirectory())
			{
				// There's still one level, recurse
				if (dir != null)
					path = dir + StringUtil.FOLDER_SEP_CHAR + s + StringUtil.FOLDER_SEP_CHAR + remainder;
				else
					path = s + StringUtil.FOLDER_SEP_CHAR + remainder;
				expandPattern(path, false);
			}
		}
	}

	/**
	 * Find all matching patterns in a directory
	 * and add them to the file list.
	 *
	 * @param dirName Path name of the directory
	 * @param pattern The pattern to match the file names against.<br>
	 * For a description of the meta characters that can be used in the pattern, see
	 * the {@link ShellMatcher#setPattern} method.
	 */
	public void expandDirectory(String dirName, String pattern)
	{
		dirName =  StringUtil.normalizeDir(dirName);

		String [] dirFiles = getFileList(dirName, pattern);
		if (dirFiles != null)
		{
			for (int i = 0; i < dirFiles.length; ++i)
			{
				String s = dirFiles [i];

				if (dirName != null)
					s = dirName + StringUtil.FOLDER_SEP_CHAR + s;

				File file = new File(s);
				if (!file.isDirectory())
					fileList.add(s);
			}
		}

		// Now recurse sub directories

		File dir = new File(dirName);
		dirFiles = dir.list();

		if (dirFiles == null)
		{
			// No files found
			return;
		}

		for (int i = 0; i < dirFiles.length; ++i)
		{
			String s = dirFiles [i];
			if (dirName != null)
				s = dirName + StringUtil.FOLDER_SEP_CHAR + s;

			File file = new File(s);
			if (file.isDirectory())
				expandDirectory(s, pattern);
		}
	}

	/**
	 * Gets a list of files in a directory that match a specific file name pattern.
	 *
	 * @param dirName Path name of the directory
	 * @param pattern The pattern to match the file names against.<br>
	 * For a description of the meta characters that can be used in the pattern, see
	 * the {@link ShellMatcher#setPattern} method.
	 * @return The list of matching files in the directory or null if no files
	 * were found
	 */
	private String [] getFileList(String dirName, String pattern)
	{
		File d = new File(dirName != null ? dirName : ".");
		ShellFilenameFilter sff = new ShellFilenameFilter(pattern);
		sff.setIgnoreCase(true);
		String [] files = d.list(sff);
		return files;
	}

	/**
	 * Splits the supplied path in directory, pattern and remainder.
	 *
	 * "/usr/heiko/ * /makefile" -> dir = "/usr/heiko", pattern = "*", remainder = "makefile"<br>
	 * "/usr/heiko/xx/makefile" -> dir = "/usr/heiko/xx/makefile", pattern = "", remainder = ""<br>
	 * "/usr/heiko/xx/ *" -> dir = "/usr/heiko/xx", pattern = "*", remainder = ""<br>
	 * "* /makefile" -> dir = "", pattern = "*", remainder = "makefile"<br>
	 * "xx/ *" -> dir = "xx", pattern = "*", remainder = ""<br>
	 * "*" -> dir = "", pattern = "*", remainder = ""
	 *
	 * @param path Path name including pattern
	 * @param dir String buffer the path name (without the pattern part) is saved in
	 * @param pattern String buffer the pattern part is saved in
	 * @param remainder String buffer the remainder (after the pattern part) is saved in
	 */
	public static void splitPath(String path, StringBuffer dir, StringBuffer pattern, StringBuffer remainder)
	{
		path =  StringUtil.normalizePathName(path);

		dir.setLength(0);
		pattern.setLength(0);
		remainder.setLength(0);

		// Search for the first meta character
		int lPath = path.length();
		int iPat;
		for (iPat = 0; iPat < lPath; ++iPat)
		{
			char c = path.charAt(iPat);
			if (c == '*' || c == '?' || c == '[')
				break;
		}

		if (iPat == lPath)
		{
			// No pattern found
			dir.append(path);
			return;
		}

		// Search for the last '/' before the pattern and get the directory
		int iSep1 = path.lastIndexOf(StringUtil.FOLDER_SEP_CHAR, iPat);
		if (iSep1 == 0)
			dir.append(StringUtil.FOLDER_SEP_CHAR);
		else if (iSep1 > 0)
			dir.append(path.substring(0, iSep1));

		// Search for the next '/' after the pattern and get the remainder
		int iSep2 = path.indexOf(StringUtil.FOLDER_SEP_CHAR, iPat);
		if (iSep2 >= 0)
			remainder.append(path.substring(iSep2 + 1));

		// Get the pattern itself
		String s = null;
		if (iSep1 >= 0)
		{
			if (iSep2 >= 0)
				s = path.substring(iSep1 + 1, iSep2);
			else
				s = path.substring(iSep1 + 1);
		}
		else
		{
			if (iSep2 >= 0)
				s = path.substring(0, iSep2);
			else
				s = path.substring(0);
		}
		pattern.append(s);
	}
}
