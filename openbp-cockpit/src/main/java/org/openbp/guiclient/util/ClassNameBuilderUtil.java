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
package org.openbp.guiclient.util;

import org.openbp.common.setting.SettingUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.activity.ActivityItem;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * Utility methods for the construction of class names.
 *
 * @author Heiko Erhardt
 */
public class ClassNameBuilderUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ClassNameBuilderUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	/**
	 * Constructs a class name for an activity.
	 *
	 * @param activity The activity
	 * @return The class name or null
	 */
	public static String constructActivityClassName(ActivityItem activity)
	{
		String className = constructClassNamePrefix(activity);
		if (className != null)
		{
			className = className + '.' + CoreConstants.PKG_ACTIVITY + '.' + activity.getName() + "Activity";
		}
		return className;
	}

	/**
	 * Constructs a class name for a data type.
	 *
	 * @param type The data type
	 * @return The class name or null
	 */
	public static String constructTypeClassName(DataTypeItem type)
	{
		String className = constructClassNamePrefix(type);
		if (className != null)
		{
			className = className + '.' + CoreConstants.PKG_DATA + '.' + type.getName();
		}
		return className;
	}

	/**
	 * Constructs the class name prefix for the given item or model
	 *
	 * @param item Item or model
	 * @return The class name or null
	 */
	public static String constructClassNamePrefix(Item item)
	{
		Model model = item.getOwningModel();
		if (model != null)
		{
			String pkg = model.getDefaultPackage();
			if (pkg == null)
			{
				// Use the name of the model as package suffix
				pkg = SettingUtil.getStringSetting("openbp.cockpit.defaultModelPackageBaseName");
				if (pkg == null)
					pkg = "org.openbp.model";
				pkg = pkg + "." + model.getName().toLowerCase();
			}

			return pkg;
		}

		return null;
	}
}
