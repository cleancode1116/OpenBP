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
package org.openbp.common.icon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openbp.common.resource.ResourceMgr;
import org.openbp.common.resource.ResourceMgrException;
import org.springframework.core.io.Resource;

/**
 * IconImage that supports multiple sizes at once.
 *
 * @author Stephan Moritz
 */
public class MultiImageIcon extends ImageIcon
	implements MultiIcon
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Contains the basic (unresized) icons (ImageIcon objects) */
	private List icons;

	/** Contains the icons derived from the basic icons (ImageIcon objects) */
	private List derivedIcons;

	/** Icon in the current standard size */
	private ImageIcon standardSizeIcon;

	/** The currently used standard size. */
	private int standardSize = STANDARD;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Copy constructor.
	 *
	 * @param src The source icon
	 */
	public MultiImageIcon(MultiImageIcon src)
	{
		icons = src.icons;
		derivedIcons = src.derivedIcons;
		standardSizeIcon = src.standardSizeIcon;
		standardSize = src.standardSize;
		setDescription(src.getDescription());
	}

	/**
	 * Creates a new multi image icon that is built around a single ImageIcon.
	 *
	 * @param source The icon
	 */
	public MultiImageIcon(ImageIcon source)
	{
		icons = new ArrayList();
		icons.add(source);

		setIconSize(source.getIconHeight());
	}

	/**
	 * Creates a new multi image icon from a given filename.
	 * @param resourceName Name of the icon resource (without the size suffix, but including the icon file type)
	 * @param resMgr Resource manager to use for resource access or null for the default resource manager
	 */
	public MultiImageIcon(String resourceName, ResourceMgr resMgr)
	{
		if (resMgr == null)
			resMgr = ResourceMgr.getDefaultInstance();

		setDescription(resourceName);

		icons = new ArrayList();

		int index = resourceName.lastIndexOf(".");
		String extension = resourceName.substring(index);
		resourceName = resourceName.substring(0, index);

		readIcons(resourceName, extension, resMgr);

		determineStandardSizeIcon();
	}

	/**
	 * Reads the given directory for all matching images and converts them into imageicons.
	 * A matching file name is the file name followed by a number with the given extension,
	 * e. g. icon1.gif, icon2.gif, icon99.gif etc.
	 *
	 * @param resourceName Base name of the icon to read
	 * @param extension Icon file name extension
	 * @throws ResourceMgrException If the file doesn't exist
	 */
	private void readIcons(String resourceName, String extension, ResourceMgr resMgr)
		throws ResourceMgrException
	{
		String resourcePattern = resourceName + "*" + extension;
		Resource [] resources = null;

		try
		{
			resources = resMgr.findResources(resourcePattern);
		}
		catch (ResourceMgrException e)
		{
			throw new ResourceMgrException("No image files found matching '" + resourcePattern + "'.");
		}
		
		if (resources.length == 0)
		{
			throw new ResourceMgrException("No image files found matching '" + resourcePattern + "'.");
		}

		for (int i = 0; i < resources.length; i++)
		{
			byte [] imageData = resMgr.loadByteResource(resources[i]);

			// Generate the icon
			ImageIcon icon = new ImageIcon(imageData);

			// and add it to out list;
			icons.add(icon);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Implemantation of MultiIcon
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @copy FlexibleSize.getIconSize
	 */
	public int getIconSize()
	{
		return standardSize;
	}

	/**
	 * @copy FlexibleSize.setIconSize
	 */
	public void setIconSize(int size)
	{
		standardSize = size;
		determineStandardSizeIcon();
	}

	/**
	 * Returns an icon with the given size.
	 * @param size Size of the desired icon or null for default
	 * @return The icon
	 */
	public Icon getIcon(int size)
	{
		ImageIcon result;

		if (size <= 0)
		{
			result = internalGet(FlexibleSize.MEDIUM);
			if (result != null)
				return result;
			result = internalGet(FlexibleSize.LARGE);
			if (result != null)
				return result;
			result = internalGet(FlexibleSize.SMALL);
			if (result != null)
				return result;
			size = FlexibleSize.SMALL;
		}

		result = internalGet(size);

		if (result == null)
		{
			result = generate(size);

			if (derivedIcons == null)
				derivedIcons = new ArrayList();
			derivedIcons.add(result);
		}

		return result;
	}

	/**
	 * @copy MultiIcon.getImage
	 */
	public Image getImage()
	{
		// Important: This is also an override of ImageIcon.getImage.
		// Do not remove!
		return standardSizeIcon.getImage();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ private methods
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the icon in the specified size (either native or derived, if any).
	 *
	 * @param size The icon size descriptor ({@link FlexibleSize#STANDARD}/{@link FlexibleSize#SMALL}/
	 * {@link FlexibleSize#MEDIUM}/{@link FlexibleSize#LARGE}/{@link FlexibleSize#HUGE})
	 * @return The icon or null if the icon doesn't exist in this size
	 */
	private ImageIcon internalGet(int size)
	{
		int n = icons.size();
		for (int i = 0; i < n; ++i)
		{
			ImageIcon icon = (ImageIcon) icons.get(i);
			if (icon.getIconHeight() == size)
				return icon;
		}

		if (derivedIcons != null)
		{
			int dn = derivedIcons.size();
			for (int i = 0; i < dn; ++i)
			{
				ImageIcon icon = (ImageIcon) derivedIcons.get(i);
				if (icon.getIconHeight() == size)
					return icon;
			}
		}

		return null;
	}

	/**
	 * Generates a new icon of the given size by scaling the closest possible
	 * existing native icon to the correct size, preferring the next largest if
	 * possible.
	 *
	 * @param size The icon size descriptor ({@link FlexibleSize#STANDARD}/{@link FlexibleSize#SMALL}/
	 * {@link FlexibleSize#MEDIUM}/{@link FlexibleSize#LARGE}/{@link FlexibleSize#HUGE})
	 * @return The icon
	 */
	private ImageIcon generate(int size)
	{
		int maxSize = 0;
		ImageIcon largest = null;

		int n = icons.size();
		for (int i = 0; i < n; ++i)
		{
			ImageIcon icon = (ImageIcon) icons.get(i);
			if (icon.getIconHeight() > maxSize)
			{
				largest = icon;
				maxSize = icon.getIconHeight();
			}
		}
		
		if (largest == null)
			return null;

		// Scale the image down (or up if larger than largest native) to the correct size
		Image image = largest.getImage();

		// Compute the width to height ratio
		int largestWidth = largest.getIconWidth();
		int largestHeight = largest.getIconHeight();
		double ratio = (double) largestWidth / (double) largestHeight;
		int newWidth = (int) (size * ratio);

		image = image.getScaledInstance(size, newWidth, Image.SCALE_AREA_AVERAGING);

		return new ImageIcon(image);
	}

	/**
	 * Creates the icon in the standard size.
	 */
	private void determineStandardSizeIcon()
	{
		if (standardSize > 0)
		{
			standardSizeIcon = (ImageIcon) getIcon(standardSize);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Icon Implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see javax.swing.Icon#getIconHeight()
	 */
	public int getIconHeight()
	{
		return standardSizeIcon != null ? standardSizeIcon.getIconHeight() : 0;
	}

	/**
	 * @see javax.swing.Icon#getIconWidth()
	 */
	public int getIconWidth()
	{
		return standardSizeIcon != null ? standardSizeIcon.getIconWidth() : 0;
	}

	/**
	 * @see javax.swing.Icon#paintIcon(Component, Graphics, int, int)
	 */
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		if (standardSizeIcon != null)
		{
			standardSizeIcon.paintIcon(c, g, x, y);
		}
	}
}
