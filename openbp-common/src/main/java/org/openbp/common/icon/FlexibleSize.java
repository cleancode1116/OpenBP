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

/**
 * JComponents that are implementing this interface are capable of displaying
 * Jaspira multi size icons ({@link MultiIcon}), either directly or in one of their child components.
 *
 * Methods of this interface can be used to set the icon size, which should be inherited
 * by qualifying child components.
 *
 * @author Stephan Moritz
 */
public interface FlexibleSize
{
	/////////////////////////////////////////////////////////////////////////
	// @@ COnstants
	/////////////////////////////////////////////////////////////////////////

	/** Undetermined size */
	public static final int UNDETERMINED = 0;

	/** Standard small size (16 pixel) */
	public static final int SMALL = 16;

	/** Standard medium size (24 pixel) */
	public static final int MEDIUM = 24;

	/** Standard large size (32 pixel) */
	public static final int LARGE = 32;

	/** Standard large size (48 pixel) */
	public static final int HUGE = 48;

	/** Standard size for icons (16 pixel) */
	public static final int STANDARD = SMALL;

	/////////////////////////////////////////////////////////////////////////
	// @@ Methods
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the current icon size.
	 *
	 * @return The icon size descriptor ({@link FlexibleSize#STANDARD}/{@link FlexibleSize#SMALL}/
	 * {@link FlexibleSize#MEDIUM}/{@link FlexibleSize#LARGE}/{@link FlexibleSize#HUGE})
	 */
	public int getIconSize();

	/**
	 * Sets the current icon size.
	 *
	 * @param size The icon size descriptor ({@link FlexibleSize#STANDARD}/{@link FlexibleSize#SMALL}/
	 * {@link FlexibleSize#MEDIUM}/{@link FlexibleSize#LARGE}/{@link FlexibleSize#HUGE})
	 */
	public void setIconSize(int size);
}
