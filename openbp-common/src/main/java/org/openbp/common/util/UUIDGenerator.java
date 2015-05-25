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
package org.openbp.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * UUID generator.
 * Generates a universally unique id based on the ip address of the computer and the current time.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public final class UUIDGenerator
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** UUID prefix (derived from the ip address of the local machine) */
	private String prefix;

	/** Static counter used to distinguish request within the same timer interval */
	private static long counter;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static UUIDGenerator singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized UUIDGenerator getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new UUIDGenerator();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private UUIDGenerator()
	{
		// Get the IP address (fixed length: 12)
		try
		{
			String ipAddress = InetAddress.getLocalHost().getHostAddress();

			// Append each section of the IP address as 2 digit hex number
			StringBuffer sb = new StringBuffer();
			StringTokenizer st = new StringTokenizer(ipAddress, ".");
			while (st.hasMoreTokens())
			{
				Long value = new Long(st.nextToken());
				appendHexValue(sb, value.longValue(), 2);
			}

			prefix = sb.toString();
		}
		catch (UnknownHostException e)
		{
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Creates a new UUID.
	 *
	 * @return The UUID (20 hex digits) or null on error (shouldn't happen)
	 */
	public String createUUID()
	{
		StringBuffer sb = new StringBuffer();

		if (prefix != null)
		{
			// Start with the IP address (fixed length: 8)
			sb.append(prefix);
		}

		// Add the current time (fixed length: 8)
		appendHexValue(sb, System.currentTimeMillis(), 8);

		// Increase counter
		if (counter >= 0xffff)
		{
			// Reset counter after overflow
			counter = 0;
		}
		counter++;

		// Add the counter (fixed length: 4)
		appendHexValue(sb, counter, 4);

		return sb.toString();
	}

	/**
	 * Appends a hexadecimal value to the given string buffer.
	 *
	 * @param sb String buffer to append to
	 * @param value The value to append
	 * @param digits Minimum number of digits<br>
	 * The method will insert leading '0' fill characters.
	 */
	public static void appendHexValue(StringBuffer sb, long value, int digits)
	{
		String s = Long.toHexString(value);

		for (int i = digits - s.length(); i > 0; --i)
		{
			sb.append('0');
		}
		sb.append(s);
	}
}
