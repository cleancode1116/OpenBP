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
package org.openbp.core.uiadapter;

import org.openbp.core.model.Model;

/**
 * Provides basic functionalities of a user interface adapters that are used by both client and server.
 *
 * @author Andreas Putz
 */
public interface UIAdapterDescriptor
{
	/** This is the key for the display name for associations. */
	public final String ASSOCIATION_DISPLAY_NAME = "association-display-name";

	/** This is the key for the hint message for associations. */
	public final String ASSOCIATION_HINT_MESSAGE = "association-hint-message";

	/** This is the key for the display name of the UI adapter in the cockpit. */
	public final String COCKPIT_DISPLAY_NAME = "cockpit-display-name";

	/**
	 * This method should be used to retrieve display texts related to this UI adapter
	 * to be shown in a user interface.
	 *
	 * @param key The key to lookup the display text for (e.g., {@link #ASSOCIATION_DISPLAY_NAME}).
	 * @return The display text for the key or null
	 */
	public String getDisplayText(String key);

	/**
	 * This method returns the file extension of visual implementation's to be handled by
	 * this adapter. Please note, that the return value must not start with a dot!
	 *
	 * @return The file extension for visual implementations
	 */
	public String getFileExtension();

	/**
	 * Gets the mime types that can be associated with a visual implementation file supported
	 * by this adapter.
	 *
	 * @return The array of mime types
	 */
	public String[] getMimeTypes();

	/**
	 * Returns the name of the class implementing the UIAdapter itself.
	 *
	 * @return The class name of the UIAdapter implementation
	 */
	public String getUIAdapterClassName();

	/**
	 * Gets the path to a visual implementation.
	 *
	 * @param visualId Id of the visual
	 * @param model Model that contains the visual item or visual node
	 * @return The visual path
	 */
	public String getVisualPathName(String visualId, Model model);

	/**
	 * Returns the symbolic name for the visuals executed by this UI adapter (like "external").
	 *
	 * @return The symbolic name for the visuals executed by this
	 */
	public String getVisualType();
}
