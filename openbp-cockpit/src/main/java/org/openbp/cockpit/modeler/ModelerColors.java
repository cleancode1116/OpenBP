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

import java.awt.Color;

import org.openbp.awt.Color2StringConverter;
import org.openbp.swing.plaf.sky.SkyTheme;

/**
 * Color constants used by the modeler.
 *
 * @author Stephan Moritz
 */
public class ModelerColors
{
	//////////////////////////////////////////////////
	// @@ Items
	//////////////////////////////////////////////////

	/** Shadow fill color */
	public static final Color SHADOW_FILL = new Color(0.8f, 0.8f, 0.8f);

	/** Shadow border color */
	public static final Color SHADOW_BORDER = new Color(0.9f, 0.9f, 0.9f);

	//////////////////////////////////////////////////
	// @@ Selection
	//////////////////////////////////////////////////

	/** Border color for selected object */
	public static final Color SELECTED_BORDER = Color.RED;

	/** Border color when hovering object */
	public static final Color HOVERED_BORDER = Color.BLUE;

	/** Border color for current position in debugger */
	public static final Color DEBUGGER_BORDER = Color.RED;

	//////////////////////////////////////////////////
	// @@ Handles
	//////////////////////////////////////////////////

	/** Line of control point handle */
	public static final Color HANDLE_LINE = Color.GRAY;

	/** Connection waypoint handle */
	public static final Color HANDLE_WAYPOINT_FILL = Color.RED;

	/** Connection waypoint handle */
	public static final Color HANDLE_WAYPOINT_BORDER = Color.BLACK;

	/** Connection angle handle */
	public static final Color HANDLE_ANGLE_FILL = Color.WHITE;

	/** Connection angle handle */
	public static final Color HANDLE_ANGLE_BORDER = Color.BLACK;

	/** Line of control point handle */
	public static final Color LABEL_HANDLE_LINE = Color.GRAY;

	/** Connection waypoint handle */
	public static final Color HANDLE_TEXTSIZE_FILL = Color.RED;

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/** Modeler workspace */
	public static final Color WORKSPACE = SkyTheme.COLOR_BACKGROUND_LIGHT;

	/** Grid */
	public static final Color GRID = Color.lightGray;

	/** Drop region */
	public static final Color DROP_REGION = Color.GREEN;

	/** Rubberband tracker */
	public static final Color RUBBERBAND = Color.BLACK;

	/** Background color for image generator */
	public static final Color IMAGE_GENERATOR_BACKGROUND = Color.WHITE;

	/** Allowed connection/drop points */
	public static final Color CONNECTION_ACCEPT = new Color(0f, 1f, 0f, 0.2f);

	/** Overlay color for figures that have an expression associated with them */
	public static final Color EXPRESSION_OVERLAY = new Color(0f, 0f, 1f, 0.15f);

	/** Breakpoint fill color */
	public static final Color BREAKPOINT_OVERLAY = new Color(255, 130, 0, 128);

	/** Miniview tracker (solid) */
	public static final Color MINIVIEW = SkyTheme.COLOR_BACKGROUND_LIGHT;

	/** Miniview tracker (solid) */
	public static final Color MINIVIEW_TRACKER = new Color(0f, 0f, 1f, 0.1f);

	static
	{
		Color2StringConverter.addPredefinedColor("workspace", WORKSPACE);
	}
}
