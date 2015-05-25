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
package org.openbp.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.string.StringUtil;
import org.openbp.common.string.shellmatcher.ShellMatcher;

/**
 * The FileUtil class provides some additional static file management utility methods
 * not contained in java.io.File.
 * The class contains static method only.
 *
 * @author Heiko Erhardt
 */
public final class FileUtil
{
	//////////////////////////////////////////////////
	// @@ Public constants
	//////////////////////////////////////////////////

	/** File type 'any file' (argument for {@link #list}) */
	public static final int FILETYPE_ANY = 0;

	/** File type 'file' (argument for {@link #list}) */
	public static final int FILETYPE_FILE = 1;

	/** File type 'directory' (argument for {@link #list}) */
	public static final int FILETYPE_DIR = 2;

	//////////////////////////////////////////////////
	// @@ Constructor
	//////////////////////////////////////////////////

	/**
	 * Do not instantiate this class!
	 */
	private FileUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ File list
	//////////////////////////////////////////////////

	/**
	 * Returns an array of strings naming the files and directories in a
	 * directory, matching the files against an optional pattern and file type.
	 *
	 * If this abstract pathname does not denote a directory, then this
	 * method returns null.  Otherwise an array of strings is
	 * returned, one for each file or directory in the directory.  Names
	 * denoting the directory itself and the directory's parent directory are
	 * not included in the result.  Each string is a file name rather than a
	 * complete path.
	 *
	 * There is no guarantee that the name strings in the resulting array
	 * will appear in any specific order; they are not, in particular,
	 * guaranteed to appear in alphabetical order.
	 *
	 * @param dirName Name of the directory or null for the current directory
	 * @param pattern Wildcard pattern (see the {@link ShellMatcher} class)<br>
	 * The pattern is case-insensitive.
	 * @param fileType Type of file to be listed ({@link #FILETYPE_ANY}/{@link #FILETYPE_FILE}/{@link #FILETYPE_DIR})
	 *
	 * KNOWN BUG: It seems that on Win32 systems, it does always return
	 * all files and directories matching pattern, regardless of fileType.
	 *
	 * @return An array of strings naming the files and directories in the
	 * directory.  The array will be empty if the directory is empty.
	 * Returns null if the pathname does not denote a directory, or if an
	 * I/O error occurs.
	 *
	 * @throws SecurityException
	 * If a security manager exists and denies read access to the directory
	 */
	public static String [] list(String dirName, String pattern, int fileType)
	{
		PatternFilenameFilter pff = null;
		if (pattern != null || fileType != FILETYPE_ANY)
			pff = new PatternFilenameFilter(pattern, fileType);

		File dir = new File(dirName);
		String [] fileNames = dir.list(pff);
		return fileNames;
	}

	/**
	 * Internal file name filter class for list method.
	 */
	static class PatternFilenameFilter
		implements FilenameFilter
	{
		/** Shell matcher object for pattern match */
		private ShellMatcher sm = null;

		/** Type of file to be listed (FILETYPE_ANY/FILETYPE_FILE/FILETYPE_DIR) */
		private int fileType;

		/**
		 * Constructor.
		 *
		 * @param pattern Wildcard pattern (see the {@link ShellMatcher} class) or null<br>
		 * The pattern is case-insensitive.
		 * @param fileType Type of file to be listed (FILETYPE_ANY/FILETYPE_FILE/FILETYPE_DIR)
		 */
		public PatternFilenameFilter(String pattern, int fileType)
		{
			if (pattern != null)
			{
				sm = new ShellMatcher(pattern);
				sm.setIgnoreCase(true);
			}
			this.fileType = fileType;
		}

		/**
		 * Tests if a specified file should be included in a file list.
		 *
		 * @param dir Directory in which the file was found
		 * @param name Name of the file
		 * @return
		 *		true	if and only if the name should be included in the file list<br>
		 *		false	otherwise
		 */
		public boolean accept(File dir, String name)
		{
			if (sm != null)
			{
				if (!sm.match(name))
					return false;
			}
			if (fileType != FILETYPE_ANY)
			{
				File f = new File(StringUtil.buildPath(dir.getPath(), name));
				if (f.isDirectory())
				{
					if (fileType == FILETYPE_FILE)
						return false;
				}
				else
				{
					if (fileType == FILETYPE_DIR)
						return false;
				}
			}
			return true;
		}
	}

	//////////////////////////////////////////////////
	// @@ File remove
	//////////////////////////////////////////////////

	/**
	 * Recursively removes a file or directory.
	 * If the file is a directory and is not empty,
	 * its sub directories and files will be deleted also.
	 *
	 * @param src File or directory to delete
	 * @throws IOException If the file could not be deleted
	 */
	public static void remove(File src)
		throws IOException
	{
		if (src.isFile())
		{
			if (!src.delete())
				throw new IOException("Can't delete file '" + src.getPath() + "'");
		}
		else
		{
			String [] files = src.list();
			if (files == null)
				return;

			for (int i = 0; i < files.length; i++)
			{
				// remove sub-directories recursive
				remove(new File(src, files [i]));
			}

			// remove empty directory
			if (!src.delete())
				throw new IOException("Can't delete directory '" + src.getPath() + "'");
		}
	}

	//////////////////////////////////////////////////
	// @@ File copy
	//////////////////////////////////////////////////

	/**
	 * Copy a file or directory.
	 * Can be used for copying files and/or directories.<br>
	 * For some reason, there is no java.io.File.copy() method, hence this method.<br>
	 * It can be used to copy file2file, file2directory, or
	 * directory2directory (recursively).<br>
	 *
	 * @param src Source file or directory
	 * @param dest Destination file or directory
	 * @exception IOException If the operation fails
	 */
	public static void copy(File src, File dest)
		throws IOException
	{
		copy(src, dest, null);
	}

	/**
	 * Copy a file or directory using a file name filter.
	 * Can be used for copying files and/or directories.<br>
	 * For some reason, there is no java.io.File.copy() method, hence this method.<br>
	 * It can be used to copy file2file, file2directory, or
	 * directory2directory (recursively).<br>
	 *
	 * @param src Source file or directory
	 * @param dest Destination file or directory
	 * @param filter File name filter that determines which files of a directory will be copied
	 * @exception IOException If the operation fails
	 */
	public static void copy(File src, File dest, FilenameFilter filter)
		throws IOException
	{
		// Make sure the specified source exists and is readable.
		if (!src.exists())
			throw new IOException("Source file not found: " + src);
		if (!src.canRead())
			throw new IOException("Source file is unreadable: " + src);

		if (src.isFile())
		{
			if (!dest.exists())
			{
				File parentdir = getParent(dest, false);
				if (parentdir != null && !parentdir.exists())
					parentdir.mkdirs();
			}
			else if (dest.isDirectory())
			{
				dest = new File(dest + StringUtil.FOLDER_SEP + getBaseNameWithExtension(src));
			}
		}
		else if (src.isDirectory())
		{
			if (dest.isFile())
				throw new IOException("Cannot copy directory " + src + " to file " + dest);

			if (!dest.exists())
				dest.mkdirs();
		}

		// If we've gotten this far everything is OK and we can copy.
		if (src.isFile())
		{
			FileInputStream source = null;
			FileOutputStream destination = null;

			try
			{
				source = new FileInputStream(src);
				destination = new FileOutputStream(dest);

				byte [] buffer = new byte [1024];
				while (true)
				{
					int bytesRead = source.read(buffer);
					if (bytesRead == -1)
						break;
					destination.write(buffer, 0, bytesRead);
				}
			}
			finally
			{
				if (source != null)
				{
					try
					{
						source.close();
					}
					catch (IOException e)
					{
					}
				}
				if (destination != null)
				{
					try
					{
						destination.close();
					}
					catch (IOException e)
					{
					}
				}
			}
		}
		else if (src.isDirectory())
		{
			String [] files = src.list(filter);

			for (int i = 0; i < files.length; i++)
			{
				String member = files [i];
				String srcMember = StringUtil.buildPath(src.getPath(), member);
				String destMember = StringUtil.buildPath(dest.getPath(), member);

				if ((new File(srcMember)).isDirectory())
				{
					copy(new File(srcMember), new File(destMember), filter);
				}
				else
				{
					FileInputStream source = null;
					FileOutputStream destination = null;

					try
					{
						source = new FileInputStream(srcMember);
						destination = new FileOutputStream(destMember);
						byte [] buffer = new byte [1024];

						for (;;)
						{
							int bytesRead = source.read(buffer);
							if (bytesRead == -1)
								break;
							destination.write(buffer, 0, bytesRead);
						}
					}
					finally
					{
						if (source != null)
						{
							try
							{
								source.close();
							}
							catch (IOException e)
							{
							}
						}
						if (destination != null)
						{
							try
							{
								destination.close();
							}
							catch (IOException e)
							{
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Deletes a file or a directory.
	 *
	 * @param f The file to delete
	 * @param filter The file name filter
	 *
	 * @return
	 *	true	The file was deleted successfully<br>
	 *	false	The deletion failed
	 */
	public static boolean deleteFile(File f, FilenameFilter filter)
	{
		if (f == null)
			return false;

		boolean success = true;
		if (f.isFile())
		{
			f.delete();
		}
		else if (f.isDirectory())
		{
			File [] files = f.listFiles(filter);
			for (int i = 0; i < files.length; i++)
			{
				if (!deleteFile(files [i], filter))
					success = false;
			}

			if (success)
			{
				f.delete();
			}
		}
		return success;
	}

	//////////////////////////////////////////////////
	// @@ Path name handling
	//////////////////////////////////////////////////

	/**
	 * Gets the base name (file name without directory path but with extension) of a path name.
	 * @param file The file
	 * @return The base name
	 */
	public static String getBaseNameWithExtension(File file)
	{
		return getBaseNameWithExtension(file.toString());
	}

	/**
	 * Gets the base name (file name without directory path but with extension) of a path name.
	 * @param pathName The path name of the file
	 * @return The base name
	 */
	public static String getBaseNameWithExtension(String pathName)
	{
		// Search for the last '/' before the pattern and get the directory
		int iSep = pathName.lastIndexOf(StringUtil.FOLDER_SEP_CHAR);

		return iSep >= 0 ? pathName.substring(iSep + 1) : pathName;
	}

	/**
	 * Gets the base name (file name without directory path and extension) of a path name.
	 * @param pathName The path name of the file
	 * @return The base name
	 */
	public static String getBaseName(String pathName)
	{
		// Search for the last '/' before the pattern and get the directory
		int iSep = pathName.lastIndexOf(StringUtil.FOLDER_SEP_CHAR);
		int iDot = pathName.lastIndexOf('.');

		if (iSep >= 0)
		{
			if (iDot > iSep)
				return pathName.substring(iSep + 1, iDot);
			return pathName.substring(iSep + 1);
		}

		if (iDot > 0)
			return pathName.substring(0, iDot);
		return pathName;
	}

	/**
	 * Gets the full path name of the directory from a file path specification.
	 *
	 * @param filePath Path specification of the file
	 * @return Directory path or null if the file is in the current directory
	 */
	public static String getDirectoryFromFilePath(String filePath)
	{
		filePath =  StringUtil.normalizePathName(filePath);
		int i = filePath.lastIndexOf(StringUtil.FOLDER_SEP_CHAR);
		return i > 0 ? filePath.substring(0, i) : null;
	}

	/**
	 * Determines the relative path from the given origin path name to the given target path name,
	 * assuming that the target is a sub directory of the origin.
	 * For a generic solution if the directories do not relate to each other see the {@link #getRelativePath} method.
	 *
	 * @param target Absolute path of the target path (the sub directory, example: "d:\Src\framework\skyfw")
	 * @param origin Absolute path of the origin path (the base directory, example: "d:\Src")
	 *
	 * @return The relative path or null if 'target' turns out not to be a sub dir of 'origin'.
	 * Example: ("framework\skyfw")
	 */
	public static String getRelativePathToParentDir(String target, String origin)
	{
		if (origin == null)
			return target;

		origin = StringUtil.absolutePathName(origin);
		target = StringUtil.absolutePathName(target);

		if (!origin.endsWith(StringUtil.FOLDER_SEP))
		{
			origin += StringUtil.FOLDER_SEP;
		}

		String retVal = null;
		if (target.startsWith(origin))
		{
			int start = origin.length();
			int end = target.length();
			retVal = target.substring(start, end);
		}

		return retVal;
	}

	/**
	 * Returns the relative path between two different files.
	 * Both parameters have to be absolute paths, but do not need to be parent/sub directories of each other.
	 *
	 * @param startPath The start directory
	 * @param targetPath The directory which should be reached by appending the resulting relative
	 * path to the start directory.
	 * @return The relative path
	 * @throws IllegalArgumentException if one of the arguments is null or a relative path
	 */
	public static String getRelativePath(String startPath, String targetPath)
	{
		// Check for null.
		if (startPath == null || targetPath == null)
		{
			throw new IllegalArgumentException("Null not allowed.");
		}

		// Convert separators.
		startPath = StringUtil.absolutePathName(startPath);
		targetPath = StringUtil.absolutePathName(targetPath);

		// Create files.
		File startDir = new File(startPath);
		File targetDir = new File(targetPath);

		// Check absoluteness.
		if (!startDir.isAbsolute() || !targetDir.isAbsolute())
		{
			throw new IllegalArgumentException("Relative pathes not allowed.");
		}

		// Tokenize file names.
		String [] startPathTokens = StringUtil.tokenize(startPath, StringUtil.FOLDER_SEP);
		String [] targetPathTokens = StringUtil.tokenize(targetPath, StringUtil.FOLDER_SEP);

		// If we have a multi-filesystem-root-OS, the roots of the arguments must be identical!
		File [] roots = File.listRoots();
		if ((roots [0].getPath() != "/") && !startPathTokens [0].equalsIgnoreCase(targetPathTokens [0]))
		{
			throw new IllegalArgumentException("Files reside in different roots.");
		}

		// Determine the number of parts that are equal in both directories.
		int common = (roots [0].getPath() != "/") ? 1 : 0;
		for (int i = common; i < Math.min(startPathTokens.length, targetPathTokens.length); i++)
		{
			if (!startPathTokens [i].equals(targetPathTokens [i]))
			{
				break;
			}

			common++;
		}

		// Create the result.
		StringBuffer result = new StringBuffer();

		// For each part in the start directory that differs from the target directory...
		for (int i = common; i < startPathTokens.length; i++)
		{
			// Separator?
			if (result.length() > 0)
			{
				result.append(StringUtil.FOLDER_SEP);
			}

			// ...we move one up.
			result.append("..");
		}

		// For each part in the target directory that differs from the start directory...
		for (int i = common; i < targetPathTokens.length; i++)
		{
			// Separator?
			if (result.length() > 0)
			{
				result.append(StringUtil.FOLDER_SEP);
			}

			// ...we go down again.
			result.append(targetPathTokens [i]);
		}

		// This is the result!
		return result.toString();
	}

	/**
	 * Constructs a file name from a directory and extension specification.
	 *
	 * @param dir Directory to place the file in
	 * @param name Base name of the file
	 * @param extension The file name extension
	 * @return The path name of the file
	 */
	public static String constructFileName(String dir, String name, String extension)
	{
		StringBuffer sb = new StringBuffer();
		if (dir != null)
		{
			sb.append(dir);
			sb.append(StringUtil.FOLDER_SEP_CHAR);
		}
		sb.append(name);
		if (extension != null && !name.endsWith(extension))
			sb.append(extension);
		return sb.toString();
	}

	//////////////////////////////////////////////////
	// @@ Temporary file creation
	//////////////////////////////////////////////////

	/**
	 * Determines the full path name for a temporary file.
	 *
	 * @param prefix Prefix for temporary file name or null
	 * @param suffix Suffix for temporary file name or null
	 * @param directory Directory of temporary file
	 * @param extension Extension of temporary file or null
	 *
	 * @return A File object specifying the temporary file
	 */
	public static File createTempFile(String directory, String prefix, String suffix, String extension)
	{
		File retFile = null;

		if (directory == null)
			directory = "";
		else
		{
			if (!directory.endsWith("/") && !directory.endsWith("\\"))
			{
				directory += StringUtil.FOLDER_SEP;
			}
		}

		if (prefix == null)
			prefix = "";
		if (suffix == null)
			suffix = "";
		if (extension == null)
			extension = "";

		if (extension.startsWith("."))
		{
			extension = extension.substring(1);
		}

		for (int i = 0;; ++i)
		{
			retFile = new File(directory + prefix + i + suffix + "." + extension);
			if (!retFile.exists())
				break;
		}

		return retFile;
	}

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * File.getParent() can return null when the file is specified without
	 * a directory or is in the root directory. This method handles those cases.
	 *
	 * @param f The target File to analyze
	 * @param returnCurrent
	 *		true	Returns a file object (the current directory) also if the file does not
	 *				contain a path specification.<br>
	 *		false	Returns null if the file does not contain a path specification.
	 * @return The parent directory as a File
	 */
	public static File getParent(File f, boolean returnCurrent)
	{
		// try/catch due to a bug causing f.getParent to throw an exception if the
		// file does not contain a path specification
		try
		{
			String dirname = f.getParent();
			if (dirname != null)
			{
				// Regular directory
				return new File(dirname);
			}

			if (f.isAbsolute())
			{
				// Root directory
				return new File(StringUtil.FOLDER_SEP);
			}

			// Current directory
		}
		catch (Exception ex)
		{
			// Current directory
		}
		return returnCurrent ? new File(System.getProperty("user.dir")) : null;
	}

	//////////////////////////////////////////////////
	// @@ Main method for test
	//////////////////////////////////////////////////

	/**
	 * Main method for test purposes.
	 *
	 * @param args Argument array
	 */
	public static void main(String [] args)
	{
		try
		{
			FileUtil.remove(new File("D://temp//test"));
		}
		catch (IOException e)
		{
			ExceptionUtil.printTrace(e);
		}
	}
}
