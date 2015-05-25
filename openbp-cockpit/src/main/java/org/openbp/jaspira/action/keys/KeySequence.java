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
package org.openbp.jaspira.action.keys;

import java.awt.event.InputEvent;
import java.util.StringTokenizer;

import javax.swing.KeyStroke;

/**
 * A key sequence describes a chain of key combinations.
 * Key sequences can be represented as strings. There are two forms: A short form and a long form
 * of the string representation.
 *
 * The short form is used to define a key sequence, e. g. in resource files.
 * Modifiers are represented by the letters C, S and A for control, shift and alt,
 * respectively. For example, Control-Shift j would be represented as "C-S-j".
 *
 * The long form will be presented to the user, e. g. as accelerator key in a tool tip.
 * Modifiers are represented by their full name (CTRL, SHIFT, ALT). The "+" character
 * is used as delimiter. For example, Control-Shift j would be represented as "CTRL+SHIFT+j".
 *
 * @author Stephan Moritz
 */
public class KeySequence
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Delimeter between modifiers and the actual key (short form) */
	public static final char SHORT_DELIM = '-';

	/** SHIFT delimiter (short form) */
	public static final String SHORT_SHIFT = "S";

	/** CTRL delimiter (short form) */
	public static final String SHORT_CTRL = "C";

	/** ALT delimiter (short form) */
	public static final String SHORT_ALT = "A";

	/** Delimeter between modifiers and the actual key (long form) */
	public static final char LONG_DELIM = '+';

	/** SHIFT delimiter (long form) */
	public static final String LONG_SHIFT = "SHIFT";

	/** CTRL delimiter (long form) */
	public static final String LONG_CTRL = "CTRL";

	/** ALT delimiter (long form) */
	public static final String LONG_ALT = "ALT";

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** String representation of the key sequence (long form) */
	private String sequenceString;

	/** Keys that make up the key sequence */
	private KeyStroke [] keys;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 * Creates a key sequence from a given key string.
	 *
	 * @param sequence Key sequence
	 */
	public KeySequence(String sequence)
	{
		if (sequence == null)
		{
			keys = new KeyStroke [0];
		}
		else
		{
			StringBuffer string = new StringBuffer();

			StringTokenizer tok = new StringTokenizer(sequence);

			keys = new KeyStroke [tok.countTokens()];

			// We divide the sequence into elements
			for (int i = 0; tok.hasMoreTokens(); i++)
			{
				if (i > 0)
					string.append(' ');

				keys [i] = stringToKey(tok.nextToken());

				string.append(keyToString(keys [i], true));
			}

			sequenceString = string.toString();
		}
	}

	/**
	 * Creates a sub sequence of the given sequence.
	 *
	 * @param source Source sequence
	 * @param start Start index or the sub sequence
	 * @param size Number of keys (length) of the sub sequence
	 */
	private KeySequence(KeySequence source, int start, int size)
	{
		StringBuffer string = new StringBuffer();

		keys = new KeyStroke [size];

		for (int i = 0; i < size; i++)
		{
			if (string.length() > 0)
			{
				string.append(' ');
			}

			keys [i] = source.keys [i + start];

			string.append(keyToString(keys [i], true));
		}

		sequenceString = string.toString();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Member access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the length of the key-sequence.
	 * @nowarn
	 */
	public int length()
	{
		return keys.length;
	}

	/**
	 * Returns the n'th key combination of the sequence.
	 * @nowarn
	 */
	public KeyStroke getKeyAt(int n)
	{
		return keys [n];
	}

	/**
	 * Returns a subsequence of this sequence, i\.e\. the first n key combinations.
	 * @param n number of combinations that the new sequence should contain
	 * @return The subsequence
	 */
	public KeySequence getSubSequence(int n)
	{
		return new KeySequence(this, 0, n);
	}

	/**
	 * Returns a tailsequence of this sequence, i\.e\. the last length - 1 key combinations.
	 * @return The subsequence
	 */
	public KeySequence getSequenceTail()
	{
		return new KeySequence(this, 1, length() - 1);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ ToString, equals, hashcode
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns a string representation of this KeySequence.
	 * @nowarn
	 */
	public String toString()
	{
		return sequenceString;
	}

	/**
	 * Checks two objects for equality.
	 * @nowarn
	 */
	public boolean equals(Object obj)
	{
		return (obj instanceof KeySequence && this.toString().equals(obj.toString()));
	}

	/**
	 * Returns the hashcode of this sequence, which is determined using the
	 * string representation .
	 * @nowarn
	 */
	public int hashCode()
	{
		return toString().hashCode();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Helper
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Converts a string representation (short form) into a corresponding key.
	 * @param string String to parse
	 * @return The key stroke
	 */
	public static KeyStroke stringToKey(String string)
	{
		int index = string.lastIndexOf(SHORT_DELIM);

		String keyString;
		int modifiers = 0;

		if (index != -1)
		{
			// Determine modifiers
			String modString = string.substring(0, index);

			if (modString.indexOf(SHORT_CTRL) != -1)
			{
				modifiers += InputEvent.CTRL_MASK;
			}
			if (modString.indexOf(SHORT_SHIFT) != -1)
			{
				modifiers += InputEvent.SHIFT_MASK;
			}
			if (modString.indexOf(SHORT_ALT) != -1)
			{
				modifiers += InputEvent.ALT_MASK;
			}

			keyString = string.substring(index + 1);
		}
		else
		{
			keyString = string;
		}

		return KeyStroke.getKeyStroke(KeyMgr.stringToKeyCode(keyString), modifiers, true);
	}

	/**
	 * Returns a string representation of a given key stroke.
	 * @param key The key to be converted
	 * @param longForm
	 *		true	Returns the long form of the string representation<br>
	 *		false	Returns the short form of the string representation
	 * @return The string representation
	 */
	public static String keyToString(KeyStroke key, boolean longForm)
	{
		char delim = longForm ? LONG_DELIM : SHORT_DELIM;

		StringBuffer result = new StringBuffer();

		int modifiers = key.getModifiers();
		if ((modifiers & InputEvent.ALT_MASK) != 0)
		{
			result.append(longForm ? LONG_ALT : SHORT_ALT);
			result.append(delim);
		}
		if ((modifiers & InputEvent.CTRL_MASK) != 0)
		{
			result.append(longForm ? LONG_CTRL : SHORT_CTRL);
			result.append(delim);
		}
		if ((modifiers & InputEvent.SHIFT_MASK) != 0)
		{
			result.append(longForm ? LONG_SHIFT : SHORT_SHIFT);
			result.append(delim);
		}

		result.append(KeyMgr.keyCodeToString(key.getKeyCode()));

		return result.toString();
	}
}
