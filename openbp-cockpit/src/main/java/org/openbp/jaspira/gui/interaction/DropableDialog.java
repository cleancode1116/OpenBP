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
package org.openbp.jaspira.gui.interaction;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;

import javax.swing.JDialog;

/**
 * Dialog implementation that supports Jaspira drag and drop.
 *
 * @author Stephan Moritz
 */
public class DropableDialog extends JDialog
	implements DropPaneContainer
{
	/**
	 * Constructor.
	 */
	public DropableDialog()
	{
		super();
	}

	/**
	 * Constructor.
	 * @param owner Owning frame or null
	 */
	public DropableDialog(Frame owner)
	{
		super(owner);
	}

	/**
	 * Constructor.
	 * @param owner Owning frame or null
	 * @param modal
	 *		true	Creates a modal dialog.<br>
	 *		false	Creates a modeless dialog.
	 */
	public DropableDialog(Frame owner, boolean modal)
	{
		super(owner, modal);
	}

	/**
	 * Constructor.
	 * @param owner Owning frame or null
	 * @param title Title of the dialog or null
	 */
	public DropableDialog(Frame owner, String title)
	{
		super(owner, title);
	}

	/**
	 * Constructor.
	 * @param owner Owning frame or null
	 * @param title Title of the dialog or null
	 * @param modal
	 *		true	Creates a modal dialog.<br>
	 *		false	Creates a modeless dialog.
	 */
	public DropableDialog(Frame owner, String title, boolean modal)
	{
		super(owner, title, modal);
	}

	/**
	 * Constructor.
	 * @param owner Owning frame or null
	 * @param title Title of the dialog or null
	 * @param modal
	 *		true	Creates a modal dialog.<br>
	 *		false	Creates a modeless dialog.
	 * @param gc Graphics configuration or null - i.e. to use alternate desktop
	 */
	public DropableDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc)
	{
		super(owner, title, modal, gc);
	}

	/**
	 * Constructor.
	 * @param owner Owning dialog or null
	 */
	public DropableDialog(Dialog owner)
	{
		super(owner);
	}

	/**
	 * Constructor.
	 * @param owner Owning dialog or null
	 * @param modal
	 *		true	Creates a modal dialog.<br>
	 *		false	Creates a modeless dialog.
	 */
	public DropableDialog(Dialog owner, boolean modal)
	{
		super(owner, modal);
	}

	/**
	 * Constructor.
	 * @param owner Owning dialog or null
	 * @param title Title of the dialog or null
	 */
	public DropableDialog(Dialog owner, String title)
	{
		super(owner, title);
	}

	/**
	 * Constructor.
	 * @param owner Owning dialog or null
	 * @param title Title of the dialog or null
	 * @param modal
	 *		true	Creates a modal dialog.<br>
	 *		false	Creates a modeless dialog.
	 */
	public DropableDialog(Dialog owner, String title, boolean modal)
	{
		super(owner, title, modal);
	}

	/**
	 * Constructor.
	 * @param owner Owning dialog or null
	 * @param title Title of the dialog or null
	 * @param modal
	 *		true	Creates a modal dialog.<br>
	 *		false	Creates a modeless dialog.
	 * @param gc Graphics configuration or null - i.e. to use alternate desktop
	 */
	public DropableDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc)
	{
		super(owner, title, modal, gc);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ DropPaneContainer implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.interaction.DropPaneContainer#addDropClient(InteractionClient)
	 */
	public void addDropClient(InteractionClient client)
	{
		getDragDropPane().addDropClient(client);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DropPaneContainer#removeDropClient(InteractionClient)
	 */
	public void removeDropClient(InteractionClient client)
	{
		getDragDropPane().removeDropClient(client);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DropPaneContainer#getDragDropPane()
	 */
	public DragDropPane getDragDropPane()
	{
		Component glassPane = getGlassPane();

		if (!(glassPane instanceof DragDropPane))
		{
			return DragDropPane.installDragDropPane(this);
		}

		return (DragDropPane) glassPane;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DropPaneContainer#setDragDropPane(DragDropPane)
	 */
	public void setDragDropPane(DragDropPane pane)
	{
		setGlassPane(pane);
	}
}
