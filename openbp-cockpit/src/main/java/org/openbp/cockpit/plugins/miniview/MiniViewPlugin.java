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
package org.openbp.cockpit.plugins.miniview;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.Box;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.option.IntegerOption;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionMgr;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.OptionModule;
import org.openbp.swing.plaf.sky.SimpleBorder;

/**
 * This Plugin displays a whole drawing view.
 *
 * @author Jens Ferchland
 */
public class MiniViewPlugin extends AbstractVisiblePlugin
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Maximum number of simultaneously displayed mini views */
	private int maxMiniViews = 2;

	/** Container for all mini views */
	private Box content;

	/** List of active miniviews (contains {@link MiniView} objects) */
	private LinkedList miniViews;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	public String getResourceCollectionContainerName()
	{
		return "plugin.modeler";
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#initializeComponents()
	 */
	protected void initializeComponents()
	{
		miniViews = new LinkedList();

		content = Box.createHorizontalBox();
		content.setOpaque(false);

		int max = OptionMgr.getInstance().getIntegerOption("modeler.miniview.numberoption", maxMiniViews);
		setMaximumDisplayedViews(max);

		// Query all active editors
		MiniViewEvent event = new MiniViewEvent(this, "miniview.created");
		fireEvent(event);

		for (Iterator it = event.getEditors().iterator(); it.hasNext();)
		{
			addModeler((Modeler) it.next());
		}

		getContentPane().add(content);
	}

	//////////////////////////////////////////////////
	// @@ Mini view management
	//////////////////////////////////////////////////

	/**
	 * Sets the maximum number of views displayed by the MiniView.
	 * @param number Maximum number of simultaneously displayed mini views
	 */
	protected void setMaximumDisplayedViews(int number)
	{
		maxMiniViews = number;

		// Check if there are too many mini views
		int count = 0;
		for (Iterator it = miniViews.iterator(); it.hasNext();)
		{
			MiniView view = (MiniView) it.next();

			if (++count >= maxMiniViews)
			{
				// Too many, remove
				it.remove();
				content.remove(view);
			}
		}

		getContentPane().revalidate();
	}

	/**
	 * Adds a modeler to the mini view.
	 * If the modeler is already displayed in the mini view plugin,
	 * the method will reposition the associated mini view to the begin of the mini view list.
	 *
	 * @param modeler Modeler to add
	 */
	protected void addModeler(Modeler modeler)
	{
		// Check if it's already there
		for (Iterator it = miniViews.iterator(); it.hasNext();)
		{
			MiniView view = (MiniView) it.next();

			if (view.getModeler() == modeler)
			{
				// Already present. Remove and insert at begin of list.
				it.remove();
				miniViews.addFirst(view);
				return;
			}
		}

		// Check if there are too many mini views
		int count = 0;
		for (Iterator it = miniViews.iterator(); it.hasNext();)
		{
			MiniView view = (MiniView) it.next();

			if (++count >= maxMiniViews)
			{
				// Too many, remove
				it.remove();
				content.remove(view);
			}
		}

		// Create a new mini view
		MiniView miniView = new MiniView(modeler, this);
		miniView.setDrawing(modeler.getDrawing());

		// Add the miniview as state change listener to the modeler
		modeler.addTrackChangedListener(miniView);

		// Add the new mini view
		content.add(miniView);
		miniViews.addFirst(miniView);

		updateMiniViewBorders();
	}

	/**
	 * Removes a modeler from the mini view.
	 *
	 * @param modeler Modeler to remove
	 */
	protected void removeModeler(Modeler modeler)
	{
		for (Iterator it = miniViews.iterator(); it.hasNext();)
		{
			MiniView miniView = (MiniView) it.next();

			if (miniView.editor() == modeler)
			{
				// Disconnect links between miniview and editor
				modeler.removeTrackChangedListener(miniView);
				miniView.unregister();

				// Remove from miniview list
				it.remove();

				// Remove from ui
				content.remove(miniView);
				break;
			}
		}

		updateMiniViewBorders();

		getPluginComponent().repaint();
	}

	/**
	 * Gets the number of currently active mini views.
	 * @nowarn
	 */
	public int getNumberOfMiniViews()
	{
		return miniViews.size();
	}

	/**
	 * Updates the borders of the mini view so that there is a single line between each displayed mini view.
	 */
	private void updateMiniViewBorders()
	{
		for (Iterator it = miniViews.iterator(); it.hasNext();)
		{
			MiniView view = (MiniView) it.next();

			if (it.hasNext())
			{
				// Set a simple border that draws the left edge only
				view.setBorder(new SimpleBorder(0, 1, 0, 0));
			}
			else
			{
				// No border for the last view
				view.setBorder(null);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Event module
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "miniview";
		}

		/**
		 * Event handler: A modeler view has become active.
		 *
		 * @event modeler.view.activated
		 * @eventobject Modeler that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_activated(JaspiraEvent je)
		{
			Object o = je.getObject();

			if (o instanceof Modeler)
			{
				addModeler((Modeler) o);

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: A modeler view has been closed.
		 *
		 * @event modeler.view.closed
		 * @eventobject Modeler that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_closed(JaspiraEvent je)
		{
			Object o = je.getObject();

			if (o instanceof Modeler)
			{
				removeModeler((Modeler) o);

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: The maximum number of mini views has changed.
		 *
		 * @event modeler.miniview.numberoption
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_miniview_numberoption(JaspiraEvent je)
		{
			Option opt = (Option) je.getObject();
			if (opt instanceof MiniViewOptionModule.NumberOption)
			{
				setMaximumDisplayedViews(((Integer) opt.getValue()).intValue());
			}

			return EVENT_HANDLED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Option module
	//////////////////////////////////////////////////

	public class MiniViewOptionModule extends OptionModule
	{
		/**
		 * This option defines the number of views displayed by the mini view.
		 */
		public class NumberOption extends IntegerOption
		{
			public NumberOption()
			{
				super(getPluginResourceCollection(), "modeler.miniview.numberoption", Integer.valueOf(2), 1, 5);
			}
		}
	}
}
