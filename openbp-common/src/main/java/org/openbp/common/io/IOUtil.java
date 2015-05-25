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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * The FileUtil class provides some additional static file management utility methods
 * not contained in java.io.File.
 * The class contains static method only.
 *
 * @author Heiko Erhardt
 */
public final class IOUtil
{
	//////////////////////////////////////////////////
	// @@ Public constants
	//////////////////////////////////////////////////

	public static final String CHAR_SET_UTF_8 = "UTF-8";

	public static final String CHAR_SET_ISO_8859_1 = "ISO-8859-1";

	//////////////////////////////////////////////////
	// @@ Constructor
	//////////////////////////////////////////////////

	/**
	 * Do not instantiate this class!
	 */
	private IOUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Stream reading
	//////////////////////////////////////////////////

	/**
	 * Reads a text file stream.
	 *
	 * @param in Inputstream
	 * @return The contents of the file.<br>
	 * The lines will be separated by "\\n".
	 * @throws IOException On i/o error
	 */
	public static String readTextFile(InputStream in)
		throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null)
		{
			if (sb.length() > 0)
			{
				sb.append('\n');
			}
			sb.append(line);
		}

		return sb.toString();
	}

	/**
	 * Reads a text file.
	 *
	 * @param inputFileName Name of the file to read
	 * @return The contents of the file.<br>
	 * The lines will be separated by "\\n".
	 * @throws IOException On i/o error
	 */
	public static String readTextFile(String inputFileName)
		throws IOException
	{
		FileInputStream in = null;

		try
		{
			in = new FileInputStream(inputFileName);

			return readTextFile(in);
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	/**
	 * Reads a text file.
	 *
	 * @param inputFile File to read
	 * @return The contents of the file.<br>
	 * The lines will be separated by "\\n".
	 * @throws IOException On i/o error
	 */
	public static String readTextFile(File inputFile)
		throws IOException
	{
		FileInputStream in = null;

		try
		{
			in = new FileInputStream(inputFile);

			return readTextFile(in);
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	/**
	 * Write a string to file.
	 *
	 * @param outString String to save; When writing the string, UTF8 encoding will be used
	 * @param outputFileName File name
	 * @param rn Retry notifier object to be called in case of i/o error or null
	 * to suppress retries
	 * @return
	 *		true	The file was successfully written.<br>
	 *		false	There was a file write error and the retry notification method
	 *				returned false (this usually means that the user chose 'Abort').
	 *
	 * @exception IOException On error if there is no retry notifier supplied.
	 */
	public static boolean writeFile(String outputFileName, String outString, RetryNotifier rn)
		throws IOException
	{
		return writeFile(outputFileName, outString, null, rn, null);
	}

	/**
	 * Write a string to file.
	 *
	 * @param outString String to save; When writing the string, UTF8 encoding will be used
	 * @param outputFileName File name
	 * @param rn Retry notifier object to be called in case of i/o error or null
	 * to suppress retries
	 * @param append
	 *		true	The string/byteArray will be appended to the file if the file exists.<br>
	 *		false	A new file will be created if the file does not exist.
	 * @return
	 *		true	The file was successfully written.<br>
	 *		false	There was a file write error and the retry notification method
	 *				returned false (this usually means that the user chose 'Abort').
	 *
	 * @exception IOException On error if there is no retry notifier supplied.
	 */
	public static boolean writeFile(String outputFileName, String outString, RetryNotifier rn, boolean append)
		throws IOException
	{
		return writeFile(outputFileName, outString, null, rn, null, append);
	}

	/**
	 * Write a string to file.
	 *
	 * @param outString String to save; When writing the string, UTF8 encoding will be used
	 * @param outputFileName File name
	 * @param rn Retry notifier object to be called in case of i/o error or null
	 * to suppress retries
	 * @param charSet The character set to be used, i.e. UTF-8, ISO 8859-1 etc.
	 * If null, then the UTF-8 character set will be used.
	 * @return
	 *		true	The file was successfully written.<br>
	 *		false	There was a file write error and the retry notification method
	 *				returned false (this usually means that the user chose 'Abort').
	 *
	 * @exception IOException On error if there is no retry notifier supplied.
	 */
	public static boolean writeFile(String outputFileName, String outString, RetryNotifier rn, String charSet)
		throws IOException
	{
		return writeFile(outputFileName, outString, null, rn, charSet);
	}

	/**
	 * Write a string to file.
	 *
	 * @param outString String to save; When writing the string, UTF8 encoding will be used
	 * @param outputFileName File name
	 * @param rn Retry notifier object to be called in case of i/o error or null
	 * to suppress retries
	 * @param charSet The character set to be used, i.e. UTF-8, ISO 8859-1 etc.
	 * If null, then the UTF-8 character set will be used.
	 * @param append
	 *		true	The string/byteArray will be appended to the file if the file exists.<br>
	 *		false	A new file will be created if the file does not exist.
	 * @return
	 *		true	The file was successfully written.<br>
	 *		false	There was a file write error and the retry notification method
	 *				returned false (this usually means that the user chose 'Abort').
	 *
	 * @exception IOException On error if there is no retry notifier supplied.
	 */
	public static boolean writeFile(String outputFileName, String outString, RetryNotifier rn, String charSet, boolean append)
		throws IOException
	{
		return writeFile(outputFileName, outString, null, rn, charSet, append);
	}

	/**
	 * Write a byte array to file.
	 *
	 * @param outBytes Byte array to save
	 * @param outputFileName File name
	 * @param rn Retry notifier object to be called in case of i/o error or null
	 * to suppress retries
	 * @return
	 *		true	The file was successfully written.<br>
	 *		false	There was a file write error and the retry notification method
	 *				returned false (this usually means that the user chose 'Abort').
	 *
	 * @exception IOException On error if there is no retry notifier supplied.
	 */
	public static boolean writeFile(String outputFileName, byte [] outBytes, RetryNotifier rn)
		throws IOException
	{
		return writeFile(outputFileName, null, outBytes, rn, null);
	}

	/**
	 * Write a byte array to file.
	 *
	 * @param outBytes Byte array to save
	 * @param outputFileName File name
	 * @param rn Retry notifier object to be called in case of i/o error or null
	 * to suppress retries
	 * @param append
	 *		true	The string/byteArray will be appended to the file if the file exists.<br>
	 *		false	A new file will be created if the file does not exist.
	 * @return
	 *		true	The file was successfully written.<br>
	 *		false	There was a file write error and the retry notification method
	 *				returned false (this usually means that the user chose 'Abort').
	 *
	 * @exception IOException On error if there is no retry notifier supplied.
	 */
	public static boolean writeFile(String outputFileName, byte [] outBytes, RetryNotifier rn, boolean append)
		throws IOException
	{
		return writeFile(outputFileName, null, outBytes, rn, null, append);
	}

	/**
	 * Write a byte array to file.
	 *
	 * @param outBytes Byte array to save
	 * @param outputFileName File name
	 * @param rn Retry notifier object to be called in case of i/o error or null
	 * to suppress retries
	 * @param charSet The character set to be used, i.e. UTF-8, ISO 8859-1 etc.
	 * If null, then the UTF-8 character set will be used.
	 * @return
	 *		true	The file was successfully written.<br>
	 *		false	There was a file write error and the retry notification method
	 *				returned false (this usually means that the user chose 'Abort').
	 *
	 * @exception IOException On error if there is no retry notifier supplied.
	 */
	public static boolean writeFile(String outputFileName, byte [] outBytes, RetryNotifier rn, String charSet)
		throws IOException
	{
		return writeFile(outputFileName, null, outBytes, rn, charSet);
	}

	/**
	 * Write a byte array to file.
	 *
	 * @param outBytes Byte array to save
	 * @param outputFileName File name
	 * @param rn Retry notifier object to be called in case of i/o error or null
	 * to suppress retries
	 * @param charSet The character set to be used, i.e. UTF-8, ISO 8859-1 etc.
	 * If null, then the UTF-8 character set will be used.
	 * @param append
	 *		true	The string/byteArray will be appended to the file if the file exists.<br>
	 *		false	A new file will be created if the file does not exist.
	 * @return
	 *		true	The file was successfully written.<br>
	 *		false	There was a file write error and the retry notification method
	 *				returned false (this usually means that the user chose 'Abort').
	 *
	 * @exception IOException On error if there is no retry notifier supplied.
	 */
	public static boolean writeFile(String outputFileName, byte [] outBytes, RetryNotifier rn, String charSet, boolean append)
		throws IOException
	{
		return writeFile(outputFileName, null, outBytes, rn, null, append);
	}

	/**
	 * Writes a string or byte array to file, attempting retries on error.
	 * If there is an exception writing the file, a message will appear
	 * asking the user if he wants to retry or abort. In the latter case,
	 * an IOException will be thrown.
	 *
	 * @param outString String to save or null
	 * @param outBytes Byte array to save or null
	 * @param outputFileName File name
	 * @param rn Retry notifier object to be called in case of i/o error or null
	 * to suppress retries
	 * @param charSet The character set to be used, i.e. UTF-8, ISO 8859-1 etc.
	 * If null, then the UTF-8 character set will be used.
	 * @return
	 *		true	The file was successfully written.<br>
	 *		false	There was a file write error and the retry notification method
	 *				returned false (this usually means that the user chose 'Abort').
	 *
	 * @exception IOException On error if there is no retry notifier supplied.
	 */
	private static boolean writeFile(String outputFileName, String outString, byte [] outBytes, RetryNotifier rn, String charSet)
		throws IOException
	{
		return writeFile(outputFileName, outString, outBytes, rn, charSet, false);
	}

	/**
	 * Writes a string or byte array to file, attempting retries on error.
	 * If there is an exception writing the file, a message will appear
	 * asking the user if he wants to retry or abort. In the latter case,
	 * an IOException will be thrown.
	 *
	 * @param outString String to save or null
	 * @param outBytes Byte array to save or null
	 * @param outputFileName File name
	 * @param rn Retry notifier object to be called in case of i/o error or null
	 * to suppress retries
	 * @param charSet The character set to be used, i.e. UTF-8, ISO 8859-1 etc.
	 * If null, then the UTF-8 character set will be used.
	 * @param append
	 *		true	The string/byteArray will be appended to the file if the file exists.<br>
	 *		false	A new file will be created if the file does not exist.
	 * @return
	 *		true	The file was successfully written.<br>
	 *		false	There was a file write error and the retry notification method
	 *				returned false (this usually means that the user chose 'Abort').
	 *
	 * @exception IOException On error if there is no retry notifier supplied.
	 */
	private static boolean writeFile(String outputFileName, String outString, byte [] outBytes, RetryNotifier rn, String charSet, boolean append)
		throws IOException
	{
		FileOutputStream stream = null;
		while (true)
		{
			try
			{
				// Now write the output file
				File f = new File(outputFileName);

				File parentdir = FileUtil.getParent(f, false);
				if (parentdir != null && !parentdir.exists())
					parentdir.mkdirs();

				stream = new FileOutputStream(outputFileName, append);

				if (outString != null)
				{
					if (charSet == null)
					{
						charSet = CHAR_SET_UTF_8;
					}

					// output for string writing
					OutputStreamWriter writer = new OutputStreamWriter(stream, charSet);

					writer.write(outString);
					writer.close();
				}
				else if (outBytes != null)
				{
					// output for byte writing
					stream.write(outBytes);
				}
				stream.flush();
				return true;
			}
			catch (IOException e)
			{
				if (rn != null)
				{
					if (rn.shallRetry(e, outputFileName))
						continue;
					return false;
				}
				throw e;
			}
			finally
			{
				if (stream != null)
					stream.close();
			}
		}
	}
}
