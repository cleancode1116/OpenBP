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
import java.io.FilenameFilter;

/**
 * The ShellFilenameFilter class implements a UNIX-shell-compatible file name filter.
 * The filter can be used with the list method of the java.io.File class or AWT's file
 * dialog component.
 *
 * @seec ShellMatcher
 *
 * @author Heiko Erhardt
 */
public class ShellFilenameFilter
	implements FilenameFilter
{
	private ShellMatcher sm;

	/**
	 * Default constructor.
	 */
	public ShellFilenameFilter()
	{
		sm = new ShellMatcher();
	}

	/**
	 * Value constructor.
	 *
	 * @param pattern The pattern to match the file names against
	 *
	 * @seem setPattern
	 */
	public ShellFilenameFilter(String pattern)
	{
		this();
		setPattern(pattern);
	}

	/**
	 * Sets the pattern.
	 *
	 * @param pattern The pattern to match the file names against.<br>
	 * For a description of the meta characters that can be used in the pattern, see
	 * the {@link ShellMatcher#setPattern} method.
	 */
	public void setPattern(String pattern)
	{
		sm.setPattern(pattern);
	}

	/**
	 * Sets the ignore case option.
	 *
	 * @param ignoreCase
	 *		true	The character case will be ignored when comparing literals.<br>
	 *		false	The pattern matcher will distinguish between lower and upper case.
	 */
	public void setIgnoreCase(boolean ignoreCase)
	{
		sm.setIgnoreCase(ignoreCase);
	}

	/**
	 * Tests if a specified file should be included in a file list
	 *
	 * @param dir The directory in which the file was found
	 * @param name The name of the file
	 * @return @return
	 *		true	if the name should be included in the file list<br>
	 *		false	otherwise
	 */
	public boolean accept(File dir, String name)
	{
		return sm.match(name);
	}
}
