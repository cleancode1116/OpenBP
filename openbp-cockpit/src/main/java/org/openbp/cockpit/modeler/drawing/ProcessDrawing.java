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
package org.openbp.cockpit.modeler.drawing;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JScrollPane;

import org.openbp.cockpit.itemeditor.ItemCreationUtil;
import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.UpdatableFigure;
import org.openbp.cockpit.modeler.figures.process.FlowConnection;
import org.openbp.cockpit.modeler.figures.process.HLineFigure;
import org.openbp.cockpit.modeler.figures.process.LineFigure;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.figures.process.ParamConnection;
import org.openbp.cockpit.modeler.figures.process.ParamFigure;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;
import org.openbp.cockpit.modeler.figures.process.ProcessElementFigureRegistry;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.cockpit.modeler.figures.process.TextElementFigure;
import org.openbp.cockpit.modeler.figures.process.VLineFigure;
import org.openbp.cockpit.modeler.figures.spline.PolySplineConnection;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;
import org.openbp.cockpit.modeler.skins.Skin;
import org.openbp.cockpit.modeler.util.FigureUtil;
import org.openbp.cockpit.modeler.util.ModelerFlavors;
import org.openbp.common.CommonUtil;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.generic.Modifiable;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.process.ControlLink;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.ItemSynchronization;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeProvider;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessObject;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.core.model.item.process.TextElement;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.action.ActionMgr;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.event.AskEvent;
import org.openbp.jaspira.gui.interaction.DropClientUtil;
import org.openbp.jaspira.gui.interaction.Importer;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.gui.interaction.ViewDropRegion;
import org.openbp.jaspira.plugins.propertybrowser.PropertyBrowserSetEvent;
import org.openbp.jaspira.util.StandardFlavors;
import org.openbp.swing.SwingUtil;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.standard.CompositeFigure;
import CH.ifa.draw.standard.StandardDrawing;

/**
 * A ProcessDrawing is the graphical representation of a OpenBP process.
 * (i.e. a JHotDraw StandardDrawing with Process related elements).
 *
 * @author Stephan Moritz
 */
public class ProcessDrawing extends StandardDrawing
	implements ProcessElementContainer, Modifiable
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The process this drawing represents */
	private ProcessItem process;

	/** Model qualifier of the process */
	private ModelQualifier processQualifier;

	/** Process skin */
	private Skin processSkin;

	/** Visual status of the drawing (see the constants of the {@link VisualElement} class) */
	private int visualStatus;

	/** Status variable: The process is readonly */
	private boolean readOnly;

	/** Editor that edits this drawing */
	private DrawingEditorPlugin editor;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param process The process this drawing represents
	 * @param editor Editor that edits this drawing
	 */
	public ProcessDrawing(ProcessItem process, DrawingEditorPlugin editor)
	{
		super();

		this.editor = editor;

		setProcess(process);
	}

	/**
	 * Gets the process represented by this drawing.
	 * @nowarn
	 */
	public ProcessItem getProcess()
	{
		return process;
	}

	/**
	 * Sets the process represented by this drawing.
	 *
	 * @param process Process to represent or null to clear.
	 * In the latter case, the method will remove all references of the
	 * current process to drawing objects (figures) from the current process, if any.<br>
	 * So call setProces (null) after you closing the modeler associated with this drawing.
	 */
	public void setProcess(ProcessItem process)
	{
		if (this.process != null)
		{
			// Clear the references of the current process to the modeler's figures
			this.process.maintainReferences(ModelObject.UNLINK_FROM_REPRESENTATION);
		}

		processSkin = null;

		if (process != null)
		{
			this.process = process;
			process.setRepresentation(this);

			processQualifier = process.getQualifier();

			// Determine the skin for this process
			processSkin = FigureUtil.determineProcessSkin(process);

			setTitle(process.getDisplayName());

			decodeGeometry();
		}
	}

	/**
	 * Gets the process skin.
	 * @nowarn
	 */
	public Skin getProcessSkin()
	{
		return processSkin;
	}

	/**
	 * Sets the process skin.
	 * @nowarn
	 */
	public void setProcessSkin(Skin processSkin)
	{
		this.processSkin = processSkin;
	}

	/**
	 * Gets the editor that edits this drawing.
	 * @nowarn
	 */
	public DrawingEditorPlugin getEditor()
	{
		return editor;
	}

	/**
	 * Sets the editor of this drawing.
	 * @nowarn
	 */
	public void setEditor(DrawingEditorPlugin editor)
	{
		this.editor = editor;
	}

	/**
	 * Gets the (only) drawing view of this editor.
	 * @nowarn
	 */
	public WorkspaceDrawingView getView()
	{
		return editor != null ? (WorkspaceDrawingView) editor.view() : null;
	}

	/**
	 * Gets the status variable: The process is readonly.
	 * @nowarn
	 */
	public boolean isReadOnly()
	{
		return readOnly;
	}

	/**
	 * Sets the status variable: The process is readonly.
	 * @nowarn
	 */
	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Drawing element access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Gets all top level figures of this drawing.
	 *
	 * @return An iterator of {@link Figure} objects
	 */
	public Iterator getAllFigures()
	{
		return getFigureList().iterator();
	}

	/**
	 * Gets all top level figures of this drawing.
	 *
	 * @return A list of {@link Figure} objects
	 */
	protected List getFigureList()
	{
		List li = new ArrayList();

		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();
			li.add(f);
		}

		return li;
	}

	/**
	 * Gets the node figures of this drawing.
	 *
	 * @return An iterator of {@link NodeFigure} objects
	 */
	public Iterator getNodeFigures()
	{
		return getNodeFigureList().iterator();
	}

	/**
	 * Gets the node figures of this drawing.
	 *
	 * @return A list of {@link NodeFigure} objects
	 */
	protected List getNodeFigureList()
	{
		List li = new ArrayList();

		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();

			if (f instanceof NodeFigure)
			{
				li.add(f);
			}
		}

		return li;
	}

	/**
	 * Gets the parameter connections of this drawing.
	 *
	 * @return An iterator of {@link ParamConnection} objects
	 */
	public Iterator getParamConnections()
	{
		return getParamConnectionList().iterator();
	}

	/**
	 * Gets the parameter connections of this drawing.
	 *
	 * @return A list of {@link ParamConnection} objects
	 */
	protected List getParamConnectionList()
	{
		List li = new ArrayList();

		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();

			if (f instanceof ParamConnection)
			{
				li.add(f);
			}
		}

		return li;
	}

	/**
	 * Gets the flow connections of this drawing.
	 *
	 * @return An iterator of {@link FlowConnection} objects
	 */
	public Iterator getFlowConnections()
	{
		return getFlowConnectionList().iterator();
	}

	/**
	 * Gets the flow connections of this drawing.
	 *
	 * @return A list of {@link FlowConnection} objects
	 */
	protected List getFlowConnectionList()
	{
		List li = new ArrayList();

		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();

			if (f instanceof FlowConnection)
			{
				li.add(f);
			}
		}

		return li;
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#add(Figure)
	 */
	public Figure add(Figure figure)
	{
		if (figure == null)
		{
			return null;
		}

		Figure result = super.add(figure);

		if (result instanceof VisualElement)
		{
			((VisualElement) result).setDrawing(this);
		}

		return result;
	}

	/**
	 * Sets the tag state of all tags of the nodes of this drawing.
	 *
	 * @param stateUpdate State to add to the current state
	 */
	public void setTagState(int stateUpdate)
	{
		// Change the state of all tag figures of each node figure
		for (Enumeration drawingFigures = figures(); drawingFigures.hasMoreElements();)
		{
			Figure f = (Figure) drawingFigures.nextElement();

			if (f instanceof NodeFigure)
			{
				for (FigureEnumeration nodeFigures = ((NodeFigure) f).figures(); nodeFigures.hasMoreElements();)
				{
					Figure nodeChild = nodeFigures.nextFigure();
					if (nodeChild instanceof SocketFigure)
					{
						SocketFigure socketFigure = (SocketFigure) nodeChild;

						int contentState = socketFigure.getContentState();
						contentState &= ~(AbstractTagFigure.CONTENT_FLOW | AbstractTagFigure.CONTENT_DATA);
						contentState |= stateUpdate;
						socketFigure.setContentState(contentState);
					}
				}
			}
		}

		// Positions may have changed, recalculate the connection layout
		layoutAllConnections();
	}

	/**
	 * Recalculate the layout of all connections.
	 */
	public void layoutAllConnections()
	{
		// Changing the tag state may require a recomputation of the connection figures
		for (FigureEnumeration drawingFigures = figures(); drawingFigures.hasMoreElements();)
		{
			Figure f = drawingFigures.nextFigure();

			if (f instanceof PolySplineConnection)
			{
				((PolySplineConnection) f).layoutConnection();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ CompositeFigure overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.standard.StandardDrawing#remove(Figure figure)
	 */
	public Figure remove(Figure figure)
	{
		if (figure instanceof ProcessElementContainer)
		{
			Figure parent = ((ProcessElementContainer) figure).getParentElement();
			if (parent instanceof ProcessDrawing)
			{
				return super.remove(figure);
			}
			else if (parent instanceof CompositeFigure)
			{
				((CompositeFigure) parent).remove(figure);
				figure.release();
				return figure;
			}
		}
		else
		{
			// This will call removeFromContainer and release
			orphan(figure);
		}
		return figure;
	}

	/**
	 * Removes a figure from the drawing.
	 * Resets the propertybrowser beforehand.
	 *
	 * @param figure Figure to be removed
	 */
	public void removeAndUpdate(Figure figure)
	{
		// Clear the PropertyBrowser.
		editor.fireEvent(new PropertyBrowserSetEvent(editor));

		// Update the cut/copy/paste button status
		editor.fireEvent("global.clipboard.updatestatus");

		// Update the modification flag
		setModified();

		// Remove the figure
		remove(figure);

		// Update screen
		// ((WorkspaceDrawingView) editor.view ()).redraw ();
		editor.repairDamage();
	}

	/**
	 * If the user clicks a flow access (e\.g\. to create a new flow connection), it often happens that
	 * he selects an already existing link to the flow access point.
	 * This is due to the fact that the links appear on top of the nodes.
	 * In order to prevent this, we override this method and use figures() instead of the
	 * original figuresReverse() for enumerating the figures.
	 * @see CH.ifa.draw.standard.StandardDrawing#findFigure(int, int)
	 */
	public Figure findFigure(int x, int y)
	{
		FigureEnumeration figures = figures();
		while (figures.hasMoreElements())
		{
			Figure figure = figures.nextFigure();
			if (figure.containsPoint(x, y))
			{
				return figure;
			}
		}
		return null;
	}

	/**
	 * Gets a process element container by the model qualifier of the corrresponding process element.
	 *
	 * @param qualifier Model qualifier of the element
	 * @return The element or null if not found
	 */
	public ProcessElementContainer getFigureByQualifier(ModelQualifier qualifier)
	{
		// We first check if this is the right process.
		if (!CommonUtil.equalsNull(processQualifier.getModel(), qualifier.getModel()))
			return null;
		if (!CommonUtil.equalsNull(processQualifier.getItem(), qualifier.getItem()))
			return null;
		if (qualifier.getItemType() != null && !CommonUtil.equalsNull(processQualifier.getItemType(), qualifier.getItemType()))
			return null;

		ProcessObject po = process.getProcessElementByName(qualifier.getObjectPath());
		if (po == null)
			return null;

		if (po.getRepresentation() == null)
		{
			Throwable t = new Throwable("No representation for qualifier '" + qualifier + "'.");
			ExceptionUtil.printTrace(t);
		}

		return (ProcessElementContainer) po.getRepresentation();
	}

	//////////////////////////////////////////////////
	// @@ Z-order algorithm
	//////////////////////////////////////////////////

	/**
	 * Brings the given figure to front.
	 * Will also bring the parent figure element to the front if its a node figure.
	 * The method will keep the required z-order of the element types (see {@link #recalculateZOrders}).
	 * @nowarn
	 */
	public void bringToFront(Figure f)
	{
		// check if we have a connection - bring it to the front and finish.
		if (!(f instanceof PolySplineConnection || f instanceof TextElementFigure))
		{
			// This should be a node object or one of its sub objects
			while (f instanceof ProcessElementContainer)
			{
				if (f instanceof NodeFigure)
					break;

				f = ((ProcessElementContainer) f).getParentElement();
			}
		}

		if (f != null)
		{
			super.bringToFront(f);
		}

		// Make sure the z-order is correct
		recalculateZOrders();
	}

	/** Comparator for the item nodes */
	private Comparator zComparator = new ZComparator();

	/**
	 * Reorders the figures of the drawing according to their preferred z-order.
	 *
	 * The z-order is important for the figure selection mechanisms to work.
	 * The selection will be determined using reverse z-order. This may seem odd, however, there is a
	 * discrepancy between visibility and selection. For example, data links should appear above the nodes
	 * they connect. However, when clicking a node parameter, the parameter should have a higher selection
	 * precedence than the link, which overlaps the parameter a little. An easy solution was to use reverse
	 * z-ordering selection. This works quite fine because the elements of a drawing are usually not stacked.
	 * Sometime, we may implement a more accurate solution.
	 *
	 * The z-order is as follows:
	 *
	 * Text figures are always positioned to the front (i. e. have the highest z-value).
	 * This ensures visibility and also allows easy selection of other figures.
	 *
	 * Splines follow next. They also should appear above nodes, but node elements need to have selection precedence.
	 *
	 * Nodes appear last in the z-order.
	 */
	protected void recalculateZOrders()
	{
		// Sort according to z-order strategy
		Collections.sort(fFigures, zComparator);

		// Reorder z-values
		int n = fFigures.size();
		for (int i = 0; i < n; ++i)
		{
			Figure f = (Figure) fFigures.elementAt(i);
			f.setZValue(i);
		}

		// Update low/high z value
		_nLowestZ = 0;
		_nHighestZ = n - 1;
	}

	// Z order of the figure types:
	// Line figures in the back, then text element figures, then connections, then node
	private static Class [] figureClassOrder = new Class [] { PolySplineConnection.class, TextElementFigure.class, LineFigure.class, };

	/**
	 * Gets the z order value for the given figure type class.
	 *
	 * @param f Event
	 * @return Figures with z-value 0 appear in the front of the drawing.
	 * Unknown figures will get z-value 0.
	 */
	private static int getFigureTypeZOrder(Figure f)
	{
		for (int i = 0; i < figureClassOrder.length; ++i)
		{
			if (figureClassOrder [i].isInstance(f))
				return i + 1;
		}
		return 0;
	}

	/**
	 * Z-order comparator.
	 */
	private static class ZComparator
		implements Comparator
	{
		/**
		 * @see java.util.Comparator#compare(Object, Object)
		 */
		public int compare(Object o1, Object o2)
		{
			Figure f1 = (Figure) o1;
			Figure f2 = (Figure) o2;

			int fo1 = getFigureTypeZOrder(f1);
			int fo2 = getFigureTypeZOrder(f2);

			if (fo1 == fo2)
			{
				// Identical figures, let the current z-order decide
				return f1.getZValue() - f2.getZValue();
			}

			return fo1 - fo2;
		}
	}

	//////////////////////////////////////////////////
	// @@ DrawingEditor implementation
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.standard.StandardDrawing#displayBox()
	 */
	public Rectangle displayBox()
	{
		int n = fFigures.size();
		if (n <= 0)
		{
			return new Rectangle(0, 0, 0, 0);
		}

		Rectangle r = ((Figure) fFigures.elementAt(0)).displayBox();
		for (int i = 1; i < n; ++i)
		{
			Figure f = (Figure) fFigures.elementAt(i);

			if (f instanceof LineFigure)
			{
				// Line figures have an inifinite dimension, so we skip them
				continue;
			}

			r.add(f.displayBox());
		}

		// Ensure that the display box of the drawing itself always starts at (0,0)
		r.x = r.y = 0;

		return r;
	}

	/**
	 * Returns the upper left corner of the used part of the drawing.
	 * @return The origin
	 */
	public Point getActualOrigin()
	{
		return super.displayBox().getLocation();
	}

	/**
	 * Normalizes the drawing, i\.e\. moves it so that its top left corner lies at 50,50.
	 */
	public void normalize()
	{
		Point p = getActualOrigin();
		moveBy(-p.x + 50, -p.y + 50);

		setModified();
	}

	//////////////////////////////////////////////////
	// @@ Geometry serialization support
	//////////////////////////////////////////////////

	/**
	 * Encodes the geometry of this process, i\.e\. causes all figures to encode their geometry.
	 */
	public void encodeGeometry()
	{
		// Encode the swim lane geometry
		String geometry = encodeProcessGeometry();
		process.setGeometry(geometry);

		// Add dependent objects from the drawing
		for (Enumeration en = figures(); en.hasMoreElements();)
		{
			Figure f = (Figure) en.nextElement();

			if (f instanceof NodeFigure)
			{
				((NodeFigure) f).encodeGeometry();
			}
			else if (f instanceof ParamConnection)
			{
				((ParamConnection) f).encodeGeometry();
			}
			else if (f instanceof FlowConnection)
			{
				((FlowConnection) f).encodeGeometry();
			}
			else if (f instanceof TextElementFigure)
			{
				((TextElementFigure) f).encodeGeometry();
			}
		}
	}

	/**
	 * Encodes the geometry of this process.
	 * Creates figures from the geometry information of the process and
	 * adds them to the drawing.
	 */
	public void decodeGeometry()
	{
		removeAll();

		// Decode the swim lane geometry
		decodeProcessGeometry(process.getGeometry());

		for (Iterator it = process.getNodes(); it.hasNext();)
		{
			NodeFigure p = createNodeFigure((Node) it.next());
			if (p != null)
				add(p);
		}

		for (Iterator it = process.getControlLinks(); it.hasNext();)
		{
			FlowConnection p = createFlowConnection((ControlLink) it.next());
			if (p != null)
				add(p);
		}

		for (Iterator it = process.getDataLinks(); it.hasNext();)
		{
			ParamConnection p = createParamConnection((DataLink) it.next());
			if (p != null)
				add(p);
		}

		for (Iterator it = process.getTextElements(); it.hasNext();)
		{
			TextElementFigure p = createTextElementFigure((TextElement) it.next());
			if (p != null)
			{
				add(p);
			}
		}

		// Make sure the elements appear in the right z-order
		recalculateZOrders();

		// Recalculate the connection layout
		layoutAllConnections();
	}

	/**
	 * Encodes the swim lane geometry of the process.
	 *
	 * @return Geometry string containing "|" and ":" separated tokens
	 */
	protected String encodeProcessGeometry()
	{
		StringBuffer sb = new StringBuffer();

		// Add dependent objects from the drawing
		for (Enumeration en = figures(); en.hasMoreElements();)
		{
			Figure f = (Figure) en.nextElement();

			if (f instanceof LineFigure)
			{
				if (sb.length() > 0)
					sb.append("|");
				if (f instanceof VLineFigure)
					sb.append("vline:");
				else
					sb.append("hline:");
				sb.append(((LineFigure) f).encodeGeometry());
			}
		}

		return sb.length() > 0 ? sb.toString() : null;
	}

	/**
	 * Decodes the swim lane geometry of the process.
	 *
	 * @param geometry Geometry string to decode or null
	 */
	protected void decodeProcessGeometry(String geometry)
	{
		if (geometry == null)
			return;

		StringTokenizer tok = new StringTokenizer(geometry, "|");
		while (tok.hasMoreTokens())
		{
			String t = tok.nextToken();

			int i = t.indexOf(":");
			if (i > 0)
			{
				String ident = t.substring(0, i);

				LineFigure lineFigure = null;

				if (ident.equalsIgnoreCase("vline"))
				{
					lineFigure = new VLineFigure(this);
				}
				else if (ident.equalsIgnoreCase("hline"))
				{
					lineFigure = new HLineFigure(this);
				}

				if (lineFigure != null)
				{
					lineFigure.decodeGeometry(t.substring(i + 1));
					add(lineFigure);
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Figure creation
	//////////////////////////////////////////////////

	/**
	 * Creates a new node figure based on the given node.
	 *
	 * @param node The node
	 * @return The new figure or null on error
	 */
	public NodeFigure createNodeFigure(Node node)
	{
		NodeFigure figure = (NodeFigure) ProcessElementFigureRegistry.getInstance().createProcessElementContainer(node);
		figure.connect(node, this);
		return figure;
	}

	/**
	 * Creates a text element figure based on the given text element.
	 *
	 * @param  textElement The text element
	 * @return The new figure or null on error
	 */
	public TextElementFigure createTextElementFigure(TextElement textElement)
	{
		TextElementFigure figure = new TextElementFigure();
		figure.connect(textElement, this);
		return figure;
	}

	/**
	 * Creates a new flow connection based on the given control link.
	 *
	 * @param link The control link
	 * @return The new figure or null on error
	 */
	public FlowConnection createFlowConnection(ControlLink link)
	{
		NodeSocket source = link.getSourceSocket();
		NodeSocket target = link.getTargetSocket();

		if (source == null)
		{
			System.err.println("Missing source socket for control link '" + link.getQualifier() + "'");
			return null;
		}
		if (target == null)
		{
			System.err.println("Missing target socket for control link '" + link.getQualifier() + "'");
			return null;
		}

		SocketFigure sourceFigure = (SocketFigure) source.getRepresentation();
		if (sourceFigure == null)
		{
			System.err.println("Control link source socket '" + source.getQualifier() + "' has no figure representation.");
			return null;
		}

		SocketFigure targetFigure = (SocketFigure) target.getRepresentation();
		if (targetFigure == null)
		{
			System.err.println("Control link target socket '" + target.getQualifier() + "' has no figure representation.");
			return null;
		}

		Connector start = sourceFigure.connectorAt(0, 0);
		Connector end = targetFigure.connectorAt(0, 0);

		link.unlink();

		FlowConnection flow = new FlowConnection(link, this);
		flow.connectStart(start);
		flow.connectEnd(end);

		return flow;
	}

	/**
	 * Creates a new parameter connection based on the given data link.
	 *
	 * @param link The data link
	 * @return The new figure or null on error
	 */
	public ParamConnection createParamConnection(DataLink link)
	{
		Param source = link.getSourceParam();
		Param target = link.getTargetParam();

		if (source == null)
		{
			System.err.println("Missing source parameter for data link '" + link.getQualifier() + "'");
			return null;
		}
		if (target == null)
		{
			System.err.println("Missing target parameter for data link '" + link.getQualifier() + "'");
			return null;
		}

		ParamFigure sourceFigure = null;
		ParamFigure targetFigure = null;

		if (source instanceof NodeParam)
		{
			sourceFigure = (ParamFigure) source.getRepresentation();

			if (sourceFigure == null)
			{
				System.err.println("Data link source parameter '" + source.getQualifier() + "' has no figure representation.");
				return null;
			}
		}
		if (target instanceof NodeParam)
		{
			targetFigure = (ParamFigure) target.getRepresentation();

			if (targetFigure == null)
			{
				System.err.println("Data link target parameter '" + target.getQualifier() + "' has no figure representation.");
				return null;
			}
		}

		if (source instanceof ProcessVariable && targetFigure != null)
		{
			// Global -> Node
			targetFigure.setProcessVariableConnection((ProcessVariable) source, link);
		}
		else if (target instanceof ProcessVariable && sourceFigure != null)
		{
			// Node -> Global
			sourceFigure.setProcessVariableConnection((ProcessVariable) target, link);
		}
		else if (sourceFigure != null && targetFigure != null)
		{
			// Node -> Node
			Connector start = sourceFigure.connectorAt(0, 0);
			Connector end = targetFigure.connectorAt(0, 0);

			link.unlink();

			return new ParamConnection(link, start, end, this);
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ ProcessElementContainer implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getProcessElement()
	 */
	public ProcessObject getProcessElement()
	{
		return process;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getReferredProcessElement()
	 */
	public ProcessObject getReferredProcessElement()
	{
		return getProcessElement();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#selectionOnDelete()
	 */
	public Figure selectionOnDelete()
	{
		// Can't be deleted, so nop
		return null;
	}

	/**
	 * Finds the process element container at the given position.
	 *
	 * @param x Position in process coordinates
	 * @param y Position in process coordinates
	 * @return The element or null
	 */
	public ProcessElementContainer findProcessElementContainer(int x, int y)
	{
		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();

			if (f instanceof ProcessElementContainer && f.containsPoint(x, y))
			{
				return (ProcessElementContainer) f;
			}
		}

		return null;
	}

	/**
	 * Finds the process element container inside the element at the given position.
	 *
	 * @param x Position in process coordinates
	 * @param y Position in process coordinates
	 * @return The element or null
	 */
	public ProcessElementContainer findProcessElementContainerInside(int x, int y)
	{
		ProcessElementContainer pec = findProcessElementContainer(x, y);

		return (pec == null ? this : pec.findProcessElementContainerInside(x, y));
	}

	//////////////////////////////////////////////////
	// @@ VisualElement implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getDrawing()
	 */
	public ProcessDrawing getDrawing()
	{
		return this;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#setDrawing(ProcessDrawing)
	 */
	public void setDrawing(ProcessDrawing processDrawing)
	{
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getParentElement()
	 */
	public VisualElement getParentElement()
	{
		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getPresentationFigure()
	 */
	public Figure getPresentationFigure()
	{
		return this;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#updatePresentationFigure()
	 */
	public void updatePresentationFigure()
	{
		// No dynamic presentation figure, so do nothing
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#isVisible()
	 */
	public boolean isVisible()
	{
		return (visualStatus & VisualElement.VISUAL_VISIBLE) != 0;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#setVisible(boolean visible)
	 */
	public void setVisible(boolean visible)
	{
		willChange();

		if (visible)
		{
			visualStatus |= VisualElement.VISUAL_VISIBLE;
		}
		else
		{
			visualStatus &= ~VisualElement.VISUAL_VISIBLE;
		}

		changed();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#handleEvent(VisualElementEvent event)
	 */
	public boolean handleEvent(VisualElementEvent event)
	{
		VisualElement child = findVisualElement(event.x, event.y);
		if (child != null)
		{
			return child.handleEvent(event);
		}
		return false;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElement(int, int)
	 */
	public VisualElement findVisualElement(int x, int y)
	{
		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();

			if (f instanceof VisualElement && f.containsPoint(x, y))
			{
				return (VisualElement) f;
			}
		}

		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElementInside(int, int)
	 */
	public VisualElement findVisualElementInside(int x, int y)
	{
		VisualElement child = findVisualElement(x, y);

		return (child != null ? child.findVisualElementInside(x, y) : this);
	}

	public boolean isDisplayAll()
	{
		return (visualStatus & VisualElement.VISUAL_DISPLAY_ALL) != 0;
	}

	public void setDisplayAll(boolean visible)
	{
		willChange();

		if (visible)
		{
			visualStatus |= VisualElement.VISUAL_DISPLAY_ALL;
		}
		else
		{
			visualStatus &= ~VisualElement.VISUAL_DISPLAY_ALL;
		}

		changed();
	}

	//////////////////////////////////////////////////
	// @@ UpdatableFigure implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.UpdatableFigure#updateFigure()
	 */
	public void updateFigure()
	{
		willChange();

		for (FigureEnumeration fe = figures(); fe.hasMoreElements();)
		{
			Figure f = fe.nextFigure();

			if (f instanceof UpdatableFigure)
			{
				((UpdatableFigure) f).updateFigure();
			}
		}

		changed();
	}

	//////////////////////////////////////////////////
	// @@ Delegation of DisplayObject methods to the process
	//////////////////////////////////////////////////

	public String getName()
	{
		return process.getName();
	}

	public void setName(String string)
	{
		process.setName(string);
	}

	public String getDescription()
	{
		return process.getDescription();
	}

	public void setDescription(String string)
	{
		process.setDescription(string);
	}

	public String getDisplayName()
	{
		return process.getDisplayName();
	}

	public void setDisplayName(String string)
	{
		process.setDisplayName(string);
	}

	public String getDisplayText()
	{
		return process.getDisplayText();
	}

	public String getDescriptionText()
	{
		return process.getDescriptionText();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Delegation of Modifiable
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the modified flag for usage by editors.
	 * @nowarn
	 */
	public boolean isModified()
	{
		return process.isModified();
	}

	/**
	 * Sets the modified flag of the process and updates the save button accordingly.
	 * @nowarn
	 */
	public void setModified()
	{
		process.setModified();
		updateModificationState();
	}

	/**
	 * Clears the modified flag of the process and updates the save button accordingly.
	 * @nowarn
	 */
	public void clearModified()
	{
		process.clearModified();
		updateModificationState();
	}

	/**
	 * Updates the status of the save button according to the modification state of the process.
	 */
	public void updateModificationState()
	{
		JaspiraAction action = ActionMgr.getInstance().getAction("standard.file.save");
		if (action != null)
		{
			action.setEnabled(process.isModified());
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ InteractionClient implementation
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragActionTriggered(Object, Point)
	 */
	public void dragActionTriggered(Object regionId, Point p)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragEnded(Transferable)
	 */
	public void dragEnded(Transferable transferable)
	{
		DropClientUtil.dragEnded(this, transferable);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragStarted(Transferable)
	 */
	public void dragStarted(Transferable transferable)
	{
		DropClientUtil.dragStarted(this, transferable);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllDropRegions(List, Transferable, MouseEvent)
	 */
	public List getAllDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		return DropClientUtil.getAllDropRegions(this, flavors, data, mouseEvent);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		String regionId = null;

		if (flavors.contains(ClientFlavors.NODE_PROVIDER))
		{
			regionId = "addNode";
		}
		else if (flavors.contains(ClientFlavors.TEXT_ELEMENT))
		{
			regionId = "addTextElement";
		}
		else if (flavors.contains(ModelerFlavors.HLINE))
		{
			regionId = "addHLine";
		}
		else if (flavors.contains(ModelerFlavors.VLINE))
		{
			regionId = "addVLine";
		}

		if (regionId != null)
		{
			WorkspaceDrawingView view = getView();

			Rectangle viewSize = new Rectangle(view.getSize());
			return Collections.singletonList(new ViewDropRegion(regionId, this, viewSize, view));
		}

		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getImportersAt(Point)
	 */
	public List getImportersAt(Point p)
	{
		WorkspaceDrawingView view = getDrawing().getView();
		JScrollPane pane = SwingUtil.getScrollPaneAncestor(view);

		Rectangle localBounds = SwingUtil.convertBoundsToGlassCoords(pane.getViewport());
		if (localBounds.contains(p))
		{
			List result = new ArrayList();

			result.add(new Importer("addNode", this, new DataFlavor [] { ClientFlavors.NODE_PROVIDER }));
			result.add(new Importer("addTextElement", this, new DataFlavor [] { ClientFlavors.TEXT_ELEMENT }));
			result.add(new Importer("addHLine", this, new DataFlavor [] { ModelerFlavors.HLINE }));
			result.add(new Importer("addVLine", this, new DataFlavor [] { ModelerFlavors.VLINE }));

			return result;
		}

		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllImportersAt(Point)
	 */
	public List getAllImportersAt(Point p)
	{
		WorkspaceDrawingView view = getDrawing().getView();
		JScrollPane pane = SwingUtil.getScrollPaneAncestor(view);

		Rectangle localBounds = SwingUtil.convertBoundsToGlassCoords(pane.getViewport());
		if (localBounds.contains(p))
		{
			Point docPoint = SwingUtil.convertFromGlassCoords(p, view);

			ProcessElementContainer pec = findProcessElementContainer(docPoint.x, docPoint.y);
			if (pec != null && !(pec instanceof PolySplineConnection))
			{
				return pec.getAllImportersAt(p);
			}

			return getImportersAt(p);
		}

		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getSubClients()
	 */
	public List getSubClients()
	{
		return FigureUtil.getTypedFigureList(this, InteractionClient.class);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#importData(Object, Transferable, Point)
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		p = SwingUtil.convertFromGlassCoords(p, getDrawing().getView());

		try
		{
			Figure addedFigure = null;

			if ("addNode".equals(regionId))
			{
				NodeProvider np = (NodeProvider) data.getTransferData(StandardFlavors.OBJECT);

				NodeFigure nodeFigure = createNodeFigureFromNodeProvider(np);
				if (nodeFigure == null)
				{
					// Failed or aborted
					return false;
				}

				editor.startUndo("Add Node");

				Node node = nodeFigure.getNode();
				process.addNode(node);
				node.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

				nodeFigure.displayBox(new Rectangle(p));
				add(nodeFigure);
				addedFigure = nodeFigure;
			}
			else if ("addTextElement".equals(regionId))
			{
				editor.startUndo("Add Text Element");

				TextElement textElement = process.createTextElement();
				process.addTextElement(textElement);

				TextElementFigure textElementFigure = createTextElementFigure(textElement);

				Rectangle db = textElementFigure.displayBox();
				db.x = p.x - db.width / 2;
				db.y = p.y - db.height / 2;
				textElementFigure.displayBox(db);

				add(textElementFigure);

				addedFigure = textElementFigure;
			}
			else if ("addHLine".equals(regionId))
			{
				editor.startUndo("Add Horizontal Swimlane Line");

				HLineFigure hLineFigure = new HLineFigure(this);

				Rectangle db = hLineFigure.displayBox();
				db.x = p.x;
				db.y = p.y;
				hLineFigure.displayBox(db);

				add(hLineFigure);

				addedFigure = hLineFigure;
			}
			else if ("addVLine".equals(regionId))
			{
				editor.startUndo("Add Vertical Swimlane Line");

				VLineFigure vLineFigure = new VLineFigure(this);

				Rectangle db = vLineFigure.displayBox();
				db.x = p.x;
				db.y = p.y;
				vLineFigure.displayBox(db);

				add(vLineFigure);

				addedFigure = vLineFigure;
			}

			if (addedFigure != null)
			{
				editor.endUndo();

				editor.repairDamage();

				// May invoke the autoconnector and others
				editor.fireEvent("modeler.drawing.figureadded", addedFigure);

				editor.repairDamage();
			}
		}
		catch (UnsupportedFlavorException e)
		{
			ExceptionUtil.printTrace(e);
		}
		catch (IOException e)
		{
			ExceptionUtil.printTrace(e);
		}

		return false;
	}

	/**
	 * Creates a new node, given a node provider.
	 *
	 * @param np The node provider
	 * @return The new node
	 */
	private NodeFigure createNodeFigureFromNodeProvider(NodeProvider np)
	{
		int syncFlags = ItemSynchronization.SYNC_ALL_EXCEPT_DESCRIPTION;
		boolean isSkeleton = true;

		if (np instanceof Item)
		{
			Item npItem = (Item) np;
			if (npItem.getRuntimeAttribute(Modeler.ATTRIBUTE_SKELETON) != null)
			{
				// We have a skeleton object. Use its item type to create a new item of this type.
				Model model = process.getModel();

				// Create the item using the item factory
				String processType = null;
				if (npItem instanceof ProcessItem)
				{
					processType = ((ProcessItem) npItem).getProcessType();
				}

				Item newItem = ItemCreationUtil.createItem(model, null, null, npItem.getItemType(), processType);
				if (newItem == null)
				{
					// Editor cancelled
					return null;
				}

				// The new item will serve as node provider
				npItem = newItem;
			}
			else
			{
				// No skeleton, we dragged an existing item.
				// Check if it is currently being edited by a modeler.
				isSkeleton = false;
				AskEvent ae = new AskEvent(editor, "global.edit.geteditedinstance", npItem);
				editor.fireEvent(ae);
				Item editedItem = (Item) ae.getAnswer();
				if (editedItem != null)
				{
					// We will use the edited item instead of the current one as node provider (may be newer!)
					npItem = editedItem;
				}
			}

			// The new item will serve as node provider
			np = (NodeProvider) npItem;

			// Show private entries only if we insert this process as sub process into itself
			if (!npItem.getQualifier().equals(processQualifier))
			{
				// External reference, hide private entries
				syncFlags |= ItemSynchronization.SYNC_HIDE_PRIVATE_ENTRIES;
			}
		}

		// Create a node referencing the item
		Node node = np.toNode(process, syncFlags);

		node.setProcess(process);

		// Make sure that the nodename is unique
		node.setName(process.createUniqueNodeName(node.getName()));

		NodeFigure nodeFigure = createNodeFigure(node);
		if (isSkeleton)
		{
			nodeFigure.setCreatedFromScratch(true);
		}
		return nodeFigure;
	}
}
