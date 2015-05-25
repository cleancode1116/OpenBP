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
package org.openbp.jaspira.option.widget;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;
import org.openbp.swing.components.popupfield.JPopupField;

/**
 * Option widget that supports the selection of a file/directory path.
 *
 * @author Jens Ferchland
 */
public class PathWidget extends OptionWidget
	implements DocumentListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Component that will be returned */
	private JPanel panel;

	/** Text field for the path */
	private JPopupField path;

	/** File name filter for the file selection dialog */
	private FileFilter filter;

	/** Directory/file selection mode */
	private boolean dirSelection;

	/** File chooser dialog */
	private JFileChooser fileChooser;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param filter File name filter for the file selection dialog
	 * @param dirSelection
	 *		true	Select directories<br>
	 *		false	Select regular files
	 */
	public PathWidget(Option option, FileFilter filter, boolean dirSelection)
	{
		super(option);

		this.filter = filter;
		this.dirSelection = dirSelection;

		// Prepare text field
		path = new JPopupField()
		{
			public boolean isPopupVisible()
			{
				return false;
			}

			public void setPopupVisible(boolean popupVisible)
			{
				if (popupVisible)
				{
					if (fileChooser == null)
					{
						// Prepare file chooser
						fileChooser = new JFileChooser();
						if (PathWidget.this.filter != null)
						{
							fileChooser.setFileFilter(PathWidget.this.filter);
							fileChooser.setFileHidingEnabled(true);
						}
						if (PathWidget.this.dirSelection)
						{
							fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						}
					}

					// Set the dir of the file chooser to the current path value
					String s = getText();
					if (s != null)
					{
						File f = new File(s);
						if (f.exists())
						{
							fileChooser.setCurrentDirectory(f);
						}
					}

					// Show the file chooser
					int retVal = fileChooser.showOpenDialog(null);

					// If a file has been choosen, set the new value.
					if (retVal == JFileChooser.APPROVE_OPTION)
					{
						setText(fileChooser.getSelectedFile().getPath());
					}
				}
			}
		};

		path.getTextField().getDocument().addDocumentListener(this);

		// Prepare component
		panel = new JPanel(new BorderLayout());
		JComponent heading = createHeading();
		if (heading != null)
		{
			panel.add(heading, BorderLayout.WEST);
		}
		panel.add(path);
	}

	//////////////////////////////////////////////////
	// @@ DocumentListener implementation
	//////////////////////////////////////////////////

	/**
	 * @see javax.swing.event.DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e)
	{
		notifyOptionMgrOfOptionChange();
	}

	/**
	 * @see javax.swing.event.DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		notifyOptionMgrOfOptionChange();
	}

	/**
	 * @see javax.swing.event.DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		notifyOptionMgrOfOptionChange();
	}

	//////////////////////////////////////////////////
	// @@ OptionWidget implementation
	//////////////////////////////////////////////////

	public Object getValue()
	{
		String s = path.getText();
		if (s != null)
		{
			try
			{
				s = new File(s).getCanonicalPath();
			}
			catch (IOException e)
			{
				// In case of file system access errors, we return the path as it is.
			}
		}
		return s;
	}

	public JComponent getWidgetComponent()
	{
		return panel;
	}

	public void setValue(Object o)
	{
		path.setText(o != null ? o.toString() : null);
	}
}
