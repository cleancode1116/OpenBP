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
package org.openbp.core.model;

import org.openbp.common.setting.SettingUtil;
import org.openbp.common.string.StringUtil;

/**
 * Defines delimiters for qualified model names etc\..
 *
 * @author Heiko Erhardt
 */
public final class ModelLocationUtil
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private ModelLocationUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Directory constants
	//////////////////////////////////////////////////

	/** Java source directory */
	public static final String DIR_SRC = "src";

	/** Java classes directory */
	public static final String DIR_CLASSES = "classes";

	/** Java classes directory */
	public static final String DIR_TARGET_CLASSES = "target" + StringUtil.FOLDER_SEP + "classes";

	/** Java lib directory */
	public static final String DIR_LIB = "lib";

	/** Java extlib directory */
	public static final String DIR_EXTLIB = "extlib";

	/** Documentaton directory */
	public static final String DIR_DOC = "doc";

	/** Model name variable for path expansion */
	private static final String VAR_MODEL_NAME = "${MODEL_NAME}";

	/** Model dir variable for path expansion */
	private static final String VAR_MODEL_DIR = "${MODEL_DIR}";

	/**
	 * Id-model location pairs.
	 *
	 * May contain the following variables:
	 * Expands the following special variables in the given string.
	 * The method will substitute the following variables:<br>
	 * ${MODEL_NAME}: Name of the model.<br>
	 * ${MODEL_DIR}: Directory of the model.<br>
	 * ${OPENBP_HOME}: Root directory of the OpenBP installation<br>
	 * ${VAR}: Value of the property file setting or system property 'VAR'
	 */
	private static final String [] MODEL_LOCATION_ID_MAPPINGS =
	{
		DIR_SRC, VAR_MODEL_DIR + StringUtil.FOLDER_SEP + "src" + StringUtil.FOLDER_SEP + "main" + StringUtil.FOLDER_SEP + "java",
		DIR_CLASSES, VAR_MODEL_DIR + StringUtil.FOLDER_SEP + "classes",
		DIR_TARGET_CLASSES, VAR_MODEL_DIR + StringUtil.FOLDER_SEP + "target" + StringUtil.FOLDER_SEP + "classes",
		DIR_LIB, VAR_MODEL_DIR + StringUtil.FOLDER_SEP + "lib",
		DIR_EXTLIB, VAR_MODEL_DIR + StringUtil.FOLDER_SEP + "extlib",
		DIR_DOC, VAR_MODEL_DIR + StringUtil.FOLDER_SEP + "doc",
	};

	/**
	 * Returns the model location that is identified by the given id.
	 * The id may be one of the public contstants of this class.
	 *
	 * @param model The model
	 * @param identifier Identifier (see the {@link ModelLocationUtil} class)
	 * @return The directory
	 */
	public static String expandModelLocation(Model model, String identifier)
	{
		String dir = null;

		int n = ModelLocationUtil.MODEL_LOCATION_ID_MAPPINGS.length;
		for (int i = 0; i < n; ++i)
		{
			String id = ModelLocationUtil.MODEL_LOCATION_ID_MAPPINGS[i++];
			if (id.equals(identifier))
			{
				dir = ModelLocationUtil.MODEL_LOCATION_ID_MAPPINGS[i];
				break;
			}
		}

		if (dir != null)
		{
			if (dir.indexOf(VAR_MODEL_NAME) >= 0)
			{
				String name = model.getName();
				dir = StringUtil.substitute(dir, VAR_MODEL_NAME, name);
			}

			else if (dir.indexOf(VAR_MODEL_DIR) >= 0)
			{
				String path = model.getModelPath();
				dir = StringUtil.substitute(dir, VAR_MODEL_DIR, path);
			}

			dir = SettingUtil.expandVariables(dir, true);
		}
		return dir;
	}
}
