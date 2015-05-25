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
package org.openbp.jaspira.gui.clipboard;

import java.awt.datatransfer.Transferable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.jaspira.action.ActionMgr;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.plugin.PluginFocusMgr;
import org.openbp.jaspira.gui.plugin.VisiblePlugin;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;

/**
 * This plugin provides the link between the ClipboardMgr and the application.
 *
 * @author Stephan Moritz
 */
public class ClipboardPlugin extends AbstractPlugin
	implements ChangeListener
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public ClipboardPlugin()
	{
		super();
	}

	public String getResourceCollectionContainerName()
	{
		return "plugin.standard";
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#pluginInstalled()
	 */
	protected void pluginInstalled()
	{
		super.pluginInstalled();

		ClipboardMgr.getInstance().addClipboardListener(this);
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#pluginUninstalled()
	 */
	protected void pluginUninstalled()
	{
		super.pluginUninstalled();

		ClipboardMgr.getInstance().removeClipboardListener(this);
	}

	//////////////////////////////////////////////////
	// @@ ChangeListener implementation
	//////////////////////////////////////////////////

	/**
	 * Called when the contents of the clipboard have changed.
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		// This may cause a change of the 'paste' status
		fireEvent("global.clipboard.updatestatus");
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Event module
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "global.clipboard";
		}

		/**
		 * Action event handler: Cause the currently focused plugin to cut its current selection to the clipboard.
		 *
		 * @event global.clipboard.cut
		 * @param je Action event
		 * @return EVENT_CONSUMED if the cut action was sucessfully performed, EVENT_IGNORED otherwise
		 */
		public JaspiraEventHandlerCode cut(JaspiraActionEvent je)
		{
			VisiblePlugin target = PluginFocusMgr.getInstance().getFocusedPlugin();
			if (target != null && target.canCut())
			{
				ClipboardMgr.getInstance().addEntry(target.cut());

				return EVENT_CONSUMED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Action event handler: Cause the currently focused plugin to delete its current selection.
		 *
		 * @event global.clipboard.delete
		 * @param je Action event
		 * @return EVENT_CONSUMED if the delete action was sucessfully performed, EVENT_IGNORED otherwise
		 */
		public JaspiraEventHandlerCode delete(JaspiraActionEvent je)
		{
			VisiblePlugin target = PluginFocusMgr.getInstance().getFocusedPlugin();
			if (target != null && target.canDelete())
			{
				target.delete();

				return EVENT_CONSUMED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Action event handler: Cause the currently focused plugin to copy its current selection to the clipboard.
		 *
		 * @event global.clipboard.copy
		 * @param je Action event
		 * @return EVENT_CONSUMED if the copy action was sucessfully performed, EVENT_IGNORED otherwise
		 */
		public JaspiraEventHandlerCode copy(JaspiraActionEvent je)
		{
			VisiblePlugin target = PluginFocusMgr.getInstance().getFocusedPlugin();
			if (target != null && target.canCopy())
			{
				ClipboardMgr.getInstance().addEntry(target.copy());

				return EVENT_CONSUMED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Action event handler: Cause the currently focused plugin to paste the current clipboard entry.
		 *
		 * @event global.clipboard.paste
		 * @param je Action event
		 * @return EVENT_CONSUMED if the paste action was sucessfully performed, EVENT_IGNORED otherwise
		 */
		public JaspiraEventHandlerCode paste(JaspiraActionEvent je)
		{
			VisiblePlugin target = PluginFocusMgr.getInstance().getFocusedPlugin();
			if (target != null)
			{
				Transferable transferable = ClipboardMgr.getInstance().getCurrentEntry();
				if (transferable != null && target.canPaste(transferable))
				{
					target.paste(transferable);
				}

				return EVENT_CONSUMED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: Updates the status of the standard clipboard actions according to the
		 * current plugin and the current clipboard contents.
		 *
		 * @event global.clipboard.updatestatus
		 * @param je Event
		 * @return EVENT_HANDLED
		 */
		public JaspiraEventHandlerCode updatestatus(JaspiraEvent je)
		{
			boolean canCopy = false;
			boolean canCut = false;
			boolean canDelete = false;
			boolean canPaste = false;

			VisiblePlugin target = PluginFocusMgr.getInstance().getFocusedPlugin();
			if (target != null)
			{
				canCopy = target.canCopy();
				canCut = target.canCut();
				canDelete = target.canDelete();

				Transferable transferable = ClipboardMgr.getInstance().getCurrentEntry();
				if (transferable != null)
				{
					canPaste = target.canPaste(transferable);
				}
			}

			ActionMgr am = ActionMgr.getInstance();

			JaspiraAction copyAction = am.getAction("global.clipboard.copy");
			if (copyAction != null)
				copyAction.setEnabled(canCopy);

			JaspiraAction cutAction = am.getAction("global.clipboard.cut");
			if (cutAction != null)
				cutAction.setEnabled(canCut);

			JaspiraAction deleteAction = am.getAction("global.clipboard.delete");
			if (deleteAction != null)
				deleteAction.setEnabled(canDelete);

			JaspiraAction pasteAction = am.getAction("global.clipboard.paste");
			if (pasteAction != null)
				pasteAction.setEnabled(canPaste);

			return EVENT_IGNORED;
		}
	}
}
