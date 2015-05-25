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
package org.openbp.jaspira.gui.interaction;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.openbp.common.icon.MultiIcon;

public class BasicCursorPrototype
	implements CursorPrototype
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Image to be laid below the custom image or null */
	private Image subTemplate;

	/** Image to be laid on top of the custom image or null */
	private Image superTemplate;

	/** Hotspot of the cursor to be defined */
	private Point hotspot;

	/** Cursor size */
	private static final int CURSORSIZE = 32;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param subTemplate Image to be laid below the custom image or null
	 * @param superTemplate Image to be laid on top of the custom image or null
	 * @param hotspot Hotspot of the cursor to be defined
	 */
	public BasicCursorPrototype(Image subTemplate, Image superTemplate, Point hotspot)
	{
		this.subTemplate = subTemplate;
		this.superTemplate = superTemplate;
		this.hotspot = hotspot;
	}

	/**
	 * Creates a new cursor based on the cursor prototype.
	 * Places the custom icon in the area defined by the custom image area between sub and super template.
	 *
	 * @param icon Custom icon this cursor should display
	 */
	public Cursor createCursor(MultiIcon icon)
	{
		Image buffer = new BufferedImage(CURSORSIZE, CURSORSIZE, BufferedImage.TYPE_4BYTE_ABGR /*TYPE_INT_ARGB*/);

		Graphics g = buffer.getGraphics();

		if (subTemplate != null)
		{
			// Draw the bottom image to the buffer
			g.drawImage(subTemplate, 0, 0, null);
		}
		if (icon != null)
		{
			// Retrieve the image that fits best into the custom image area from the multi icon
			// and draw it to the buffer
			Image image = ((ImageIcon) icon.getIcon(0)).getImage();
			int w = image.getWidth(null);
			int h = image.getHeight(null);
			g.drawImage(image, CURSORSIZE - w, CURSORSIZE - h, null);
		}
		if (superTemplate != null)
		{
			// Draw the top image to the buffer
			g.drawImage(superTemplate, 0, 0, null);
		}

		// Create a cursor from the buffered image
		return Toolkit.getDefaultToolkit().createCustomCursor(buffer, hotspot, "custom");
	}
}
