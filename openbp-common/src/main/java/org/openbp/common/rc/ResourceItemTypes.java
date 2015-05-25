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

import java.util.HashMap;
import java.util.Map;

import org.openbp.common.ReflectUtil;
import org.openbp.common.string.StringUtil;

/**
 * Provides the access to the {@link ResourceItem} implementations.
 * Loads the class only one time, and caches the classes for later use.
 *
 * The resource technology to read one by the mime type
 * is realized like following.
 *
 * Example:
 *
 * The mime type is "text/plain" and the resource object is
 * an instance of a the 'org.openbp.common.rc.text.Plain'.
 *
 * @author Andreas Putz
 */
public final class ResourceItemTypes
{
	//////////////////////////////////////////////////
	// @@ Standard mime types
	//////////////////////////////////////////////////

	/** Mime type text/plain */
	public static final String TEXT_PLAIN = "text/plain";

	/** Mime type image/gif */
	public static final String IMAGE_GIF = "image/gif";

	/** Mime type image/jpeg */
	public static final String IMAGE_JPEG = "image/jpeg";

	/** Mime type image/png */
	public static final String IMAGE_PNG = "image/png";

	/** Mime type image/bmp */
	public static final String IMAGE_BMP = "image/bmp";

	/** Mime type multiicon. */
	public static final String MULTIICON = "image/multi";

	//////////////////////////////////////////////////
	// @@ Custom mime types
	//////////////////////////////////////////////////

	/** Mime type primitive/integer */
	public static final String PRIMITIVE_INTEGER = "primitive/integer";

	/** Mime type primitive/character*/
	public static final String PRIMITIVE_CHARACTER = "primitive/character";

	/** Mime type data/keystroke */
	public static final String DATA_KEYSTROKE = "data/keystroke";

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The base package of the resource item classes */
	private static final String PACKAGE = "org.openbp.common.rc";

	/** Table mapping mime types (Strings) to Class objects (that implement {@link ResourceItem}) */
	private static Map resourceItemTypeInfos = new HashMap();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	private ResourceItemTypes()
	{
	}

	//////////////////////////////////////////////////
	// @@ Resource item class access
	//////////////////////////////////////////////////

	/**
	 * Determines the resource item class for the given mime-type.
	 *
	 * @param mimeType Mime type of the resource object
	 * @return The {@link ResourceItem} class suitable for this mime type or null if no such resource item type exists
	 */
	public static Class determineResourceItemClass(String mimeType)
	{
		Class cls = (Class) resourceItemTypeInfos.get(mimeType);

		if (cls == null)
		{
			String className = determineResourceItem(mimeType);
			cls = ReflectUtil.loadClass(className);
			resourceItemTypeInfos.put(mimeType, cls);
		}

		return cls;
	}

	/**
	 * Determines the resource item implementation of the mime-type.
	 *
	 * @return Fully qualified class name
	 */
	private static String determineResourceItem(String mimeType)
	{
		mimeType = mimeType.toLowerCase();

		StringBuffer sb = new StringBuffer(200);
		sb.append(PACKAGE);
		sb.append('.');

		// Replace special characters
		String tmp = mimeType.replace('.', '_');
		tmp = tmp.replace('-', '_');
		tmp = tmp.replace('\\', '/');

		int index = tmp.indexOf('/');
		if (index > 0)
		{
			sb.append(tmp.substring(0, index));
			sb.append('.');

			// Appends a 'T' for type, to make a difference to other objects like Integer
			sb.append('T');
			sb.append(StringUtil.capitalize(tmp.substring(index + 1)));
		}
		else
		{
			// Appends a 'T' for type, to make a difference to other objects like Integer
			sb.append('T');
			sb.append(StringUtil.capitalize(tmp));
		}

		return sb.toString();
	}
}
