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
package org.openbp.swing.components.popupfield;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.openbp.swing.SwingUtil;
import org.openbp.swing.layout.VerticalFlowLayout;
import org.openbp.swing.plaf.sky.SkyLookAndFeel;

/**
 * Test class for {@link JSelectionField}
 *
 * @author Stephan Schmid
 */
public class TestJSelectionField extends JFrame
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	private TestJSelectionField()
	{
		super("JSelectionField Test");
		getContentPane().setLayout(new VerticalFlowLayout());

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setLocation(200, 200);
		setSize(300, 300);

		JSelectionField sel = new JSelectionField();
		sel.setEditable(false);

		/*
		 sel.addItem ("First item", "item1");
		 sel.addItem ("Second item", "item2");
		 sel.addItem ("Third item", "item3");
		 sel.addItem ("Fourth item", "item4");
		 sel.addItem ("Fifth item", "item5");
		 sel.addItem ("Sixth item", "item6");
		 sel.addItem ("Seventh item", "item7");
		 sel.addItem ("Eight item", "item8");
		 sel.setSelectedItem ("item5");
		 */
		sel.addItem("First");
		sel.addItem("Second");
		sel.addItem("Third");
		sel.addItem("Fourth");
		sel.addItem("Fifth");
		sel.addItem("Sixth");
		sel.addItem("Seventh");
		sel.addItem("Eight");
		sel.setSelectedItem("Sixth");

		JComboBox cb = new JComboBox();
		cb.setEditable(false);
		cb.addItem("First item");
		cb.addItem("Second item");
		cb.addItem("Third item");
		cb.addItem("Fourth item");
		cb.addItem("Fifth item");
		cb.addItem("Sixth item");
		cb.addItem("Seventh item");
		cb.addItem("Eight item");

		getContentPane().add(new JTextField());
		getContentPane().add(sel);
		getContentPane().add(new JTextField());
		getContentPane().add(cb);
		getContentPane().add(new JTextField());
	}

	/**
	 * Main method for test.
	 *
	 * @param args Arguments
	 */
	public static void main(String [] args)
	{
		try
		{
			UIManager.setLookAndFeel(new SkyLookAndFeel());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		SwingUtil.startApplication(new TestJSelectionField(), true);
	}
}
