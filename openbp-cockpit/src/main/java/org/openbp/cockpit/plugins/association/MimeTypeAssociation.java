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
package org.openbp.cockpit.plugins.association;

import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.string.StringUtil;

/**
 * Association object containing the mime type and the
 * application belongs to that.
 *
 * @author Andreas Putz
 */
public class MimeTypeAssociation extends DisplayObjectImpl
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The associated program */
	private String associatedProgram;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public MimeTypeAssociation()
	{
	}

	/**
	 * Constructor.
	 *
	 * @param name The name of the mime type
	 */
	public MimeTypeAssociation(String name)
	{
		super(name);
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Returns the associated program.
	 * @nowarn
	 */
	public String getAssociatedProgram()
	{
		return associatedProgram;
	}

	/**
	 * Sets the associated program.
	 * @nowarn
	 */
	public void setAssociatedProgram(String associatedProgram)
	{
		this.associatedProgram = associatedProgram;
	}

	//////////////////////////////////////////////////
	// @@ Display object overridden methods
	//////////////////////////////////////////////////

	/**
	 * Gets the default display name of this object.
	 * The display name is the human-readable name of the object (i. e. a name that is
	 * displayed in the user interface).
	 * @nowarn
	 */
	public String getDisplayName()
	{
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Copyable implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.common.generic.Copyable#copyFrom(Object, int)
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;

		MimeTypeAssociation mtaSource = (MimeTypeAssociation) source;

		super.copyFrom(mtaSource, copyMode);

		associatedProgram = mtaSource.getAssociatedProgram();
	}

	//////////////////////////////////////////////////
	// @@ Serialize and deserialize methods
	//////////////////////////////////////////////////

	/**
	 * Serializes the mime type association.
	 *
	 * @return The serialized object
	 */
	String serialize()
	{
		StringBuffer serialized = new StringBuffer(200);

		StringUtil.append(serialized, "name(", getName(), ")");
		StringUtil.append(serialized, "program(", associatedProgram == null ? "" : associatedProgram, ")");
		StringUtil.append(serialized, "description(", getDescription() == null ? "" : getDescription(), ")");

		return serialized.toString();
	}

	/**
	 * Deserializes a string to an mime type association.
	 *
	 * @param serialized The serialized object as string
	 *
	 * @return The deserialized object
	 */
	static MimeTypeAssociation deserialize(String serialized)
	{
		MimeTypeAssociation retVal = null;

		if (serialized != null && serialized.indexOf("name(") != -1)
		{
			int pos1 = serialized.indexOf("name(") + 5;
			int pos2 = serialized.indexOf(")", pos1);

			String name = serialized.substring(pos1, pos2);
			retVal = new MimeTypeAssociation(name);

			if (serialized.indexOf("program(") != -1)
			{
				pos1 = serialized.indexOf("program(") + 8;
				pos2 = serialized.indexOf(")", pos1);

				if (pos1 < pos2)
					retVal.setAssociatedProgram(serialized.substring(pos1, pos2));
			}

			if (serialized.indexOf("description(") != -1)
			{
				pos1 = serialized.indexOf("description(") + 12;
				pos2 = serialized.indexOf(")", pos1);

				if (pos1 < pos2)
					retVal.setDescription(serialized.substring(pos1, pos2));
			}
		}

		return retVal;
	}
}
