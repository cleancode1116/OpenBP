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
package org.openbp.core.model.item.type;

import org.openbp.core.model.item.ItemTypes;

/**
 * A primitive type.
 *
 * The name of the type must follow Java naming conventions and must begin with an uppercase letter.
 *
 * @author Heiko Erhardt
 */
public class SimpleTypeItemImpl extends DataTypeItemImpl
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SimpleTypeItemImpl()
	{
		setItemType(ItemTypes.TYPE);
	}

	/**
	 * Determines if this type is a simple type or a complex type.
	 * @return Always true
	 */
	public boolean isSimpleType()
	{
		return true;
	}

	/**
	 * Determines if if this type is a simple type or a complex type is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasSimpleType()
	{
		return true;
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Determines the name of the type relative to the model that owns the item.
	 * The method will search the imports of the model and return the name of the
	 * item relative to an imported model if the item belongs to an imported model
	 * or a sub model of an imported model (or the System model, since the System
	 * model is imported automatically). If the item could not be found in the
	 * list of imported models, the method will return the fully qualified name
	 * of the item.
	 *
	 * @return The relative name of a simple type is always the type name because
	 * simple types are contained in the System model
	 */
	public String determineItemRef()
	{
		return getName();
	}
}
