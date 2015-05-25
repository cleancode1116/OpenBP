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
package org.openbp.cockpit.modeler;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.openbp.cockpit.CockpitConstants;
import org.openbp.cockpit.modeler.util.FigureResources;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;

/**
 * Simple drawing objects like strokes and cursors used by the modeler.
 *
 * @author Heiko Erhardt
 */
public class ModelerGraphics
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Default node size */
	public static final int DEFAULT_NODE_SIZE = 50;

	//////////////////////////////////////////////////
	// @@ Local members
	//////////////////////////////////////////////////

	/** Cursor/image resource */
	private static ResourceCollection resourceCollection;

	/** Text font */
	private static Font standardTextFont;

	/** Metrics for default dialog font */
	private static FontMetrics defaultFontMetrics;

	static
	{
		JLabel dummy = new JLabel();
		defaultFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(dummy.getFont());
		standardTextFont = new Font("Helvetica", Font.PLAIN, 12);
	}

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ModelerGraphics()
	{
	}

	/**
	 * Gets the specified message string from the modeler graphics resource.
	 *
	 * @param key Resource key
	 * @return The message or null if not found
	 */
	public static String getMsg(String key)
	{
		if (resourceCollection == null)
		{
			resourceCollection = ResourceCollectionMgr.getDefaultInstance().getResource(CockpitConstants.RESOURCE_COCKPIT, ModelerGraphics.class);
		}

		return resourceCollection.getOptionalString(key);
	}

	/**
	 * Gets the text font.
	 * @nowarn
	 */
	public static Font getStandardTextFont()
	{
		return standardTextFont;
	}

	/**
	 * Gets the metrics for default dialog font.
	 * @nowarn
	 */
	public static FontMetrics getDefaultFontMetrics()
	{
		return defaultFontMetrics;
	}

	/**
	 * Creates a cursor.
	 *
	 * @param name Name or the cursor.
	 * This must specify a multi icon resource in the resource file "cockpit.modeler.ModelerGraphics.xml"
	 * of the resource component "cockpit".
	 * @param hotSpotX X coordinate of the hot spot
	 * @param hotSpotY Y coordinate of the hot spot
	 * @return The cursor
	 */
	private static Cursor createCursor(String name, int hotSpotX, int hotSpotY)
	{
		if (resourceCollection == null)
		{
			resourceCollection = ResourceCollectionMgr.getDefaultInstance().getResource(CockpitConstants.RESOURCE_COCKPIT, ModelerGraphics.class);
		}

		Image image = ((ImageIcon) resourceCollection.getRequiredObject(name)).getImage();

		Point hotSpot = new Point(hotSpotX, hotSpotY);

		Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(image, hotSpot, name);
		return cursor;
	}

	//////////////////////////////////////////////////
	// @@ Cursors
	//////////////////////////////////////////////////

	/** Default cursor for empty process space */
	public static final Cursor defaultCursor = Cursor.getDefaultCursor();

	/** Standard cursor */
	public static final Cursor standardCursor = defaultCursor;

	/** Standard cursor (add to selection) */
	public static final Cursor standardAddCursor = defaultCursor;

	/** Hand click cursor */
	public static final Cursor handClickCursor = createCursor("cursor.handclick", 16, 4);

	/** Hand click cursor (add to selection) */
	public static final Cursor handClickAddCursor = createCursor("cursor.handclickadd", 16, 4);

	/** Hand scroll cursor */
	public static final Cursor handScrollCursor = createCursor("cursor.handscroll", 16, 16);

	/** Hand scroll cursor */
	public static final Cursor zoomCursor = createCursor("cursor.zoom", 16, 16);

	/** Move node cursor */
	public static final Cursor moveNodeCursor = createCursor("cursor.movenode", 16, 16);

	/** Move node cursor */
	public static final Cursor resizeNodeCursor = createCursor("cursor.resizenode", 16, 16);

	/** Socket rotation cursor */
	public static final Cursor moveSocketCursor = createCursor("cursor.movesocket", 16, 16);

	/** Parameter reorder cursor */
	public static final Cursor moveParamCursor = createCursor("cursor.moveparam", 16, 16);

	/** Move text element cursor */
	public static final Cursor moveTextCursor = createCursor("cursor.movetext", 16, 16);

	/** Move horizontal line cursor */
	public static final Cursor moveHLineCursor = createCursor("cursor.movehline", 16, 16);

	/** Move vertical line cursor */
	public static final Cursor moveVLineCursor = createCursor("cursor.movevline", 16, 16);

	/** Create control link cursor */
	public static final Cursor createControlLinkCursor = createCursor("cursor.createcontrollink", 30, 11);

	/** Create data link cursor */
	public static final Cursor createDataLinkCursor = createCursor("cursor.createdatalink", 30, 11);

	/** Move spline point cursor */
	public static final Cursor moveSplinePointCursor = createCursor("cursor.movesplinepoint", 16, 16);

	/** Move spline handle cursor */
	public static final Cursor moveSplineHandleCursor = createCursor("cursor.movesplinehandle", 16, 16);

	//////////////////////////////////////////////////
	// @@ Strokes
	//////////////////////////////////////////////////

	/** Stroke for selected objects */
	public static final Stroke selectionStroke = new BasicStroke(1.5f);

	/** Stroke for figures hovered with the mouse */
	public static final Stroke hoveredStroke = new BasicStroke(1.5f);

	/** Stroke for current debugger position markation */
	public static final Stroke debuggerStroke = FigureResources.standardStroke3;

	/** Stroke for the label handle line of control and data links and process variables */
	public static final Stroke labelHandleStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 10f, new float [] { 2f, 2f }, 0f);
}
