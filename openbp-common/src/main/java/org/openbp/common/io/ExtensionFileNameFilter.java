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
import java.io.FilenameFilter;

/**
 * File name filter for XML files (singleton class).
 *
 * @author Heiko Erhardt
 */
public final class ExtensionFileNameFilter
	implements FilenameFilter
{
	/** Extension to search for */
	private String extension;

	/** Singleton instance */
	private static ExtensionFileNameFilter xmlInstance;

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized ExtensionFileNameFilter getXmlInstance()
	{
		if (xmlInstance == null)
		{
			xmlInstance = new ExtensionFileNameFilter("xml");
		}
		return xmlInstance;
	}

	/**
	 * Private constructor.
	 * @param extension Extension to check for; leading '.' will be prepended if not present.
	 */
	public ExtensionFileNameFilter(String extension)
	{
		this.extension = extension;
		if (! this.extension.startsWith("."))
			this.extension = "." + this.extension;
	}

	/**
	 * The method implements the accept method for the FilenameFilter
	 * interface, and implements the functionality for comparison of the
	 * desired filename to a .xml extension.
	 *
	 * @param dir Directory in which the file is to be found
	 * @param name The file name
	 * @return
	 *		true	If the file is an XML file.<br>
	 *		false	Otherwise
	 */
	public boolean accept(File dir, String name)
	{
		return name.endsWith(extension);
	}
}
