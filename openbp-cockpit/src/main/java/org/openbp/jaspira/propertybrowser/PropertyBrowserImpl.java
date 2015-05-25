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
package org.openbp.jaspira.propertybrowser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.openbp.common.CollectionUtil;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.description.DescriptionObject;
import org.openbp.common.generic.propertybrowser.CollectionDescriptor;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.common.string.TextUtil;
import org.openbp.common.util.CopyUtil;
import org.openbp.jaspira.JaspiraConstants;
import org.openbp.jaspira.action.ActionMgr;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraPopupMenu;
import org.openbp.jaspira.action.keys.KeySequence;
import org.openbp.jaspira.gui.StdIcons;
import org.openbp.jaspira.gui.clipboard.ClipboardMgr;
import org.openbp.jaspira.gui.interaction.BasicTransferable;
import org.openbp.jaspira.propertybrowser.editor.PropertyEditor;
import org.openbp.jaspira.propertybrowser.nodes.AbstractNode;
import org.openbp.jaspira.propertybrowser.nodes.CollectionNode;
import org.openbp.jaspira.propertybrowser.nodes.ObjectNode;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.JMsgBox;
import org.openbp.swing.components.treetable.DefaultTableCellEditor;
import org.openbp.swing.components.treetable.DefaultTableCellRenderer;
import org.openbp.swing.components.treetable.DefaultTreeCellRenderer;
import org.openbp.swing.components.treetable.JTreeTable;
import org.openbp.swing.components.treetable.TreeTableNode;
import org.openbp.swing.components.treetable.resize.ColumnSizeConstraint;
import org.openbp.swing.components.treetable.resize.RatioConstraint;

/**
 * This provides a JTreeTable implementation of the property browser interface.
 * The {@link PropertyBrowserModel} provides the model for the {@link JTreeTable} which internally builds the tree
 * and table structure according to the object passed to this property browser by the {@link #setObject(Object, boolean)}
 * method. The property browser implementation will retrieve an object descriptor for the
 * class of the passed object and initialize its user interface accordingly.
 *
 * @author Erich Lauterbach
 */
public class PropertyBrowserImpl extends JTreeTable
	implements PropertyBrowser, MouseListener, TreeSelectionListener, FocusListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Object */
	private Object object;

	/** Modified object */
	private Object modifiedObject;

	/**
	 * Optional object the 'object' is based upon.
	 * This is used for name uniqueness checks by the model object validator
	 * if the object passed to the property browser has been cloned.
	 * Since the property browser clones the given object once more (into the modifiedObject)
	 * we loose the reference to the original.
	 * originalObject will refer the non-cloned original.
	 */
	private Object originalObject;

	/** Flag that determines if the object has just been created */
	private boolean isObjectNew;

	/** Array of property names that should be displayed or null for all */
	private String [] visibleMembers;

	/** Object change listener list */
	private List objectChangeListenerList;

	/** Save stratergy used to save the edited object */
	private SaveStrategy saveStrategy;

	/** List containing all property change listeners. */
	private EventListenerList editListenerList;

	/** Image for the root element */
	private MultiIcon rootIcon;

	/** Resource containing the column headers */
	private ResourceCollection resourceCollection;

	/** Save after modifying property */
	private boolean saveImmediately;

	/** Show tooltips */
	private boolean showTooltips;

	/** Cell editor for property cells */
	private static PropertyCellEditor propertyCellEditor;

	/** Table renderer */
	private static PropertyCellRenderer propertyCellRenderer;

	/** Actions used by the property browser.
	 * I. e. collection manipulation actions (add, remove, move up/down).
	 * Indexed by the constants below.
	 */
	private JaspiraAction [] actions;

	/** Number of property browser actions */
	private static final int NACTIONS = 7;

	/** Array index of the 'Add element' action */
	private static final int ACTION_INDEX_ADD = 0;

	/** Array index of the 'Copy element' action */
	private static final int ACTION_INDEX_COPY = 1;

	/** Array index of the 'Cut element' action */
	private static final int ACTION_INDEX_CUT = 2;

	/** Array index of the 'Paste element' action */
	private static final int ACTION_INDEX_PASTE = 3;

	/** Array index of the 'Remove element' action */
	private static final int ACTION_INDEX_REMOVE = 4;

	/** Array index of the 'Move element up' action */
	private static final int ACTION_INDEX_MOVEUP = 5;

	/** Array index of the 'Move element down' action */
	private static final int ACTION_INDEX_MOVEDOWN = 6;

	//////////////////////////////////////////////////
	// @@ Status variables
	//////////////////////////////////////////////////

	/** The last property editor to have gotten focus and edited something */
	private PropertyEditor currentPE;

	/** The currently selected {@link AbstractNode} for which the context menu is shown */
	private AbstractNode currentNode;

	/** Flag used to determine if the original object has changed */
	private boolean objectModified;

	/**
	 * Flag used to indicate that a requested save operation should not be performed.
	 * This is the case if the validation for this object has just failed and the error needs to be
	 * corrected by the user before trying another approach. This prevents endless error message loops.<br>
	 * Another case is if context menus or popup boxes need to be displayed that will cause the object
	 * editor to loose its focus.
	 */
	private boolean saveDisabled;

	/** Flag to inidicate that we are in the process of performing a save operation */
	private boolean performingSave;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default Constructor.
	 *
	 * @param saveStrategy Strategy class used to save the object
	 * @param resourceCollection Resource containing the column headers
	 */
	public PropertyBrowserImpl(SaveStrategy saveStrategy, ResourceCollection resourceCollection)
	{
		super(new PropertyBrowserModel());

		if (resourceCollection == null)
		{
			resourceCollection = ResourceCollectionMgr.getDefaultInstance().getResource(JaspiraConstants.RESOURCE_JASPIRA, getClass());
		}
		this.resourceCollection = resourceCollection;

		// We don't need no table header
		setTableHeader(null);

		PropertyBrowserModel model = (PropertyBrowserModel) getModel();
		model.setPropertyBrowser(this);
		model.setResourceCollection(resourceCollection);

		this.saveStrategy = saveStrategy;

		// Set renderer and editor for property cells
		if (propertyCellRenderer == null)
			propertyCellRenderer = new PropertyCellRenderer();
		setDefaultRenderer(PropertyEditor.class, propertyCellRenderer);

		if (propertyCellEditor == null)
			propertyCellEditor = new PropertyCellEditor();
		setDefaultEditor(PropertyEditor.class, propertyCellEditor);

		getTree().setCellRenderer(new TreeCellRenderer(this));
		getTree().getSelectionModel().addTreeSelectionListener(this);

		addFocusListener(this);
		addMouseListener(this);

		setRootVisible(false);
		setRowSelectionAllowed(false);

		// Col. 2 = 2 x col 1
		// Editor column min. 200 pixel
		ColumnSizeConstraint csc = new RatioConstraint(new int [] { 1, 2 }, true);
		csc.setMinimumOfColumn(200, 1, this);
		setColumnSizeConstraint(csc);

		createActions();

		processSelection(null);
	}

	/**
	 * Creates the actions that modify collection elements.
	 */
	private void createActions()
	{
		actions = new JaspiraAction [NACTIONS];

		actions [ACTION_INDEX_ADD] = new JaspiraAction(resourceCollection, "propertybrowser.addelement")
		{
			public void actionPerformed(ActionEvent ae)
			{
				// Add a new node to the collection
				final AbstractNode newPos = addNewNode();
				if (newPos != null)
				{
					// Set the focus to the edit column of the next row (usually the 'name' field of the new node)
					// Adding a new node caused a model change, so select after all model change events have been processed.
					// (Don't ask, it works...)
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							SwingUtilities.invokeLater(new Runnable()
							{
								public void run()
								{
									selectNode(newPos, 1, 1);
								}
							});
						}
					});
				}
			}
		};

		actions [ACTION_INDEX_COPY] = new JaspiraAction(resourceCollection, "propertybrowser.copyelement")
		{
			public void actionPerformed(ActionEvent ae)
			{
				// Copy the node contents to the clipboard
				copyNode();

				// We have copied something, make sure the paste action reflects this.
				updateActionState();
			}
		};

		actions [ACTION_INDEX_CUT] = new JaspiraAction(resourceCollection, "propertybrowser.cutelement")
		{
			public void actionPerformed(ActionEvent ae)
			{
				// Copy the node contents to the clipboard
				copyNode();

				// Remove the node from the collection
				AbstractNode newPos = removeNode();
				if (newPos != null)
				{
					// Set the focus to the edit column of the next row (usually the 'name' field of the new node)
					selectNode(newPos, 0, 0);
				}

				// We have copied something, make sure the paste action reflects this.
				updateActionState();
			}
		};

		actions [ACTION_INDEX_PASTE] = new JaspiraAction(resourceCollection, "propertybrowser.pasteelement")
		{
			public void actionPerformed(ActionEvent ae)
			{
				// Paste an element from the clipboard to a new node of the collection
				final AbstractNode newPos = pasteNode();
				if (newPos != null)
				{
					// Set the focus to the edit column of the next row (usually the 'name' field of the new node)
					// Adding a new node caused a model change, so select after all model change events have been processed.
					// (Don't ask, it works...)
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							SwingUtilities.invokeLater(new Runnable()
							{
								public void run()
								{
									selectNode(newPos, 1, 1);
								}
							});
						}
					});
				}
			}
		};

		actions [ACTION_INDEX_REMOVE] = new JaspiraAction(resourceCollection, "propertybrowser.removeelement")
		{
			public void actionPerformed(ActionEvent ae)
			{
				// Remove the node from the collection
				AbstractNode newPos = removeNode();
				if (newPos != null)
				{
					// Set the focus to the edit column of the next row (usually the 'name' field of the new node)
					selectNode(newPos, 0, 0);
				}
			}
		};

		actions [ACTION_INDEX_MOVEUP] = new JaspiraAction(resourceCollection, "propertybrowser.moveelementup")
		{
			public void actionPerformed(ActionEvent ae)
			{
				// Move the node one position up in the collection
				moveNodeUp();
			}
		};

		actions [ACTION_INDEX_MOVEDOWN] = new JaspiraAction(resourceCollection, "propertybrowser.moveelementdown")
		{
			public void actionPerformed(ActionEvent ae)
			{
				// Move the node one position down in the collection
				moveNodeDown();
			}
		};

		setupActionKeys();
	}

	/**
	 * Sets up the keyboard shortcuts of the actions that modify collection elements.
	 */
	protected void setupActionKeys()
	{
		// Create new input and action maps, using the original ones as parent
		InputMap focusInputMap = new InputMap();
		focusInputMap.setParent(SwingUtilities.getUIInputMap(this, WHEN_FOCUSED));

		InputMap focusAncestorInputMap = new InputMap();
		focusAncestorInputMap.setParent(SwingUtilities.getUIInputMap(this, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));

		ActionMap actionMap = new ActionMap();
		actionMap.setParent(SwingUtilities.getUIActionMap(this));

		// Enter the key bindings of our actions in the maps
		for (int i = 0; i < actions.length; ++i)
		{
			JaspiraAction action = actions [i];

			KeySequence [] sequences = action.getKeySequences();
			if (sequences != null)
			{
				for (int is = 0; is < sequences.length; ++is)
				{
					KeySequence sequence = sequences [is];
					if (sequence.length() == 1)
					{
						String actionId = "OE" + i;

						// Register the action id for the provided key code
						KeyStroke ks = sequence.getKeyAt(0);
						focusInputMap.put(ks, actionId);
						focusAncestorInputMap.put(ks, actionId);

						// Determine the command
						actionMap.put(actionId, action);
					}
				}
			}
		}

		// Install the new input and action maps
		SwingUtilities.replaceUIInputMap(this, WHEN_FOCUSED, focusInputMap);
		SwingUtilities.replaceUIInputMap(this, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, focusAncestorInputMap);
		SwingUtilities.replaceUIActionMap(this, actionMap);
	}

	//////////////////////////////////////////////////
	// @@ PropertyBrowser implementation
	//////////////////////////////////////////////////

	/**
	 * Sets the object to be displayed/edited.
	 *
	 * @param object The object to edit or null
	 * @param isObjectNew Flag that determines if the object has just been created
	 * @throws XMLDriverException If no object descriptor could be found for the specified object
	 * @throws CloneNotSupportedException If the object is not cloneable
	 */
	public void setObject(Object object, boolean isObjectNew)
		throws XMLDriverException, CloneNotSupportedException
	{
		setObject(object, null, isObjectNew, null, null);
	}

	/**
	 * Sets the object to be displayed/edited.
	 *
	 * @param object The object to edit or null
	 * @param isObjectNew Flag that determines if the object has just been created
	 * @param rootNode Root node of the property browser tree or null if the property browser
	 * should create an appropriate property browser tree based on the class of the object
	 * @throws XMLDriverException If no object descriptor could be found for the specified object
	 * @throws CloneNotSupportedException If the object is not cloneable
	 */
	public void setObject(Object object, boolean isObjectNew, ObjectNode rootNode)
		throws XMLDriverException, CloneNotSupportedException
	{
		setObject(object, null, isObjectNew, null, rootNode);
	}

	/**
	 * Sets the object to be displayed/edited.
	 * The class of the object will be used to determine the object descriptor from.
	 *
	 * @param object Object to edit
	 * @param isObjectNew Flag that determines if the object has just been created
	 * @param rootIcon Image of the root node
	 *
	 * @throws XMLDriverException If no object descriptor could be found for the specified object
	 */
	public void setObject(Object object, boolean isObjectNew, MultiIcon rootIcon)
		throws XMLDriverException, CloneNotSupportedException
	{
		setObject(object, null, isObjectNew, rootIcon);
	}

	/**
	 * Sets the object to be displayed/edited.
	 * The class of the object will be used to determine the object descriptor from.
	 *
	 * @param object Object to edit
	 * @param modifiedObject The modified Object
	 * @param isObjectNew Flag that determines if the object has just been created
	 * @param rootIcon Image of the root node
	 *
	 * @throws XMLDriverException If no object descriptor could be found for the specified object
	 */
	public void setObject(Object object, Object modifiedObject, boolean isObjectNew, MultiIcon rootIcon)
		throws XMLDriverException, CloneNotSupportedException
	{
		setObject(object, modifiedObject, isObjectNew, rootIcon, null);
	}

	/**
	 * Sets the object to be displayed/edited.
	 * The class of the object will be used to determine the object descriptor from.
	 *
	 * @param object Object to edit
	 * @param modifiedObject The modified Object
	 * @param isObjectNew Flag that determines if the object has just been created
	 * @param rootIcon Image of the root node
	 * @param rootNode Root node of the property browser tree or null if the property browser
	 * should create an appropriate property browser tree based on the class of the object
	 *
	 * @throws XMLDriverException If no object descriptor could be found for the specified object
	 */
	public void setObject(Object object, Object modifiedObject, boolean isObjectNew, MultiIcon rootIcon, ObjectNode rootNode)
		throws XMLDriverException, CloneNotSupportedException
	{
		if (!saveObject())
			return;

		this.object = object;
		if (object != null && modifiedObject == null)
		{
			// Copy the original object to the modified object
			// Creates a new Object as clone of the original object
			modifiedObject = CopyUtil.copyObject(object, Copyable.COPY_FIRST_LEVEL, null);
		}

		this.modifiedObject = modifiedObject;
		this.isObjectNew = isObjectNew;

		setRootIcon(rootIcon);

		PropertyBrowserModel model = (PropertyBrowserModel) getModel();
		model.setVisibleMembers(visibleMembers);
		model.setObject(modifiedObject, rootNode);

		currentCol = currentRow = -1;

		currentNode = null;
		processSelection(null);
	}

	/**
	 * Gets the object that is currently edited.
	 *
	 * @return The object that is currently edited or null
	 */
	public Object getObject()
	{
		return object;
	}

	/**
	 * Gets the clone of the edited object that has been modified.
	 * The clone is usually a first-level clone (see the {@link Copyable} class).
	 *
	 * @return The modified object or null if nothing has been changed
	 */
	public Object getModifiedObject()
	{
		if (objectModified)
			return modifiedObject;
		return null;
	}

	/**
	 * Gets the optional object the 'object' is based upon.
	 * This is used for name uniqueness checks by the model object validator
	 * if the object passed to the property browser has been cloned.
	 * Since the property browser clones the given object once more (into the modifiedObject)
	 * we loose the reference to the original.
	 * originalObject will refer the non-cloned original.
	 *
	 * @return The modified object or null if there is no original
	 */
	public Object getOriginalObject()
	{
		return originalObject;
	}

	/**
	 * Sets the optional object the 'object' is based upon.
	 * This is used for name uniqueness checks by the model object validator
	 * if the object passed to the property browser has been cloned.
	 * Since the property browser clones the given object once more (into the modifiedObject)
	 * we loose the reference to the original.
	 * originalObject will refer the non-cloned original.
	 *
	 * @param originalObject The modified object or null if there is no original
	 */
	public void setOriginalObject(Object originalObject)
	{
		this.originalObject = originalObject;
	}

	/**
	 * Gets the flag that determines if the object has just been created.
	 * @nowarn
	 */
	public boolean isObjectNew()
	{
		return isObjectNew;
	}

	/**
	 * Sets the flag that determines if the object has just been created.
	 * @nowarn
	 */
	public void setObjectNew(boolean isObjectNew)
	{
		this.isObjectNew = isObjectNew;
	}

	/**
	 * Gets the save stratergy used to save the edited object.
	 * @nowarn
	 */
	public SaveStrategy getSaveStrategy()
	{
		return saveStrategy;
	}

	/**
	 * Sets the save stratergy used to save the edited object.
	 * @nowarn
	 */
	public void setSaveStrategy(SaveStrategy saveStrategy)
	{
		this.saveStrategy = saveStrategy;
	}

	/**
	 * Gets the flag used to determine if the original object has changed.
	 * @nowarn
	 */
	public boolean isObjectModified()
	{
		return objectModified;
	}

	/**
	 * Sets the flag used to determine if the original object has changed.
	 * @nowarn
	 */
	public void setObjectModified(boolean objectModified)
	{
		this.objectModified = objectModified;

		// We should be able to save the object again if necessary
		saveDisabled = false;

		// Enable the save button
		if (!saveImmediately)
		{
			JaspiraAction action = ActionMgr.getInstance().getAction("standard.file.save");
			if (action != null)
				action.setEnabled(objectModified);
		}
	}

	/**
	 * Temporarily disables save operations.
	 *
	 * This can be useful if context menus or popup boxes need to be displayed that will cause the object
	 * editor to loose its focus and thus force it to save the edited object.<br>
	 * The flag will be automatically reset if the property browser gains the focus again.
	 */
	public void disableSave()
	{
		saveDisabled = true;
	}

	/**
	 * Enables save operations.
	 *
	 * Call this method to explicitely enable save operations after you have called {@link #disableSave}.
	 */
	public void enableSave()
	{
		saveDisabled = false;
	}

	/**
	 * Save the object.
	 *
	 * @return
	 *	true	The object has been successfully saved or the change was discarded<br>
	 *	false	The object has not been saved, return
	 */
	public boolean saveObject()
	{
		if (performingSave || saveDisabled)
			return false;

		try
		{
			performingSave = true;

			if (currentPE != null)
			{
				// Save the current property before saving the object
				if (!currentPE.saveProperty())
					return false;
			}

			if (getModifiedObject() == null)
				return true;

			if (!objectModified || saveStrategy == null)
			{
				return true;
			}

			// Validate the edited object using the validator of the root object descriptor node
			ObjectNode root = (ObjectNode) ((PropertyBrowserModel) getModel()).getRoot();
			if (root != null)
			{
				ObjectValidator validator = root.getValidator();
				if (validator != null)
				{
					if (!validator.validateObject(modifiedObject, this))
					{
						// We should re-validate the object until changes have been made
						saveDisabled = true;
						return false;
					}
				}
			}

			// Execute the save strategy to save the object
			boolean saved = saveStrategy.executeSave(this);
			if (saved)
			{
				setObjectModified(false);
				return true;
			}

			return false;
		}
		finally
		{
			performingSave = false;
		}
	}

	/**
	 * Resets the contents of the property browser by re-copying the content of the original object to the modified object.
	 * Also resets the object modified flag.
	 */
	public boolean reset()
	{
		try
		{
			Object object = getObject();

			setObjectModified(false);

			setObject(object, null, isObjectNew, null);

			fireObjectChanged();
			return true;
		}
		catch (XMLDriverException e)
		{
			// Should not occurr
			ExceptionUtil.printTrace(e);
		}
		catch (CloneNotSupportedException e)
		{
			ExceptionUtil.printTrace(e);
		}

		return false;
	}

	/**
	 * Adds a new node to the object collection the current node refers to
	 * @return The newly added node or null if no node has been added
	 */
	public AbstractNode addNewNode()
	{
		if (currentNode == null)
			return null;

		CollectionNode cdn = currentNode.getAssociatedCollectionNode();
		if (cdn == null)
		{
			// Node not associated with a collection, no popup menu
			return null;
		}

		// Get the node we refer to
		AbstractNode objectNode = cdn == currentNode ? currentNode : currentNode.getObjectNode();

		// Open the path to the collection
		TreePath path = getPathByNode(cdn);
		expandPath(path);

		// Add a new member
		AbstractNode newNode = cdn.addNewNodeAfter(objectNode, null);

		if (newNode != null)
		{
			// Open the new list member
			path = getPathByNode(newNode);
			expandPath(path);

			if (saveImmediately)
			{
				fireObjectModified();
				saveObject();
			}
		}

		firePropertyBrowserEvent(new PropertyBrowserEvent(PropertyBrowserEvent.ELEMENT_CREATED, this, newNode));

		return newNode;
	}

	/**
	 * Removes the current node from the object collection the node refers to.
	 * @return The node that should become the current node after removal of the current node<br>
	 * This is the node that succeeds the current node in the collection or - if not present -
	 * the node that preceeds the current node or - if the list is empty - the collection node itself
	 * (or null if not removal has taken place).
	 */
	public AbstractNode removeNode()
	{
		if (currentNode == null)
			return null;

		AbstractNode newCurrentNode = currentNode;

		if (currentNode instanceof CollectionNode)
		{
			// Remove all elements of the collection
			String msg = resourceCollection.getRequiredString("propertybrowser.removeall");
			int result = JMsgBox.show(null, msg, JMsgBox.TYPE_YESNO | JMsgBox.DEFAULT_NO);
			if (result != JMsgBox.TYPE_YES)
			{
				return null;
			}

			((CollectionNode) currentNode).removeAllNodes();

			firePropertyBrowserEvent(new PropertyBrowserEvent(PropertyBrowserEvent.ELEMENT_DELETED, this, currentNode));
		}
		else
		{
			CollectionNode cdn = currentNode.getAssociatedCollectionNode();
			if (cdn == null)
			{
				// Node not associated with a collection, no popup menu
				return null;
			}

			// Get the node we refer to
			AbstractNode objectNode = currentNode.getObjectNode();

			// Make the collection node remove the element
			newCurrentNode = cdn.removeNode(objectNode);

			firePropertyBrowserEvent(new PropertyBrowserEvent(PropertyBrowserEvent.ELEMENT_DELETED, this, objectNode));
		}

		if (saveImmediately)
		{
			fireObjectModified();
			saveObject();
		}

		return newCurrentNode;
	}

	/**
	 * Copys the value of the current node to the clipboard.
	 */
	public void copyNode()
	{
		if (!(currentNode instanceof ObjectNode))
			return;

		ObjectNode on = (ObjectNode) currentNode;
		Object o = on.getObject();

		BasicTransferable transferable = new BasicTransferable(o);
		ClipboardMgr.getInstance().addEntry(transferable);

		firePropertyBrowserEvent(new PropertyBrowserEvent(PropertyBrowserEvent.ELEMENT_COPIED, this, on));
	}

	/**
	 * Pastes a node value from the clipboard into a new node of the object collection the current node refers to.
	 * @return The newly added node or null if no node has been added
	 */
	public AbstractNode pasteNode()
	{
		// Check if there is something on the clipboard
		Transferable transferable = ClipboardMgr.getInstance().getCurrentEntry();
		if (transferable == null)
		{
			// No clipboard data present
			return null;
		}

		CollectionNode cdn = currentNode.getAssociatedCollectionNode();

		if (cdn == null)
		{
			// Node not associated with a collection, no popup menu
			return null;
		}

		CollectionDescriptor cd = cdn.getCollectionDescriptor();
		Class elementClass = cd.getSafeTypeClass();
		DataFlavor elementFlavor = new DataFlavor(elementClass, elementClass.getName());

		// Try to get the type of element we need from the clipboard
		Object o = null;
		try
		{
			o = transferable.getTransferData(elementFlavor);
			o = CopyUtil.copyObject(o, Copyable.COPY_DEEP, null);
		}
		catch (UnsupportedFlavorException e)
		{
			// Do nothing
			return null;
		}
		catch (IOException e)
		{
			// Do nothing
			return null;
		}
		catch (CloneNotSupportedException e)
		{
			// Do nothing
			return null;
		}

		// Get the node we refer to
		AbstractNode objectNode = cdn == currentNode ? currentNode : currentNode.getObjectNode();

		// Open the path to the collection
		TreePath path = getPathByNode(cdn);
		expandPath(path);

		// Add a new member
		AbstractNode newNode = cdn.addNewNodeAfter(objectNode, o);

		firePropertyBrowserEvent(new PropertyBrowserEvent(PropertyBrowserEvent.ELEMENT_PASTED, this, newNode));

		if (newNode != null)
		{
			// Open the new list member
			path = getPathByNode(newNode);
			expandPath(path);

			if (saveImmediately)
			{
				fireObjectModified();
				saveObject();
			}
		}

		return newNode;
	}

	/**
	 * Move the current node one position up in the collection
	 */
	protected void moveNodeUp()
	{
		if (currentNode == null)
			return;

		CollectionNode cdn = currentNode.getAssociatedCollectionNode();
		if (cdn == null)
		{
			// Node not associated with a collection, no popup menu
			return;
		}

		AbstractNode objectNode = currentNode.getObjectNode();
		cdn.moveNodeUp(objectNode);

		if (saveImmediately)
		{
			fireObjectModified();
			saveObject();
		}

		selectCell(currentRow, currentCol);
	}

	/**
	 * Move the current node one position down in the collection
	 */
	protected void moveNodeDown()
	{
		if (currentNode == null)
			return;

		CollectionNode cdn = currentNode.getAssociatedCollectionNode();
		if (cdn == null)
		{
			// Node not associated with a collection, no popup menu
			return;
		}

		AbstractNode objectNode = currentNode.getObjectNode();
		cdn.moveNodeDown(objectNode);

		if (saveImmediately)
		{
			fireObjectModified();
			saveObject();
		}

		selectCell(currentRow, currentCol);
	}

	/**
	 * Selects the given node.
	 *
	 * @param node Node to select
	 * @param rowOffset Number of rows to add to the row of the node
	 * @param column Column to place the cursor in:<br>
	 * 0: The tree column<br>
	 * 1: The editable cell column<br>
	 * -1: The current column
	 */
	public void selectNode(AbstractNode node, int rowOffset, int column)
	{
		TreePath path = getPathByNode(node);
		if (path != null)
		{
			int row = getTree().getRowForPath(path) + rowOffset;
			if (column < 0)
				column = getSelectedColumn();
			selectCell(row, column);
		}
	}

	/**
	 * Override in order to prevent redraw problems when scrolling.
	 * If some keyboard action causes the property browser to scroll in it's enclosing scroll pane,
	 * the highlighted selection in string property editors is not being removed properly from
	 * the field that looses the focus. In order to prevent this, we explicitely unhighlight the
	 * current property editor before changing the selection in the table.
	 *
	 * @see org.openbp.swing.components.treetable.JTreeTable#changeSelection(int, int, boolean, boolean)
	 */
	public void changeSelection(int newRow, int newCol, boolean toggle, boolean extend)
	{
		if (currentPE != null)
		{
			// Show the current property editor component in an unfocused state
			currentPE.resetComponentDisplay();
		}

		super.changeSelection(newRow, newCol, toggle, extend);

		AbstractNode node = (AbstractNode) getNodeByRow(newRow);
		firePropertyBrowserEvent(new PropertyBrowserEvent(PropertyBrowserEvent.SELECTION_CHANGED, this, node));
	}

	/**
	 * Processses a key event.
	 *
	 * @param e Event
	 */
	public void handleKeyEvent(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			handleEscape();
			e.consume();
			return;
		}

		super.handleKeyEvent(e);
	}

	/**
	 * Handles a command.
	 *
	 * @param command Command to process
	 */
	protected void processCommand(int command)
	{
		if (command == CMD_ESC)
		{
			handleEscape();
			return;
		}

		super.processCommand(command);
	}

	/**
	 * Handles the escape key.
	 * Resets the object if it is modified and closes the dialog (cancel) if the property browser is embedded in a dialog.
	 */
	protected void handleEscape()
	{
		if (isObjectModified())
		{
			reset();
		}

		Dialog dialog = SwingUtil.getDialog(this);
		if (dialog != null)
		{
			// Post a 'window closing' event
			dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
		}
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the resource containing the column headers.
	 * @nowarn
	 */
	public ResourceCollection getPropertyBrowserResource()
	{
		return resourceCollection;
	}

	/**
	 * Set the image for the root element.
	 * @nowarn
	 */
	private void setRootIcon(MultiIcon icon)
	{
		this.rootIcon = icon;
	}

	/**
	 * Sets the propertybrowser readonly.
	 * @nowarn
	 */
	public void setReadOnly(boolean readOnly)
	{
		if (readOnly)
		{
			setDefaultEditor(PropertyEditor.class, null);
		}
		else
		{
			setDefaultEditor(PropertyEditor.class, propertyCellEditor);
		}
	}

	/**
	 * Gets wheter the object is read only.
	 * @nowarn
	 */
	public boolean isReadOnly()
	{
		return getDefaultEditor(PropertyEditor.class) == null;
	}

	/**
	 * Gets the save after modifying property flag.
	 * @nowarn
	 */
	public boolean isSaveImmediately()
	{
		return saveImmediately;
	}

	/**
	 * Sets the save after modifying property flag.
	 * @nowarn
	 */
	public void setSaveImmediately(boolean saveImmediately)
	{
		this.saveImmediately = saveImmediately;
	}

	/**
	 * Gets the show tooltips.
	 * @nowarn
	 */
	public boolean isShowTooltips()
	{
		return showTooltips;
	}

	/**
	 * Sets the show tooltips.
	 * @nowarn
	 */
	public void setShowTooltips(boolean showTooltips)
	{
		this.showTooltips = showTooltips;
	}

	/**
	 * Gets the array of property names that should be displayed or null for all.
	 * This can be used to limit the number of properties that are displayed for complex objects.
	 * @nowarn
	 */
	public String [] getVisibleMembers()
	{
		return visibleMembers;
	}

	/**
	 * Sets the array of property names that should be displayed or null for all.
	 * This can be used to limit the number of properties that are displayed for complex objects.
	 * @nowarn
	 */
	public void setVisibleMembers(String [] visibleMembers)
	{
		this.visibleMembers = visibleMembers;
	}

	/**
	 * Gets the 'Add element' action
	 * @nowarn
	 */
	public JaspiraAction getAddAction()
	{
		return actions [ACTION_INDEX_ADD];
	}

	/**
	 * Gets the 'Copy element' action
	 * @nowarn
	 */
	public JaspiraAction getCopyAction()
	{
		return actions [ACTION_INDEX_COPY];
	}

	/**
	 * Gets the 'Cut element' action
	 * @nowarn
	 */
	public JaspiraAction getCutAction()
	{
		return actions [ACTION_INDEX_CUT];
	}

	/**
	 * Gets the 'Paste element' action
	 * @nowarn
	 */
	public JaspiraAction getPasteAction()
	{
		return actions [ACTION_INDEX_PASTE];
	}

	/**
	 * Gets the 'Remove element' action
	 * @nowarn
	 */
	public JaspiraAction getRemoveAction()
	{
		return actions [ACTION_INDEX_REMOVE];
	}

	/**
	 * Gets the 'Move element up' action
	 * @nowarn
	 */
	public JaspiraAction getMoveUpAction()
	{
		return actions [ACTION_INDEX_MOVEUP];
	}

	/**
	 * Gets the 'Move element down' action
	 * @nowarn
	 */
	public JaspiraAction getMoveDownAction()
	{
		return actions [ACTION_INDEX_MOVEDOWN];
	}

	/**
	 * Gets the TreePath for a specified {@link TreeTableNode} in the visible tree table.
	 *
	 * @param treeNode The {@link TreeTableNode} for which the TreePath is desired
	 * @return The TreePath, if the node exists in the visible tree, else null
	 */
	public TreePath getPathByNode(TreeNode treeNode)
	{
		ArrayList list = new ArrayList();

		for (AbstractNode node = (AbstractNode) treeNode; node != null; node = (AbstractNode) node.getParent())
		{
			// Insert at start
			list.add(0, node);
		}

		// Construct a TreePath from the elements
		Object [] elements = CollectionUtil.toArray(list, Object.class);
		return new TreePath(elements);
	}

	//////////////////////////////////////////////////
	// @@ TreeSelectionListener and FocusListener implementation
	//////////////////////////////////////////////////

	public void valueChanged(TreeSelectionEvent event)
	{
		processSelection(event.getPath());
	}

	/**
	 * Called when the object gets the focus.
	 * @nowarn
	 */
	public void focusGained(FocusEvent e)
	{
		if (getTree().getSelectionCount() == 0)
		{
			selectCell(0, 0);
		}

		if (getTree().getSelectionCount() == 0)
		{
			processSelection(null);
		}
		else
		{
			processSelection(getTree().getSelectionPath());
		}
	}

	/**
	 * Called when the object loses the focus.
	 * @nowarn
	 */
	public void focusLost(FocusEvent e)
	{
		// Do nothing
	}

	/**
	 * Updates the property browser according to the selected row.
	 *
	 * @param path Path of the selected node
	 */
	protected void processSelection(TreePath path)
	{
		currentNode = path != null ? (AbstractNode) path.getLastPathComponent() : null;

		updateActionState();
	}

	/**
	 * Updates the enabled state of the property browser's actions according to the current node.
	 */
	protected void updateActionState()
	{
		CollectionNode cdn = currentNode != null ? currentNode.getAssociatedCollectionNode() : null;

		// Enable/disable actions

		// Add enabled if the collection descriptor allows it
		actions [ACTION_INDEX_ADD].setEnabled(cdn != null && cdn.allowsNodeAddition());

		// Copy enabled if we have an object selected
		actions [ACTION_INDEX_COPY].setEnabled(currentNode instanceof ObjectNode);

		// Cut enabled if we have an object selected and we can remove elements
		actions [ACTION_INDEX_CUT].setEnabled(cdn != null && cdn.allowsNodeRemoval() && cdn.getChildCount() > 0 && currentNode instanceof ObjectNode);

		// Paste enabled if we have an object selected and the current clipboard content
		// is compatible to the object class we manage in the currently selected collection.
		boolean canPaste = false;
		if (cdn != null && cdn.allowsNodeAddition())
		{
			Transferable transferable = ClipboardMgr.getInstance().getCurrentEntry();
			if (transferable != null)
			{
				CollectionDescriptor cd = cdn.getCollectionDescriptor();
				Class elementClass = cd.getSafeTypeClass();
				DataFlavor elementFlavor = new DataFlavor(elementClass, elementClass.getName());
				canPaste = transferable.isDataFlavorSupported(elementFlavor);
			}
		}
		actions [ACTION_INDEX_PASTE].setEnabled(canPaste);

		// Remove enabled if the collection descriptor allows it and the current node is either the collection
		// descriptor node or one of its direct successors
		actions [ACTION_INDEX_REMOVE].setEnabled(cdn != null && cdn.allowsNodeRemoval() && cdn.getChildCount() > 0 && (currentNode instanceof CollectionNode || currentNode instanceof ObjectNode));

		boolean canMoveUp = false;
		boolean canMoveDown = false;

		if (cdn != null && cdn != currentNode && cdn.allowsNodeReordering())
		{
			AbstractNode objectNode = currentNode.getObjectNode();

			int index = cdn.getChildIndex(objectNode);
			int n = cdn.getChildCount();

			canMoveUp = n > 1 && index > 0;
			canMoveDown = n > 1 && index < n - 1;
		}

		actions [ACTION_INDEX_MOVEUP].setEnabled(canMoveUp);
		actions [ACTION_INDEX_MOVEDOWN].setEnabled(canMoveDown);
	}

	//////////////////////////////////////////////////
	// @@ MouseListener implementation
	//////////////////////////////////////////////////

	/**
	 * Mouse listener to detect when the user has clicked to open
	 * the context menu and when the user started dragging a node.
	 */
	public void mouseClicked(MouseEvent event)
	{
		if (event.getButton() == MouseEvent.BUTTON3)
		{
			final Point point = event.getPoint();

			currentNode = (AbstractNode) getNodeByPoint(point);
			if (currentNode == null)
				return;

			selectNode(currentNode, 0, -1);
			if (!PropertyBrowserImpl.this.hasFocus())
			{
				PropertyBrowserImpl.this.requestFocus();
			}

			// Wrap the context menu display using invokeLater in order to
			// give the property browser to get the focus
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					JPopupMenu contextMenu = createContextMenu();
					if (contextMenu != null)
					{
						contextMenu.show(PropertyBrowserImpl.this, point.x, point.y);
					}
				}
			});
		}
	}

	public void mousePressed(MouseEvent event)
	{
		/*
		 if (event.getButton () == MouseEvent.BUTTON1)
		 {
		 startPoint = event.getPoint ();
		 dragging = true;
		 }
		 */
	}

	public void mouseReleased(MouseEvent event)
	{
		/*
		 if (event.getButton () == MouseEvent.BUTTON1)
		 {
		 endPoint = event.getPoint ();
		 dragging = false;
		 }
		 */
	}

	/**
	 * Invoked when the mouse enters a component.
	 */
	public void mouseEntered(MouseEvent e)
	{
	}

	/**
	 * Invoked when the mouse exits a component.
	 */
	public void mouseExited(MouseEvent e)
	{
	}

	protected JPopupMenu createContextMenu()
	{
		boolean actionEnabled = false;

		for (int i = 0; i < actions.length; ++i)
		{
			if (actions [i].isEnabled())
			{
				actionEnabled = true;
				break;
			}
		}

		if (!actionEnabled)
			return null;

		JaspiraPopupMenu menu = new JaspiraPopupMenu();
		boolean needSeparator = false;

		for (int i = 0; i < actions.length; ++i)
		{
			if (actions [i].isEnabled())
			{
				if (needSeparator)
				{
					menu.add(new JSeparator());
					needSeparator = false;
				}

				menu.add(actions [i].toMenuItem());
			}

			if (i == ACTION_INDEX_ADD || i == ACTION_INDEX_PASTE || i == ACTION_INDEX_REMOVE)
			{
				needSeparator = true;
			}
		}

		return menu;
	}

	//////////////////////////////////////////////////
	// @@ PropertyBrowserListener implementation
	//////////////////////////////////////////////////

	/**
	 * Adds a property browser event listener.
	 *
	 * @param listener Listener to add
	 */
	public void addPropertyBrowserListener(PropertyBrowserListener listener)
	{
		if (editListenerList == null)
		{
			editListenerList = new EventListenerList();
		}
		else
		{
			if (SwingUtil.containsListener(editListenerList, PropertyBrowserListener.class, listener))
				return;
		}
		editListenerList.add(PropertyBrowserListener.class, listener);
	}

	/**
	 * Removes a property browser event listener.
	 *
	 * @param listener Listener to remove
	 */
	public void removePropertyBrowserListener(PropertyBrowserListener listener)
	{
		if (editListenerList != null)
		{
			editListenerList.remove(PropertyBrowserListener.class, listener);
		}
	}

	/**
	 * Notifies all registered property browser event listeners of the given edit event.
	 *
	 * @param e Event
	 */
	public void firePropertyBrowserEvent(PropertyBrowserEvent e)
	{
		// Perform default handling first
		defaultHandlePropertyBrowserEvent(e);

		if (editListenerList != null)
		{
			// Guaranteed to return a non-null array
			Object [] listeners = editListenerList.getListenerList();

			// Process the listeners last to first, notifying
			// those that are interested in this event
			for (int i = listeners.length - 2; i >= 0; i -= 2)
			{
				if (listeners [i] == PropertyBrowserListener.class)
				{
					((PropertyBrowserListener) listeners [i + 1]).handlePropertyBrowserEvent(e);
				}
			}
		}
	}

	/**
	 * Default property browser event handler.
	 *
	 * @param event Event
	 */
	public void defaultHandlePropertyBrowserEvent(PropertyBrowserEvent event)
	{
		PropertyEditor pe = event.propertyEditor;

		switch (event.eventType)
		{
		case PropertyBrowserEvent.PROPERTY_CHANGED:
			// TODO Feature 4: Later Sometime should go a request for an object lock here...
			fireObjectModified();
			break;

		case PropertyBrowserEvent.FOCUS_GAINED:
			currentPE = pe;

			// We should be able to save the object again if necessary
			saveDisabled = false;

			break;

		case PropertyBrowserEvent.FOCUS_LOST:
			// Save the property value when the focus gets lost

			// First, let the property editor's validator check the value and save the property to the object
			if (!pe.saveProperty())
				return;

			currentPE = null;

			if (objectModified)
			{
				if (saveImmediately)
				{
					// Save the object when loosing the focus
					// Don't do this right now as this may cause problems e. g. when
					// changing focus to a BooleanEditor (the state change of the BooleanEditor)
					// will not be considered in the saved object. Instead, postpone until focus
					// processing has been finished.
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							saveObject();
						}
					});
				}
			}
			break;
		}
	}

	//////////////////////////////////////////////////
	// @@ Object change listeners
	//////////////////////////////////////////////////

	/**
	 * Notifies this property browser that changes have occurred on the object that this editor
	 * is responsible for.
	 */
	public void fireObjectModified()
	{
		setObjectModified(true);

		fireObjectChanged();
	}

	/**
	 * Notifies this property browser that changes have occurred on the object that this editor
	 * is responsible for.
	 */
	public void fireObjectChanged()
	{
		if (objectChangeListenerList != null)
		{
			Iterator listeners = objectChangeListenerList.iterator();
			while (listeners.hasNext())
			{
				ObjectChangeListener listener = (ObjectChangeListener) listeners.next();
				listener.objectChanged(getObject(), getModifiedObject());
			}
		}
	}

	/**
	 * Adds an object change listener.
	 *
	 * @param listener The object change listener to add
	 */
	public void addObjectChangeListener(ObjectChangeListener listener)
	{
		if (objectChangeListenerList == null)
			objectChangeListenerList = new ArrayList();

		objectChangeListenerList.add(listener);
	}

	/**
	 * Removes an object change listener.
	 *
	 * @param listener The object change listener to remove
	 */
	public void removeObjectChangeListener(ObjectChangeListener listener)
	{
		if (objectChangeListenerList == null)
			return;

		objectChangeListenerList.remove(listener);
	}

	//////////////////////////////////////////////////
	// @@ Cell renderers and editors
	//////////////////////////////////////////////////

	/** Regular table background */
	private static final Color tableBackground = UIManager.getColor("Table.background");

	/** Regular table foreground */
	private static final Color tableForeground = UIManager.getColor("Table.foreground");

	/** Selected table background */
	private static final Color tableSelectionBackground = UIManager.getColor("Table.selectionBackground");

	/** Selected table foreground */
	private static final Color tableSelectionForeground = UIManager.getColor("Table.selectionForeground");

	/**
	 * Cell editor for the item type properties.
	 */
	public static class PropertyCellEditor extends DefaultTableCellEditor
	{
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			if (value instanceof PropertyEditor)
			{
				PropertyEditor pe = (PropertyEditor) value;

				setValue(value);

				JComponent component = pe.getPropertyComponent();
				if (component != null)
				{
					// Configure the component for usage by the property browser
					((PropertyBrowser) table).configureSubComponent(component);

					if (component instanceof JCheckBox)
					{
						component.setBackground(tableSelectionBackground);
						component.setForeground(tableSelectionForeground);
					}
				}
				return component;
			}

			return null;
		}
	}

	/**
	 * Cell renderer for the property editor column.
	 */
	public static class PropertyCellRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if (!(value instanceof PropertyEditor))
			{
				// Hack: Never show focus or selection in text components
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}

			PropertyEditor pe = (PropertyEditor) value;

			JComponent component = pe.getPropertyComponent();
			if (component == null)
			{
				return super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
			}

			// Configure the component for usage by the property browser
			((PropertyBrowser) table).configureSubComponent(component);

			int defaultHeight = table.getRowHeight();

			Dimension ps = component.getPreferredSize();
			if (ps.height < defaultHeight)
			{
				ps.height = defaultHeight;
				component.setPreferredSize(ps);
			}

			if (component instanceof JCheckBox)
			{
				if (isSelected || hasFocus)
				{
					component.setBackground(tableSelectionBackground);
					component.setForeground(tableSelectionForeground);
				}
				else
				{
					component.setBackground(tableBackground);
					component.setForeground(tableForeground);
				}
			}

			return component;
		}
	}

	/**
	 * Renderer for the tree cell.
	 */
	public class TreeCellRenderer extends DefaultTreeCellRenderer
	{
		/**
		 * Constructor.
		 * @param treeTable Tree table that owns the renderer
		 */
		public TreeCellRenderer(JTreeTable treeTable)
		{
			super(treeTable);
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			if (c instanceof JLabel)
			{
				MultiIcon icon = null;

				if (row == 0 && tree.isRootVisible())
				{
					icon = rootIcon;
				}
				else if (!leaf)
				{
					icon = expanded ? StdIcons.openFolderIcon : StdIcons.closedFolderIcon;
				}

				((JLabel) c).setIcon(icon);

				if (value instanceof AbstractNode)
				{
					value = ((AbstractNode) value).getColumnValue(0);
				}

				if (isShowTooltips())
				{
					if (value instanceof DescriptionObject)
					{
						String tooltip = ((DescriptionObject) value).getDescription();
						if (tooltip != null)
						{
							((JLabel) c).setToolTipText(TextUtil.convertToHTML(new String [] { tooltip }, false, -1, 50));
						}
					}
				}
			}

			return c;
		}
	}
}
