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
package org.openbp.swing.components;

import java.awt.Dimension;

import javax.swing.JTextPane;

/**
 * A JTextPane that doesn't word-wrap.
 *
 * @author Heiko Erhardt
 */
public class JNonWrappingTextPane extends JTextPane
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public JNonWrappingTextPane()
	{
		super();
	}

	//////////////////////////////////////////////////
	// @@ JTextPane overrides
	//////////////////////////////////////////////////

	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	public void setSize(Dimension d)
	{
		int w = getParent().getSize().width;
		if (d.width < w)
		{
			d.width = w;
		}

		super.setSize(d);
	}
}
