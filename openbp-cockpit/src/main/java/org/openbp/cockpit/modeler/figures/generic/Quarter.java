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
 * Constants for circle quarters.
 */
public enum Quarter
	implements IntEnum<Quarter>
{
	SE (0),
	SW (1),
	NW (2),
	NE (3);

	int value;

	private Quarter(int value)
	{
		this.value = value;
	}

	public int toInt()
	{
		return value;
	}

	public static Quarter fromInt(int i)
	{
		for (Quarter v : Quarter.values())
		{
			if (v.toInt() == i)
				return v;
		}
		throw new IllegalArgumentException ("Invalid value '" + i + "' for enumeration of type '" + Quarter.class.getName() + "'.");
	}

	public String toString()
	{
		return "" + value;
	}
}
