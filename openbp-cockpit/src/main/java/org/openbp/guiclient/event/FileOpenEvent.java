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
package org.openbp.guiclient.event;

import org.openbp.jaspira.plugin.Plugin;

/**
 * This event is used if a file is to be opened in its associated editor.
 *
 * @author Andreas Putz
 */
public class FileOpenEvent extends OpenEvent
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Line number to select */
	private int lineNumber;

	/** Column number to select */
	private int columnNumber;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * In Comparison to the object that shall be opened the underlying object
	 * contains the environmental information of former.
	 *
	 * @param source Source of the event
	 * @param eventName Name of the event
	 * @param filePath Path of the file that shall be opened
	 * @param mimeTypes MIME types of the object or null
	 */
	public FileOpenEvent(Plugin source, String eventName, String filePath, String [] mimeTypes)
	{
		super(source, eventName, filePath);
		setMimeTypes(mimeTypes);
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the file path
	 *
	 * @nowarn
	 */
	public String getFilePath()
	{
		return (String) getObject();
	}

	/**
	 * Gets the line number to select.
	 * @nowarn
	 */
	public int getLineNumber()
	{
		return lineNumber;
	}

	/**
	 * Sets the line number to select.
	 * @nowarn
	 */
	public void setLineNumber(int lineNumber)
	{
		this.lineNumber = lineNumber;
	}

	/**
	 * Gets the column number to select.
	 * @nowarn
	 */
	public int getColumnNumber()
	{
		return columnNumber;
	}

	/**
	 * Sets the column number to select.
	 * @nowarn
	 */
	public void setColumnNumber(int columnNumber)
	{
		this.columnNumber = columnNumber;
	}
}
