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
package org.openbp.cockpit.modeler.skins;

import java.awt.event.ActionEvent;
import java.util.List;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.action.JaspiraToolbarCombo;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.option.SelectionOption;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.OptionModule;

/**
 * Invisible plugin that provides management of modeler skins.
 * It contains the Skin combo box that appears in the toolbar as well as the default skin options.
 *
 * @author Heiko Erhardt
 */
public class SkinPlugin extends AbstractPlugin
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Current modeler */
	private Modeler currentModeler;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SkinPlugin()
	{
	}

	public String getResourceCollectionContainerName()
	{
		return "plugin.modeler";
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#pluginInstalled()
	 */
	protected void pluginInstalled()
	{
		super.pluginInstalled();

		// Update button status
		fillSkinSelectionBox();
	}

	//////////////////////////////////////////////////
	// @@ Event module
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class SkinEvents extends EventModule
	{
		public String getName()
		{
			return "plugin.skin";
		}

		/**
		 * Selects the skin for the current process.
		 * This event will be produced by the skin combo box in the main toolbar.
		 *
		 * @event plugin.skin.selectskin
		 * @param jae Action event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode selectskin(JaspiraActionEvent jae)
		{
			ActionEvent ae = jae.getActionEvent();
			if (ae.getSource() instanceof JaspiraToolbarCombo)
			{
				JaspiraToolbarCombo combo = (JaspiraToolbarCombo) ae.getSource();

				// The skin is the selection item of the combo box
				Skin skin = (Skin) combo.getSelectedItem();

				if (currentModeler != null)
				{
					WorkspaceDrawingView view = currentModeler.getDrawingView();

					// We must clear the selection because all drawing objects will be cleared.
					view.clearSelection();

					ProcessDrawing drawing = (ProcessDrawing) view.drawing();

					// Save any geometry changes
					drawing.encodeGeometry();

					currentModeler.startUndo("Change Presentation Skin");

					// Completely reinitialize the drawing by re-setting the process
					ProcessItem process = drawing.getProcess();
					process.setSkinName(skin.getName());
					drawing.setProcess(process);

					currentModeler.endUndo();

					view.redraw();

					if (currentModeler.getPluginComponent().isShowing())
					{
						currentModeler.focusPlugin();
					}

					fireEvent("modeler.view.skinchanged", currentModeler);
				}
			}

			return EVENT_CONSUMED;
		}

		/**
		 * Shows the display name of the given skin in the skin combo box.
		 *
		 * @event plugin.skin.displayskin
		 * @eventparam Skin The current skin
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode displayskin(JaspiraEvent je)
		{
			JaspiraAction action = getAction("plugin.skin.selectskin");
			if (action != null)
			{
				Skin skin = (Skin) je.getObject();
				String text = skin != null ? skin.getDisplayText() : null;
				action.putValue(JaspiraToolbarCombo.PROPERTY_TEXT, text);
				action.setEnabled(text != null);
			}

			return EVENT_CONSUMED;
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
			Object o = je.getObject();

			if (o instanceof Modeler)
			{
				currentModeler = ((Modeler) o);

				updateSkinSelectionBox(currentModeler.getDrawing().getProcessSkin());

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
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
			Object o = je.getObject();

			if (o instanceof Modeler)
			{
				currentModeler = null;

				updateSkinSelectionBox(null);

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Option module
	//////////////////////////////////////////////////

	/**
	 * Option module containing all core Modeler options.
	 */
	public class SkinOptions extends OptionModule
	{
		/**
		 * Option that defines the operation mode of the data link autoconnector.
		 */
		public class SkinOption extends SelectionOption
		{
			public SkinOption()
			{
				super(getPluginResourceCollection(), "editor.skin.default", SkinMgr.getInstance().getDefaultSkin(), SkinMgr.getInstance().getSkinNames(), SkinMgr.getInstance().getSkins());
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Displays the given skin in the selection box.
	 *
	 * @param skin Skin or null
	 */
	protected void updateSkinSelectionBox(Skin skin)
	{
		JaspiraAction action = getAction("plugin.skin.selectskin");
		if (action != null)
		{
			String text = skin != null ? skin.getDisplayText() : null;
			action.putValue(JaspiraToolbarCombo.PROPERTY_TEXT, text);
			action.setEnabled(text != null);
		}
	}

	/**
	 * Fills the skin selection box with the available skins.
	 */
	protected void fillSkinSelectionBox()
	{
		JaspiraAction action = getAction("plugin.skin.selectskin");
		if (action != null)
		{
			action.clearValues();

			List skins = SkinMgr.getInstance().getSkinList();
			int n = skins.size();
			for (int i = 0; i < n; ++i)
			{
				Skin skin = (Skin) skins.get(i);
				String text = skin.getDisplayText();

				// Save the skin display name and the skin itself as selection text/selection value
				// in the property map of the action.
				// The toolbar combo will initialize itself from this.
				action.putValue(JaspiraToolbarCombo.PROPERTY_SELECTION_VALUE + i, skin);
				action.putValue(JaspiraToolbarCombo.PROPERTY_SELECTION_TEXT + i, text);
			}

			action.putValue(JaspiraToolbarCombo.PROPERTY_TEXT, null);
			action.setEnabled(false);
		}
	}
}
