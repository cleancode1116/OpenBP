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
package org.openbp.cockpit.modeler.undo;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.option.IntegerOption;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.OptionModule;
import org.openbp.jaspira.undo.UndoMgr;

/**
 * This plugin reacts on the actionevents fired by the
 * undo und redo action and communicates with the {@link UndoMgr}
 *
 * @author Jens Ferchland
 */
public class ModelerUndoPlugin extends AbstractPlugin
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Currently active modeler */
	private Modeler currentModeler;

	public String getResourceCollectionContainerName()
	{
		return "plugin.modeler";
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "undo";
		}

		/**
		 * Returns the priority of the module.
		 * We are low-level priority.
		 * @nowarn
		 */
		public int getPriority()
		{
			return 101;
		}

		/**
		 * Undo the last action.
		 *
		 * @nowarn
		 */
		public JaspiraEventHandlerCode undo(JaspiraActionEvent je)
		{
			if (currentModeler != null)
			{
				UndoMgr undoMgr = currentModeler.getUndoMgr();
				if (undoMgr.canUndo())
				{
					undoMgr.undo();
					return EVENT_CONSUMED;
				}
			}
			return EVENT_IGNORED;
		}

		/**
		 * Redo the last undoable.
		 *
		 * @nowarn
		 */
		public JaspiraEventHandlerCode redo(JaspiraActionEvent je)
		{
			if (currentModeler != null)
			{
				UndoMgr undoMgr = currentModeler.getUndoMgr();
				if (undoMgr.canRedo())
				{
					undoMgr.redo();
					return EVENT_CONSUMED;
				}
			}
			return EVENT_IGNORED;
		}

		/**
		 * Event handler: A modeler view has become active.
		 *
		 * @event modeler.view.activated
		 * @eventobject Editor that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_activated(JaspiraEvent je)
		{
			currentModeler = (Modeler) je.getObject();

			UndoMgr undoMgr = currentModeler.getUndoMgr();
			undoMgr.updateActions();

			return EVENT_HANDLED;
		}

		/**
		 * Event handler: A modeler view has become inactive.
		 *
		 * @event modeler.view.closed
		 * @eventobject Editor that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_closed(JaspiraEvent je)
		{
			currentModeler = null;
			return EVENT_HANDLED;
		}
	}

	/**
	 * Undo option module.
	 */
	public class UndoOptions extends OptionModule
	{
		public class TransitionOption extends IntegerOption
		{
			public TransitionOption()
			{
				super(getPluginResourceCollection(), UndoMgr.TRANSITION_OPTION_NAME, Integer.valueOf(UndoMgr.DEFAULT_HISTORY_SIZE), 0, 100);
			}
		}
	}
}
