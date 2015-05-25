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
package org.openbp.swing.layout.dock;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.openbp.common.ExceptionUtil;
import org.openbp.swing.SwingUtil;

/**
 * Test class for the dock layout.
 */
public class TestDock2 extends JFrame
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	private BorderLayout borderLayout1 = new BorderLayout();

	private JLabel jLabel1 = new JLabel();

	private JLabel jLabel3 = new JLabel();

	private JLabel jLabel4 = new JLabel();

	private JLabel jLabel5 = new JLabel();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public TestDock2()
	{
		try
		{
			jbInit();
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
		}
	}

	private void jbInit()
		throws Exception
	{
		jLabel1.setBorder(BorderFactory.createEtchedBorder());
		jLabel1.setText("jLabel1");
		this.getContentPane().setLayout(borderLayout1);
		jLabel3.setBorder(BorderFactory.createEtchedBorder());
		jLabel3.setText("jLabel3");
		jLabel4.setBorder(BorderFactory.createEtchedBorder());
		jLabel4.setText("jLabel4");
		jLabel5.setBorder(BorderFactory.createEtchedBorder());
		jLabel5.setText("jLabel5");
		borderLayout1.setHgap(2);
		borderLayout1.setVgap(2);
		this.getContentPane().add(jLabel1, BorderLayout.WEST);
		this.getContentPane().add(jLabel3, BorderLayout.NORTH);
		this.getContentPane().add(jLabel4, BorderLayout.SOUTH);
		this.getContentPane().add(jLabel5, BorderLayout.EAST);
	}

	//////////////////////////////////////////////////
	// @@ Main method
	//////////////////////////////////////////////////

	/**
	 * Main method for test.
	 *
	 * @param args Argument vector
	 */
	public static void main(String [] args)
	{
		SwingUtil.startApplication(new TestDock2(), true);
	}
}
