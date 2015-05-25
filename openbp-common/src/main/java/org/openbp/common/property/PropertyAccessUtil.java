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
package org.openbp.common.property;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.openbp.common.string.StringUtil;

/**
 * Utility class that provides methods to access properties of an object dynamically.
 * Wraps Apache Common-PropertyUtils.
 *
 * @author Heiko Erhardt
 */
public final class PropertyAccessUtil
{
	/**
	 * Private constructor prevents instantiation.
	 */
	private PropertyAccessUtil()
	{
	}

	/**
	 * Gets the value of the specified property of an object.
	 * This just wraps the Apache Common-PropertyUtils class in convenient exception handling.
	 *
	 * @param base Object that holds the property
	 * @param property Property name; all property access methods supported by PropertyUtils is valid.
	 * @return The value of the property
	 * @throws PropertyException If no appropriate access method could be found or if the invocation
	 * of one of the property access methods failed. The nested exception describes the error in detail.
	 */
	public static Object getProperty(Object base, String property)
		throws PropertyException
	{
		// TODO Doesn't work for property chains; actually, decapitalization should be removed and the descriptors should be corrected.
		property = StringUtil.decapitalize(property);

		if (base == null)
			throw new PropertyException("Cannot access property '" + property + "' with null base object.");

		Object ret = null;
		Exception ex = null;
		try
		{
			ret = PropertyUtils.getProperty(base, property);
		}
		catch (NestedNullException e)
		{
			// Ignore nested nulls
		}
		catch (IllegalAccessException e)
		{
			ex = e;
		}
		catch (IllegalArgumentException e)
		{
			ex = e;
		}
		catch (InvocationTargetException e)
		{
			ex = e;
		}
		catch (NoSuchMethodException e)
		{
			ex = e;
		}
		if (ex != null)
		{
			throw new PropertyException("Error accessing property '" + property + "' of object of type '" + base.getClass().getName() + "'.", ex);
		}
		return ret;
	}

	/**
	 * Sets the value of the specified property of an object.
	 * In order to access the property, the method uses the following strategy.
	 * We assume the name of the property is "Sample"
	 * <ol>
	 * <li>Search for a method named get\iSample\i</li>
	 * <li>Try direct field access on the member \isample\i</li>
	 * <li>Try to use map-style access to properties by calling the method get("Sample")</li>
	 * </ol>
	 *
	 * @param base Object to invoke the method on
	 * @param property Property name<br>
	 * @param value Property value
	 * Property chains of the form "prop1.prop2" are *not* supported.
	 * @throws PropertyException If no appropriate access method could be found or if the invocation
	 * of one of the property access methods failed.<br>
	 * The nested exception describes the error in detail.
	 */
	public static void setProperty(Object base, String property, Object value)
		throws PropertyException
	{
		// TODO Doesn't work for property chains; actually, decapitalization should be removed and the descriptors should be corrected.
		property = StringUtil.decapitalize(property);

		if (base == null)
			throw new PropertyException("Cannot access property '" + property + "' with null base object.");

		Exception ex = null;
		try
		{
			PropertyUtils.setProperty(base, property, value);
		}
		catch (NestedNullException e)
		{
			ex = e;
		}
		catch (IllegalAccessException e)
		{
			ex = e;
		}
		catch (IllegalArgumentException e)
		{
			ex = e;
		}
		catch (InvocationTargetException e)
		{
			ex = e;
		}
		catch (NoSuchMethodException e)
		{
			ex = e;
		}
		if (ex != null)
		{
			String valueType = value != null ? value.getClass().getName() : null;
			throw new PropertyException("Error setting property '" + property + "' of object of type '" + base.getClass().getName() + "' to value of type '" + valueType + "'.", ex);
		}
	}
}
