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

import java.util.ArrayList;
import java.util.List;

/**
 * Generic string parser class.
 * Provides methods to scan a string for identifiers, numbers etc.
 *
 * @author Heiko Erhardt
 */
public class StringParser
{
	//////////////////////////////////////////////////
	// @@ Data
	//////////////////////////////////////////////////

	/** Working buffer */
	private static StringBuffer tempBuffer = new StringBuffer();

	/** Empty array; used as return value for parameter-less function calls */
	private static List emptyList = new ArrayList();

	/** The string to work on */
	private String str;

	/** Length of the string */
	private int length;

	/** Current position in string */
	private int index;

	/** Current line number */
	private int line;

	/** File name for error messages */
	private String errorFileName;

	/** Valid characters in identifiers beside letter, digit or underscore */
	private String extraIdentifierChars;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param strArg String to parse
	 */
	public StringParser(String strArg)
	{
		reInit(strArg);
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Gets the valid characters in identifiers beside letter, digit or underscore.
	 * @nowarn
	 */
	public String getExtraIdentifierChars()
	{
		return extraIdentifierChars;
	}

	/**
	 * Sets the valid characters in identifiers beside letter, digit or underscore.
	 * @nowarn
	 */
	public void setExtraIdentifierChars(String extraIdentifierChars)
	{
		this.extraIdentifierChars = extraIdentifierChars;
	}

	/**
	 * Re-inits the string parse.
	 * Sets the string to parse and resets the current index and line number.
	 *
	 * @param str String to parse
	 */
	public void reInit(String str)
	{
		this.str = str;
		length = str.length();
		line = 1;
		index = 0;
	}

	/**
	 * Sets the file name to prepend to error messages.
	 * If an error file name is set, every error message will be prepended by
	 * "errorFileName (linenr): ".
	 *
	 * @param errorFileName File name or null
	 */
	public void setFileName(String errorFileName)
	{
		this.errorFileName = errorFileName;
	}

	/**
	 * Get the current character.
	 *
	 * @return The current character. The current index is not advanced.
	 */
	public char getChar()
	{
		return index < length ? str.charAt(index) : 0;
	}

	/**
	 * Get a character relative to the current position.
	 *
	 * @param i The index of the character to return from the current position
	 *
	 * @return The character. The current index is not advanced.
	 */
	public char getChar(int i)
	{
		i += index;
		return i < length ? str.charAt(i) : 0;
	}

	/**
	 * Proceed to the next character.
	 */
	public void nextChar()
	{
		if (getChar() == '\n')
			++line;
		++index;
	}

	/**
	 * Skip characters.
	 *
	 * @param n Number of characters to skip
	 */
	public void nextChar(int n)
	{
		while (n-- > 0 && index < length)
		{
			if (str.charAt(index) == '\n')
				++line;
			++index;
		}
	}

	/**
	 * Rewind to the previous character.
	 */
	public void rewindChar()
	{
		if (index > 0)
		{
			--index;
			if (getChar() == '\n')
				--line;
		}
	}

	/**
	 * Rewind several characters.
	 *
	 * @param n Number of characters to rewind
	 */
	public void rewindChar(int n)
	{
		while (n-- > 0 && index > 0)
		{
			--index;
			if (str.charAt(index) == '\n')
				--line;
		}
	}

	/**
	 * Skips all white spaces.
	 */
	public void skipSpace()
	{
		for (; index < length; ++index)
		{
			char c = str.charAt(index);
			if (c == '\n')
				++line;
			if (!Character.isWhitespace(c))
				break;
		}
	}

	/**
	 * Skips spaces and tabs only, no newlines
	 */
	public void skipNoNewlineSpace()
	{
		for (; index < length; ++index)
		{
			char c = str.charAt(index);
			if (c == '\n')
				++line;
			if (c != ' ' && c != '\t')
				break;
		}
	}

	/**
	 * Skips an assignemnt operator (" = ")
	 *
	 * @param ident The identifier before the operator (for error messages)
	 * @return
	 *		true	The operator was skipped successfully<br>
	 *		false	There is no assignment operator.<br>
	 *				In this case, an error is printed.
	 */
	public boolean skipAssignmentOperator(String ident)
	{
		skipSpace();
		if (isEnd() || getChar() != '=')
		{
			printError("'=' exspected after " + ident);
			return false;
		}
		nextChar();
		skipSpace();
		return true;
	}

	/**
	 * Checks if we are at the end of the str.
	 *
	 * @return
	 *		true	The end of the string is reached<br>
	 *		false	The end of the string is not yet reached<br>
	 */
	public boolean isEnd()
	{
		return index >= length;
	}

	/**
	 * Gets the current character position.
	 *
	 * @return The character position, starting with 0
	 */
	public int getPos()
	{
		return index;
	}

	/**
	 * Sets the current character position.
	 *
	 * @param pos The character position, starting with 0
	 */
	public void setPos(int pos)
	{
		index = pos;
	}

	/**
	 * Restores the current character position to a previously saved state.
	 * The current line number is corrected accordingly.
	 *
	 * @param pos The character position to restore
	 */
	public void restorePos(int pos)
	{
		while (index > pos)
		{
			--index;
			char c = getChar();
			if (c == '\n')
				--line;
		}
	}

	/**
	 * Gets the length of the string.
	 *
	 * @return The string length
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * Gets the file name.
	 *
	 * @return The file name if one was assigned using {@link #setFileName}
	 */
	public String getFileName()
	{
		return errorFileName;
	}

	/**
	 * Gets the current line number.
	 *
	 * @return The file name if one was assigned using {@link #setFileName}
	 */
	public int getLineNr()
	{
		return line;
	}

	/**
	 * Sets the current line number.
	 *
	 * @param line The new line number
	 */
	public void setLineNr(int line)
	{
		this.line = line;
	}

	/**
	 * Get the text until the end of the line.
	 * The current position will be at the first character after the end of line characters.
	 *
	 * @return The identifier
	 */
	public String getUntilEndOfLine()
	{
		tempBuffer.setLength(0);

		for (; index < length; nextChar())
		{
			char c = getChar();
			if (c == '\n' || c == '\r')
				break;
			tempBuffer.append(c);
		}

		while (index < length)
		{
			char c = getChar();
			if (c != '\n' && c != '\r')
				break;
			nextChar();
		}

		return tempBuffer.toString();
	}

	/**
	 * Get the identifier at the current position.
	 * The current position will be at the first character after the identifier.
	 *
	 * @return The identifier
	 */
	public String getIdentifier()
	{
		skipSpace();
		char c = getChar();
		if (!Character.isLetter(c) && c != '_')
			return null;

		tempBuffer.setLength(0);
		tempBuffer.append(c);

		for (nextChar(); index < length; nextChar())
		{
			c = getChar();
			if (!Character.isLetterOrDigit(c) && c != '_' &&
				(extraIdentifierChars == null || extraIdentifierChars.indexOf(c) == -1))
				break;
			tempBuffer.append(c);
		}

		return tempBuffer.toString();
	}

	/**
	 * Get the (qualified) identifier at the current position.
	 * The current position will be at the first character after the identifier.
	 *
	 * @return The identifier
	 */
	public String getQualifiedIdentifier()
	{
		skipSpace();
		char c = getChar();
		if (!Character.isLetter(c) && c != '_')
			return null;

		tempBuffer.setLength(0);
		tempBuffer.append(c);

		for (nextChar(); index < length; nextChar())
		{
			c = getChar();
			if (!Character.isLetterOrDigit(c) && c != '_' && c != '.' &&
				(extraIdentifierChars == null || extraIdentifierChars.indexOf(c) == -1))
				break;
			tempBuffer.append(c);
		}

		return tempBuffer.toString();
	}

	/**
	 * Get the text until the next white space at the current position.
	 * The current position will be at the first white space character.
	 *
	 * @return The text
	 */
	public String getNonWhitespace()
	{
		skipSpace();
		char c = getChar();
		if (c == 0 || Character.isWhitespace(c))
			return null;

		tempBuffer.setLength(0);
		tempBuffer.append(c);

		for (nextChar(); index < length; nextChar())
		{
			c = getChar();
			if (Character.isWhitespace(c))
				break;
			tempBuffer.append(c);
		}

		return tempBuffer.toString();
	}

	/**
	 * Get the number constant at the current position.
	 * The current position will be at the first character after the number.
	 *
	 * @return The number. If there is no number, null is returned.
	 */
	public String getNumber()
	{
		skipSpace();
		char c = getChar();
		if (!Character.isDigit(c))
			return null;

		tempBuffer.setLength(0);
		tempBuffer.append(c);

		for (nextChar(); index < length; nextChar())
		{
			c = getChar();
			if (!Character.isDigit(c))
				break;
			tempBuffer.append(c);
		}

		return tempBuffer.toString();
	}

	/**
	 * Get the string constant at the current position.
	 * The string is delimited by \" or &quot;
	 * The current position will be at the first character after the string.
	 *
	 * @return The string
	 */
	public String getOptionArgument()
	{
		String value;
		char c = getChar();
		if (c == '"' || c == '\'')
			value = getString('\0');
		else
			value = str.substring(index);
		return value;
	}

	/**
	 * Get the string constant at the current position.
	 * The string is delimited by \" or &quot;
	 * The current position will be at the first character after the string.
	 *
	 * @param delimChar Specifies the delimiter character that should enclose the returned string.<br>
	 * If you want the string returned without delimiter characters, use '\0' for this parameter.
	 * @return The string
	 */
	public String getString(char delimChar)
	{
		boolean htmlQuote = false;

		tempBuffer.setLength(0);

		skipSpace();
		char c = getChar();
		char cEnd = 0;
		if (c == '"')
		{
			cEnd = c;
			nextChar();
		}
		else if (c == '\'')
		{
			cEnd = c;
			nextChar();
		}
		else if (str.startsWith("&quot;", index))
		{
			htmlQuote = true;
			index += 6;
		}
		else
		{
			// No string constant
			return null;
		}

		if (delimChar != '\0')
			tempBuffer.append(delimChar);

		boolean escape = false;
		for (; index < length; nextChar())
		{
			c = getChar();

			if (!escape)
			{
				if (htmlQuote)
				{
					if (str.startsWith("&quot;", index))
					{
						index += 6;
						break;
					}
				}
				else
				{
					if (c == cEnd)
					{
						nextChar();
						break;
					}
				}
			}

			if (c == '\\')
			{
				escape = true;
				continue;
			}

			escape = false;
			tempBuffer.append(c);
		}

		if (delimChar != '\0')
			tempBuffer.append(delimChar);

		return tempBuffer.toString();
	}

	/**
	 * Get the parameters enclosed in () as an array of strings.
	 * The parameters are separated by ','.
	 * The current position will be at the first character after the ')'.
	 *
	 * @return A list of parameter strings or null if there is no parameter list<br>
	 * Note: In order to reduce object allocation, in case of an empty parameter list,
	 * a static reference to an empty list will be returned. So do not modify
	 * the return value or subsequent calls to this method may not produce the desired
	 * results.
	 */
	public List getOptionalParameters()
	{
		// TODO Minor: Implement parenthesis levels for string parser
		int savIndex = index;

		skipSpace();
		if (getChar() != '(')
		{
			index = savIndex;
			return null;
		}
		nextChar(); // Skip '('

		skipSpace();
		if (getChar() == ')')
		{
			// No arguments
			nextChar(); // Skip ')'
			return emptyList;
		}

		List list = new ArrayList();
		tempBuffer.setLength(0);

		// Collect parameters
		while (index < length)
		{
			char c = getChar();
			if (c == ',' || c == ')')
			{
				// End of parameter recognized

				// Cut trailing spaces
				int j;
				for (j = tempBuffer.length() - 1; j >= 0; --j)
				{
					if (tempBuffer.charAt(j) != ' ')
						break;
				}
				if (j < 0)
				{
					// Parameter of length 0
					throwError("Invalid argument");
				}

				tempBuffer.setLength(j + 1);
				list.add(tempBuffer.toString());

				nextChar(); // Skip ',' or ')'
				if (c == ',')
					skipSpace();
				tempBuffer.setLength(0);

				if (c == ')')
				{
					// End of parameter list
					return list;
				}

				continue;
			}

			// Collect parameter character, convert white space to space
			tempBuffer.append(Character.isWhitespace(c) ? ' ' : c);
			nextChar();
		}

		// End of file
		throwError("Missing ')'");
		return null;
	}

	/**
	 * Check if 'pattern' matches the string at the current position.
	 *
	 * @param pattern The pattern to search for
	 * @return
	 *		true	The match succeeded<br>
	 *		false	The match failed
	 */
	public boolean startsWith(String pattern)
	{
		return str.startsWith(pattern, index);
	}

	/**
	 * Skips until a pattern is found.
	 *
	 * @param pattern The pattern to search for
	 * @param skipPattern
	 *		true	The pattern itself is skipped. The current position will
	 *				be at the first character after the pattern<br>
	 *		false	The current position will be at the first character of the pattern.
	 */
	public void skipUntil(String pattern, boolean skipPattern)
	{
		for (; index < length; nextChar())
		{
			if (str.startsWith(pattern, index))
			{
				if (skipPattern)
					index += pattern.length();
				return;
			}
		}
		throwError("Missing '" + pattern + '\'');
	}

	/**
	 * Performs a substring operation on the string.
	 *
	 * @param begin The start index
	 * @param end The end index
	 * @return The sub string
	 */
	public String substring(int begin, int end)
	{
		return str.substring(begin, end);
	}

	/**
	 * Performs a substring operation on the string.
	 *
	 * @param begin The start index
	 * @return The sub string
	 */
	public String substring(int begin)
	{
		return str.substring(begin);
	}

	/**
	 * Performs a substring operation on the string from the current position up to the
	 * end of the string.
	 *
	 * @return The sub string
	 */
	public String substring()
	{
		return str.substring(index);
	}

	/**
	 * Throws an error runtime exception.
	 *
	 * @param msg The error message
	 */
	public void throwError(String msg)
	{
		throw new StringParserException(errorMsg(msg), getFileName(), getLineNr());
	}

	/**
	 * Prints an error message
	 *
	 * @param msg The error message
	 */
	public void printError(String msg)
	{
		System.err.println(errorMsg(msg));
	}

	/**
	 * Gets the source string.
	 * @nowarn
	 */
	public String getSourceString()
	{
		return str;
	}

	/**
	 * Returns an error message prefixed by the standard error prefix and a
	 * file/line specification (see the {@link #setFileName} methods).
	 *
	 * @param msg The 'raw' error message
	 * @return The formatted error message
	 */
	public String errorMsg(String msg)
	{
		tempBuffer.setLength(0);
		if (errorFileName != null)
		{
			tempBuffer.append(errorFileName);
			tempBuffer.append("(");
			tempBuffer.append(line);
			tempBuffer.append("): ");
		}
		tempBuffer.append(msg);
		return tempBuffer.toString();
	}
}
