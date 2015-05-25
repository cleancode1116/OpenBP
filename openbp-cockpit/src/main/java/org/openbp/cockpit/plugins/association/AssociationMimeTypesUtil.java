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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.core.MimeTypes;

/**
 * Contains the default mime type structure.
 *
 * @author Andreas Putz
 */
public class AssociationMimeTypesUtil
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/**
	 * Default assignment map.
	 */
	private static Map defaultAssociations = new LinkedHashMap();
	static
	{
		defaultAssociations.put(MimeTypes.URL, "External Web Browser");
		defaultAssociations.put(MimeTypes.TEXT_FILE, "Text Editor");
		defaultAssociations.put(MimeTypes.SOURCE_FILE, "Source Editor");
		defaultAssociations.put(MimeTypes.HTML_FILE, "HTML Editor");
		defaultAssociations.put(MimeTypes.XML_FILE, "XML/XSL Editor");
		defaultAssociations.put(MimeTypes.APPLICATION_PDF, "PDF Reader");
	}

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	private AssociationMimeTypesUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Adds the default MIME type associations to the given list (if not already contained in the list).
	 *
	 * @param mimeTypes List of customized associations
	 * @param res The resource
	 *
	 * @return List containing the customized as well as default associations
	 */
	static List addDefaultAssociations(List mimeTypes, ResourceCollection res)
	{
		if (mimeTypes == null)
		{
			mimeTypes = new ArrayList();
		}

		// Check if all required types are contained in the given list
		for (Iterator it = defaultAssociations.keySet().iterator(); it.hasNext();)
		{
			String type = (String) it.next();

			boolean found = false;
			int n = mimeTypes.size();
			for (int i = 0; i < n; ++i)
			{
				MimeTypeAssociation mta = (MimeTypeAssociation) mimeTypes.get(i);
				String name = mta.getName();
				if (name.equals(type))
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				// This required type is not contained in the list, so add the default
				mimeTypes.add(createMimeType(type, res));
			}
		}

		return mimeTypes;
	}

	/**
	 * Creates the default file extension program association for the given
	 * mime type.
	 *
	 * @param mimeType The mimetype needs the default association
	 * @param res The resource
	 * @nowarn
	 */
	private static MimeTypeAssociation createMimeType(String mimeType, ResourceCollection res)
	{
		MimeTypeAssociation mta = new MimeTypeAssociation(mimeType);
		if (mimeType == null)
			return mta;

		String defaultValue = (String) defaultAssociations.get(mimeType);
		mta.setDescription(res.getOptionalString("mimetype." + mimeType.replace('/', '.') + "description", defaultValue));

		return mta;
	}
}
