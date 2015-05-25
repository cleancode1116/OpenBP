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
package org.openbp.common.rc.image;

import org.openbp.common.icon.MultiImageIcon;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.markup.XMLUtil;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceItem;
import org.openbp.common.rc.ResourceItemTypes;
import org.openbp.common.string.StringUtil;
import org.w3c.dom.Element;

/**
 * Abstract image resource item implementation for the ImageIcon object.
 *
 * @author Andreas Putz
 */
public class TMulti extends ResourceItem
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** XML tag 'path' */
	protected static final String TAG_IMAGE_PATH = "path";

	protected static final String [] EXTENSIONS = new String [] { "png", "gif", "jpg", "jpeg", "tif" };

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Image path */
	private String path;

	//////////////////////////////////////////////////
	// @@ ResourceItem implementation
	//////////////////////////////////////////////////

	/**
	 * Determines the information from the DOM element
	 * and set this to the resource item.
	 *
	 * @param res Resource the item belongs to
	 * @param sourceElement Resource item node
	 * @param group Name of the resource item group or null
	 */
	public void initializeFromDOM(ResourceCollection res, Element sourceElement, String group)
	{
		super.initializeFromDOM(res, sourceElement, group);

		path = XMLUtil.getChildNodeValue(sourceElement, TAG_IMAGE_PATH);

		if (path == null)
		{
			LogUtil.error(getClass(), "Resource $0: Missing or invalid tag $1.", getErrorName(), TAG_IMAGE_PATH);
			return;
		}

		// Perform global replacements
		path = res.getResourceCollectionMgr().performVariableReplacement(path);
		path = StringUtil.normalizePathName(path);

		if (!checkFileExtension(path.substring(path.lastIndexOf('.') + 1)))
		{
			LogUtil.error(getClass(), "Resource $0: Unsupported image file name extension for mim type $1.", getErrorName(), getMimeType());
			return;
		}

		path = res.getResourceItemPath(path);
	}

	/**
	 * Gets the mime-type of the resource item.
	 *
	 * @return A string in mime-type format
	 */
	public String getMimeType()
	{
		return ResourceItemTypes.MULTIICON;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Loads the object associated with the resource.
	 * Loads the image from the image path specified in the resource XML file.
	 *
	 * @return The image or null on error
	 */
	protected Object loadResourceObject()
	{
		if (path == null)
			return null;

		MultiImageIcon image = new MultiImageIcon(path, getResourceCollection().getResourceCollectionMgr().getResourceMgr());
		return image;
	}

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Gets the image path.
	 * @nowarn
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Checks the validation of the file extension.
	 *
	 * @param extension File extension f.e. 'gif'
	 * @return
	 *  true    Is valid<br>
	 *  false   Is invalid
	 */
	protected boolean checkFileExtension(String extension)
	{
		extension = extension.toLowerCase();

		for (int i = 0; i < EXTENSIONS.length; i++)
		{
			if (extension.equals(EXTENSIONS [i]))
				return true;
		}

		return false;
	}
}
