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

import java.awt.Image;

import javax.swing.Icon;

/**
 * Icon that supports multiple sizes.
 *
 * @author Stephan Moritz
 */
public interface MultiIcon
	extends Icon, FlexibleSize
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Methods
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns an icon with the given size.
	 *
	 * @param size The icon size descriptor ({@link FlexibleSize#STANDARD}/{@link FlexibleSize#SMALL}/
	 * {@link FlexibleSize#MEDIUM}/{@link FlexibleSize#LARGE}/{@link FlexibleSize#HUGE}) or null for the
	 * default size
	 * @return The icon
	 */
	public Icon getIcon(int size);

	/**
	 * Returns an image representation of this icon in the currently active size.
	 * @nowarn
	 */
	public Image getImage();
}
