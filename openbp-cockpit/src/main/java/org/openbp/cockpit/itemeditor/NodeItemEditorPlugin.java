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
package org.openbp.cockpit.itemeditor;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openbp.cockpit.modeler.ClipboardSupport;
import org.openbp.cockpit.modeler.StandardToolSupportSetup;
import org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.drawing.Trackable;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.generic.Colorizable;
import org.openbp.cockpit.modeler.figures.process.MultiSocketNodeFigure;
import org.openbp.cockpit.modeler.tools.ModelerToolSupport;
import org.openbp.cockpit.modeler.util.ModelerFlavors;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.listener.SwingListenerSupport;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessItemImpl;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.clipboard.ClipboardMgr;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.InteractionModule;
import org.openbp.jaspira.plugins.propertybrowser.PropertyBrowserSaveEvent;
import org.openbp.jaspira.undo.Undoable;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Tool;
import CH.ifa.draw.framework.ViewChangeListener;
import CH.ifa.draw.util.UndoManager;

/**
 * This plugin represents an editor for a single node
 *
 * @author Stephan Moritz
 */
public class NodeItemEditorPlugin extends AbstractVisiblePlugin
	implements DrawingEditorPlugin, Trackable, ComponentListener, FocusListener
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** The modeler tool support object holding all tools */
	private ModelerToolSupport toolSupport;

	/** The (only) drawing view of this editor. */
	protected WorkspaceDrawingView workspaceView;

	/** Process drawing */
	protected ProcessDrawing drawing;

	/** The single figure of our dummy process. */
	private MultiSocketNodeFigure figure;

	/** the scroll pane of our View */
	private JScrollPane scrollPane;

	/** Clipboard support helper class */
	private ClipboardSupport clipboardSupport;

	/** Listener support object holding the listeners */
	private SwingListenerSupport listenerSupport;

	/** If true, do not notify miniview about updates */
	private boolean trackSuspended;

	//////////////////////////////////////////////////
	// @@ Public interface
	//////////////////////////////////////////////////

	public String getResourceCollectionContainerName()
	{
		return "plugin.cockpit";
	}

	/**
	 * Sets the node to edit.
	 *
	 * @param node The node
	 */
	public void setNode(Node node)
	{
		clipboardSupport = new ClipboardSupport(workspaceView, getPluginResourceCollection(), true);

		// Get the node's dummy process
		ProcessItem process = node.getProcess();
		drawing = new ProcessDrawing(process, this);

		// In the component editor, we always display parameters and flow connectors
		drawing.setDisplayAll(true);

		figure = (MultiSocketNodeFigure) drawing.figureAt(0);

		positionFigure();

		workspaceView.setDrawing(drawing);
		workspaceView.singleSelect(figure);

		addPluginFocusListener(this);
	}

	/**
	 * Saves the edited node.
	 */
	public void saveNode()
	{
		figure.encodeGeometry();
	}

	/**
	 * Gets the the modeler tool support object holding all tools.
	 * @nowarn
	 */
	public ModelerToolSupport getToolSupport()
	{
		return toolSupport;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#initializeComponents()
	 */
	protected void initializeComponents()
	{
		workspaceView = new WorkspaceDrawingView(this);

		// We don't need no oversize for a one-node workspace (to prevent unnessecary scrolling)
		workspaceView.setSizeOffset(0);

		// This is just temporary; will be reinitialized when calling setNode
		drawing = new ProcessDrawing(new ProcessItemImpl(), this);
		workspaceView.setDrawing(drawing);

		toolSupport = new ModelerToolSupport(this);
		StandardToolSupportSetup.setupToolSupport(toolSupport, false);

		scrollPane = new JScrollPane(workspaceView);
		getPluginComponent().add(scrollPane);

		getPluginComponent().addComponentListener(this);
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#pluginShown()
	 */
	public void pluginShown()
	{
		super.pluginShown();

		if (figure != null)
		{
			figure.updateFigure();

			positionFigure();

			workspaceView.singleSelect(figure);

			toolSupport.updateToolState();
		}
	}

	/**
	 * Positions the node figure in the center of the workspace.
	 */
	private void positionFigure()
	{
		figure.displayBox(new Rectangle(getPluginComponent().getSize()));
	}

	//////////////////////////////////////////////////
	// @@ ComponentListener implementation
	//////////////////////////////////////////////////

	/**
	 * @see java.awt.event.ComponentListener#componentResized(ComponentEvent)
	 */
	public void componentResized(ComponentEvent e)
	{
		if (figure != null)
		{
			positionFigure();

			workspaceView.redraw();
		}
	}

	/**
	 * @see java.awt.event.ComponentListener#componentHidden(ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e)
	{
	}

	/**
	 * @see java.awt.event.ComponentListener#componentShown(ComponentEvent)
	 */
	public void componentShown(ComponentEvent e)
	{
	}

	/**
	 * @see java.awt.event.ComponentListener#componentMoved(ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e)
	{
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ DrawingEditor implementation
	/////////////////////////////////////////////////////////////////////////

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
	 * @see CH.ifa.draw.framework.DrawingEditor#addViewChangeListener(ViewChangeListener)
	 */
	public void addViewChangeListener(ViewChangeListener arg0)
	{
		// Not supported
	}

	/**
	 * @see CH.ifa.draw.framework.DrawingEditor#removeViewChangeListener(ViewChangeListener)
	 */
	public void removeViewChangeListener(ViewChangeListener arg0)
	{
		// Not supported
	}

	/**
	 * @see CH.ifa.draw.framework.DrawingEditor#showStatus(String)
	 */
	public void showStatus(String arg0)
	{
		// Not supported
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

	public void repairDamage()
	{
		workspaceView.repairDamage();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ D&D/Clipboard
	/////////////////////////////////////////////////////////////////////////

	public List getSubClients()
	{
		if (figure != null)
		{
			return Collections.singletonList(figure);
		}
		return null;
	}

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
		return clipboardSupport != null ? clipboardSupport.canCopy() && clipboardSupport.canDelete() : false;
	}

	public boolean canPaste(Transferable transferable)
	{
		if (transferable != null)
		{
			if (transferable.isDataFlavorSupported(ClientFlavors.NODE_SOCKETS))
				return true;

			if (transferable.isDataFlavorSupported(ClientFlavors.NODE_PARAMS))
				return true;
		}
		return false;
	}

	public Transferable copy()
	{
		Transferable transferable = clipboardSupport.getCopyData();
		ClipboardMgr.getInstance().addEntry(transferable);
		return transferable;
	}

	public Transferable cut()
	{
		Transferable transferable = clipboardSupport.cut();
		ClipboardMgr.getInstance().addEntry(transferable);
		return transferable;
	}

	public void delete()
	{
		clipboardSupport.delete();
	}

	public void paste(Transferable transferable)
	{
		clipboardSupport.paste(transferable);
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
	}

	/**
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent e)
	{
		// Update the cut/copy/paste button status
		fireEvent("global.clipboard.updatestatus");
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

	//////////////////////////////////////////////////
	// @@ Undo support
	//////////////////////////////////////////////////

	/**
	 * The node item editor doesn't support undo, so this method does nothing.
	 * @see org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin#startUndo(String displayName)
	 * @return Always null
	 */
	public Undoable startUndo(String displayName)
	{
		return null;
	}

	/**
	 * The node item editor doesn't support undo, so this method does nothing.
	 * @see org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin#endUndo()
	 */
	public void endUndo()
	{
	}

	/**
	 * The node item editor doesn't support undo, so this method does nothing.
	 * @see org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin#cancelUndo()
	 */
	public void cancelUndo()
	{
	}

	/**
	 * The node item editor doesn't support undo, so this method does nothing.
	 * @see org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin#isUndoRecording()
	 * @return Always false
	 */
	public boolean isUndoRecording()
	{
		return false;
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
			return "plugin.nodeeditor";
		}

		/**
		 * Event handler: Handle the save object event from the property browser.
		 *
		 * @event plugin.propertybrowser.executesave
		 * @param oee Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode plugin_propertybrowser_executesave(PropertyBrowserSaveEvent oee)
		{
			// In contrast to the regular modeler plugin, the object can be an item object also,
			// not just a ProcessObject (the WorkspaceDrawingView will show the underlying activity
			// in the property browser if the node was clicked instead of the activity node)
			if (!(oee.original instanceof ModelObject))
				return EVENT_IGNORED;

			ModelObject orig = (ModelObject) oee.original;

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

			drawing.updateFigure();
			drawing.invalidate();
			workspaceView.checkDamage();

			oee.saved = true;

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Tries to import the event object into each currently selected element.
		 *
		 * @event modeler.view.importtoselection
		 * @param je Event
		 * @return EVENT_HANDLED
		 */
		public JaspiraEventHandlerCode modeler_view_importtoselection(JaspiraEvent je)
		{
			boolean imported = false;
			String regionId = null;
			Transferable transferable = (Transferable) je.getObject();

			// Check if the selection contains something that is flipable
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

			if (imported)
			{
				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Interaction module
	//////////////////////////////////////////////////

	/**
	 * Interaction module.
	 */
	public class InteractionEvents extends InteractionModule
	{
		/**
		 * Gets the module priority.
		 * We are high priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 5;
		}

		/**
		 * Standard event handler that is called when a popup menu is to be shown.
		 * Adds the popup menu entries for node sockets and parameters.
		 *
		 * @event global.interaction.popup
		 * @param ie Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode popup(final InteractionEvent ie)
		{
			if (ie.getSourcePlugin() != NodeItemEditorPlugin.this)
				return EVENT_IGNORED;

			DataFlavor [] flavor = ie.getTransferDataFlavors();

			JaspiraAction group = new JaspiraAction("popup", null, null, null, null, 100, JaspiraAction.TYPE_GROUP);

			for (int i = 0; i < flavor.length; i++)
			{
				if (flavor [i].equals(ModelerFlavors.FIGURE))
				{
					Figure figure = (Figure) ie.getSafeTransferData(flavor [i]);
					workspaceView.singleSelect(figure);
				}

				if (flavor [i].equals(ModelerFlavors.COLORIZABLE))
				{
					final Colorizable col = (Colorizable) ie.getSafeTransferData(flavor [i]);

					if (col.getFillColor() != null && !col.getFillColor().equals(col.getDefaultFillColor()))
					{
						group.addMenuChild(new JaspiraAction(NodeItemEditorPlugin.this, "modeler.edit.resetcolor")
						{
							public void actionPerformed(ActionEvent e)
							{
								col.setFillColor(col.getDefaultFillColor());
								col.invalidate();
							}
						});
					}
				}

				final NodeItemEditorPlugin modeler = NodeItemEditorPlugin.this;
				boolean copyEnabled = modeler.canCopy();
				boolean cutEnabled = modeler.canCut();
				boolean deleteEnabled = modeler.canDelete();
				boolean pasteEnabled = modeler.canPaste(ClipboardMgr.getInstance().getCurrentEntry());
				if (copyEnabled || deleteEnabled || cutEnabled || pasteEnabled)
				{
					JaspiraAction copyPasteGroup = new JaspiraAction("copypaste", null, null, null, null, 2, JaspiraAction.TYPE_GROUP);

					JaspiraAction ja;

					ja = new JaspiraAction(modeler, "modeler.edit.copy")
					{
						public void actionPerformed(ActionEvent e)
						{
							copy();
						}
					};
					ja.setEnabled(copyEnabled);
					copyPasteGroup.addMenuChild(ja);

					ja = new JaspiraAction(modeler, "modeler.edit.cut")
					{
						public void actionPerformed(ActionEvent e)
						{
							cut();
						}
					};
					ja.setEnabled(cutEnabled);
					copyPasteGroup.addMenuChild(ja);

					ja = new JaspiraAction(modeler, "modeler.edit.paste")
					{
						public void actionPerformed(ActionEvent e)
						{
							Transferable transferable = ClipboardMgr.getInstance().getCurrentEntry();
							paste(transferable);
						}
					};
					ja.setEnabled(pasteEnabled);
					copyPasteGroup.addMenuChild(ja);

					ie.add(copyPasteGroup);

					ja = new JaspiraAction(modeler, "modeler.edit.delete")
					{
						public void actionPerformed(ActionEvent e)
						{
							delete();
						}
					};
					ja.setEnabled(deleteEnabled);
					ie.add(ja);

				}
			}

			ie.add(group);

			return EVENT_HANDLED;
		}
	}
}
