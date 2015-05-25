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

import javax.swing.JFileChooser;

/**
 * Component selection editor customizer.
 * Allows to customize the display and operation of the file chooser.
 *
 * @author Heiko Erhardt
 */
public class PathEditorCustomizer
{
	/**
	 * Default constructor.
	 */
	public PathEditorCustomizer()
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
	public boolean initializeChooser(PathEditor editor, JFileChooser chooser, String [] pathRef)
	{
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
	public boolean chooserInitialized(PathEditor editor, JFileChooser chooser, String [] pathRef)
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
	public boolean chooserClosed(PathEditor editor, JFileChooser chooser, String [] pathRef)
	{
		return true;
	}
}
