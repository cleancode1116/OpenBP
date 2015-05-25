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
package org.openbp.common.rc.primitive;

import org.openbp.common.markup.XMLUtil;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceItem;
import org.openbp.common.rc.ResourceItemTypes;
import org.w3c.dom.Element;

/**
 * Resource item for primitive/char.
 *
 * @author Jens Ferchland
 */
public class TCharacter extends ResourceItem
{
	//////////////////////////////////////////////////
	// @@ ResourceItem implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the mime-type of the resource item.
	 *
	 * @return A string in mime-type format
	 */
	public String getMimeType()
	{
		return ResourceItemTypes.PRIMITIVE_CHARACTER;
	}

	/**
	 * Determines the information from the DOM element
	 * and set this to the resource item.
	 *
	 * @param res Resource the item belongs to
	 * @param source Resource item node
	 * @param group Name of the resource item group or null
	 */
	public void initializeFromDOM(ResourceCollection res, Element source, String group)
	{
		super.initializeFromDOM(res, source, group);

		String s = XMLUtil.getNodeValue(source);
		if (s != null)
		{
			resourceObject = new Character(s.toCharArray() [0]);
		}
	}
}
