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

import java.util.ArrayList;
import java.util.List;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.application.Application;
import org.openbp.common.dump.Dumper;
import org.openbp.common.generic.propertybrowser.ObjectDescriptor;
import org.openbp.common.generic.propertybrowser.ObjectDescriptorMgr;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.guiclient.GUIClientModule;
import org.openbp.guiclient.model.ModelConnector;

/**
 * Test class for the model service.
 *
 * @author Heiko Erhardt
 */
public class TestClient
{
	/**
	 * Constructor.
	 */
	public TestClient()
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
	}

	/**
	 * Performs the application processing
	 */
	private void process()
	{
		try
		{
			//////////////////////////////////////////////////
			// Test processsing
			//////////////////////////////////////////////////

			String testMode = null;
			testMode = "Children";
			// testMode = "Update";
			// testMode = "Trace";
			// testMode = "ObjectDescriptor";
			// testMode = "ProcessDump";
			// testMode = "Items";
			// testMode = "CopyModel";
			// testMode = "ItemTypeRegistry";
			// testMode = "ActionItems";
			// testMode = "Models";

			Dumper dumper = new Dumper();

			if (testMode.equals("Children"))
			{
				// Get the process to execute
				Model model = ModelConnector.getInstance().getModelByQualifier(ModelQualifier.constructModelQualifier("AddressBookDemo"));
				List l = new ArrayList();
				l.add(model);

				showObjectList(l);
			}

			if (testMode.equals("ObjectDescriptor"))
			{
				try
				{
					ObjectDescriptor od = ObjectDescriptorMgr.getInstance().getDescriptor(ProcessItem.class, ObjectDescriptorMgr.ODM_THROW_ERROR);
					dumper.dump(od);
				}
				catch (XMLDriverException e)
				{
					ExceptionUtil.printTrace(e);
					System.exit(3);
				}
			}
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
			System.exit(3);
		}
	}

	/**
	 * Adds a list of objects to the tree.
	 *
	 * @param list Object list
	 */
	private static void showObjectList(List list)
	{
		int n = list.size();
		for (int i = 0; i < n; ++i)
		{
			Object o = list.get(i);

			if (!canDisplay(o))
			{
				// Ignore this object
				continue;
			}

			System.out.println(o.toString());

			if (o instanceof ModelObject)
			{
				// This is a model object, get its children and add them to the new node
				List subObjects = ((ModelObject) o).getChildren();
				showObjectList(subObjects);
			}
		}
	}

	/**
	 * Determines if an object shall be displayed in the tree.
	 *
	 * @param o Object to check
	 * @return
	 *		true	The class of the object (or a super class/interface of it)
	 *				has been specified in the displayedObjectClasses property.<br>
	 *		false	The object shall be ignored.
	 */
	private static boolean canDisplay(Object o)
	{
		Class cls = o.getClass();
		if (Model.class.isAssignableFrom(cls) || ProcessItem.class.isAssignableFrom(cls) || InitialNode.class.isAssignableFrom(cls))
			return true;

		return false;
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

			TestClient tc = new TestClient();
			tc.process();

			System.exit(0);
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
			System.exit(1);
		}
	}
}
