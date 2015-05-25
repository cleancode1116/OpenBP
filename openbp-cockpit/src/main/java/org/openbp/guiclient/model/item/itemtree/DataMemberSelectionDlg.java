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
package org.openbp.guiclient.model.item.itemtree;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

/**
 * Data member path selection dialog.
 *
 * @author Heiko Erhardt
 */
public class DataMemberSelectionDlg extends ItemSelectionDialog
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 * @param owner Owning frame. The dialog will be centered over the owner.
	 * @param modal
	 * true: Display as modal dialog.<br>
	 * false: Display as modeless dialog.
	 */
	public DataMemberSelectionDlg(Frame owner, boolean modal)
	{
		super(owner, modal);
	}

	//////////////////////////////////////////////////
	// @@ Overrides
	//////////////////////////////////////////////////

	/**
	 * Creates the item tree.
	 * Called by the constructor.
	 * Creates a regular {@link ItemTree} by default.
	 */
	protected void createTree()
	{
		tree = new DataMemberTree();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the selected member path.
	 * @nowarn
	 */
	public String getSelectedMemberPath()
	{
		List selectedObjects = getSelectedObjects();

		if (selectedObjects != null && selectedObjects.size() == 1)
		{
			String path = (String) selectedObjects.get(0);
			return path;
		}
		return null;
	}

	/**
	 * Sets the selected member path.
	 * @nowarn
	 */
	public void setSelectedMemberPath(String selectedMemberPath)
	{
		if (selectedMemberPath != null)
		{
			ArrayList selectedObjects = new ArrayList();
			selectedObjects.add(selectedMemberPath);
			setSelectedObjects(selectedObjects);
		}
		else
		{
			setSelectedObjects(null);
		}
	}
}
