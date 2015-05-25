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
package org.openbp.swing.layout.splitter;

import java.awt.Component;
import java.awt.Dimension;

/**
 * A SplitterSpace is a completely plain lightweight AWT Component.
 * Its only purpose in life is to hold a space that can act as a handle for a SplitterBar.
 *
 * @seec SplitterBar
 */
public class SplitterSpace extends Component
{
	public synchronized Dimension getMinimumSize()
	{
		return new Dimension(10, 10);
	}

	public synchronized Dimension getPreferredSize()
	{
		return new Dimension(10, 10);
	}
}
