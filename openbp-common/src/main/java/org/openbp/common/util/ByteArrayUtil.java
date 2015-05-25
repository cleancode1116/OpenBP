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

/**
 * Byte array manipulation methods.
 *
 * @author Heiko Erhardt
 * @author Dr. Achim Leubner
 */
public final class ByteArrayUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ByteArrayUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	public static byte[] replaceBytes(byte[] bytes, byte[] pattern, byte[] replacement)
	{
		int index = findPattern(bytes, pattern);
		if (index < 0)
			return bytes;

		byte[] result = new byte[bytes.length - pattern.length + replacement.length];

		// Copy from start to index to result
		System.arraycopy(bytes, 0, result, 0, index);

		// Copy replacement to result
		System.arraycopy(replacement, 0, result, index, replacement.length);

		// Copy remainder to result
		int remaining = bytes.length - index - pattern.length;
		System.arraycopy(bytes, index + pattern.length, result, index + replacement.length, remaining);

		return result;
	}

	private static int findPattern(byte[] bytes, byte[] pattern)
	{
		int n = bytes.length - pattern.length;
		for (int i = 0; i < n; ++i)
		{
			if (matchPattern(bytes, i, pattern))
				return i;
		}
		return - 1;
	}

	private static boolean matchPattern(byte[] bytes, int bytesOffset, byte[] pattern)
	{
		int n = pattern.length;

		for (int i = 0; i < n; ++i)
		{
			if (bytes[bytesOffset + i] != pattern[i])
				return false;
		}
		return true;
	}

	// setup a mapping from byte to hex string
	private static final String[] b2h;

	static
	{
		b2h = new String[0x100];
		for (int i = 0; i < b2h.length; i++)
		{
			final int highNibble = i >> 4 & 0xF;
			final int lowNibble = i & 0xF;

			b2h[i] = (Integer.toHexString(highNibble) + Integer.toHexString(lowNibble)).toUpperCase();
		}
	}

	/**
	 * Returns a hexadecimal representation of the byte.
	 *
	 * @param b Byte
	 * @return The string representation
	 */
	public static String toHexString(final byte b)
	{
		return b2h[(b & 0xFF)];
	}

	/**
	 * Returns a hexadecimal representation of the byte array.
	 *
	 * @param b Byte array
	 * @return The string representation
	 */
	public static String toHexString(final byte[] b)
	{
		if (b == null)
		{
			return null;
		}

		final StringBuilder ret = new StringBuilder();
		for (int i = 0; i < b.length; i++)
		{
			ret.append((i > 0 ? "," : "") + toHexString(b[i]));
		}
		return ret.toString();
	}

	/**
	 * Returns a String representation of the byte array formatted as in "[F1, 01, 10]".
	 *
	 * @param b Byte array
	 * @return The string representation
	 */
	public static String toString(final byte[] b)
	{
		if (b == null)
		{
			return "null";
		}

		if (b.length == 0)
		{
			return "empty";
		}

		final StringBuilder ret = new StringBuilder(b.length * 4);
		ret.append("[");
		for (int i = 0; i < b.length; i++)
		{
			ret.append(toHexString(b[i]));
			if (i < b.length - 1)
			{
				ret.append(", ");
			}
		}
		ret.append("]");
		return ret.toString().toUpperCase();
	}

	/**
	 * Returns a short hexadecimal representation of the byte array.
	 */
	public static String toShortHexString(final byte[] b)
	{
		if (b == null)
		{
			return null;
		}

		final StringBuilder ret = new StringBuilder();
		for (byte element : b)
		{
			ret.append(toHexString(element));
		}
		return ret.toString();
	}

	public static byte[] fromShortHexString(final String hex)
		throws IllegalArgumentException
	{
		if (hex == null)
			return null;

		if (hex.length() % 2 != 0)
			throw new IllegalArgumentException("Hex strings must have even length");

		char[] carr = hex.toCharArray();
		byte[] ret = new byte[hex.length() / 2];
		for (int pos = 0; pos < ret.length; pos++)
		{
			ret[pos] = (byte) (dehex(carr[pos * 2]) << 4 | dehex(carr[pos * 2 + 1]));
		}
		return ret;
	}

	public static byte fromHex(final String hex)
		throws IllegalArgumentException
	{
		if (hex == null)
			throw new IllegalArgumentException("Hex strings may not be null");

		if (hex.length() % 2 != 0)
			throw new IllegalArgumentException("Hex strings must have length 2");

		return (byte) (dehex(hex.charAt(0)) << 4 | dehex(hex.charAt(1)));
	}

	private static int dehex(char c)
		throws IllegalArgumentException
	{
		int v = Character.digit(c, 16);
		if (v == - 1)
			throw new IllegalArgumentException("'" + c + "' is no valid hex digit");
		return v;
	}

	/**
	 * Same as toShortHexString(), but after each 8 bytes, a space is inserted.
	 */
	public static String toGroupedShortHexString(final byte[] b)
	{
		if (b == null)
		{
			return null;
		}

		final StringBuffer ret = new StringBuffer();
		for (int i = 0; i < b.length; i++)
		{
			ret.append(toHexString(b[i]));
			if ((i + 1) % 8 == 0)
			{
				ret.append(' ');
			}
		}

		return ret.toString();
	}

	/**
	 * Returns the byte array representation of  the long.
	 */
	public static byte[] toBytes(long lp)
	{
		long l = lp;
		byte[] b = new byte[8];

		b[7] = (byte) (l);
		l >>>= 8;
		b[6] = (byte) (l);
		l >>>= 8;
		b[5] = (byte) (l);
		l >>>= 8;
		b[4] = (byte) (l);
		l >>>= 8;
		b[3] = (byte) (l);
		l >>>= 8;
		b[2] = (byte) (l);
		l >>>= 8;
		b[1] = (byte) (l);
		l >>>= 8;
		b[0] = (byte) (l);

		return b;
	}
}
