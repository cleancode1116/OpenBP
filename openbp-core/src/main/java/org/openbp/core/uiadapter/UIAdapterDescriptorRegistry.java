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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openbp.common.CollectionUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.model.Model;

/**
 * This class implements a registry for UI adapters and their descriptions.
 * A UI adapter is registered here by adding its description. A key role
 * for most of the operations here plays the notion of the "visual type",
 * which is a code like "external" and so on denoting a UI adapter.
 *
 * @author Heiko Erhardt
 */
public final class UIAdapterDescriptorRegistry
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Map from visual types ({@link java.lang.String}) to {@link UIAdapterDescriptor}) */
	private final SortedMap adapterDescriptors;

	/** Holds the currently known visual types. */
	private String[] visualTypes;

	//////////////////////////////////////////////////
	// @@ Internal state
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static UIAdapterDescriptorRegistry singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance for this class.
	 * @nowarn
	 */
	public static synchronized UIAdapterDescriptorRegistry getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new UIAdapterDescriptorRegistry();
		}

		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private UIAdapterDescriptorRegistry()
	{
		// Initialize list of AdapterDescriptor's.
		adapterDescriptors = new TreeMap();
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * This method adds a new UIAdapterDescriptor to the internal data structures.
	 *
	 * @param adapterDescriptor The adapter description to be added
	 */
	public void addAdapterDescriptor(UIAdapterDescriptor adapterDescriptor)
	{
		// Add the new adapter description.
		adapterDescriptors.put(adapterDescriptor.getVisualType(), adapterDescriptor);

		// Re"calculate" registered visual types.
		List visualTypeList = new ArrayList(adapterDescriptors.keySet());
		visualTypes = CollectionUtil.toStringArray(visualTypeList);
	}

	/**
	 * This method returns all visual types for which UI adapters are registered.
	 *
	 * @return A String array containing visual types
	 */
	public String[] getVisualTypes()
	{
		return visualTypes;
	}

	/**
	 * Returns the adaptor class name for the given visual type.
	 *
	 * @param visualType The visual type
	 * @return The adaptor class name (instance of {@link UIAdapterDescriptor}) or null if none was found
	 */
	public String getUIAdapterClassName(String visualType)
	{
		UIAdapterDescriptor adapterDescriptor = getAdapterDescriptor(visualType);
		if (adapterDescriptor != null)
			return adapterDescriptor.getUIAdapterClassName();
		return null;
	}

	/**
	 * This method returns the display text for the given visual type and the
	 * registered key.
	 *
	 * @param visualType The visual type
	 * @param key The key under which the display text is registered
	 * @return The display text or an empty string, if none was found
	 */
	public String getDisplayText(String visualType, String key)
	{
		UIAdapterDescriptor adapterDescriptor = getAdapterDescriptor(visualType);
		if (adapterDescriptor != null)
			return adapterDescriptor.getDisplayText(key);
		return "";
	}

	/**
	 * Gets the valid mime types for the visual type.
	 *
	 * @param visualType Type of the visual
	 * @return The string array of the valid mime types or null if the visual type is unknown
	 */
	public String[] getMimeTypes(String visualType)
	{
		UIAdapterDescriptor adapterDescriptor = getAdapterDescriptor(visualType);
		if (adapterDescriptor != null)
			return adapterDescriptor.getMimeTypes();
		return null;
	}

	/**
	 * Gets the path to a visual implementation.
	 *
	 * @param visualType Type of the visual
	 * @param visualId Id of the visual
	 * @param model Model that contains the visual item or visual node
	 * @return The visual path or null
	 */
	public String getVisualPath(String visualType, String visualId, Model model)
	{
		UIAdapterDescriptor adapterDescriptor = getAdapterDescriptor(visualType);
		if (adapterDescriptor == null)
			// If no UIAdapterDescription is available, we have no path at all.
			return null;

		// Construct path to visual implementation.
		String pathName = adapterDescriptor.getVisualPathName(visualId, model);

		// Check, whether the implementation exists.
		if (pathName != null && new File(pathName).exists())
			return pathName;
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Helpers for adapter and description handling
	//////////////////////////////////////////////////

	/**
	 * Gets the specified adapter description if it is available.
	 *
	 * @param visualType Type of the visual to execute ("external" etc\.).
	 * @return The adapter description or null if not present
	 */
	private UIAdapterDescriptor getAdapterDescriptor(String visualType)
	{
		UIAdapterDescriptor result;

		// Try to get the UIAdapterDescriptor.
		result = (UIAdapterDescriptor) adapterDescriptors.get(visualType);

		// Log, if we haven't got one.
		if (result == null)
		{
			// Create message.
			LogUtil.error(getClass(), "No UI adapter description for type $0 registered.", visualType);
		}

		// In any case, return the result.
		return result;
	}
}
