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
package org.openbp.common.generic.description;

/**
 * An object that has a name, a display name and a description and a leaf node flag for
 * display in tree-like structures.
 *
 * @author Heiko Erhardt
 */
public class DisplayTreeObjectImpl extends DisplayObjectImpl
	implements DisplayTreeObject
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Flag if the object is a leaf node */
	private boolean leaf;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		DisplayTreeObjectImpl src = (DisplayTreeObjectImpl) source;

		// Copy primitive data members
		leaf = src.leaf;
	}

	//////////////////////////////////////////////////
	// @@ DisplayObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the flag if the object is a leaf node.
	 * @nowarn
	 */
	public boolean isLeaf()
	{
		return leaf;
	}

	/**
	 * Sets the flag if the object is a leaf node.
	 * @nowarn
	 */
	public void setLeaf(boolean leaf)
	{
		this.leaf = leaf;
	}
}
