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
package org.openbp.jaspira.propertybrowser.editor.standard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicArrowButton;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.common.string.shellmatcher.ShellMatcher;
import org.openbp.jaspira.propertybrowser.editor.EditorParameterParser;
import org.openbp.swing.plaf.sky.SimpleBorder;

/**
 * File name/path editor.
 * Property editor that allows input of a file or path name and also provides a button which will
 * activate a file chooser dialog.
 *
 * This editor accepts the following parameters:<br>
 * title: Dialog title<br>
 * type: Type of the dialog ("open"/"save" or a custom string that will appear on the "Accept" button)<br>
 * dirselection: "true" if the dialog should be used to select a directory, "false" for file selection<br>
 * filter: Selecteable file filters. The first file filter will be the active one. Each entry consists<br>
 * of a description and a Unix shell-compatibale pattern, e. g. "XML Files|*.xml".
 * customizer: Name of a customizer class (must extend {@link PathEditorCustomizer})
 *
 * @author Heiko Erhardt
 */
public class PathEditor extends StringEditor
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Selection dialog title */
	private String title;

	/** Selection dialog type ("open"/"save"/custom) */
	private String type;

	/** Filter list (contains {@link FileFilter} objects) */
	private List filterList;

	/** Directory/file selection mode */
	private boolean dirSelection;

	/** Customizer class name */
	private String customizerClassName;

	/** Customizer */
	private PathEditorCustomizer customizer;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public PathEditor()
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
	 * Parses the editor parameters specified in the property descriptor.
	 *
	 * @param parser Editor parameter parser or null if there are no parameters defined
	 */
	protected void parseParams(EditorParameterParser parser)
	{
		title = parser.getString("title");
		if (title == null)
		{
			// TOLOCALIZE
			title = "File Selection";
		}

		type = parser.getString("type");

		String s = parser.getString("dirselection");
		dirSelection = "true".equals(s);

		Iterator iter = parser.get("filter");
		while (iter.hasNext())
		{
			String param = (String) iter.next();

			String description = EditorParameterParser.determineDisplayValue(param);
			String pattern = EditorParameterParser.determineInternalValue(param);

			if (filterList == null)
				filterList = new ArrayList();

			filterList.add(new PatternFileFilter(pattern, description));
		}

		customizerClassName = parser.getString("customizer");
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
			// CTRL/ALT ENTER/SPACE/DOWN shows file chooser
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
	// @@ File chooser
	//////////////////////////////////////////////////

	/**
	 * Shows the component browser.
	 */
	public void showBrowser()
	{
		// File chooser dialog
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(title);

		// Don't display hidden files
		fileChooser.setFileHidingEnabled(true);

		if (dirSelection)
		{
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		else
		{
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}

		if (type != null)
		{
			if (type.equals("open"))
			{
				fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
			}
			else if (type.equals("save"))
			{
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			}
			else
			{
				fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
				fileChooser.setApproveButtonText(type);
			}
		}
		else
		{
			fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		}

		if (filterList != null)
		{
			int n = filterList.size();
			for (int i = 0; i < n; ++i)
			{
				FileFilter filter = (FileFilter) filterList.get(i);
				fileChooser.addChoosableFileFilter(filter);
				if (i == 0)
				{
					fileChooser.setFileFilter(filter);
				}
			}
		}

		// Instantiate the customizer class
		if (customizer == null && customizerClassName != null)
		{
			try
			{
				customizer = (PathEditorCustomizer) ReflectUtil.instantiate(customizerClassName, PathEditorCustomizer.class, "component selection editor customizer class");
			}
			catch (Exception e)
			{
				ExceptionUtil.printTrace(e);
			}
		}

		String [] pathRef = new String [1];
		pathRef [0] = (String) value;

		if (customizer != null)
		{
			if (!customizer.initializeChooser(this, fileChooser, pathRef))
				return;
		}

		// Determine the current object from the value of the property (i. e. the object name)
		if (pathRef [0] != null)
		{
			// Set the dir of the file chooser to the current path value
			File f = new File(pathRef [0]);
			if (f.exists())
			{
				fileChooser.setSelectedFile(f);
			}

			if (!f.isDirectory())
			{
				f = f.getParentFile();
			}
			fileChooser.setCurrentDirectory(f);
		}

		if (customizer != null)
		{
			if (!customizer.chooserInitialized(this, fileChooser, pathRef))
				return;
		}

		// Show the file chooser
		int retVal = fileChooser.showOpenDialog(null);

		// If a file has been choosen, set the new value.
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			pathRef [0] = fileChooser.getSelectedFile().getPath();
		}

		if (customizer != null)
		{
			if (!customizer.chooserClosed(PathEditor.this, fileChooser, pathRef))
				return;
		}

		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			textField.setText(pathRef [0]);
			textField.requestFocus();

			propertyChanged();
		}
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

	/**
	 * File filter for shell-compatible patterns.
	 */
	private static class PatternFileFilter extends FileFilter
	{
		/** Pattern */
		private String pattern;

		/** Description */
		private String description;

		/** Matcher */
		private ShellMatcher matcher;

		/**
		 * Constructor.
		 *
		 * @param pattern Pattern
		 * @param description Description
		 */
		public PatternFileFilter(String pattern, String description)
		{
			this.pattern = pattern;
			this.description = description;
			matcher = new ShellMatcher(pattern);
			matcher.setIgnoreCase(true);
		}

		/**
		 * Determines wether the file should be accepted by this filter.
		 *
		 * @param f File to check
		 * @return
		 *		true	The filter accepts the file
		 *		false	The filter declines the file
		 */
		public boolean accept(File f)
		{
			if (f.isDirectory())
			{
				// Always show directories
				return true;
			}

			String name = f.getName();
			if (matcher != null && !matcher.match(name))
				return false;

			return true;
		}

		/**
		 * Gets the description.
		 * @nowarn
		 */
		public String getDescription()
		{
			if (description != null)
			{
				return description;
			}
			return pattern;
		}
	}
}
