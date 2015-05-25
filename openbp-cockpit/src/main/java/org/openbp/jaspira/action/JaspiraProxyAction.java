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
package org.openbp.jaspira.action;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

/**
 * A Jaspira Proxy action can be used to insert arbitary JComponent elements
 * into a toolbar (and later a menu).
 *
 * @author Stephan Moritz
 */
public class JaspiraProxyAction extends JaspiraAction
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** The component to be displayed. */
	private JComponent component;

	/////////////////////////////////////////////////////////////////////////
	// @@ COnstruction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param name The internal name of the object
	 * @param component Component to wrap by this proxy action
	 * @param priority Priority
	 */
	public JaspiraProxyAction(String name, JComponent component, int priority)
	{
		super(name, null, null, null, null, priority, TYPE_ACTION);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.action.JaspiraAction#toComponent()
	 */
	public JComponent toComponent()
	{
		return component;
	}

	/**
	 * Always returns null.
	 * @see org.openbp.jaspira.action.JaspiraAction#toMenuItem()
	 */
	public JMenuItem toMenuItem()
	{
		return null;
	}

	/**
	 * Always returns null.
	 * @see org.openbp.jaspira.action.JaspiraAction#toMenuItem(String currentPageName)
	 */
	public JMenuItem toMenuItem(String currentPageName)
	{
		return null;
	}

	/**
	 * Returns the component.
	 * @see org.openbp.jaspira.action.JaspiraAction#toToolBarComponent()
	 */
	public JComponent toToolBarComponent()
	{
		return component;
	}

	/**
	 * Returns the component.
	 * @see org.openbp.jaspira.action.JaspiraAction#toToolBarComponent(String currentPageName)
	 */
	public JComponent toToolBarComponent(String currentPageName)
	{
		if (!matchesPageName(currentPageName))
			return component;
		return null;
	}
}
