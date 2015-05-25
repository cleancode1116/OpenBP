/*
 *   Copyright 2010 skynamics AG
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
package org.openbp.cockpit.modeler.figures.generic;

/**
 * Constants for circle eights.
 */
public enum Eight
	implements IntEnum<Eight>
{
	/** E (right) */
	E(0),

	/** SE (bottom right) */
	SE(1),

	/** S (bottom) */
	S(2),

	/** SW (bottom left) */
	SW(3),

	/** W (left) */
	W(4),

	/** NW (top left) */
	NW(5),

	/** N (top) */
	N(6),

	/** NE (top left) */
	NE(7);

	int value;

	private Eight(int value)
	{
		this.value = value;
	}

	public int toInt()
	{
		return value;
	}

	public static Eight fromInt(int i)
	{
		for (Eight v : Eight.values())
		{
			if (v.toInt() == i)
				return v;
		}
		throw new IllegalArgumentException ("Invalid value '" + i + "' for enumeration of type '" + Eight.class.getName() + "'.");
	}

	public String toString()
	{
		return "" + value;
	}
}
