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
package org.openbp.guiclient.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.application.Application;
import org.openbp.common.dump.Dumper;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.core.model.item.type.ComplexTypeItemImpl;
import org.openbp.core.model.item.type.DataMemberImpl;
import org.openbp.core.model.item.type.SimpleTypeItemImpl;
import org.openbp.guiclient.GUIClientModule;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.jaspira.propertybrowser.PropertyBrowserImpl;
import org.openbp.jaspira.propertybrowser.SaveStrategy;
import org.openbp.swing.plaf.sky.SimpleBorder;

/**
 * Test class for the model service.
 *
 * @author Heiko Erhardt
 */
public class TestPropertyBrowser
{
	/**
	 * Constructor.
	 */
	public TestPropertyBrowser()
	{
		try
		{
			// Initialize the client environment
			GUIClientModule.getInstance().initialize();
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace("OpenBP client initialization error", e);
			System.exit(1);
		}

		try
		{
			UIManager.setLookAndFeel("org.openbp.swing.plaf.sky.SkyLookAndFeel");
		}
		catch (ClassNotFoundException e)
		{
			ExceptionUtil.printTrace(e);
		}
		catch (InstantiationException e)
		{
			ExceptionUtil.printTrace(e);
		}
		catch (IllegalAccessException e)
		{
			ExceptionUtil.printTrace(e);
		}
		catch (UnsupportedLookAndFeelException e)
		{
			ExceptionUtil.printTrace(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ Main method
	//////////////////////////////////////////////////

	/**
	 * Main method for test.
	 * @param args Command line arguments
	 */
	public static void main(String [] args)
	{
		try
		{
			Application.setArguments(args);

			new TestPropertyBrowser();

			TestFrame frame = new TestFrame();
			frame.setVisible(true);
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
			System.exit(1);
		}
	}

	//////////////////////////////////////////////////
	// @@ Frame class
	//////////////////////////////////////////////////

	static class TestFrame extends JFrame
		implements SaveStrategy
	{
		/** The property browser */
		private PropertyBrowserImpl propertyBrowser;

		/** Error border */
		private SimpleBorder errorBorder;

		/** Scroll pane */
		private JScrollPane scrollPane;

		static boolean on;

		public TestFrame()
		{
			super("Property Browser Test");
			setBounds(100, 100, 600, 400);

			errorBorder = new SimpleBorder(2, 2, 2, 2);
			errorBorder.setWidth(2);
			errorBorder.setColor(Color.RED);

			propertyBrowser = new PropertyBrowserImpl(this, null);

			// propertyBrowser.setSaveImmediately (true);

			JPanel cp = (JPanel) getContentPane();
			cp.setLayout(new BorderLayout());

			scrollPane = new JScrollPane(propertyBrowser);

			JButton btn = new JButton("Border on/off");
			btn.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					on = !on;
					if (on)
					{
						scrollPane.setBorder(errorBorder);
					}
					else
					{
						scrollPane.setBorder(null);
					}
				}
			});

			cp.add("East", btn);
			cp.add("Center", scrollPane);
			cp.add("South", new JTextField());

			// scrollPane.setBorder (errorBorder);

			addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent we)
				{
					System.exit(0);
				}
			});

			Object o = createTestObject();
			try
			{
				propertyBrowser.setObject(o, true);
			}
			catch (XMLDriverException e)
			{
				e.printStackTrace();
			}
			catch (CloneNotSupportedException e)
			{
				e.printStackTrace();
			}
		}

		/**
		 * Creates a test object.
		 *
		 * @return The object
		 */
		private Object createTestObject()
		{
			SimpleTypeItemImpl stringType = new SimpleTypeItemImpl();
			stringType.setJavaClass(String.class);
			stringType.setClassName(String.class.getName());

			ComplexTypeItemImpl type = new ComplexTypeItemImpl();
			type.setName("TestType");
			type.setDisplayName("Test Type");
			type.setDescription("A test data type object");

			DataMemberImpl member;

			member = new DataMemberImpl();
			member.setName("Member1");
			member.setDisplayName("Member 1");
			member.setDescription("Data member #1");
			member.setTypeName("String");
			member.setDataType(stringType);
			member.setRequired(true);
			type.addMember(member);

			member = new DataMemberImpl();
			member.setName("Member2");
			member.setDisplayName("Member 2");
			member.setDescription("Data member #2");
			member.setTypeName("String");
			member.setDataType(stringType);
			type.addMember(member);

			member = new DataMemberImpl();
			member.setName("Member3");
			member.setDisplayName("Member 3");
			member.setDescription("Data member #3");
			member.setTypeName("String");
			member.setDataType(stringType);
			type.addMember(member);

			member = new DataMemberImpl();
			member.setName("Member4");
			member.setDisplayName("Member 4");
			member.setDescription("Data member #4");
			member.setTypeName("String");
			member.setDataType(stringType);
			type.addMember(member);

			return type;
		}

		//////////////////////////////////////////////////
		// @@ Save strategy implementation
		//////////////////////////////////////////////////

		/**
		 * Executes the save procedure for the current item of the specified property browser.
		 *
		 * @param editor The property browser
		 *
		 * @return
		 *		true	If object was saved successfully.<br>
		 *		false	There were errors during the save operations or the user
		 *				choosed to cancel the save operation.
		 *				The implementor of the strategy should issue an error message
		 *				if appropriate.
		 */
		public boolean executeSave(PropertyBrowser editor)
		{
			Object modObject = editor.getModifiedObject();

			Dumper dumper = new Dumper();
			dumper.dump(modObject);

			return true;
		}
	}
}
