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
package org.openbp.cockpit;

import org.openbp.common.string.StringUtil;

/**
 * Cockpit constants.
 *
 * @author Heiko Erhardt
 */
public abstract class CockpitConstants
{
	//////////////////////////////////////////////////
	// @@ Resource names
	//////////////////////////////////////////////////

	/** Name of the resource component for general Jaspira resources */
	public static final String RESOURCE_COCKPIT = "cockpit";

	//////////////////////////////////////////////////
	// @@ Sub directories of cockpit
	//////////////////////////////////////////////////

	/** Cockpit directory */
	public static final String COCKPIT = "cockpit";

	/** Plugin directory */
	public static final String PLUGIN = "plugin";

	/** Generator directory */
	public static final String GENERATOR = "generator";

	/** Skin directory */
	public static final String SKIN = "skin";

	/** Custom object descriptor set directory */
	public static final String CUSTOM_OBJECT_DESCRIPTOR_SETS = "codsets";

	//////////////////////////////////////////////////
	// @@ Documentation file names
	//////////////////////////////////////////////////

	/** overview document */
	public static final String DOC_MANUAL = "pdf" + StringUtil.FOLDER_SEP + "OpenBP-Manual.pdf";

	/** Java API reference document */
	public static final String DOC_JAVA_API = "api" + StringUtil.FOLDER_SEP + "index.html";
}
