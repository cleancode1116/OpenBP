package org.openbp.cockpit.modeler;

import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.process.FlowConnection;
import org.openbp.cockpit.modeler.figures.process.HLineFigure;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.figures.process.ParamConnection;
import org.openbp.cockpit.modeler.figures.process.ParamFigure;
import org.openbp.cockpit.modeler.figures.process.ProcessVariableFigure;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.cockpit.modeler.figures.process.TextElementFigure;
import org.openbp.cockpit.modeler.figures.process.VLineFigure;
import org.openbp.cockpit.modeler.figures.tag.SimpleTextTagFigure;
import org.openbp.cockpit.modeler.tools.FlowConnectionTool;
import org.openbp.cockpit.modeler.tools.ModelerTool;
import org.openbp.cockpit.modeler.tools.ModelerToolSupport;
import org.openbp.cockpit.modeler.tools.ParamConnectionTool;
import org.openbp.cockpit.modeler.tools.ParamReorderTracker;
import org.openbp.cockpit.modeler.tools.RotationTracker;
import org.openbp.cockpit.modeler.tools.RubberBandTool;
import org.openbp.cockpit.modeler.tools.ScaleTracker;
import org.openbp.cockpit.modeler.tools.ScrollTool;
import org.openbp.cockpit.modeler.tools.SimpleSelectTool;
import org.openbp.cockpit.modeler.tools.XDragTracker;
import org.openbp.cockpit.modeler.tools.XHandleTracker;
import org.openbp.cockpit.modeler.tools.ZoomTool;
import org.openbp.cockpit.modeler.util.InputState;

import CH.ifa.draw.framework.Handle;

/**
 * Standard tool support setup.
 *
 * @author Heiko Erhardt
 */
public final class StandardToolSupportSetup
{
	/**
	 * Private constructor prevents instantiation.
	 */
	private StandardToolSupportSetup()
	{
	}

	public static void setupToolSupport(ModelerToolSupport toolSupport, boolean isModeler)
	{
		ModelerTool tool;

		// Process variable -> select
		tool = new SimpleSelectTool(toolSupport);
		toolSupport.addToolDecisionTableEntry(tool, ProcessVariableFigure.class, 0);

		// Flow connection -> select
		tool = new SimpleSelectTool(toolSupport);
		tool.setToolCursor(ModelerGraphics.moveSplinePointCursor);
		tool.setToolHintMsg(ModelerGraphics.getMsg("msg.spline"));
		toolSupport.addToolDecisionTableEntry(tool, FlowConnection.class, 0);

		// Param connection -> select
		tool = new SimpleSelectTool(toolSupport);
		tool.setToolCursor(ModelerGraphics.moveSplinePointCursor);
		tool.setToolHintMsg(ModelerGraphics.getMsg("msg.spline"));
		toolSupport.addToolDecisionTableEntry(tool, ParamConnection.class, 0);

		// Text element -> drag
		tool = new XDragTracker(toolSupport);
		tool.setToolCursor(ModelerGraphics.moveTextCursor);
		tool.setToolHintMsg(ModelerGraphics.getMsg("msg.textelement"));
		toolSupport.addToolDecisionTableEntry(tool, TextElementFigure.class, 0);

		// Horizontal line -> drag
		tool = new XDragTracker(toolSupport);
		tool.setToolCursor(ModelerGraphics.moveHLineCursor);
		tool.setToolHintMsg(ModelerGraphics.getMsg("msg.line"));
		toolSupport.addToolDecisionTableEntry(tool, HLineFigure.class, 0);

		// Vertical line -> drag
		tool = new XDragTracker(toolSupport);
		tool.setToolCursor(ModelerGraphics.moveVLineCursor);
		tool.setToolHintMsg(ModelerGraphics.getMsg("msg.line"));
		toolSupport.addToolDecisionTableEntry(tool, VLineFigure.class, 0);

		if (isModeler)
		{
			// Node -> drag
			tool = new XDragTracker(toolSupport);
			tool.setToolCursor(ModelerGraphics.moveNodeCursor);
			tool.setToolHintMsg(ModelerGraphics.getMsg("msg.node"));
			toolSupport.addToolDecisionTableEntry(tool, NodeFigure.class, 0);
		}
		else
		{
			// Node -> select
			tool = new SimpleSelectTool(toolSupport);
			tool.setToolCursor(ModelerGraphics.moveNodeCursor);
			tool.setToolHintMsg(ModelerGraphics.getMsg("msg.nodefixed"));
			toolSupport.addToolDecisionTableEntry(tool, NodeFigure.class, 0);
		}

		// CTRL Node -> scale
		tool = new ScaleTracker(toolSupport);
		tool.setToolCursor(ModelerGraphics.resizeNodeCursor);
		if (isModeler)
		{
			tool.setToolHintMsg(ModelerGraphics.getMsg("msg.node"));	// For modeler only
		}
		else
		{
			tool.setToolHintMsg(ModelerGraphics.getMsg("msg.nodefixed"));	// For node editor only
		}
		toolSupport.addToolDecisionTableEntry(tool, NodeFigure.class, InputState.CTRL);

		// CTRL Param -> reorder
		tool = new ParamReorderTracker(toolSupport);
		tool.setToolCursor(ModelerGraphics.moveParamCursor);
		tool.setToolHintMsg(ModelerGraphics.getMsg("msg.param"));
		toolSupport.addToolDecisionTableEntry(tool, ParamFigure.class, InputState.CTRL);

		if (! isModeler)
		{
			// Param -> reorder
			tool.setToolHintMsg(ModelerGraphics.getMsg("msg.paramfixed"));
			toolSupport.addToolDecisionTableEntry(tool, ParamFigure.class, 0);
		}

		if (isModeler)
		{
			// Param -> connect
			tool = new ParamConnectionTool(toolSupport);
			tool.setToolCursor(ModelerGraphics.createDataLinkCursor);
			tool.setToolHintMsg(ModelerGraphics.getMsg("msg.param"));
			toolSupport.addToolDecisionTableEntry(tool, ParamFigure.class, 0);
		}

		tool = new RotationTracker(toolSupport);
		tool.setToolCursor(ModelerGraphics.moveTextCursor);
		tool.setToolHintMsg(ModelerGraphics.getMsg("msg.tag"));
		toolSupport.addToolDecisionTableEntry(tool, SimpleTextTagFigure.class, 0);

		// CTRL Socket -> rotate
		tool = new RotationTracker(toolSupport);
		tool.setToolCursor(ModelerGraphics.moveSocketCursor);
		tool.setToolHintMsg(ModelerGraphics.getMsg("msg.socket"));
		toolSupport.addToolDecisionTableEntry(tool, SocketFigure.class, InputState.CTRL);

		if (isModeler)
		{
			// Socket -> connect
			tool = new FlowConnectionTool(toolSupport);
			tool.setToolCursor(ModelerGraphics.createControlLinkCursor);
			tool.setToolHintMsg(ModelerGraphics.getMsg("msg.socket"));
			toolSupport.addToolDecisionTableEntry(tool, SocketFigure.class, 0);
		}
		else
		{
			toolSupport.addToolDecisionTableEntry(tool, SocketFigure.class, 0);
		}

		tool = new RubberBandTool(toolSupport);
		tool.setToolCursor(ModelerGraphics.standardCursor);
		toolSupport.addToolDecisionTableEntry(tool, ProcessDrawing.class, 0);

		tool = new ZoomTool(toolSupport);
		tool.setToolCursor(ModelerGraphics.zoomCursor);
		toolSupport.addToolDecisionTableEntry(tool, ProcessDrawing.class, InputState.ALT | InputState.CTRL | InputState.HOVER);

		tool = new ScrollTool(toolSupport);
		tool.setToolCursor(ModelerGraphics.handScrollCursor);
		toolSupport.addToolDecisionTableEntry(tool, ProcessDrawing.class, InputState.ALT | InputState.HOVER);

		tool = new XHandleTracker(toolSupport);
		tool.setToolCursor(ModelerGraphics.moveSplineHandleCursor);
		toolSupport.addToolDecisionTableEntry(tool, Handle.class, 0);
	}
}

