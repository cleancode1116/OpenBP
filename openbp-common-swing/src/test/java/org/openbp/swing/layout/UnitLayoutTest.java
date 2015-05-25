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
package org.openbp.swing.layout;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.openbp.swing.SwingUtil;

/**
 * Model layout test.
 *
 * @author Heiko Erhardt
 */
public class UnitLayoutTest
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private UnitLayoutTest()
	{
	}

	public static void main(String [] args)
	{
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new UnitLayout(UnitLayout.RIGHT, UnitLayout.CENTER));
		frame.getContentPane().add(new JButton("Component"));
		frame.setBounds(100, 100, 400, 400);
		SwingUtil.show(frame);
	}
}
