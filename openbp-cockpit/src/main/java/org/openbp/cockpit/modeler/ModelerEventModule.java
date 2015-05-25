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
package org.openbp.cockpit.modeler;

import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.util.Iterator;
import java.util.List;

import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.drawing.shadowlayout.ShadowLayouter;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.figures.process.ParamConnection;
import org.openbp.cockpit.modeler.figures.process.ParamFigure;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;
import org.openbp.cockpit.modeler.paramvaluewizard.ParamValueWizard;
import org.openbp.cockpit.plugins.miniview.MiniViewEvent;
import org.openbp.common.ExceptionUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.MultiSocketNode;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessObject;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.guiclient.event.OpenEvent;
import org.openbp.guiclient.event.QualifierEvent;
import org.openbp.guiclient.plugins.displayobject.DisplayObjectPlugin;
import org.openbp.jaspira.action.ActionMgr;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.AskEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.event.VetoableEvent;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionMgr;
import org.openbp.jaspira.plugin.ExternalEventModule;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugin.PluginMgr;
import org.openbp.jaspira.plugins.propertybrowser.PropertyBrowserSaveEvent;
import org.openbp.jaspira.plugins.statusbar.StatusBarTextEvent;
import org.openbp.swing.components.JMsgBox;

import CH.ifa.draw.framework.Figure;

/**
 * Event module of the {@link Modeler} class.
 *
 * @author Heiko Erhardt
 */
public class ModelerEventModule extends ExternalEventModule
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Modeler we are associated with */
	private Modeler modeler;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param modeler Modeler we are associated with
	 */
	public ModelerEventModule(Plugin modeler)
	{
		super(modeler);

		this.modeler = (Modeler) modeler;
	}

	public String getName()
	{
		return "modeler.view";
	}

	//////////////////////////////////////////////////
	// @@ Global events
	//////////////////////////////////////////////////

	/**
	 * Event handler: A Jaspira page container has been activated.
	 *
	 * @event global.frame.activated
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode global_frame_activated(JaspiraEvent je)
	{
		if (modeler.getToolSupport()!= null)
		{
			modeler.getToolSupport().updateToolState();
		}

		return EVENT_CONSUMED;
	}

	/**
	 * Event handler: Uses the update clipboard status event to enable/disable various edit actions.
	 *
	 * @event global.clipboard.updatestatus
	 * @param je Event
	 * @return EVENT_HANDLED
	 */
	public JaspiraEventHandlerCode global_clipboard_updatestatus(JaspiraEvent je)
	{
		if (je.getSourcePlugin() == modeler)
		{
			boolean flipableSelected = false;

			// Check if the selection contains something that is flipable
			WorkspaceDrawingView workspaceView = modeler.getDrawingView();
			if (workspaceView != null)
			{
				List selectedFigures = workspaceView.selection();

				int n = selectedFigures.size();
				if (n > 0)
				{
					for (int i = 0; i < n; ++i)
					{
						Object o = selectedFigures.get(i);

						if (o instanceof ParamConnection || o instanceof ParamFigure)
						{
							flipableSelected = true;
						}
					}
				}

				JaspiraAction action = ActionMgr.getInstance().getAction("modeler.edit.fliporientation");
				if (action != null)
				{
					action.setEnabled(flipableSelected);
				}
			}

			return EVENT_HANDLED;
		}

		return EVENT_IGNORED;
	}

	//////////////////////////////////////////////////
	// @@ File operation events
	//////////////////////////////////////////////////

	/**
	 * Catches the open event, check if the object to open is the currently edited
	 * process and if so, cause it to request the focus.
	 *
	 * @event open.modeler
	 * @eventparam The event object is the model qualifier of the process to open
	 * @param oe Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode open_modeler(OpenEvent oe)
	{
		ModelQualifier qualifier = null;

		Object o = oe.getObject();

		if (o instanceof String)
		{
			qualifier = new ModelQualifier((String) o);
			qualifier.setItemType(ItemTypes.PROCESS);
		}
		else if (o instanceof ModelQualifier)
		{
			qualifier = (ModelQualifier) o;
		}
		else if (o instanceof ProcessItem)
		{
			qualifier = ((ProcessItem) o).getQualifier();
		}

		if (qualifier != null && modeler.getProcessQualifier().matches(qualifier, ModelQualifier.COMPARE_MODEL | ModelQualifier.COMPARE_ITEM | ModelQualifier.COMPARE_TYPE))
		{
			modeler.focusPlugin();

			ProcessElementContainer pec = modeler.getDrawing().getFigureByQualifier(qualifier);
			if (pec != null)
			{
				WorkspaceDrawingView view = modeler.getDrawingView();
				view.singleSelect(pec);
			}

			return EVENT_CONSUMED;
		}

		return EVENT_IGNORED;
	}

	/**
	 * Returns the edited instance of the given process if edited by this modeler.
	 *
	 * @event global.edit.geteditedinstance
	 * @eventparam The event object is the model qualifier or some other instance of the wanted process
	 * or null for the current process of this modeler
	 * @param ae Event; The answer of this event will be the {@link ProcessItem} that is being edited currently
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode global_edit_geteditedinstance(AskEvent ae)
	{
		Object o = ae.getObject();

		if (o == null || (o instanceof ModelQualifier && ((ModelQualifier) o).matches(modeler.getProcessQualifier())) || (o instanceof ProcessItem && ((ProcessItem) o).getQualifier().matches(modeler.getProcessQualifier())))
		{
			ae.setAnswer(modeler.getProcess());

			return EVENT_CONSUMED;
		}

		return EVENT_IGNORED;
	}

	/**
	 * Returns this modeler if it edits the given process.
	 *
	 * @event global.edit.geteditor
	 * @eventparam The event object is the model qualifier or some other instance of the wanted process
	 * or null for the current process of this modeler
	 * @param ae Event; The answer of this event will be the {@link ProcessItem} that is being edited currently
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode global_edit_geteditor(AskEvent ae)
	{
		Object o = ae.getObject();

		if (o == null || (o instanceof ModelQualifier && ((ModelQualifier) o).matches(modeler.getProcessQualifier())) || (o instanceof ProcessItem && ((ProcessItem) o).getQualifier().matches(modeler.getProcessQualifier())))
		{
			ae.setAnswer(modeler);

			return EVENT_CONSUMED;
		}

		return EVENT_IGNORED;
	}

	/**
	 * Event handler: Save the current process if visible.
	 *
	 * @event standard.file.save
	 * @param jae Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode standard_file_save(JaspiraActionEvent jae)
	{
		if (!modeler.getPluginComponent().isShowing())
		{
			// Nothing to save
			return EVENT_IGNORED;
		}

		return standard_file_saveall(jae);
	}

	/**
	 * Event handler: Save all processes.
	 *
	 * @event standard.file.saveall
	 * @param jae Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode standard_file_saveall(JaspiraActionEvent jae)
	{
		ProcessItem process = modeler.getProcess();
		if (process == null)
		{
			// Nothing to save
			return EVENT_IGNORED;
		}

		if (!process.isModified())
		{
			// Nothing to save
			return EVENT_IGNORED;
		}

		VetoableEvent ve = new VetoableEvent(modeler, "modeler.view.asksave", modeler);
		modeler.fireEvent(ve);
		if (!ve.isVetoed())
		{
			if (modeler.saveProcess())
			{
				modeler.getDrawing().clearModified();
				modeler.fireEvent(new StatusBarTextEvent(modeler, "Process " + modeler.getProcessQualifier() + " saved."));
				modeler.fireEvent(new JaspiraEvent(modeler, "modeler.view.saved", modeler));
			}
		}

		return EVENT_HANDLED;
	}

	/**
	 * Event handler: A process is about to be deleted.
	 * Tries to close this view, along wih its editor. If the process has
	 * been modified, offers to cancel.
	 *
	 * @event standard.file.askdelete
	 * @eventparam Model qualifier or process, ignored otherwise.
	 * @param ve Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode standard_file_askdelete(VetoableEvent ve)
	{
		Object o = ve.getObject();
		ModelQualifier qualifier = null;

		if (o instanceof ModelQualifier)
		{
			qualifier = (ModelQualifier) o;
		}
		else if (o instanceof ProcessItem)
		{
			qualifier = ((ProcessItem) o).getQualifier();
		}
		if (!modeler.getProcessQualifier().matches(qualifier))
		{
			return EVENT_IGNORED;
		}

		ProcessItem process = modeler.getProcess();

		if (process.isModified())
		{
			String msg = "" + process.getQualifier() + " has been modified. Really delete?";
			int result = JMsgBox.show(null, msg, JMsgBox.TYPE_YESNO | JMsgBox.DEFAULT_NO);
			if (result != JMsgBox.TYPE_YES)
			{
				ve.veto();
				return EVENT_CONSUMED;
			}

			process.clearModified();
		}

		// TODO Fix 4: Check if deleting open process works, I assume there will be left over something...
		PluginMgr.getInstance().removeInstance(modeler);
		modeler.getPluginHolder().unlinkHolder();
		modeler.fireEvent("modeler.view.closed", modeler);

		return EVENT_HANDLED;
	}

	//////////////////////////////////////////////////
	// @@ Workspace update events
	//////////////////////////////////////////////////

	/**
	 * Event handler: Invalidates a process element container identified by a given model qualifier into view.
	 * Call {@link #refresh} to update the invalidated elements.
	 *
	 * @event modeler.view.invalidate
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode invalidate(QualifierEvent je)
	{
		ProcessElementContainer pec = modeler.getDrawing().getFigureByQualifier(je.getQualifier());
		if (pec == null)
		{
			// This doesn't seem to be addressed to the current process
			// or the item to show doesn't exist in this process
			return EVENT_IGNORED;
		}

		if (pec instanceof AbstractTagFigure)
		{
			((AbstractTagFigure) pec).checkDecoratedContentState();
		}
		pec.invalidate();

		return modeler.getPluginComponent().isShowing() ? EVENT_CONSUMED : EVENT_IGNORED;
	}

	/**
	 * Event handler: Refreshes the workspace view.
	 * Checks damage of the workspace display and updates the workspace accordingly.
	 * This will redraw all objects that have been invalidated using the modeler.view.invalidate event.
	 *
	 * @event modeler.view.refresh
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode refresh(JaspiraEvent je)
	{
		modeler.getDrawingView().checkDamage();

		return modeler.getPluginComponent().isShowing() ? EVENT_CONSUMED : EVENT_IGNORED;
	}

	/**
	 * Event handler: Redraws the workspace.
	 * Performs a complete redraw of the workspace.
	 *
	 * @event modeler.view.redraw
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode redraw(JaspiraEvent je)
	{
		modeler.getDrawingView().redraw();

		return modeler.getPluginComponent().isShowing() ? EVENT_CONSUMED : EVENT_IGNORED;
	}

	/**
	 * Event handler: Sets the zoom factor of the currently visible view.
	 *
	 * @event modeler.view.setzoomfactor
	 * @eventparam Double The zoom factor
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode setzoomfactor(JaspiraEvent je)
	{
		if (modeler.getPluginComponent().isShowing())
		{
			double factor = ((Double) je.getObject()).doubleValue();

			WorkspaceDrawingView view = modeler.getDrawingView();
			view.setScaleFactor(factor);
			view.redraw();
			view.revalidate();

			modeler.focusPlugin();

			return EVENT_CONSUMED;
		}

		return EVENT_IGNORED;
	}

	/**
	 * Event handler: View mode change was performed.
	 *
	 * @event modeler.view.modechange
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode modechange(JaspiraEvent je)
	{
		// Update the tag state of all tags
		int state = ViewModeMgr.getInstance().getTagState(modeler);
		modeler.getDrawing().setTagState(state);

		WorkspaceDrawingView view = modeler.getDrawingView();

		// Clear the selection in order to prevent having invisible objects selected
		view.clearSelection();

		// Redraw to reflect the changes
		view.redraw();

		return EVENT_HANDLED;
	}

	/**
	 * Event handler: Synchronizes the display model with the process model.
	 *
	 * @event modeler.view.updatedrawing
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode updatedrawing(JaspiraEvent je)
	{
		modeler.getDrawing().updateFigure();

		return modeler.getPluginComponent().isShowing() ? EVENT_CONSUMED : EVENT_IGNORED;
	}

	/**
	 * Event handler: Scrolls a process element container identified by a given model qualifier into view.
	 * Adds an enlargement offset of 100 to the display box of the element to prevent it
	 * from hanging in the corner.
	 *
	 * @event modeler.view.show
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode show(QualifierEvent je)
	{
		ProcessElementContainer pec = modeler.getDrawing().getFigureByQualifier(je.getQualifier());
		if (pec == null)
		{
			// This doesn't seem to be addressed to the current process
			// or the item to show doesn't exist in this process
			return EVENT_IGNORED;
		}

		modeler.getDrawingView().scrollIntoView(pec, true);

		return modeler.getPluginComponent().isShowing() ? EVENT_CONSUMED : EVENT_IGNORED;
	}

	/**
	 * Event handler: Scrolls a process element container identified by a given model qualifier into view.
	 * Does not add an enlargement offset to the display box of the element.
	 *
	 * @event modeler.view.showexact
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode showexact(QualifierEvent je)
	{
		ProcessElementContainer pec = modeler.getDrawing().getFigureByQualifier(je.getQualifier());
		if (pec == null)
		{
			// This doesn't seem to be addressed to the current process
			// or the item to show doesn't exist in this process
			return EVENT_IGNORED;
		}

		modeler.getDrawingView().scrollIntoView(pec, false);

		return modeler.getPluginComponent().isShowing() ? EVENT_CONSUMED : EVENT_IGNORED;
	}

	//////////////////////////////////////////////////
	// @@ Workspace figure and selection access
	//////////////////////////////////////////////////

	/**
	 * Event handler: Scrolls a process element container identified by a given model qualifier into view and selects it.
	 *
	 * @event modeler.view.select
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode select(QualifierEvent je)
	{
		ProcessElementContainer pec = modeler.getDrawing().getFigureByQualifier(je.getQualifier());
		if (pec == null)
		{
			// This doesn't seem to be addressed to the current process
			// or the item to show doesn't exist in this process
			return EVENT_IGNORED;
		}

		WorkspaceDrawingView view = modeler.getDrawingView();
		view.singleSelect(pec);

		return modeler.getPluginComponent().isShowing() ? EVENT_CONSUMED : EVENT_IGNORED;
	}

	/**
	 * Event handler: Gets the number of selected elements.
	 * This modeler instance will respond to the event (and consume it) only when it is currently visible,
	 * so the target of the event will be the currently active modeler.
	 *
	 * @event modeler.view.getselectioncount
	 * @param ae Event; The answer of this event will be an Integer specifying the number
	 * of currently selected elements.
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode getselectioncount(AskEvent ae)
	{
		if (modeler.getPluginComponent().isShowing())
		{
			WorkspaceDrawingView workspaceView = modeler.getDrawingView();
			ae.setAnswer(Integer.valueOf(workspaceView.selectionCount()));

			return EVENT_CONSUMED;
		}

		return EVENT_IGNORED;
	}

	/**
	 * Event handler: Gets the selected elements.
	 * This modeler instance will respond to the event (and consume it) only when it is currently visible,
	 * so the target of the event will be the currently active modeler.
	 *
	 * @event modeler.view.getselection
	 * @param ae Event; The answer of this event will be a List containing the selected figures
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode getselection(AskEvent ae)
	{
		if (modeler.getPluginComponent().isShowing())
		{
			WorkspaceDrawingView workspaceView = modeler.getDrawingView();
			ae.setAnswer(workspaceView.selection());

			return EVENT_CONSUMED;
		}

		return EVENT_IGNORED;
	}

	/**
	 * Event handler: Gets a process element by its model qualifier.
	 *
	 * @event modeler.view.getbyqualifier
	 * @eventobject The {@link ModelQualifier} of the object to retrieve
	 * @param ae Event; The answer of this event will be the {@link ProcessElementContainer}
	 * specified by the event object or null if the view does not contain such an element
	 * or the view is not currently visible.
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode getbyqualifier(AskEvent ae)
	{
		if (modeler.getPluginComponent().isShowing())
		{
			ProcessElementContainer pec = modeler.getDrawing().getFigureByQualifier((ModelQualifier) ae.getObject());
			if (pec != null)
			{
				ae.setAnswer(pec);
				return EVENT_CONSUMED;
			}
		}

		return EVENT_IGNORED;
	}

	/**
	 * Event handler: Tries to import the event object into each currently selected element.
	 *
	 * @event modeler.view.importtoselection
	 * @param je Event
	 * @return EVENT_HANDLED
	 */
	public JaspiraEventHandlerCode importtoselection(JaspiraEvent je)
	{
		boolean imported = false;
		String regionId = null;
		Transferable transferable = (Transferable) je.getObject();

		// Check if the selection contains something that is flipable
		WorkspaceDrawingView workspaceView = modeler.getDrawingView();
		if (workspaceView != null)
		{
			List selectedFigures = workspaceView.selection();

			int n = selectedFigures.size();
			if (n > 0)
			{
				for (int i = 0; i < n; ++i)
				{
					Object o = selectedFigures.get(i);

					if (o instanceof InteractionClient)
					{
						InteractionClient interactionClient = (InteractionClient) o;

						if (interactionClient.importData(regionId, transferable, null))
						{
							imported = true;
						}
					}
				}
			}
		}

		if (imported)
		{
			return EVENT_HANDLED;
		}

		return EVENT_IGNORED;
	}

	//////////////////////////////////////////////////
	// @@ Action events
	//////////////////////////////////////////////////

	/**
	 * Event handler: Select all elements of the drawing.
	 *
	 * @event modeler.edit.selectall
	 * @param jae Action event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode modeler_edit_selectall(JaspiraActionEvent jae)
	{
		if (jae.getSourcePlugin() == modeler)
		{
			WorkspaceDrawingView view = modeler.getDrawingView();

			view.clearSelection();

			for (Iterator it = modeler.getDrawing().getAllFigures(); it.hasNext();)
			{
				Figure figure = (Figure) it.next();
				view.addToSelection(figure);
			}

			return EVENT_CONSUMED;
		}

		return EVENT_IGNORED;
	}

	/**
	 * Event handler: Normalizes the drawing.
	 *
	 * @event modeler.edit.normalize
	 * @param jae Action event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode modeler_edit_normalize(JaspiraActionEvent jae)
	{
		if (jae.getSourcePlugin() == modeler)
		{
			modeler.startUndoForAction(jae);

			modeler.getDrawing().normalize();

			modeler.endUndo();

			return EVENT_CONSUMED;
		}

		return EVENT_IGNORED;
	}

	/**
	 * Toggles snap to grid mode.
	 * @event modeler.view.snaptogrid
	 * @param jae Action event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode snaptogrid(JaspiraActionEvent jae)
	{
		return EVENT_HANDLED;
	}

	/**
	 * Event handler: Normalizes the drawing.
	 *
	 * @event modeler.edit.fliporientation
	 * @param jae Action event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode modeler_edit_fliporientation(JaspiraActionEvent jae)
	{
		if (jae.getSourcePlugin() == modeler)
		{
			// Flip the data link orientation of all selected figures that are or have data links
			WorkspaceDrawingView workspaceView = modeler.getDrawingView();
			List selectedFigures = workspaceView.selection();

			int n = selectedFigures.size();
			if (n > 0)
			{
				boolean found = false;

				for (int i = 0; i < n; ++i)
				{
					Object o = selectedFigures.get(i);

					if (o instanceof ParamConnection)
					{
						if (!found)
						{
							modeler.startUndoForAction(jae);
							found = true;
						}

						((ParamConnection) o).flipOrientation();
					}
				}

				if (found)
				{
					modeler.endUndo();
				}
			}

			return EVENT_CONSUMED;
		}

		return EVENT_IGNORED;
	}

	//////////////////////////////////////////////////
	// @@ Option events
	//////////////////////////////////////////////////

	/**
	 * Event handler: The shadow type option has been changed.
	 *
	 * @event editor.shadow
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode editor_shadow(JaspiraEvent je)
	{
		Object o = ((Option) je.getObject()).getValue();
		modeler.getDrawingView().setShadowLayouter((ShadowLayouter) o);

		return EVENT_HANDLED;
	}

	/**
	 * Event handler: The grid layout option has been changed.
	 *
	 * @event editor.grid.type
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode editor_grid_type(JaspiraEvent je)
	{
		Object o = ((Option) je.getObject()).getValue();
		modeler.getDrawingView().setGridType(((Integer) o).intValue());

		return EVENT_HANDLED;
	}

	/**
	 * Event handler: The grid display option has been changed.
	 *
	 * @event editor.grid.display
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode editor_grid_display(JaspiraEvent je)
	{
		Object o = ((Option) je.getObject()).getValue();
		modeler.getDrawingView().setGridDisplayed(((Boolean) o).booleanValue());

		return EVENT_HANDLED;
	}

	/**
	 * Event handler: The workspace color has been changed.
	 *
	 * @event editor.color.workspace
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode editor_color_workspace(JaspiraEvent je)
	{
		Color color = (Color) ((Option) je.getObject()).getValue();
		modeler.getDrawingView().setBackground(color);

		return EVENT_HANDLED;
	}

	/**
	 * Event handler: The data link auto connector mode has been changed.
	 *
	 * @event editor.autoconnector.datalink
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode editor_autoconnector_datalink(JaspiraEvent je)
	{
		AutoConnector.determineDataLinkAutoConnectorMode();

		return EVENT_HANDLED;
	}

	/**
	 * Called after the titlemode option of the role manager ({@link DisplayObjectPlugin})
	 * has been changed.
	 *
	 * @event displayobject.changed.titlemode
	 * @param event Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode displayobject_changed_titlemode(JaspiraEvent event)
	{
		modeler.getDrawing().updateFigure();
		modeler.getDrawingView().redraw();

		return EVENT_HANDLED;
	}

	//////////////////////////////////////////////////
	// @@ Miscelleanous event module
	//////////////////////////////////////////////////

	/**
	 * Event handler: Miniview initialization.
	 *
	 * A new mini view was created and wants to know which editors
	 * are present - return our editor to it.
	 *
	 * @event miniview.created
	 * @param mve Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode miniview_created(MiniViewEvent mve)
	{
		mve.addEditor(modeler);

		return EVENT_HANDLED;
	}

	/**
	 * Event handler: Save the object currently edited by the property browser.
	 * Saves the object if it is a process object of our current process.
	 *
	 * @event plugin.propertybrowser.executesave
	 * @param oee Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode plugin_propertybrowser_executesave(PropertyBrowserSaveEvent oee)
	{
		if (!(oee.original instanceof ProcessObject))
			return EVENT_IGNORED;

		ProcessObject orig = (ProcessObject) oee.original;

		if (!modeler.getProcessQualifier().equals(orig.getProcess().getQualifier()))
		{
			return EVENT_IGNORED;
		}

		modeler.startUndo("Edit Properties");

		try
		{
			orig.copyFrom(oee.getObject(), ModelObject.COPY_SHALLOW);
		}
		catch (CloneNotSupportedException e)
		{
			ExceptionUtil.printTrace(e);
			return EVENT_CONSUMED;
		}

		// Make sure all subordinate objects refer to this object
		orig.maintainReferences(0);

		if (orig instanceof MultiSocketNode)
		{
			MultiSocketNode node = (MultiSocketNode) orig;

			// If we edit an action node and the configuration bean contains default values only, we remove it
			// so it won't be persisted to the XML file.
			if (node.getConfigurationBean() != null && node.getConfigurationBean().hasDefaultValues())
			{
				node.setConfigurationBean(null);
			}
		}

		modeler.getDrawing().updateFigure();

		Object o = orig.getRepresentation();
		if (o instanceof Figure)
		{
			((Figure) o).invalidate();
			modeler.getDrawingView().checkDamage();
		}
		else
		{
			modeler.getDrawingView().redraw();
		}

		if (orig instanceof ProcessVariable)
		{
			// After changing the properties of a global, force update of the process variables plugin
			modeler.fireEvent("variables.refresh");
		}

		oee.saved = true;

		modeler.endUndo();

		return EVENT_CONSUMED;
	}

	/**
	 * Event handler: A figure was added to the workspace.
	 * Called after a node has been added to the drawing.
	 * Invokes the node autoconnector if the figure is a node and selects the figure.
	 * If a node is currently selected, connect the selected and the new node via their default sockets.
	 *
	 * @event modeler.drawing.figureadded
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode modeler_drawing_figureadded(JaspiraEvent je)
	{
		if (je.getSourcePlugin() != modeler)
		{
			return EVENT_IGNORED;
		}

		final Figure addedFigure = (Figure) je.getObject();

		if (addedFigure instanceof NodeFigure)
		{
			// Try the autoconnector
			NodeFigure newNode = (NodeFigure) addedFigure;

			// Display parameter value wizard if desired
			if (OptionMgr.getInstance().getBooleanOption("editor.paramvaluewizard", true))
			{
				ParamValueWizard.displayParameterValueWizard(modeler, newNode, null, null);
			}

			// Try to auto-connect the inserted node to the current node/socket.
			AutoConnector autoConnector = new AutoConnector(modeler, newNode);
			autoConnector.autoConnectAll();
		}

		// Select the new element
		WorkspaceDrawingView workspaceView = modeler.getDrawingView();
		workspaceView.singleSelect(addedFigure);

		if (addedFigure instanceof NodeFigure && ((NodeFigure) addedFigure).isCreatedFromScratch())
		{
			// Show the in place editor for all nodes that have been just created
			// (don't show for existing items that have been dragged from the compoennt browser)
			modeler.getToolSupport().displayInPlaceEditor((NodeFigure) addedFigure);
		}
		else
		{
			modeler.focusPlugin();
		}

		return modeler.getPluginComponent().isShowing() ? EVENT_CONSUMED : EVENT_IGNORED;
	}

	/**
	 * Event handler: Socket autoconnector.
	 * Called after a socket has been added to a node.
	 * If a node is currently selected, connect the selected and the new node via their default sockets.
	 *
	 * @event modeler.drawing.socketadded
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode modeler_drawing_socketadded(JaspiraEvent je)
	{
		if (je.getSourcePlugin() != modeler)
		{
			return EVENT_IGNORED;
		}

		// Try the autoconnector
		SocketFigure newSocket = (SocketFigure) je.getObject();

		// Try to auto-connect the inserted node to the current node/socket.
		AutoConnector autoConnector = new AutoConnector(modeler, newSocket);
		autoConnector.autoConnectAll();

		// Select the new element
		WorkspaceDrawingView workspaceView = modeler.getDrawingView();
		workspaceView.singleSelect(newSocket);

		modeler.getToolSupport().displayInPlaceEditor(newSocket);

		return modeler.getPluginComponent().isShowing() ? EVENT_CONSUMED : EVENT_IGNORED;
	}

	/**
	 * Event handler: Socket autoconnector.
	 * Called after a parameter has been added to a socket.
	 *
	 * @event modeler.drawing.paramadded
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode modeler_drawing_paramadded(JaspiraEvent je)
	{
		if (je.getSourcePlugin() != modeler)
		{
			return EVENT_IGNORED;
		}

		ParamFigure newParam = (ParamFigure) je.getObject();

		// Select the new element
		WorkspaceDrawingView workspaceView = modeler.getDrawingView();
		workspaceView.singleSelect(newParam);

		modeler.getToolSupport().displayInPlaceEditor(newParam);

		return modeler.getPluginComponent().isShowing() ? EVENT_CONSUMED : EVENT_IGNORED;
	}

	/**
	 * Event handler: Modification flag.
	 *
	 * Notifies the modeler that the process has been modified.
	 *
	 * @event modeler.process.modified
	 * @param je Event
	 * @return The event status code
	 */
	public JaspiraEventHandlerCode modeler_process_modified(JaspiraEvent je)
	{
		modeler.getDrawing().setModified();

		return modeler.getPluginComponent().isShowing() ? EVENT_CONSUMED : EVENT_IGNORED;
	}

	/**
	 * Receiver for the undo history size option event.
	 *
	 * @nowarn
	 */
	public JaspiraEventHandlerCode undo_history(JaspiraEvent je)
	{
		modeler.getUndoMgr().reloadHistorySize();

		return EVENT_CONSUMED;
	}
}
