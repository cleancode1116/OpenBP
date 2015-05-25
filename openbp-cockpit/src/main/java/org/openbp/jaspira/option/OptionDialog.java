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
package org.openbp.jaspira.option;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.JMsgBox;
import org.openbp.swing.components.tree.TreeUtil;
import org.openbp.swing.plaf.sky.ShadowBorder;

/**
 * The OptionDialog edits all options from a root node of the
 * Option tree. Only the OptionWidgets will be displayed. If
 * an OptionWidget registers a change in it's option representation
 * it sends a Event "option.changed". This Event will be received
 * normally by the OptionEventManager, which build a new Event and
 * send it for all components.
 *
 * This class implements the JaspiraListener and will be
 * registered for all options. It consumes all event and after apply
 * or ok the Editor send this Events to the OptionMgr.
 *
 * If the Editor get a chancel signal it will be delete the events.
 *
 * @author Jens Ferchland
 */
public class OptionDialog extends JDialog
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Default size of the editor */
	private static final Dimension SIZE = new Dimension(800, 600);

	/** Border to seperate the content from the scrollpane */
	private static final EmptyBorder BORDER_PAGE = new EmptyBorder(3, 3, 3, 3);

	/** Border to seperate the tree celles */
	private static final EmptyBorder BORDER_TREE = new EmptyBorder(2, 3, 2, 0);

	/** Resource for button texts */
	private ResourceCollection resourceCollection;

	/** Option section tree to the left */
	private JTree tree;

	/** Ok btn */
	private JButton okBtn;

	/** Cancel btn */
	private JButton cancelBtn;

	/** Apply btn */
	private JButton applyBtn;

	/** Widget scroll pane */
	private JScrollPane widgetScrollPane;

	/** Description scroll pane */
	private JScrollPane descriptionScrollPane;

	/**
	 * Table containing options that have changed during the editing of the {@link OptionDialog}
	 * Maps option names to {@link OptionWidget}
	 */
	private Map modifiedOptions;

	//////////////////////////////////////////////////
	// @@ Construction/Init
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param title Title of the dialog
	 */
	public OptionDialog(String title)
	{
		super(ApplicationUtil.getActiveWindow(), title, true);

		setSize(SIZE);
		setLocationRelativeTo(getOwner());

		// We use the message box resource for our button texts
		resourceCollection = ResourceCollectionMgr.getDefaultInstance().getResource(SwingUtil.RESOURCE_COMMON, JMsgBox.class);

		// Create scroll areas for the various sections of the dialog
		JScrollPane treeScrollPane = new JScrollPane();
		treeScrollPane.setBorder(new ShadowBorder());

		widgetScrollPane = new JScrollPane();
		widgetScrollPane.setBorder(new ShadowBorder());

		descriptionScrollPane = new JScrollPane();
		descriptionScrollPane.setBorder(new ShadowBorder());

		// Create the control buttons
		okBtn = createButton("buttons.ok.caption");
		okBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				optionDialogCommitted();
				optionDialogClosed();
				close();
			}
		});
		okBtn.setEnabled(false);

		cancelBtn = createButton("buttons.cancel.caption");
		cancelBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				optionDialogCancelled();
				close();
			}
		});

		applyBtn = createButton("buttons.apply.caption");
		applyBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				optionDialogCommitted();
			}
		});
		applyBtn.setEnabled(false);

		JPanel btnPanel = new JPanel();
		btnPanel.setBorder(new ShadowBorder());

		btnPanel.add(okBtn);
		btnPanel.add(applyBtn);
		btnPanel.add(cancelBtn);

		// Create the split panes
		JSplitPane horizontalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane verticalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		horizontalSplit.setTopComponent(verticalSplit);
		horizontalSplit.setBottomComponent(descriptionScrollPane);
		horizontalSplit.setDividerLocation(400);

		verticalSplit.setLeftComponent(treeScrollPane);
		verticalSplit.setRightComponent(widgetScrollPane);
		verticalSplit.setDividerLocation(200);

		// Add it all to the dialog
		JPanel contentPane = new JPanel(new BorderLayout());

		contentPane.add(btnPanel, BorderLayout.SOUTH);
		contentPane.add(horizontalSplit);

		// Create the tree that displays the options
		tree = new JTree(OptionMgr.getInstance().createOptionTree());
		tree.putClientProperty("JTree.lineStyle", "Angled");

		TreeSelectionListener selectionListener = new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				// Get the selected option (note that {@link Option} implements TreeNode)
				// This is usually a group option.
				Option option = (Option) e.getPath().getLastPathComponent();

				OptionWidget widget = option.getCachedOptionWidget();

				// Create the option page component
				setPageComponent(widget.getWidgetComponent());

				// Place the description component into the description scroll pane
				setDescription(widget.getDescriptionComponent());
			}
		};
		tree.addTreeSelectionListener(selectionListener);

		// Modify the default renderer to display own icons
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer()
		{
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
			{
				Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				if (c instanceof JLabel)
				{
					JLabel label = (JLabel) c;

					if (value instanceof Option)
					{
						// set the new icon
						Option opt = (Option) value;
						label.setIcon(opt.getIcon());
						label.setToolTipText(opt.getDescription());
					}
					else
					{
						// if the tree contains different TreeNodes, they get no icon.
						label.setIcon(null);
					}

					label.setBorder(BORDER_TREE);
				}

				return c;
			}
		};
		tree.setCellRenderer(renderer);

		treeScrollPane.getViewport().setView(tree);

		// Expands two levels
		TreeUtil.expandTreeLevels(tree, true, 2);
		tree.setRootVisible(false);

		//	Set the first element selected
		TreePath firstPath = tree.getPathForRow(0);
		if (firstPath != null)
		{
			tree.setSelectionPath(firstPath);
		}

		// ESC maps to cancel
		contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "exit");
		contentPane.getActionMap().put("exit", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				optionDialogCancelled();
				close();
			}
		});

		setContentPane(contentPane);

		OptionMgr.getInstance().fireEvent("optiondialog.displayed");
	}

	/**
	 * Creates a buttons from the resource.
	 *
	 * @param name Resource name of the button
	 */
	private JButton createButton(String name)
	{
		String text = resourceCollection.getRequiredString(name);

		int mnemonicpos = text.indexOf('_');
		if (mnemonicpos != -1)
		{
			// Cut the delimiter from the display name
			text = text.substring(0, mnemonicpos) + text.substring(mnemonicpos + 1);
		}

		JButton button = new JButton(text);
		if (mnemonicpos > -1)
		{
			button.setDisplayedMnemonicIndex(mnemonicpos);
			button.setMnemonic(text.charAt(mnemonicpos));
		}

		return button;
	}

	/**
	 * Performs all nessesary actions for closing the editor.
	 */
	protected void close()
	{
		dispose();
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Set the currently displayed option description.
	 * @param comp Component to be shown as description area at the bottom of the dialog
	 */
	public void setDescription(JComponent comp)
	{
		comp.setBorder(BORDER_PAGE);

		if (comp instanceof JTextComponent)
		{
			// If we have a text component set the caret position to 0,
			// so the text component will be displayed at the top of the scrollpane.
			((JTextComponent) comp).setCaretPosition(0);
		}

		descriptionScrollPane.getViewport().setView(comp);
	}

	/**
	 * Set the currently displayed option page.
	 * @param comp Component to be shown as option page on the right side of the dialog
	 */
	public void setPageComponent(JComponent comp)
	{
		comp.setBorder(BORDER_PAGE);
		widgetScrollPane.getViewport().setView(comp);
	}

	//////////////////////////////////////////////////
	// @@ Option widget change support
	//////////////////////////////////////////////////

	/**
	 * This method is called by an option widget to notify the option manager
	 * of an option change.
	 * The option manager will store the changed values in its table of modified options.
	 *
	 * @param widget Widget
	 */
	public void notifyOptionChange(OptionWidget widget)
	{
		if (modifiedOptions == null)
			modifiedOptions = new HashMap();
		modifiedOptions.put(widget, widget);

		applyBtn.setEnabled(true);
		okBtn.setEnabled(true);
	}

	/**
	 * Commits the option changes and and broadcasts option events for any modified options.
	 * The event name equals the name of the option.
	 * The event object is the option ({@link Option}) itself.<br>
	 * If any options have been changed, an optiondialog.commit event will also be broadcasted.<br>
	 * Called by the {@link OptionDialog}
	 */
	void optionDialogCommitted()
	{
		if (modifiedOptions == null)
		{
			// Nothing changed
			return;
		}

		OptionMgr optionMgr = OptionMgr.getInstance();

		for (Iterator it = modifiedOptions.values().iterator(); it.hasNext();)
		{
			OptionWidget widget = (OptionWidget) it.next();

			// Commit the changes of the option.
			// This will also broadcast an appropiate option event.
			Option option = widget.getOption();
			Object value = widget.getValue();
			optionMgr.setOption(option, value);
		}

		modifiedOptions = null;

		optionMgr.saveOptions();

		// Fire an event that options have changed
		optionMgr.fireEvent("optiondialog.commit");
	}

	/**
	 * Cancels any option modifications.
	 * Also broadcasts an optiondialog.commit event.<br>
	 * Called by the {@link OptionDialog}
	 */
	void optionDialogCancelled()
	{
		modifiedOptions = null;

		OptionMgr.getInstance().fireEvent("optiondialog.cancelled");
	}

	/**
	 * Notifies closing of the option dialog.
	 * Broadcasts an optiondialog.closed event.<br>
	 * Called by the {@link OptionDialog}
	 */
	void optionDialogClosed()
	{
		modifiedOptions = null;

		OptionMgr.getInstance().fireEvent("optiondialog.closed");
	}
}
