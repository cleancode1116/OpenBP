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
package org.openbp.guiclient;

import org.openbp.core.CoreModule;
import org.openbp.jaspira.propertybrowser.editor.PropertyEditorMgr;
import org.openbp.swing.components.JMsgBox;

/**
 * This singleton class provides access to various installation-dependent settings.
 *
 * @author Heiko Erhardt
 */
public final class GUIClientModule extends CoreModule
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static GUIClientModule singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized GUIClientModule getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new GUIClientModule();
		}
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private GUIClientModule()
	{
		super();

		// Use the product short name as default title for message boxes
		JMsgBox.setDefaultTitle(getProductProfile().getShortProductName());
	}

	/**
	 * Initializes the client environment.
	 * This method should be called right after the startup of the client.
	 */
	public void initialize()
	{
		// Perform the core initialization
		super.initialize();

		// The model managers should not instantiate the items, so turn off item instantiation
		getModelMgr().setInstantiateItems(false);

		// Add the package for property editors specific to the client
		PropertyEditorMgr.getInstance().addPackage("org.openbp.guiclient.propertyeditors");
	}
}
