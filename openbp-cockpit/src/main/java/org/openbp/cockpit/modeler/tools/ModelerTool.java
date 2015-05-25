package org.openbp.cockpit.modeler.tools;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin;
import org.openbp.cockpit.modeler.drawing.Trackable;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;

/**
 * Abstract modeler tool.
 *
 * @author Heiko Erhardt
 */
public abstract class ModelerTool
{
	/** Tool support object that owns this tool */
	private ModelerToolSupport toolSupport;

	/** Tool cursor */
	private Cursor toolCursor;

	/** Hint message for this tool */
	private String toolHintMsg;

	/** Affected figure */
	private Object affectedObject;

	/**
	 * Default constructor.
	 * @param toolSupport Tool support object that owns this tool
	 */
	public ModelerTool(ModelerToolSupport toolSupport)
	{
		this.toolSupport = toolSupport;
	}

	/**
	 * Checks if the tool can be applied to the given figure.
	 *
	 * @param affectedObject Object the cursor is over
	 * @return true if the tool is suitable for the figure
	 */
	public boolean appliesTo(Object affectedObject)
	{
		return true;
	}

	/**
	 * Gets the tool cursor.
	 * @nowarn
	 */
	public Cursor getToolCursor()
	{
		return toolCursor;
	}

	/**
	 * Sets the tool cursor.
	 * @nowarn
	 */
	public void setToolCursor(Cursor toolCursor)
	{
		this.toolCursor = toolCursor;
	}

	/**
	 * Gets the hint message for this tool.
	 * @nowarn
	 */
	public String getToolHintMsg()
	{
		return toolHintMsg;
	}

	/**
	 * Sets the hint message for this tool.
	 * @nowarn
	 */
	public void setToolHintMsg(String toolHintMsg)
	{
		this.toolHintMsg = toolHintMsg;
	}

	/**
	 * Gets the affected figure.
	 * @nowarn
	 */
	public Figure getAffectedFigure()
	{
		return (Figure) affectedObject;
	}

	/**
	 * Gets the affected object.
	 * @nowarn
	 */
	public Object getAffectedObject()
	{
		return affectedObject;
	}

	/**
	 * Sets the affected object.
	 * @nowarn
	 */
	public void setAffectedObject(Object affectedObject)
	{
		this.affectedObject = affectedObject;
	}

	/**
	 * Gets the editor that owns this tool.
	 *
	 * @return The editor
	 */
	public DrawingEditorPlugin getEditor()
	{
		return toolSupport.getEditor();
	}

	/**
	 * Gets the view that owns this tool.
	 *
	 * @return The view
	 */
	public WorkspaceDrawingView getView()
	{
		return toolSupport.getView();
	}

	public Drawing getDrawing()
	{
		return toolSupport.getView().drawing();
	}

	/**
	 * Returns the last known mouse coordinates.
	 * Used for break out box display.
	 * @nowarn
	 */
	public Point getLastPoint()
	{
		return toolSupport.getLastPoint();
	}

	/**
	 * Gets the tool support object that owns this tool.
	 * @nowarn
	 */
	public ModelerToolSupport getToolSupport()
	{
		return toolSupport;
	}

	//////////////////////////////////////////////////
	// @@ AbstractTool overrides
	//////////////////////////////////////////////////

	public void activate()
	{
		if (getEditor() instanceof Trackable)
		{
			// Suspend tracking while some tool is active
			((Trackable) getEditor()).suspendTrack();
		}
	}

	public void deactivate()
	{
		getEditor().cancelUndo();

		if (getEditor() instanceof Trackable)
		{
			// Resume tracking again
			((Trackable) getEditor()).resumeTrack();
		}

		setAffectedObject(null);
		toolSupport.clearTool();
	}

	public void mouseMove(MouseEvent e, int x, int y)
	{
	}

	public void mouseDown(MouseEvent e, int x, int y)
	{
	}

	public void mouseUp(MouseEvent e, int x, int y)
	{
		deactivate();
	}

	public void mouseDrag(MouseEvent e, int x, int y)
	{
	}

	public void keyDown(KeyEvent e, int key)
	{
	}

	public void keyUp(KeyEvent e, int key)
	{
		deactivate();
	}
}
