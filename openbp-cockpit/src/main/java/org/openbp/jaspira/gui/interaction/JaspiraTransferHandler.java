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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.openbp.swing.SwingUtil;

/**
 * Basic transfer handler for the Jaspira DnD mechanism.
 * Responsible for notifiying the corresponding {@link DragDropPane} of encountered DnD events.
 *
 * @author Stephan Moritz
 */
public class JaspiraTransferHandler extends TransferHandler
{
	/**
	 * Notifies the DnD pane that a drag/drop action has been encountered.
	 * Returns false since imports should be handled solely via the drag/drop pane
	 * @nowarn
	 */
	public boolean canImport(JComponent comp, DataFlavor [] transferFlavors)
	{
		return false;
	}

	/**
	 * Finalizes the Drag/Drop action.
	 * Does nothing.
	 * @nowarn
	 */
	protected void exportDone(JComponent source, Transferable data, int action)
	{
	}

	/**
	 * @see javax.swing.TransferHandler#exportAsDrag(JComponent, InputEvent, int)
	 */
	public void exportAsDrag(final JComponent comp, final InputEvent e, final int action)
	{
		Component master = SwingUtil.getGlassPane(comp);
		if (master instanceof DragDropPane)
		{
			master.setVisible(true);
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JaspiraTransferHandler.super.exportAsDrag(comp, e, action);
			}
		});
	}
}
