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
package org.openbp.swing.plaf.sky;

import java.awt.Color;
import java.awt.Font;

/**
 * This class is used to set in the OpenBP look and feel the default colors.
 * All colors have to be set here so any component looks equal.
 *
 * @author Jens Ferchland
 */
public interface SkyTheme
{
	public static final Color COLOR_BORDER = new Color(188, 187, 184);

	public static final Color COLOR_BORDER_BUTTON = new Color(149, 176, 203);

	public static final Color COLOR_BACKGROUND = new Color(240, 240, 230);

	public static final Color COLOR_BACKGROUND_LIGHT = new Color(250, 250, 240);

	public static final Color COLOR_BACKGROUND_LIGHT_LIGHT = new Color(255, 255, 255);

	public static final Color COLOR_BACKGROUND_DARK = Color.lightGray;

	public static final Color COLOR_BACKGROUND_DARK_DARK = Color.darkGray;

	public static final Color COLOR_HIGHLIGHT = new Color(255, 212, 142);

	public static final Color COLOR_FOCUS = new Color(59, 86, 113);

	public static final Color COLOR_FOCUS_OVERLAY = new Color(0f, 0f, 1f, 0.1f);

	public static final Color COLOR_TEXT_ENABLED = Color.black;

	public static final Color COLOR_TEXT_DISABLED = Color.gray;

	public static final Font FONT_LARGE = new Font("SansSerif", Font.PLAIN, 14);

	public static final Font FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 12);

	public static final Font FONT_SMALL = new Font("SansSerif", Font.TRUETYPE_FONT, 10);

	public static final Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);

	public static final Color COLOR_TABLE_HEADER = new Color(225, 225, 215);
}
