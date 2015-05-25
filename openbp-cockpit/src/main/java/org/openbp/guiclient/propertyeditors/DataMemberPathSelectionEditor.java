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
package org.openbp.guiclient.propertyeditors;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.property.PropertyAccessUtil;
import org.openbp.common.property.PropertyException;
import org.openbp.core.model.ModelObject;
import org.openbp.guiclient.model.item.itemtree.DataMemberSelectionDlg;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.propertybrowser.editor.EditorParameterParser;

/**
 * Component selection editor.
 * Property editor that allows input of a component name and also provides a button which will
 * activate an object browser to choose from.
 *
 * @author Heiko Erhardt
 */
public class DataMemberPathSelectionEditor extends ComponentSelectionEditor
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Property path of the edited object to the complex type item to use as root object */
	private String rootObjectPropertyPath;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DataMemberPathSelectionEditor()
	{
	}

	/**
	 * Parses the editor parameters.
	 */
	protected void parseParams()
	{
		EditorParameterParser parser = new EditorParameterParser(this);

		Iterator itNames = parser.get("propertyname");
		if (itNames.hasNext())
		{
			properties = new HashMap();

			Iterator itValues = parser.get("propertyvalue");

			while (itNames.hasNext())
			{
				String name = (String) itNames.next();
				Object value = itValues.next();

				properties.put(name, value);
			}
		}

		rootObjectPropertyPath = parser.getString("rootobjectpropertypath");

		title = parser.getString("title");
		if (title == null)
		{
			// TOLOCALIZE
			title = "Data Member Path Selection";
		}

		customizerClassName = parser.getString("customizer");
	}

	//////////////////////////////////////////////////
	// @@ Item Selection Dialog
	//////////////////////////////////////////////////

	/**
	 * Shows the component browser.
	 */
	public void showBrowser()
	{
		// Initialize the item selection dialog
		final DataMemberSelectionDlg dlg = new DataMemberSelectionDlg(ApplicationUtil.getActiveWindow(), true);
		dlg.setTitle(title);

		// Determine the root object (a complex type item) from the edited object
		if (rootObjectPropertyPath != null)
		{
			Object o = getObject();
			try
			{
				ModelObject rootObject = (ModelObject) PropertyAccessUtil.getProperty(o, rootObjectPropertyPath);
				dlg.setRootObject(rootObject);
			}
			catch (PropertyException e)
			{
				LogUtil.error(getClass(), "Error accessing property $0 in object of type $1", rootObjectPropertyPath, o.getClass().getName(), e);
			}
			catch (ClassCastException e)
			{
				LogUtil.error(getClass(), "Value of property $0 in object of type $1 is not a ModelObject", rootObjectPropertyPath, o.getClass().getName(), e);
			}
		}

		dlg.rebuildTree();
		dlg.expand(1);

		// Determine the current data member path from the value of the property
		String memberPath = (String) value;
		dlg.setSelectedMemberPath(memberPath);

		// Instantiate the customizer class
		if (customizer == null && customizerClassName != null)
		{
			try
			{
				customizer = (ComponentSelectionEditorCustomizer) ReflectUtil.instantiate(customizerClassName, ComponentSelectionEditorCustomizer.class, "component selection editor customizer class");
			}
			catch (Exception e)
			{
				ExceptionUtil.printTrace(e);
			}
		}

		if (customizer != null)
		{
			if (!customizer.initializeDialog(this, dlg))
				return;
		}

		dlg.addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent we)
			{
				String memberPath = dlg.getSelectedMemberPath();

				if (customizer != null)
				{
					if (!customizer.dialogClosed(DataMemberPathSelectionEditor.this, dlg, memberPath))
						return;
				}

				if (memberPath != null)
				{
					textField.setText(memberPath);
					textField.requestFocus();

					propertyChanged();
				}
			}
		});

		if (customizer != null)
		{
			if (!customizer.dialogInitialized(this, dlg))
				return;
		}

		// Show the dialog
		dlg.setVisible(true);
	}
}
