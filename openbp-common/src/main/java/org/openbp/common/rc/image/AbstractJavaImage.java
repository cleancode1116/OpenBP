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

import javax.swing.ImageIcon;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.markup.XMLUtil;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceItem;
import org.openbp.common.resource.ResourceMgrException;
import org.w3c.dom.Element;

/**
 * Abstract image resource item implementation for the ImageIcon object.
 *
 * @author Andreas Putz
 */
public abstract class AbstractJavaImage extends ResourceItem
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** XML tag 'path' */
	protected static final String TAG_IMAGE_PATH = "path";

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

		path = path.replace('\\', '/');

		if (!checkFileExtension(path.substring(path.lastIndexOf('.') + 1)))
		{
			LogUtil.error(getClass(), "Resource $0: Unsupported image file name extension.", getErrorName());
			return;
		}
	}

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

		byte [] imageData = null;
		try
		{
			imageData = resourceCollection.readResourceItem(path);
		}
		catch (ResourceMgrException e)
		{
			LogUtil.error(getClass(), "Resource $0: Image file $1 does not exist.", getErrorName(), resourceCollection.getResourceItemPath(path));
			return null;
		}

		if (imageData == null)
		{
			LogUtil.error(getClass(), "Resource $0: Image file $1 is empty.", getErrorName(), resourceCollection.getResourceItemPath(path));
			return null;
		}

		ImageIcon image = new ImageIcon(imageData);
		image.setDescription(resourceCollection.getResourceItemPath(path));
		return image;
	}

	//////////////////////////////////////////////////
	// @@ File extension support
	//////////////////////////////////////////////////

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
		String [] extensions = getFileExtensions();
		if (extension == null)
			return false;

		extension = extension.toLowerCase();

		for (int i = 0; i < extensions.length; i++)
		{
			if (extension.equals(extensions [i]))
				return true;
		}

		return false;
	}

	//////////////////////////////////////////////////
	// @@ Abstract methods
	//////////////////////////////////////////////////

	/**
	 * Gets the file extensions.
	 *
	 * @return Array with the extensions
	 */
	protected abstract String [] getFileExtensions();
}
