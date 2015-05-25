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

import org.openbp.core.model.ModelObject;
import org.openbp.guiclient.model.item.itemtree.ItemSelectionDialog;

/**
 * Component selection editor customizer.
 * Allows to customize the display and operation of the item selection dialog of the editor.
 *
 * @author Heiko Erhardt
 */
public class ComponentSelectionEditorCustomizer
{
	/**
	 * Default constructor.
	 */
	public ComponentSelectionEditorCustomizer()
	{
	}

	/**
	 * Called before the dialog is being initialized.
	 * The method may perform additional initializations of the dialog, e. g. set the root object.
	 * The default implementation returns true.
	 *
	 * @param editor The editor
	 * @param dlg The dialog to initialize
	 * @return
	 *		true	To proceed
	 *		false	To cancel the dialog display
	 */
	public boolean initializeDialog(ComponentSelectionEditor editor, ItemSelectionDialog dlg)
	{
		return true;
	}

	/**
	 * Called after the dialog has been initialized.
	 * The method may perform additional initializations of the dialog, e. g. select the current object.
	 * The default implementation returns true.
	 *
	 * @param editor The editor
	 * @param dlg The initialized dialog
	 * @return
	 *		true	To proceed
	 *		false	To cancel the dialog display
	 */
	public boolean dialogInitialized(ComponentSelectionEditor editor, ItemSelectionDialog dlg)
	{
		return true;
	}

	/**
	 * Called after the dialog has been closed.
	 * The method may perform additional initializations of the dialog, e. g. select the current object.
	 * The default implementation returns true.
	 *
	 * @param editor The component selection editor that owns the customizer
	 * @param dlg The item selection dialog to display
	 * @param selectedObject The selected object (usually a {@link ModelObject}) or null if the cancel button was pressed
	 *
	 * @return
	 *		true	To accept the selected object
	 *		false	To abort the selection. The property the editor refers to stays unchanged.
	 */
	public boolean dialogClosed(ComponentSelectionEditor editor, ItemSelectionDialog dlg, Object selectedObject)
	{
		return true;
	}
}
