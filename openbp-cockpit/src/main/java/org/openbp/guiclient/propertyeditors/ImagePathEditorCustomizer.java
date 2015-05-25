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

import java.io.File;

import javax.swing.JFileChooser;

import org.openbp.common.string.StringUtil;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.jaspira.propertybrowser.editor.standard.PathEditor;
import org.openbp.jaspira.propertybrowser.editor.standard.PathEditorCustomizer;

/**
 * Image path editor customizer.
 *
 * @author Heiko Erhardt
 */
public class ImagePathEditorCustomizer extends PathEditorCustomizer
{
	/**
	 * Default constructor.
	 */
	public ImagePathEditorCustomizer()
	{
	}

	/**
	 * Called before the chooser is being initialized.
	 * The method may perform additional initializations of the chooser, e. g. customize the file filter.
	 * The default implementation returns true.
	 *
	 * @param editor The editor
	 * @param chooser The file chooser to initialize
	 * @param pathRef Reference to the current path value. Contains exactly one string element, which can be null
	 * @return
	 *		true	To proceed
	 *		false	To cancel the chooser display
	 */
	public boolean initializeChooser(final PathEditor editor, final JFileChooser chooser, final String[] pathRef)
	{
		String modelPath = determineModelPath(editor);
		if (modelPath != null)
		{
			chooser.setCurrentDirectory(new File(modelPath));
		}

		String path = pathRef[0];
		if (path != null && modelPath != null)
		{
			path = StringUtil.absolutePathName(path);

			if (path.startsWith(modelPath + StringUtil.FOLDER_SEP))
			{
				pathRef[0] = path.substring(modelPath.length() + 1);
			}
			else
			{
				String tmp = modelPath + StringUtil.FOLDER_SEP + path;
				if (new File(tmp).exists())
				{
					pathRef[0] = tmp;
				}
			}
		}
		return true;
	}

	/**
	 * Called after the chooser has been initialized.
	 * The method may perform additional initializations of the chooser, e. g. set the current path.
	 * The default implementation returns true.
	 *
	 * @param editor The editor
	 * @param chooser The initialized file chooser
	 * @param pathRef Reference to the current path value. Contains exactly one string element, which can be null
	 * @return
	 *		true	To proceed
	 *		false	To cancel the chooser display
	 */
	public boolean chooserInitialized(final PathEditor editor, final JFileChooser chooser, final String[] pathRef)
	{
		return true;
	}

	/**
	 * Called after the chooser has been closed.
	 * The default implementation returns true.
	 *
	 * @param editor The component selection editor that owns the customizer
	 * @param chooser The file chooser to display
	 * @param pathRef Reference to the current path value. Contains exactly one string element, which can be null
	 * if the cancel button was pressed
	 *
	 * @return
	 *		true	To accept the selected object
	 *		false	To abort the selection. The property the editor refers to stays unchanged.
	 */
	public boolean chooserClosed(final PathEditor editor, final JFileChooser chooser, final String[] pathRef)
	{
		String path = pathRef[0];
		if (path != null)
		{
			String modelPath = determineModelPath(editor);
			path = StringUtil.absolutePathName(path);

			if (StringUtil.indexOfIgnoreCase(path, 0, modelPath + StringUtil.FOLDER_SEP) == 0)
			{
				pathRef[0] = path.substring(modelPath.length() + 1);
			}
		}
		return true;
	}

	private String determineModelPath(final PathEditor editor)
	{
		Object editedObject = editor.getObject();
		if (editedObject instanceof ModelObject)
		{
			Model model = ((ModelObject) editedObject).getOwningModel();
			if (model != null)
				return model.getModelPath();
		}
		return null;
	}
}
