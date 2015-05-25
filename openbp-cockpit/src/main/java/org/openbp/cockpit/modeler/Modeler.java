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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.drawing.Trackable;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.tools.ModelerToolSupport;
import org.openbp.cockpit.modeler.undo.ModelerUndoable;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.listener.SwingListenerSupport;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.process.NodeProvider;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.jaspira.action.ActionMgr;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.VetoableEvent;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.PluginState;
import org.openbp.jaspira.plugins.statusbar.StatusBarTextEvent;
import org.openbp.jaspira.undo.UndoMgr;
import org.openbp.jaspira.undo.Undoable;
import org.openbp.swing.components.JMsgBox;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Tool;
import CH.ifa.draw.framework.ViewChangeListener;
import CH.ifa.draw.util.UndoManager;

/**
 * The Modeler class is one of the core components of the OpenBP modeler.
 * It is an invisible plugin that contains the drawing for a single OpenBP process.
 *
 * @author Stephan Moritz
 */
public class Modeler extends AbstractVisiblePlugin
	implements DrawingEditorPlugin, Trackable, FocusListener
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Runtime attribute for items: The item is an item skeleton that is used
	 * as a pattern for the creation of new items that are dragged from the standard toolbox.
	 */
	public static final String ATTRIBUTE_SKELETON = "_skeleton";

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Process drawing associated with this modeler */
	private ProcessDrawing drawing;

	/** The model qualifier of the process edited by this editor */
	private ModelQualifier processQualifier;

	/** The JHotDraw View object we represent */
	private WorkspaceDrawingView workspaceView;

	/** the scroll pane of our View */
	private JScrollPane scrollPane;

	/** Clipboard support helper class */
	private ClipboardSupport clipboardSupport;

	/** Undo manager */
	private UndoMgr undoMgr;

	/** Current undoable */
	private ModelerUndoable currentUndoable;

	/** The modeler tool support object holding all tools */
	private ModelerToolSupport toolSupport;

	/** Listener support object holding the listeners */
	private SwingListenerSupport listenerSupport;

	/** If true, do not notify miniview about updates */
	private boolean trackSuspended;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	public String getResourceCollectionContainerName()
	{
		return "plugin.modeler";
	}

	/**
	 * Init the visual components of the plugin.
	 *
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#initializeComponents()
	 */
	protected void initializeComponents()
	{
		// Initialization will be done after the process has been set
	}

	/**
	 * Initializes the modeler after the process/drawing to edit has been set.
	 */
	private void initializeModeler()
	{
		workspaceView = new WorkspaceDrawingView(this);
		workspaceView.setDrawing(drawing);

		toolSupport = new ModelerToolSupport(this);
		StandardToolSupportSetup.setupToolSupport(toolSupport, true);

		clipboardSupport = new ClipboardSupport(workspaceView, getPluginResourceCollection(), false);

		undoMgr = new UndoMgr();

		getContentPane().removeAll();

		scrollPane = new JScrollPane(workspaceView);
		scrollPane.getViewport().addChangeListener(new ChangeListener()
		{
			/**
			 * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
			 */
			public void stateChanged(ChangeEvent e)
			{
				fireTrackChangedEvent(e);
			}
		});
		getContentPane().add(scrollPane);

		addPluginFocusListener(this);

		fireEvent(new JaspiraEvent(this, "modeler.view.opened", this));
	}

	/**
	 * Gets the the modeler tool support object holding all tools.
	 * @nowarn
	 */
	public ModelerToolSupport getToolSupport()
	{
		return toolSupport;
	}

	//////////////////////////////////////////////////
	// @@ Various
	//////////////////////////////////////////////////

	/**
	 * Sets the process to be edited.
	 * The method clones the given process.
	 * @param process Process to be edited
	 */
	public void setProcess(ProcessItem process, boolean readonly)
	{
		ProcessItem clonedProcess = null;

		try
		{
			clonedProcess = (ProcessItem) process.clone();
		}
		catch (Exception e)
		{
			// Shouldn't happen
			ExceptionUtil.printTrace(e);
			return;
		}

		// Make sure that the parent-child links and control and data links are valid
		clonedProcess.maintainReferences(ModelObject.RESOLVE_LOCAL_REFS);

		processQualifier = clonedProcess.getQualifier();

		drawing = new ProcessDrawing(clonedProcess, this);
		drawing.setReadOnly(readonly);

		initializeModeler();

		if (process != null)
		{
			// Update any plugins that refer to the current process
			fireEvent("modeler.view.activated", this);
		}
	}

	/**
	 * Returns the edited process.
	 * @nowarn
	 */
	public ProcessItem getProcess()
	{
		return drawing != null ? drawing.getProcess() : null;
	}

	/**
	 * Gets the drawing view that displays the contents of this view plugin.
	 * @nowarn
	 */
	public WorkspaceDrawingView getDrawingView()
	{
		return workspaceView;
	}

	/**
	 * Returns the drawing associated with this editor.
	 * @nowarn
	 */
	public ProcessDrawing getDrawing()
	{
		return drawing;
	}

	/**
	 * Returns the model qualifier of the edited process.
	 * @nowarn
	 */
	public ModelQualifier getProcessQualifier()
	{
		return processQualifier;
	}

	/**
	 * Saves the process to the server.
	 * @return
	 * true: The process was saved successfully.<br>
	 * false: There was an error saving the process. The error has been reported to the user.
	 */
	public boolean saveProcess()
	{
		drawing.encodeGeometry();

		return ModelConnector.getInstance().saveItem(getProcess(), false);
	}

	/**
	 * Repairs damages in all views of the modeler.
	 */
	public void repairDamage()
	{
		workspaceView.repairDamage();
	}

	//////////////////////////////////////////////////
	// @@ Placeholder support
	//////////////////////////////////////////////////

	/**
	 * Converts the given placeholder to some 'real' node.
	 *
	 * @param nodeFigure Placeholder node figure to convert
	 * @param np Node provider that will provide the node to convert to or null.
	 * In the latter case, a menu should be displayed to the user where he can choose the type of node to create.
	 * @param p Current mouse position
	 * @return
	 * true: The conversion was successfully completed. Any pending operations can be performed.<br>
	 * false: The conversion failed, was cancelled or is not completed yet.
	 */
	public boolean convertPlaceholder(NodeFigure nodeFigure, NodeProvider np, Point p)
	{
		return false;
	}

	//////////////////////////////////////////////////
	// @@ Plugin overrides
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#getExternalEventModuleClasses()
	 */
	protected Class [] getExternalEventModuleClasses()
	{
		return new Class [] { ModelerEventModule.class, ModelerInteractionModule.class };
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#pluginUninstalled()
	 */
	protected void pluginUninstalled()
	{
		super.pluginUninstalled();

		toolSupport.deactivate();

		// Disconnect the current process of the drawing from the actual process model.
		// This is necessary because otherwise the process objects of the process item would
		// reference figures of the drawing, which would not be used any more, but could also
		// not be garbage collected.
		drawing.setProcess(null);
		drawing.setEditor(null);

		// Dispose the drawing view
		workspaceView.unregister();
		workspaceView.removeFigureSelectionListener(this);

		// Clear references for better garbage collection in case of memory leaks
		drawing = null;
		processQualifier = null;
		workspaceView = null;
		scrollPane = null;
		clipboardSupport = null;
		toolSupport = null;
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#setPluginState(PluginState)
	 */
	public void setPluginState(PluginState state)
	{
		super.setPluginState(state);
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#getPluginState()
	 */
	public PluginState getPluginState()
	{
		return super.getPluginState();
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#getToolbarType()
	 */
	public int getToolbarType()
	{
		return TOOLBAR_DYNAMIC;
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#canDrag()
	 */
	public boolean canDrag()
	{
		return false;
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#hasCloseButton()
	 */
	public boolean hasCloseButton()
	{
		return true;
	}

	/**
	 * @see org.openbp.jaspira.plugin.Plugin#canClose()
	 */
	public boolean canClose()
	{
		if (drawing == null)
		{
			// This might be due to an out of memory error
			return true;
		}

		VetoableEvent ve = new VetoableEvent(this, "modeler.view.askclose", this);
		fireEvent(ve);
		if (ve.isVetoed())
		{
			return false;
		}

		// TOLOCALIZE

		ProcessItem process = getProcess();
		if (process.isModified())
		{
			String msg = "" + process.getQualifier() + " has been modified. Save?";
			int result = JMsgBox.show(null, msg, JMsgBox.TYPE_YESNOCANCEL);

			if (result == JMsgBox.TYPE_CANCEL)
			{
				return false;
			}
			else if (result == JMsgBox.TYPE_YES)
			{
				if (!saveProcess())
				{
					// Save failed
					return false;
				}

				fireEvent(new StatusBarTextEvent(this, "Process " + processQualifier + " saved."));
			}
			else
			{
				drawing.clearModified();
			}
		}

		return true;
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#preClose()
	 */
	protected void preClose()
	{
		// Clear the selection, so the property browser get disconnected from the process objects
		workspaceView.clearSelection();

		fireEvent("modeler.view.closed", this);
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#pluginShown()
	 */
	public void pluginShown()
	{
		super.pluginShown();

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				fireEvent("modeler.view.activated", Modeler.this);

				fireEvent("modelerpage.view.showzoomfactor", new Double(workspaceView.getScaleFactor()));
				drawing.updateModificationState();
			}
		});

		toolSupport.updateToolState();
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#pluginHidden()
	 */
	public void pluginHidden()
	{
		super.pluginHidden();

		fireEvent("modeler.view.deactivated", this);

		fireEvent("modelerpage.view.showzoomfactor", null);
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#getTitle()
	 */
	public String getTitle()
	{
		if (drawing == null)
		{
			return super.getTitle();
		}

		return super.getTitle() + " - " + drawing.getDisplayText();
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#getSubTitle()
	 */
	public String getSubTitle()
	{
		return processQualifier.toUntypedString();
	}

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#getSubClients()
	 */
	public List getSubClients()
	{
		return Collections.singletonList(drawing);
	}

	//////////////////////////////////////////////////
	// @@ FocusListener implementation
	//////////////////////////////////////////////////

	/**
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent e)
	{
		// Update the cut/copy/paste button status
		fireEvent("global.clipboard.updatestatus");

		// Update process modification state
		if (drawing != null)
		{
			drawing.updateModificationState();
		}

		if (workspaceView != null)
		{
			workspaceView.updateSelection();
		}
	}

	/**
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent e)
	{
		// Update the cut/copy/paste button status
		fireEvent("global.clipboard.updatestatus");

		// Update process modification state
		if (drawing != null)
		{
			drawing.updateModificationState();
		}
	}

	//////////////////////////////////////////////////
	// @@ Trackable implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.drawing.Trackable#getDocumentSize()
	 */
	public Dimension getDocumentSize()
	{
		return workspaceView.getSize();
	}

	/**
	 * @see org.openbp.cockpit.modeler.drawing.Trackable#getVisibleArea()
	 */
	public Rectangle getVisibleArea()
	{
		// The the view rectangle
		Rectangle r = scrollPane.getViewport().getViewRect();

		// Convert to document coordinates
		return workspaceView.applyScale(r, true);
	}

	/**
	 * @see org.openbp.cockpit.modeler.drawing.Trackable#setVisibleArea(Rectangle)
	 */
	public void setVisibleArea(Rectangle r)
	{
		workspaceView.setVisibleRect(r);
	}

	/**
	 * @see org.openbp.cockpit.modeler.drawing.Trackable#centerTrackerAt(Point)
	 */
	public void centerTrackerAt(Point p)
	{
		// Convert to component coordinates
		p = workspaceView.applyScale(p, false);

		Dimension d = scrollPane.getViewport().getSize();

		p.x -= d.width / 2;
		p.y -= d.height / 2;

		// Limit tracking area, so we don't leave the view boundaries
		if (p.x < 0)
		{
			p.x = 0;
		}
		if (p.y < 0)
		{
			p.y = 0;
		}
		if (p.x + d.width > workspaceView.getWidth())
		{
			p.x = workspaceView.getWidth() - d.width;
		}
		if (p.y + d.height > workspaceView.getHeight())
		{
			p.y = workspaceView.getHeight() - d.height;
		}

		scrollPane.getViewport().setViewPosition(p);
	}

	/**
	 * @see org.openbp.cockpit.modeler.drawing.Trackable#moveTrackerBy(int, int)
	 */
	public void moveTrackerBy(int x, int y)
	{
		Rectangle r = getVisibleArea();
		r.translate(x, y);
		workspaceView.scrollRectToVisible(r);
	}

	/**
	 * The listener is registered for all properties as a WEAK listener, i. e. it may
	 * be garbage-collected if not referenced otherwise.<br>
	 * ATTENTION: Never add an automatic class (i. e new FocusListener () { ... }) or an inner
	 * class that is not referenced otherwise as a weak listener to the list. These objects
	 * will be cleared by the garbage collector during the next gc run!
	 *
	 * @see org.openbp.cockpit.modeler.drawing.Trackable#addTrackChangedListener(ChangeListener)
	 */
	public void addTrackChangedListener(ChangeListener listener)
	{
		if (listenerSupport == null)
		{
			listenerSupport = new SwingListenerSupport();
		}
		listenerSupport.addWeakListener(ChangeListener.class, listener);
	}

	/**
	 * @see org.openbp.cockpit.modeler.drawing.Trackable#removeTrackChangedListener(ChangeListener)
	 */
	public void removeTrackChangedListener(ChangeListener listener)
	{
		if (listenerSupport != null)
		{
			listenerSupport.removeListener(ChangeListener.class, listener);
		}
	}

	/**
	 * @see org.openbp.cockpit.modeler.drawing.Trackable#fireTrackChangedEvent(ChangeEvent)
	 */
	public void fireTrackChangedEvent(ChangeEvent event)
	{
		if (!trackSuspended)
		{
			if (listenerSupport != null && listenerSupport.containsListeners(ChangeListener.class))
			{
				listenerSupport.fireStateChanged(event);
			}
		}
	}

	/**
	 * Prevents track change events to be propagated.
	 */
	public void suspendTrack()
	{
		trackSuspended = true;
	}

	/**
	 * Resumes propagation of track change events.
	 */
	public void resumeTrack()
	{
		trackSuspended = false;

		fireTrackChangedEvent(new ChangeEvent(this));
	}

	/**
	 * Returns true if the tracking has been suspended.
	 * @nowarn
	 */
	public boolean isTrackSuspended()
	{
		return trackSuspended;
	}

	/**
	 * Sets the scaling factor of the workspace.
	 * @nowarn
	 */
	public void setScaleFactor(double scaleFactor)
	{
		workspaceView.setScaleFactor(scaleFactor);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Clipboard support
	/////////////////////////////////////////////////////////////////////////

	public boolean canCopy()
	{
		return clipboardSupport != null ? clipboardSupport.canCopy() : false;
	}

	public boolean canDelete()
	{
		return clipboardSupport != null ? clipboardSupport.canDelete() : false;
	}

	public boolean canCut()
	{
		return clipboardSupport != null ? clipboardSupport.canCut() : false;
	}

	public boolean canPaste(Transferable transferable)
	{
		return clipboardSupport != null ? clipboardSupport.canPaste(transferable) : false;
	}

	public Transferable copy()
	{
		return clipboardSupport.getCopyData();
	}

	public Transferable cut()
	{
		startUndo("Cut Selection");
		Transferable result = clipboardSupport.cut();
		endUndo();
		return result;
	}

	public void delete()
	{
		startUndo("Delete Selection");
		clipboardSupport.delete();
		endUndo();
	}

	public void paste(Transferable transferable)
	{
		startUndo("Paste");
		clipboardSupport.paste(transferable);
		endUndo();
	}

	//////////////////////////////////////////////////
	// @@ Undo support
	//////////////////////////////////////////////////

	/**
	 * Creates an undoable object given the display name of the operation that can be undone with this undoable.
	 * The undoable returned will be a {@link ModelerUndoable} that contains a copy of the current process.
	 * The method will also save the returned undoable so it can be retrieved with {@link #getCurrentUndoable}.
	 * In order to provide the data after the operation and to register the undoable, call the {@link #endUndo} method.
	 *
	 * @param displayName Display name or null<br>
	 * This text will appear after the 'undo: ' text in the edit menu.
	 * @return The new undoable
	 */
	public Undoable startUndo(String displayName)
	{
		if (currentUndoable != null)
		{
			// Commit the last transaction
			endUndo();
		}

		ModelerUndoable undoable = new ModelerUndoable(this);
		undoable.setDisplayName(displayName);

		currentUndoable = undoable;

		return undoable;
	}

	/**
	 * Creates an undoable object given the name of the action that initiated the operation that can be undone with this undoable.
	 * For further details, see {@link #startUndo}.
	 *
	 * @param eventName Event name of the action event
	 * @return The new undoable
	 */
	public Undoable startUndoForAction(String eventName)
	{
		String displayName = null;

		JaspiraAction action = ActionMgr.getInstance().getAction(eventName);
		if (action != null)
		{
			displayName = action.getDisplayName();
		}

		return startUndo(displayName);
	}

	/**
	 * Creates an undoable object given the name of the action that initiated the operation that can be undone with this undoable.
	 * For further details, see {@link #startUndo}.
	 *
	 * @param jae Action event
	 * @return The new undoable
	 */
	public Undoable startUndoForAction(JaspiraActionEvent jae)
	{
		return startUndoForAction(jae.getEventName());
	}

	/**
	 * Updates the current undoable with the current 'after operation' state and registers it with the undo manager.
	 * This method may be called only after the {@link #startUndo} method has been called.
	 */
	public void endUndo()
	{
		if (currentUndoable != null)
		{
			getDrawing().setModified();
			undoMgr.registerUndoable(currentUndoable);
			currentUndoable = null;
		}
	}

	/**
	 * Cancels the current undoable.
	 * This method may be called only after the {@link #startUndo} method has been called.
	 */
	public void cancelUndo()
	{
		currentUndoable = null;
	}

	/**
	 * Checks if currently an undo operation is being recorded.
	 *
	 * @return
	 * true: {@link #startUndo} was called.<br>
	 * false: No current undoable is present.
	 */
	public boolean isUndoRecording()
	{
		return currentUndoable != null;
	}

	/**
	 * Gets the current undoable.
	 * @return The undoable or null if the method is not being called between {@link #startUndo} and {@link #endUndo}/{@link #cancelUndo}.
	 */
	public Undoable getCurrentUndoable()
	{
		return currentUndoable;
	}

	/**
	 * Sets the process to be edited due to an undo or redo operation.
	 * The method clones the given process.
	 * @param process Process to be edited
	 */
	public void setProcessByUndoRedo(ProcessItem process)
	{
		// Clone the process
		try
		{
			process = (ProcessItem) process.clone();
		}
		catch (Exception e)
		{
			// Shouldn't happen
			ExceptionUtil.printTrace(e);
		}

		process.maintainReferences(ModelObject.RESOLVE_LOCAL_REFS);

		// Update the drawing.
		// This will also decode the geometry data.
		drawing.setProcess(process);

		// Redraw the view
		fireEvent("modeler.view.redraw");
	}

	/**
	 * Gets the undo manager.
	 * @nowarn
	 */
	public UndoMgr getUndoMgr()
	{
		return undoMgr;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ DrawingEditorPlugin implementation
	/////////////////////////////////////////////////////////////////////////

	/***
	 * @see CH.ifa.draw.framework.DrawingEditor#addViewChangeListener(ViewChangeListener)
	 */
	public void addViewChangeListener(ViewChangeListener arg0)
	{
	}

	/**
	 * @see CH.ifa.draw.framework.FigureSelectionListener#figureSelectionChanged(DrawingView)
	 */
	public void figureSelectionChanged(DrawingView arg0)
	{
	}

	/**
	 * @see CH.ifa.draw.framework.DrawingEditor#getUndoManager()
	 */
	public UndoManager getUndoManager()
	{
		return null;
	}

	/**
	 * @see CH.ifa.draw.framework.DrawingEditor#removeViewChangeListener(ViewChangeListener)
	 */
	public void removeViewChangeListener(ViewChangeListener arg0)
	{
	}

	/**
	 * @see CH.ifa.draw.framework.DrawingEditor#showStatus(String)
	 */
	public void showStatus(String arg0)
	{
		fireEvent("global.status", arg0);
	}

	/**
	 * @see CH.ifa.draw.framework.DrawingEditor#tool()
	 */
	public Tool tool()
	{
		return toolSupport;
	}

	/**
	 * @see CH.ifa.draw.framework.DrawingEditor#toolDone()
	 */
	public void toolDone()
	{
	}

	/**
	 * Returns the currently active view.
	 * @see CH.ifa.draw.framework.DrawingEditor#view()
	 */
	public DrawingView view()
	{
		return workspaceView;
	}

	/**
	 * @see CH.ifa.draw.framework.DrawingEditor#views()
	 */
	public DrawingView [] views()
	{
		return new DrawingView [] { workspaceView };
	}
}
