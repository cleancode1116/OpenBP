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
package org.openbp.common.string.parser;

/**
 * StringParserException encapsulates an exception thrown by the methods of the
 * {@link StringParser} class.
 *
 * @author Heiko Erhardt
 */
public class StringParserException extends RuntimeException
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** File name for error messages */
	private String fileName;

	/** Current line number */
	private int lineNr;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor
	 *
	 * @param msg The error message
	 */
	StringParserException(String msg)
	{
		super(msg);
	}

	/**
	 * Default constructor
	 *
	 * @param msg The error message
	 * @param fileName File name for error messages
	 * @param lineNr Current line number
	 */
	StringParserException(String msg, String fileName, int lineNr)
	{
		super(msg);
		this.fileName = fileName;
		this.lineNr = lineNr;
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the file name for error messages.
	 * @nowarn
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * Gets the current line number.
	 * @nowarn
	 */
	public int getLineNr()
	{
		return lineNr;
	}
}
