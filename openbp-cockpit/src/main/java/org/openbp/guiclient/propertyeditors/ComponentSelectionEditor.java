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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;

import org.openbp.common.CollectionUtil;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.guiclient.model.item.itemtree.ItemSelectionDialog;
import org.openbp.guiclient.model.item.itemtree.ItemTree;
import org.openbp.guiclient.model.item.itemtree.ItemTreeState;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.propertybrowser.editor.EditorParameterParser;
import org.openbp.jaspira.propertybrowser.editor.standard.StringEditor;
import org.openbp.swing.plaf.sky.SimpleBorder;

/**
 * Component selection editor.
 * Property editor that allows input of a component name and also provides a button which will
 * activate an object browser to choose from.
 *
 * @author Heiko Erhardt
 */
public class ComponentSelectionEditor extends StringEditor
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Item types to select */
	private String itemType;

	/** Supported item type list (contains String objects) */
	private List supportedItemTypeList;

	/** Selectable item type list (contains String objects) */
	private List selectableItemTypeList;

	/** Supported object class list (contains Class objects) */
	private List supportedObjectClassList;

	/** Selectable object class list (contains Class objects) */
	private List selectableObjectClassList;

	/** Selection dialog title */
	protected String title;

	/** Customizer class name */
	protected String customizerClassName;

	/** Customizer */
	protected ComponentSelectionEditorCustomizer customizer;

	/** Open system model */
	private boolean openSystemModel;

	/**
	 * Table of properties that must be fullfilled by the displayed objects.
	 * Maps property names to property values (objects)
	 */
	protected Map properties;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ComponentSelectionEditor()
	{
	}

	/**
	 * Creates the editor component of the property editor.
	 */
	public void createComponent()
	{
		super.createComponent();

		if (!readonly)
		{
			// Create the arrow button
			JButton btn = new BasicArrowButton(BasicArrowButton.SOUTH);
			btn.setBorder(new SimpleBorder(0, 2, 0, 2));
			btn.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					showBrowser();
				}
			});
			btn.setEnabled(true);

			// We do not want the button to be focusable, it can be clicked only
			btn.setFocusable(false);
			btn.setRequestFocusEnabled(false);

			// Make the button the same size as the text field
			Dimension size = new Dimension(btn.getMinimumSize());
			int h = textField.getHeight();
			size.height = h;
			btn.setMinimumSize(size);
			btn.setMaximumSize(size);
			btn.setPreferredSize(size);

			// Create the panel for the text field and the button and use it as the actual editor component
			component = new EditorPanel(textField, btn);

			// Parse the editor parameters
			parseParams();
		}
	}

	/**
	 * Highlights the content of the component.
	 * @param on
	 *		true	Turns the highlight on if the component has the focus<br>
	 *		false	Turns the highlight off
	 */
	public void highlight(boolean on)
	{
		boolean show = on && textField.hasFocus();
		if (show)
		{
			textField.setSelectionStart(0);
			int length = textField.getText().length();
			textField.setSelectionEnd(length);
		}
		else
		{
			textField.setSelectionStart(0);
			textField.setSelectionEnd(0);
		}

		// Show/hide the caret
		textField.getCaret().setVisible(show);
		textField.getCaret().setSelectionVisible(show);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				textField.repaint();
			}
		});
	}

	/**
	 * Parses the editor parameters.
	 */
	protected void parseParams()
	{
		EditorParameterParser parser = new EditorParameterParser(this);

		// First, check for the most common form.
		// Just one item type is given, which will be displayed and is also selectable
		itemType = parser.getString("type");

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

		supportedItemTypeList = parseList(parser, "supporteditemtype", false);
		selectableItemTypeList = parseList(parser, "selectableitemtype", false);
		supportedObjectClassList = parseList(parser, "supportedobjectclass", true);
		selectableObjectClassList = parseList(parser, "selectableobjectclass", true);

		title = parser.getString("title");
		if (title == null)
		{
			// TOLOCALIZE
			title = "Component Selection";
		}

		String s = parser.getString("opensystemmodel");
		openSystemModel = "true".equals(s);

		customizerClassName = parser.getString("customizer");
	}

	/**
	 * Parses a list of editor parameters.
	 *
	 * @param parser Parser for the editor parameters
	 * @param key Parameter name
	 * @param classNames
	 *		true	The parameter values are to be interpreted as class names.
	 *				The returned list contains Class objects.
	 *		false	The parameter values are strings that are directly inserted in the list
	 * @return The list containing classes or strings or null if no such parameter has been defined
	 */
	private List parseList(EditorParameterParser parser, String key, boolean classNames)
	{
		List list = null;

		for (Iterator it = parser.get(key); it.hasNext();)
		{
			String s = (String) it.next();

			if (list == null)
				list = new ArrayList();

			if (classNames)
			{
				try
				{
					Class cls = Class.forName(s);
					list.add(cls);
				}
				catch (ClassNotFoundException e)
				{
					ExceptionUtil.printTrace(e);
				}
			}
			else
			{
				list.add(s);
			}
		}

		return list;
	}

	//////////////////////////////////////////////////
	// @@ Listener overrides
	//////////////////////////////////////////////////

	/**
	 * Invoked when a key has been pressed.
	 *
	 * @param e Key event
	 */
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();

		switch (keyCode)
		{
		case KeyEvent.VK_ENTER:
		case KeyEvent.VK_SPACE:
		case KeyEvent.VK_DOWN:
			// CTRL/ALT ENTER/SPACE/DOWN shows item browser dialog
			if (e.isControlDown() || e.isAltDown() || e.isAltGraphDown())
			{
				showBrowser();
				e.consume();
				return;
			}
		}

		super.keyPressed(e);
	}

	//////////////////////////////////////////////////
	// @@ Item Selection Dialog
	//////////////////////////////////////////////////

	/**
	 * Shows the component browser.
	 */
	public void showBrowser()
	{
		// Try to get the current model from the edited object
		Object o = getObject();
		final Model model = (o instanceof ModelObject) ? ((ModelObject) o).getOwningModel() : null;

		// Initialize the item selection dialog
		final ItemSelectionDialog dlg = new ItemSelectionDialog(ApplicationUtil.getActiveWindow(), true);
		dlg.setTitle(title);

		// We may select a single object only
		dlg.setSelectionMode(ItemTree.SELECTION_SINGLE);
		dlg.setShowGroups(false);

		// Determine the types of displayed and selectable objects from the editor parameters
		String [] supportedItemTypes = null;
		String [] selectableItemTypes = null;
		Class [] supportedObjectClasses = null;
		Class [] selectableObjectClasses = null;

		if (itemType != null)
		{
			if (supportedItemTypeList == null)
			{
				supportedItemTypeList = new ArrayList();
				supportedItemTypeList.add(ItemTypes.MODEL);
				if (!itemType.equals(ItemTypes.MODEL))
				{
					supportedItemTypeList.add(itemType);
				}
			}

			if (selectableItemTypeList == null)
			{
				selectableItemTypeList = new ArrayList();
				selectableItemTypeList.add(itemType);
			}
		}

		if (supportedItemTypeList != null)
			supportedItemTypes = CollectionUtil.toStringArray(supportedItemTypeList);
		if (selectableItemTypeList != null)
			selectableItemTypes = CollectionUtil.toStringArray(selectableItemTypeList);
		if (supportedObjectClassList != null)
			supportedObjectClasses = (Class []) CollectionUtil.toArray(supportedObjectClassList, Class.class);
		if (selectableObjectClassList != null)
			selectableObjectClasses = (Class []) CollectionUtil.toArray(selectableObjectClassList, Class.class);

		dlg.setSupportedItemTypes(supportedItemTypes);
		dlg.setSelectableItemTypes(selectableItemTypes);
		dlg.setSupportedObjectClasses(supportedObjectClasses);
		dlg.setSelectableObjectClasses(selectableObjectClasses);

		// Determine the current object from the value of the property (i. e. the object name)
		String objectRef = (String) value;
		ModelObject object = null;

		if (objectRef != null && itemType != null)
		{
			try
			{
				if (model != null)
				{
					object = model.resolveObjectRef(objectRef, itemType);
				}
				else
				{
					ModelQualifier qualifier = new ModelQualifier(objectRef);
					qualifier.setItemType(itemType);
					object = ModelConnector.getInstance().getItemByQualifier(qualifier, false);
				}
			}
			catch (ModelException e)
			{
			}
		}

		ItemTreeState state = new ItemTreeState();

		Model currentModel = null;
		if (object != null)
		{
			// Open the current object
			currentModel = object.getOwningModel();
		}
		else
		{
			// Open the current model
			if (model != null)
			{
				currentModel = model;
			}

			if (openSystemModel)
			{
				// Open the System model
				currentModel = ModelConnector.getInstance().getModelByQualifier(CoreConstants.SYSTEM_MODEL_QUALIFIER);
			}
		}
		if (currentModel != null)
		{
			state.addExpandedQualifier(currentModel.getQualifier());
		}

		// Select the previously selected initial nodes if present
		if (object != null)
		{
			state.addSelectedQualifier(object.getQualifier());
		}
		else if (objectRef != null)
		{
			state.addSelectedQualifier(new ModelQualifier(objectRef));
		}
		else
		{
			if (currentModel != null)
			{
				state.addSelectedQualifier(currentModel.getQualifier());
			}
		}

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

		// Build the tree, expanding the first level and the currently selected item
		dlg.rebuildTree();
		dlg.expand(1);
		dlg.restoreState(state);

		dlg.addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent we)
			{
				List selection = dlg.getSelectedObjects();
				if (selection != null)
				{
					ModelObject selectedObject = (ModelObject) selection.get(0);

					if (customizer != null)
					{
						if (!customizer.dialogClosed(ComponentSelectionEditor.this, dlg, selectedObject))
							return;
					}

					String objectRef = null;
					if (model != null)
					{
						if (!(selectedObject instanceof ComplexTypeItem))
						{
							objectRef = model.determineObjectRef(selectedObject);
						}
					}

					if (objectRef == null)
					{
						objectRef = selectedObject.getQualifier().toString();
					}

					textField.setText(objectRef);
					textField.requestFocus();

					propertyChanged();
				}
				else
				{
					if (customizer != null)
					{
						customizer.dialogClosed(ComponentSelectionEditor.this, dlg, null);
					}
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

	private static class EditorPanel extends JPanel
	{
		JTextField textField;

		/**
		 * Constructor.
		 *
		 * @param textField Text field
		 * @param btn Popup button
		 */
		public EditorPanel(JTextField textField, JButton btn)
		{
			super(new BorderLayout());

			setFocusable(false);

			this.textField = textField;

			// 2 pixels distance to the button
			textField.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 2), textField.getBorder()));

			add(textField, BorderLayout.CENTER);
			add(btn, BorderLayout.EAST);
		}

		public void requestFocus()
		{
			// Delegate the focus to the text field
			textField.requestFocus();
		}
	}
}
