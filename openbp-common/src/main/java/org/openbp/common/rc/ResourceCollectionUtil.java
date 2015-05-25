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
package org.openbp.common.rc;

import java.util.Locale;

import org.openbp.common.MsgFormat;
import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.common.generic.description.DisplayObject;

/**
 * Util class for the resource framework.
 *
 * @author Andreas Putz
 */
public final class ResourceCollectionUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor for ResourceCollectionUtil.
	 */
	private ResourceCollectionUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Message formatting (basic methods)
	//////////////////////////////////////////////////

	/**
	 * Retrieves a message from the given resource and formats it using the given arguments.
	 *
	 * @param res Resource that contains the message
	 * @param key Resource name of the message
	 * @param args Formatting arguments (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(ResourceCollection res, String key, Object [] args)
	{
		String msg = res.getRequiredString(key);
		if (msg != null)
		{
			msg = MsgFormat.format(msg, args);
		}
		else
		{
			msg = MsgFormat.format("Cannot access string resource item $0 in resource $1.", key, res.getErrorName());
		}
		return msg;
	}

	/**
	 * Retrieves a message from the specified resource and formats it using the given arguments
	 * using the default locale.
	 *
	 * @param containerName Name of the resource container that contains the message
	 * @param resourceName Name of the resource that contains the message
	 * @param key Resource name of the message
	 * @param args Formatting arguments (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(String containerName, String resourceName, String key, Object [] args)
	{
		ResourceCollection res = ResourceCollectionMgr.getDefaultInstance().getResource(containerName, resourceName);
		String msg;
		if (res != null)
		{
			msg = formatMsg(res, key, args);
		}
		else
		{
			msg = MsgFormat.format("Cannot access resource $0 in container $1.", resourceName, containerName);
		}
		return msg;
	}

	/**
	 * Retrieves a message from the specified resource and formats it using the given arguments
	 * using the given locale.
	 *
	 * @param containerName Name of the resource container that contains the message
	 * @param resourceName Name of the resource that contains the message
	 * @param locale Locale to use; If the locale is null, the default locale will be used
	 * @param key Resource name of the message
	 * @param args Formatting arguments (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(String containerName, String resourceName, Locale locale, String key, Object [] args)
	{
		ResourceCollection res = ResourceCollectionMgr.getDefaultInstance().getResource(containerName, resourceName, locale);
		String msg;
		if (res != null)
		{
			msg = formatMsg(res, key, args);
		}
		else
		{
			msg = MsgFormat.format("Cannot access resource $0 in container $1.", resourceName, containerName);
		}
		return msg;
	}

	//////////////////////////////////////////////////
	// @@ Message formatting (convenience methods)
	//////////////////////////////////////////////////

	/**
	 * Retrieves a message from the given resource and formats it using the given arguments
	 * (convenience method).
	 *
	 * @param res Resource that contains the message
	 * @param key Resource name of the message
	 * @param arg1 Formatting argument (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(ResourceCollection res, String key, Object arg1)
	{
		return formatMsg(res, key, new Object [] { arg1 });
	}

	/**
	 * Retrieves a message from the given resource and formats it using the given arguments
	 * (convenience method).
	 *
	 * @param res Resource that contains the message
	 * @param key Resource name of the message
	 * @param arg1 Formatting argument (see {@link MsgFormat})
	 * @param arg2 Formatting argument (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(ResourceCollection res, String key, Object arg1, Object arg2)
	{
		return formatMsg(res, key, new Object [] { arg1, arg2 });
	}

	/**
	 * Retrieves a message from the given resource and formats it using the given arguments
	 * (convenience method).
	 *
	 * @param res Resource that contains the message
	 * @param key Resource name of the message
	 * @param arg1 Formatting argument (see {@link MsgFormat})
	 * @param arg2 Formatting argument (see {@link MsgFormat})
	 * @param arg3 Formatting argument (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(ResourceCollection res, String key, Object arg1, Object arg2, Object arg3)
	{
		return formatMsg(res, key, new Object [] { arg1, arg2, arg3 });
	}

	/**
	 * Retrieves a message from the specified resource and formats it using the given arguments
	 * using the default locale (convenience method).
	 *
	 * @param containerName Name of the resource container that contains the message
	 * @param resourceName Name of the resource that contains the message
	 * @param key Resource name of the message
	 * @param arg1 Formatting argument (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(String containerName, String resourceName, String key, Object arg1)
	{
		return formatMsg(containerName, resourceName, key, new Object [] { arg1 });
	}

	/**
	 * Retrieves a message from the specified resource and formats it using the given arguments
	 * using the default locale (convenience method).
	 *
	 * @param containerName Name of the resource container that contains the message
	 * @param resourceName Name of the resource that contains the message
	 * @param key Resource name of the message
	 * @param arg1 Formatting argument (see {@link MsgFormat})
	 * @param arg2 Formatting argument (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(String containerName, String resourceName, String key, Object arg1, Object arg2)
	{
		return formatMsg(containerName, resourceName, key, new Object [] { arg1, arg2 });
	}

	/**
	 * Retrieves a message from the specified resource and formats it using the given arguments
	 * using the default locale (convenience method).
	 *
	 * @param containerName Name of the resource container that contains the message
	 * @param resourceName Name of the resource that contains the message
	 * @param key Resource name of the message
	 * @param arg1 Formatting argument (see {@link MsgFormat})
	 * @param arg2 Formatting argument (see {@link MsgFormat})
	 * @param arg3 Formatting argument (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(String containerName, String resourceName, String key, Object arg1, Object arg2, Object arg3)
	{
		return formatMsg(containerName, resourceName, key, new Object [] { arg1, arg2, arg3 });
	}

	/**
	 * Retrieves a message from the specified resource and formats it using the given arguments
	 * using the given locale (convenience method).
	 *
	 * @param containerName Name of the resource container that contains the message
	 * @param resourceName Name of the resource that contains the message
	 * @param locale Locale to use; If the locale is null, the default locale will be used
	 * @param key Resource name of the message
	 * @param arg1 Formatting argument (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(String containerName, String resourceName, Locale locale, String key, Object arg1)
	{
		return formatMsg(containerName, resourceName, locale, key, new Object [] { arg1 });
	}

	/**
	 * Retrieves a message from the specified resource and formats it using the given arguments
	 * using the given locale (convenience method).
	 *
	 * @param containerName Name of the resource container that contains the message
	 * @param resourceName Name of the resource that contains the message
	 * @param locale Locale to use; If the locale is null, the default locale will be used
	 * @param key Resource name of the message
	 * @param arg1 Formatting argument (see {@link MsgFormat})
	 * @param arg2 Formatting argument (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(String containerName, String resourceName, Locale locale, String key, Object arg1, Object arg2)
	{
		return formatMsg(containerName, resourceName, locale, key, new Object [] { arg1, arg2 });
	}

	/**
	 * Retrieves a message from the specified resource and formats it using the given arguments
	 * using the given locale (convenience method).
	 *
	 * @param containerName Name of the resource container that contains the message
	 * @param resourceName Name of the resource that contains the message
	 * @param locale Locale to use; If the locale is null, the default locale will be used
	 * @param key Resource name of the message
	 * @param arg1 Formatting argument (see {@link MsgFormat})
	 * @param arg2 Formatting argument (see {@link MsgFormat})
	 * @param arg3 Formatting argument (see {@link MsgFormat})
	 * @return The formatted message
	 */
	public static String formatMsg(String containerName, String resourceName, Locale locale, String key, Object arg1, Object arg2, Object arg3)
	{
		return formatMsg(containerName, resourceName, locale, key, new Object [] { arg1, arg2, arg3 });
	}

	//////////////////////////////////////////////////
	// @@ Access to optional resources
	//////////////////////////////////////////////////

	/**
	 * Gets the integer value or a default if not found.
	 *
	 * @param res Resource to get the resource item
	 * @param key The resource key
	 * @param dflt Default value is resource does not exist
	 *
	 * @return The resource value
	 */
	public static int getOptionalInt(ResourceCollection res, String key, int dflt)
	{
		if (res != null)
		{
			String value = res.getOptionalString(key);
			if (value != null)
				return Integer.valueOf(value).intValue();
		}

		return dflt;
	}

	/**
	 * Gets the character value or a default if not found.
	 *
	 * @param res Resource to get the resource item
	 * @param key The resource key
	 * @param dflt Default value is resource does not exist
	 *
	 * @return The resource value
	 */
	public static char getOptionalChar(ResourceCollection res, String key, char dflt)
	{
		if (res != null)
		{
			String value = res.getOptionalString(key);
			if (value != null)
			{
				if (value.length() == 1)
				{
					return value.charAt(0);
				}
				return (char) Integer.valueOf(value).intValue();
			}
		}

		return dflt;
	}

	//////////////////////////////////////////////////
	// @@ DescriptionObject/DisplayObject support
	//////////////////////////////////////////////////

	/** Description resource key */
	public static final String RESOURCE_KEY_DESCRIPTION = "description";

	/** Description resource key */
	public static final String RESOURCE_KEY_DISPLAYNAME = "displayname";

	/**
	 * Loads the description from the given resource.
	 *
	 * @param o Object that will receive the display name and description from the resource
	 * @param res Resource
	 * @param prefix Resource prefix or null<br>
	 * If given, the prefix (plus the '.' character) will be prepended to the resouce key.
	 */
	public static void loadDescriptionObjectFromResource(DescriptionObject o, ResourceCollection res, String prefix)
	{
		String key = prefix != null ? prefix + "." + RESOURCE_KEY_DESCRIPTION : RESOURCE_KEY_DESCRIPTION;
		o.setDescription(res.getOptionalString(key));
	}

	/**
	 * Loads the description from the given resource.
	 *
	 * @param o Object that will receive the display name and description from the resource
	 * @param res Resource
	 * @param prefix Resource prefix or null<br>
	 * If given, the prefix (plus the '.' character) will be prepended to the resouce key.
	 */
	public static void loadDisplayObjectFromResource(DisplayObject o, ResourceCollection res, String prefix)
	{
		loadDescriptionObjectFromResource(o, res, prefix);

		String key = prefix != null ? prefix + "." + RESOURCE_KEY_DISPLAYNAME : RESOURCE_KEY_DISPLAYNAME;
		o.setDisplayName(res.getOptionalString(key));
	}
}
