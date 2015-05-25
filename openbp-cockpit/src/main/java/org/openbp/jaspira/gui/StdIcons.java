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
package org.openbp.jaspira.gui;

import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.jaspira.JaspiraConstants;

/**
 * Standard class.
 * This utility class provides access to commonly used standard icons such as folder icons.
 *
 * @author Heiko Erhardt
 */
public class StdIcons
{
	//////////////////////////////////////////////////
	// @@ Public members
	//////////////////////////////////////////////////

	/** Open folder icon (16 pixel) */
	public static MultiIcon openFolderIcon;

	/** Closed folder icon (16 pixel) */
	public static MultiIcon closedFolderIcon;

	/** File chooser (16 pixel) */
	public static MultiIcon fileChooserIcon;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	// Static initializer
	static
	{
		ResourceCollection res = ResourceCollectionMgr.getDefaultInstance().getResource(JaspiraConstants.RESOURCE_JASPIRA, StdIcons.class);

		openFolderIcon = (MultiIcon) res.getRequiredObject("icon.openfolder");
		openFolderIcon.setIconSize(FlexibleSize.SMALL);

		closedFolderIcon = (MultiIcon) res.getRequiredObject("icon.closedfolder");
		closedFolderIcon.setIconSize(FlexibleSize.SMALL);

		fileChooserIcon = (MultiIcon) res.getRequiredObject("icon.filechooser");
		fileChooserIcon.setIconSize(FlexibleSize.SMALL);
	}

	/**
	 * Private constructor prevents instantiation.
	 */
	private StdIcons()
	{
	}
}
