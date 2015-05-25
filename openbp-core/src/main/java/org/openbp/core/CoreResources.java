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
package org.openbp.core;

import java.util.Locale;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;

/**
 * Resources for the core module.
 *
 * @author Heiko Erhardt
 */
public final class CoreResources
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	private CoreResources()
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets a string from the resource for the default locale.
	 *
	 * @param key Resource key
	 * @return The string or null
	 */
	public static String getOptionalString(String key)
	{
		return getOptionalString(key, null);
	}

	/**
	 * Gets a string from the resource for the given locale.
	 *
	 * @param key Resource key
	 * @param locale Locale
	 * @return The string or null
	 */
	public static String getOptionalString(String key, Locale locale)
	{
		ResourceCollection res = ResourceCollectionMgr.getDefaultInstance().getResource(CoreConstants.RC_CORE, CoreResources.class, locale);
		if (res != null)
		{
			return res.getOptionalString(key);
		}
		return null;
	}
}
