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
package org.openbp.jaspira.action;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;

import org.openbp.common.ExceptionUtil;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * Event fired by {@link JaspiraAction} objects.
 *
 * @author Stephan Moritz
 */
public class JaspiraActionEvent extends JaspiraEvent
	implements Transferable
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Properties
	/////////////////////////////////////////////////////////////////////////

	/** Optional transferable this event refers to */
	private Transferable transferable;

	/** Underylying original action event */
	private ActionEvent actionEvent;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param source Plugin the event originated from
	 * @param eventName Name of the event
	 * @param transferable Optional transferable this event refers to
	 * @param level Event level (see {@link JaspiraEvent#getLevel})
	 * @param actionEvent Underylying original action event
	 */
	public JaspiraActionEvent(Plugin source, String eventName, Transferable transferable, int level, ActionEvent actionEvent)
	{
		super(source, eventName, TYPE_FLOOD, level);
		this.transferable = transferable;
		this.actionEvent = actionEvent;
	}

	/**
	 * Constructor.
	 *
	 * @param source Plugin the event originated from
	 * @param eventName Name of the event
	 * @param actionEvent Underylying original action event
	 * @param level Event level (see {@link JaspiraEvent#getLevel})
	 */
	public JaspiraActionEvent(Plugin source, String eventName, ActionEvent actionEvent, int level)
	{
		super(source, eventName, TYPE_FLOOD, level);
		this.actionEvent = actionEvent;
	}

	/**
	 * Constructor.
	 *
	 * @param source Plugin the event originated from
	 * @param eventName Name of the event
	 * @param level Event level (see {@link JaspiraEvent#getLevel})
	 */
	public JaspiraActionEvent(Plugin source, String eventName, int level)
	{
		super(source, eventName, TYPE_FLOOD, level);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Property access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the underylying original action event.
	 * @nowarn
	 */
	public ActionEvent getActionEvent()
	{
		return actionEvent;
	}

	/**
	 * Returns true if this event carries a transferable.
	 * @nowarn
	 */
	public boolean hasTransferable()
	{
		return transferable != null;
	}

	//////////////////////////////////////////////////
	// @@ Transferable implementation
	//////////////////////////////////////////////////

	/**
	 * @see java.awt.datatransfer.Transferable#getTransferData(DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException
	{
		return transferable != null ? transferable.getTransferData(flavor) : null;
	}

	/**
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor [] getTransferDataFlavors()
	{
		return transferable != null ? transferable.getTransferDataFlavors() : new DataFlavor [0];
	}

	/**
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return transferable != null ? transferable.isDataFlavorSupported(flavor) : false;
	}

	/**
	 * Gets the transfer data in the desired format (convenience method that supresses exceptions).
	 * You should call this method only if have previously checked if the flavor is supported
	 * by calling {@link #isDataFlavorSupported}.
	 *
	 * @param flavor Flavor to get
	 * @return The desired object or null if the flavor is not supported or an i/o error has occurred.
	 * In the latter case, the method prints a stack trace to stderr.
	 */
	public Object getSafeTransferData(DataFlavor flavor)
	{
		try
		{
			return transferable.getTransferData(flavor);
		}
		catch (UnsupportedFlavorException e)
		{
			// Silently ignore
		}
		catch (IOException e)
		{
			ExceptionUtil.printTrace(e);
		}
		return null;
	}
}
