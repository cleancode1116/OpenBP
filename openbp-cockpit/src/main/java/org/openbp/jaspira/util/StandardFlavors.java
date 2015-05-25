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
package org.openbp.jaspira.util;

import java.awt.datatransfer.DataFlavor;

import org.openbp.jaspira.gui.plugin.JaspiraPage;
import org.openbp.jaspira.gui.plugin.VisiblePlugin;
import org.openbp.jaspira.plugin.PluginState;

/**
 * This class contains constants for commonly used data flavors.
 * Data flavors are used for drag and drop or copy/paste operations.
 *
 * @author Heiko Erhardt
 */
public class StandardFlavors
{
	/**
	 * Private constructor prevents instantiation.
	 */
	private StandardFlavors()
	{
	}

	/** Java object flavor */
	public static final DataFlavor OBJECT = new DataFlavor(Object.class, "Java Object");

	/** Plugin state flavor */
	public static final DataFlavor PLUGIN = new DataFlavor(VisiblePlugin.class, "Jaspira Plugin");

	/** Jaspira plugin state flavor */
	public static final DataFlavor PLUGIN_STATE = new DataFlavor(PluginState.class, "Jaspira Plugin State");

	/** Jaspira page flavor */
	public static final DataFlavor JASPIRA_PAGE = new DataFlavor(JaspiraPage.class, "Jaspira Page");
}
