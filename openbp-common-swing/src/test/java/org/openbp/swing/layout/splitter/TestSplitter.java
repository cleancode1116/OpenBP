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

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

import org.openbp.common.ExceptionUtil;
import org.openbp.swing.SwingUtil;

/**
 * Test class for splitter bars.
 */
public class TestSplitter extends JFrame
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	private SplitterLayout splitterLayout = new SplitterLayout();

	private JButton button1 = new JButton();

	private SplitterBar splitterBar1 = new SplitterBar();

	private JButton button2 = new JButton();

	private SplitterBar splitterBar2 = new SplitterBar();

	private JButton button3 = new JButton();

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	private TestSplitter()
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
		this.getContentPane().setLayout(splitterLayout);

		button1.setBorder(BorderFactory.createEtchedBorder());
		button1.setPreferredSize(new Dimension(100, 50));
		button1.setHorizontalAlignment(SwingConstants.CENTER);
		button1.setText("button1");

		button2.setText("button2");
		button2.setHorizontalAlignment(SwingConstants.CENTER);
		button2.setPreferredSize(new Dimension(100, 50));
		button2.setBorder(BorderFactory.createEtchedBorder());

		button3.setText("button3");
		button3.setHorizontalAlignment(SwingConstants.CENTER);
		button3.setPreferredSize(new Dimension(100, 50));
		button3.setBorder(BorderFactory.createEtchedBorder());

		splitterBar1.setLiveLayout(true);
		splitterBar2.setLiveLayout(true);

		this.getContentPane().add(button1, null);
		this.getContentPane().add(splitterBar1, null);
		this.getContentPane().add(button2, null);
		this.getContentPane().add(splitterBar2, null);
		this.getContentPane().add(button3, null);
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
		SwingUtil.startApplication(new TestSplitter(), true);
	}
}
