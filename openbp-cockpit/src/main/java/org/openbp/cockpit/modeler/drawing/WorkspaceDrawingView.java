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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.PrintGraphics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.openbp.cockpit.itemeditor.NodeItemEditorPlugin;
import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.drawing.shadowlayout.ParallelProjectionShadowLayouter;
import org.openbp.cockpit.modeler.drawing.shadowlayout.ShadowLayouter;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.ChildFigure;
import org.openbp.cockpit.modeler.figures.generic.Colorizable;
import org.openbp.cockpit.modeler.figures.process.LineFigure;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;
import org.openbp.cockpit.modeler.skins.Skin;
import org.openbp.cockpit.modeler.tools.ModelerToolSupport;
import org.openbp.cockpit.modeler.util.InputState;
import org.openbp.cockpit.modeler.util.ShadowEnumerator;
import org.openbp.common.CommonUtil;
import org.openbp.common.string.TextUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.process.MultiSocketNode;
import org.openbp.core.model.item.process.SubprocessNode;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.action.JaspiraPopupMenu;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.gui.interaction.BasicTransferable;
import org.openbp.jaspira.gui.interaction.BreakoutEvent;
import org.openbp.jaspira.gui.interaction.BreakoutProvider;
import org.openbp.jaspira.gui.interaction.DragDropPane;
import org.openbp.jaspira.gui.interaction.MultiTransferable;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionMgr;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugins.propertybrowser.PropertyBrowserSetEvent;
import org.openbp.swing.Scalable;
import org.openbp.swing.SwingUtil;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingChangeEvent;
import CH.ifa.draw.framework.DrawingChangeListener;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.standard.CompositeFigure;
import CH.ifa.draw.standard.SimpleUpdateStrategy;
import CH.ifa.draw.standard.StandardDrawingView;

/**
 * This is the standard drawing view of the modeler.
 *
 * @author Jens Ferchland
 */
public class WorkspaceDrawingView extends StandardDrawingView
	implements DrawingChangeListener, Scalable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Grid type: Point*/
	public static final int GRIDTYPE_POINT = 1;

	/** Grid type: Line */
	public static final int GRIDTYPE_LINE = 2;

	/** Grid type: Hexadecimal grid */
	public static final int GRIDTYPE_HEX = 3;

	/** Minimum width of this view */
	private static final int MINWIDTH = 640;

	/** Minimum height of this view */
	private static final int MINHEIGHT = 480;

	/** Offset to be added to the size of the drawing to enalbe convenient drag and drop to the right and below the drawing */
	private static final int SIZE_OFFSET = 300;

	/** Minimum scaling factor */
	private static final double MIN_SCALE_FACTOR = 0.25;

	/** Maximum scaling factor */
	private static final double MAX_SCALE_FACTOR = 2.0;

	//////////////////////////////////////////////////
	// @@ Drawing properties
	//////////////////////////////////////////////////

	/** Scaling factor */
	private double scaleFactor = 1.0d;

	/** Size offset for workspace view enlargement */
	private int sizeOffset = SIZE_OFFSET;

	// TODO Refactor 6: The following option should be kept as ModelerOption members

	/** Grid visibility */
	private boolean gridDisplayed;

	/** Grid layout type */
	private int gridType = GRIDTYPE_LINE;

	/** Grid spacing */
	private int gridSpacing = 100;

	/** Shadow layouter */
	private ShadowLayouter shadowLayouter = new ParallelProjectionShadowLayouter(5, 5);

	/** Decorator for the current selection */
	private SelectionDecorator selectionDecorator;

	//////////////////////////////////////////////////
	// @@ Status variables
	//////////////////////////////////////////////////

	/** List of currently selected objects (contains {@link Figure} objects) */
	private List selection;

	/** Cache of currently active selection handles (contains Handle objects) */
	private Vector activeHandles;

	/** Figure currently under the cursor or null */
	private Figure figureUnderCursor;

	/** Indicates that the mouse moves within the view */
	protected boolean mouseInView;

	/** Current cursor */
	private Cursor cursor;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param editor Editor that uses the view
	 */
	public WorkspaceDrawingView(final DrawingEditorPlugin editor)
	{
		super(editor, MINWIDTH, MINHEIGHT);

		// This will prevent some default FlowLayoutManager from moving ad-hoc controls
		// such as in-place editors around the workspace. 
		setLayout(null);

		// Set the background color
		Color workspaceColor = ModelerColors.WORKSPACE;
		Option o = OptionMgr.getInstance().getOption("editor.color.workspace");
		if (o != null)
		{
			workspaceColor = (Color) o.getValue();
		}
		setBackground(workspaceColor);

		// setDisplayUpdate (new BufferedUpdateStrategy ());
		setDisplayUpdate(new SimpleUpdateStrategy());

		ToolTipManager.sharedInstance().registerComponent(this);

		// Decorator handles its own adding to the decomgr.
		selectionDecorator = new SelectionDecorator(this, editor);
		selection = new LinkedList();

		// Mouse and mouse wheel handling
		addMouseListener(new MouseAdapter()
		{
			public void mouseEntered(final MouseEvent e)
			{
				mouseInView = true;
			}

			public void mouseExited(final MouseEvent e)
			{
				mouseInView = false;
			}
		});
		addMouseWheelListener(new MouseWheelListener()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				int notches = e.getWheelRotation();

				if (InputState.isCtrlDown())
				{
					invalidate();

					// Scale according to mouse movement using heuristic values
					double scaleFactor = getScaleFactor();
					scaleFactor *= (100d + (notches * 2)) / 100;
					setScaleFactor(scaleFactor);

					redraw();
				}
				else
				{
					int mult = e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL ? 25 : 100;
					((Trackable) editor()).moveTrackerBy(0, notches * mult);
				}
			}
		});

		// Add support for selection and workspace movement by cursor keys
		addCursorKeySupport();

		// Breakout menu support for standard and user toolbox breakout
		addBreakoutSupport("breakout", KeyEvent.VK_SPACE);
		addBreakoutSupport("userbreakout", KeyEvent.VK_1);

		// Load user options
		gridType = OptionMgr.getInstance().getIntegerOption("editor.grid.type", gridType);
		gridDisplayed = OptionMgr.getInstance().getBooleanOption("editor.grid.display", gridDisplayed);
		o = OptionMgr.getInstance().getOption("editor.shadow");
		if (o != null)
		{
			shadowLayouter = (ShadowLayouter) o.getValue();
		}
	}

	/**
	 * Performs necessary cleanup-work when the view is not needed anymore - should allow
	 * the view to be processed by the garbage-collection.
	 */
	public void unregister()
	{
		// Remove from tooltip manager
		ToolTipManager.sharedInstance().unregisterComponent(this);

		// Remove our selectiondecorator
		selectionDecorator.uninstall();

		// Disconnect from the drawing
		if (drawing() != null)
			drawing().removeDrawingChangeListener(this);

		// Clear references for better garbage collection in case of memory leaks
		setEditor(null);
		selectionDecorator = null;
		activeHandles = null;
		selection = null;
	}

	public void setDrawing(final Drawing drawing)
	{
		if (drawing() != null)
		{
			// If the drawing changes, disconnect from the old drawing
			drawing().removeDrawingChangeListener(this);
		}

		super.setDrawing(drawing);

		if (drawing() != null)
		{
			// We need to be updated when the drawing changes
			drawing.addDrawingChangeListener(this);
		}
	}

	//////////////////////////////////////////////////
	// @@ JComponent overrides
	//////////////////////////////////////////////////

	/**
	 * Links the preferred size of the component to the size of the drawing.
	 * @return The size of the drawing + sizeOffset per axis
	 */
	public Dimension getPreferredSize()
	{
		if (drawing() == null)
			return new Dimension();

		// we make the space larger so the view grows automatically
		Rectangle r = ((Figure) drawing()).displayBox();
		r = applyScale(r, false);

		int offset = applyScale(sizeOffset, false);
		Dimension d = new Dimension(r.width + offset, r.height + offset);
		return d;
	}

	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}

	public void setPreferredSize(final Dimension d)
	{
		if (d.width < MINWIDTH)
		{
			d.width = MINWIDTH;
		}

		if (d.height < MINHEIGHT)
		{
			d.height = MINHEIGHT;
		}

		super.setPreferredSize(d);
	}

	/**
	 * Sets the current cursor.
	 * @nowarn
	 */
	public void setCursor(final Cursor cursor)
	{
		if (this.cursor != cursor)
		{
			this.cursor = cursor;
			super.setCursor(cursor);
		}
	}

	/**
	 * Returns the tool tip for the given mouse event (i\.e\. the given position).
	 * This is derived from an underlying process element.
	 * @param e Current mouse event specifying the position of the cursor
	 * @return The text or null if there is no process element at this position
	 */
	public String getToolTipText(final MouseEvent e)
	{
		if (e != null)
		{
			Point p = e.getPoint();
			p = applyScale(p, true);

			ProcessElementContainer pec = ((ProcessDrawing) drawing()).findProcessElementContainerInside(p.x, p.y);

			if (pec != null)
			{
				// No tool tip for the drawing itself
				if (pec != drawing())
					// We can use the getInfoText Method of ModelObject
					return TextUtil.convertToHTML(pec.getReferredProcessElement().getInfoText(), true, 1, 50);
			}
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Painting
	//////////////////////////////////////////////////

	/**
	 * Redraws the view.
	 */
	public void redraw()
	{
		if (drawing() != null)
		{
			repaint();
		}
	}

	/**
	 * Draws the background. If a background pattern is set it
	 * is used to fill the background. Otherwise the background
	 * is filled in the background color.
	 * @param g Graphics object to draw to
	 */
	public void drawBackground(final Graphics g)
	{
		// Don't call super.drawBackground, we need to take the scaling into account, so we do background painting ourself

		// Paint the background
		g.setColor(getBackground());
		g.fillRect(0, 0, getBounds().width, getBounds().height);

		boolean isPrinting = g instanceof PrintGraphics;
		if (! isPrinting)
		{
			// Paint the grid
			g.setColor(ModelerColors.GRID);

			int height = getHeight();
			int width = getWidth();
			int max = Math.max(height, width);

			if (gridDisplayed)
			{
				// Scale the grid spacing
				int spacing = applyScale(gridSpacing, false);

				if (gridType == GRIDTYPE_POINT)
				{
					for (int x = spacing; x < max; x += spacing)
					{
						for (int y = spacing; y < max; y += spacing)
						{
							g.fillArc(x - 1, y - 1, ((x % (5 * spacing) == 0) && (y % (5 * spacing) == 0)) ? 4 : 2, ((x % (5 * spacing) == 0) && (y
								% (5 * spacing) == 0)) ? 4 : 2, 0, 360);
						}
					}
				}
				else if (gridType == GRIDTYPE_LINE)
				{
					for (int i = spacing; i < max; i += spacing)
					{
						g.drawLine(i, 0, i, height);
						g.drawLine(0, i, width, i);
					}
				}
				else if (gridType == GRIDTYPE_HEX)
				{
					Graphics2D g2 = (Graphics2D) g;

					int even = spacing / 2;

					for (double y = 0d; y < max + spacing; y += ((spacing * 5d) / 6d))
					{
						for (int x = even; x < max + spacing; x += spacing)
						{
							g2.drawLine(x, (int) y, x, (int) y + (spacing / 2));
							g2.drawLine(x, (int) y + (spacing / 2), x - (spacing / 2), (int) (y + (spacing * 5d) / 6d));
							g2.drawLine(x, (int) y + (spacing / 2), x + (spacing / 2), (int) (y + (spacing * 5d) / 6d));
							g2.fillArc(x - 1, (int) y - 1, 2, 2, 0, 360);
						}

						even = Math.abs(even - (spacing / 2));
					}
				}
			}
		}
	}

	/**
	 * Paints the Shadow of the process view.
	 *
	 * @param g the graphics object where the shadow has to paint
	 */
	public void drawShadow(final Graphics g)
	{
		Skin skin = ((ProcessDrawing) drawing()).getProcessSkin();

		if (shadowLayouter != null && ! skin.isDisableShadows())
		{
			shadowLayouter.drawShadows(ShadowEnumerator.enumerate((CompositeFigure) drawing()), g);
		}
	}

	/**
	 * Overrides StandardDrawingView for adding scale effect.
	 * @param g Graphics object to draw to
	 */
	public void drawAll(final Graphics g)
	{
		if (! (g instanceof Graphics2D))
			return;
		boolean isPrinting = g instanceof PrintGraphics;

		drawBackground(g);

		Graphics2D g2 = (Graphics2D) g;

		AffineTransform oldTransform = g2.getTransform();

		// Prepare the graphics object only if not already prepared
		if ((oldTransform.getType() & AffineTransform.TYPE_MASK_SCALE) == 0)
		{
			prepareGraphics(g2);
		}

		drawShadow(g);
		drawDrawing(g);
		if (! isPrinting)
		{
			drawHandles(g);
		}

		g2.setTransform(oldTransform);
	}

	/**
	 * Override for adding scale effect.
	 * @see CH.ifa.draw.standard.StandardDrawingView#draw(Graphics g, FigureEnumeration fe)
	 */
	public void draw(final Graphics g, final FigureEnumeration fe)
	{
		if (! (g instanceof Graphics2D))
			return;

		Graphics2D g2 = (Graphics2D) g;

		AffineTransform oldTransform = g2.getTransform();

		// Prepare the graphics object only if not already prepared
		if ((oldTransform.getType() & AffineTransform.TYPE_MASK_SCALE) == 0)
		{
			prepareGraphics(g2);
		}

		super.draw(g, fe);

		g2.setTransform(oldTransform);
	}

	/**
	 * Prepares a graphics context for rendering.
	 * Sets scaling and rendering hints.
	 *
	 * @param g2 Graphics to preprare
	 */
	public void prepareGraphics(final Graphics2D g2)
	{
		g2.scale(scaleFactor, scaleFactor);

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		//g2.setRenderingHint (RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//g2.setRenderingHint (RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	}

	/**
	 * Draws the currently active handles.
	 * @param g Graphics object to draw to
	 */
	public void drawHandles(final Graphics g)
	{
		Enumeration k = selectionHandles();

		while (k.hasMoreElements())
		{
			((Handle) k.nextElement()).draw(g);
		}
	}

	/**
	 * Scrolls a figure container into view.
	 * @param figure Figure to show
	 * @param addEnlargement
	 * true: Adds an offset of 50 px to the display box of the element.<br>
	 * false: Does not add an enlargement offset
	 */
	public void scrollIntoView(final Figure figure, final boolean addEnlargement)
	{
		if (figure == null)
			return;

		// Get the bounding rectangle
		Rectangle rect = new Rectangle(figure.displayBox());

		// Enlarge by 100 pixel to prevent it from hanging in the corners
		if (addEnlargement)
		{
			rect.grow(50, 50);
		}

		scrollRectToVisible(rect);
	}

	/**
	 * @see javax.swing.JComponent#scrollRectToVisible(java.awt.Rectangle)
	 *
	 * @param rect Rectangle to scroll into view
	 */
	public void scrollRectToVisible(Rectangle rect)
	{
		// Make sure that the rectangle lies within the preferred size of the drawing view
		Rectangle db = ((Figure) drawing()).displayBox();
		db.width += sizeOffset;
		db.height += sizeOffset;
		rect = rect.intersection(db);
		if (rect.isEmpty())
			return;

		rect = applyScale(rect, false);
		super.scrollRectToVisible(rect);
	}

	//////////////////////////////////////////////////
	// @@ Scaling
	//////////////////////////////////////////////////

	public void drawingRequestUpdate(final DrawingChangeEvent e)
	{
		revalidate();
	}

	/**
	 * Adds a rectangle to the damage section after transforming it using the shadow layouter.
	 *
	 * @param e The DrawingChangeEvent that describes the invalidation
	 */
	public void drawingInvalidated(final DrawingChangeEvent e)
	{
		Rectangle r = e.getInvalidatedRectangle();
		if (shadowLayouter != null)
		{
			r.add(shadowLayouter.transformRectangle(r));
		}

		r = applyScale(r, false);

		super.drawingInvalidated(new DrawingChangeEvent(e.getDrawing(), r));

		revalidate();
	}

	/**
	 * Applies the current scale factor of the view to a rectangle.
	 *
	 * @param r rectangle to be scaled
	 * @param scaleToDoc
	 * true: Assumes that the coordinates are component coordinates that should be translated to document coordinates.<br>
	 * false: Assumes that the coordinates are document coordinates that should be translated to component coordinates.
	 * @return The scaled rectangle
	 */
	public Rectangle applyScale(final Rectangle r, final boolean scaleToDoc)
	{
		Rectangle result = new Rectangle();
		result.x = applyScale(r.x, scaleToDoc);
		result.y = applyScale(r.y, scaleToDoc);
		result.width = applyScale(r.width, scaleToDoc);
		result.height = applyScale(r.height, scaleToDoc);
		return result;
	}

	/**
	 * Applies the current scale factor of the view to a point.
	 *
	 * @param p Point to be scaled
	 * @param scaleToDoc
	 * true: Assumes that the coordinates are component coordinates that should be translated to document coordinates.<br>
	 * false: Assumes that the coordinates are document coordinates that should be translated to component coordinates.
	 * @return The scaled point
	 */
	public Point applyScale(final Point p, final boolean scaleToDoc)
	{
		Point result = new Point();
		result.x = applyScale(p.x, scaleToDoc);
		result.y = applyScale(p.y, scaleToDoc);
		return result;
	}

	/**
	 * Applies the current scale factor of the view to a coordinate.
	 *
	 * @param coordinate X or Y coordinate to be scaled
	 * @param scaleToDoc
	 * true: Assumes that the coordinates are component coordinates that should be translated to document coordinates.<br>
	 * false: Assumes that the coordinates are document coordinates that should be translated to component coordinates.
	 * @return The resulting coordinate
	 */
	public int applyScale(int coordinate, final boolean scaleToDoc)
	{
		if (scaleToDoc)
		{
			// Scale component coordinates to document coordinates
			coordinate = CommonUtil.rnd(coordinate / scaleFactor);
		}
		else
		{
			// Scale document coordinates to component coordinates
			coordinate = CommonUtil.rnd(coordinate * scaleFactor);
		}

		return coordinate;
	}

	/**
	 * Adjusts the scaling factor and view position according to the given rectangle.
	 *
	 * @param r Rectangle to scroll into view
	 */
	public void setVisibleRect(final Rectangle r)
	{
		// Make sure that the rectangle lies within the preferred size of the drawing view
		JScrollPane pane = SwingUtil.getScrollPaneAncestor(this);
		Rectangle vr = pane.getViewport().getBounds();

		double xFactor = (double) vr.width / r.width;
		double yFactor = (double) vr.height / r.height;
		double factor = Math.min(xFactor, yFactor);
		setScaleFactor(factor);

		redraw();
		revalidate();

		scrollRectToVisible(r);
	}

	//////////////////////////////////////////////////
	// @@ Scalable implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the scaling factor.
	 * @nowarn
	 */
	public double getScaleFactor()
	{
		return scaleFactor;
	}

	/**
	 * Sets the scaling factor.
	 * @nowarn
	 */
	public void setScaleFactor(double scaleFactor)
	{
		// Constrain the scaling
		if (scaleFactor > MAX_SCALE_FACTOR)
			scaleFactor = MAX_SCALE_FACTOR;
		else if (scaleFactor < MIN_SCALE_FACTOR)
			scaleFactor = MIN_SCALE_FACTOR;

		this.scaleFactor = scaleFactor;

		((DrawingEditorPlugin) editor()).fireEvent("modelerpage.view.showzoomfactor", new Double(scaleFactor));
	}

	//////////////////////////////////////////////////
	// @@ Selection
	//////////////////////////////////////////////////

	/**
	 * Gets the number of selected figures.
	 * @nowarn
	 */
	public int selectionCount()
	{
		return selection.size();
	}

	/**
	 * Tests whether a given figure is selected.
	 * @nowarn
	 */
	public boolean isFigureSelected(final Figure checkFigure)
	{
		return selection.contains(checkFigure);
	}

	/**
	 * @see CH.ifa.draw.standard.StandardDrawingView#selection()
	 */
	public Vector selection()
	{
		return new Vector(selection);
	}

	/**
	 * @see CH.ifa.draw.standard.StandardDrawingView#selectionZOrdered()
	 */
	public Vector selectionZOrdered()
	{
		return selection();
	}

	/**
	 * Adds a figure to the current selection.
	 * The figure is only selected if it is also contained in the drawing
	 * associated with this drawing view.
	 */
	public void addToSelection(Figure figure)
	{
		// Note that we can select only process element containers
		while (figure != null)
		{
			if (figure instanceof ProcessElementContainer || figure instanceof LineFigure)
			{
				break;
			}

			if (! (figure instanceof ChildFigure))
				// No way in going up the hierarchy, give up
				return;

			figure = ((ChildFigure) figure).getParent();
		}

		if (figure == null)
			// No valid parent figure found
			return;

		if (! isFigureSelected(figure))
		{
			// Add to selection
			selection.add(figure);

			// Clear handles so they will be recalculated when needed
			activeHandles = null;
			markSelectionFigureChanged(figure, true);

			fireSelectionChanged();
		}

		updateSelection();

		// Reorder z-order
		drawing().bringToFront(figure);
	}

	/**
	 * Removes a figure from the selection.
	 */
	public void removeFromSelection(final Figure figure)
	{
		if (isFigureSelected(figure))
		{
			selection.remove(figure);

			activeHandles = null;

			figure.invalidate();
			fireSelectionChanged();

			markSelectionFigureChanged(figure, false);
			repairDamage();

			updateSelection();
		}
	}

	public void singleSelect(final Figure figure)
	{
		clearSelection();
		if (figure != null)
		{
			addToSelection(figure);
			scrollIntoView(figure, true);
		}
	}

	/**
	 * Clears the current selection.
	 */
	public void clearSelection()
	{
		// there is nothing selected
		if (selection.isEmpty())
			// avoid unnecessary selection changed event when nothing has to be cleared
			return;

		// Save the current selection
		List oldSelection = new ArrayList(selection);

		selection.clear();
		activeHandles = null;

		for (Iterator it = oldSelection.iterator(); it.hasNext();)
		{
			markSelectionFigureChanged((Figure) it.next(), false);
		}

		fireSelectionChanged();
		updateSelection();

		repairDamage();
	}

	/**
	 * Marks a selected or deselected figure as changed.
	 *
	 * @param figure Figure
	 * @param selected
	 * true: The figure has been selected.<br>
	 * false: The figure has been deselected.
	 */
	private void markSelectionFigureChanged(Figure figure, final boolean selected)
	{
		if (figure instanceof VisualElement)
		{
			// Create a visual element event
			String type = selected ? VisualElementEvent.SELECTED : VisualElementEvent.DESELECTED;
			((VisualElement) figure).handleEvent(new VisualElementEvent(type, (DrawingEditorPlugin) editor()));
		}

		while (figure != null)
		{
			if (figure instanceof VisualElement)
			{
				// Create a visual element event
				((VisualElement) figure).handleEvent(new VisualElementEvent(VisualElementEvent.UPDATE_STATE, (DrawingEditorPlugin) editor()));
			}

			figure.changed();

			if (figure instanceof ChildFigure)
				figure = ((ChildFigure) figure).getParent();
			else
				break;
		}
	}

	/**
	 * In addition to the super method, we fire an modeler.view.selectionchanged event.
	 * @see CH.ifa.draw.standard.StandardDrawingView#fireSelectionChanged()
	 */
	public void fireSelectionChanged()
	{
		super.fireSelectionChanged();

		((DrawingEditorPlugin) editor()).fireEvent("modeler.view.selectionchanged");
	}

	/**
	 * Notifies the property browser of selection changes. If none or more than one
	 * element is selected, clear the property browser, else show the single element.
	 */
	public void updateSelection()
	{
		DrawingEditorPlugin modeler = (DrawingEditorPlugin) editor();

		int selSize = selection.size();
		if (selSize == 1)
		{
			// Single element selected, show it in the property browser.
			Figure figure = (Figure) selection.get(0);

			if (figure instanceof ProcessElementContainer)
			{
				ModelObject mo = ((ProcessElementContainer) figure).getReferredProcessElement();
				Object originalObject = null;

				// Determine the item the figure element references to
				Item referencedItem = null;
				if (mo instanceof SubprocessNode)
				{
					referencedItem = ((SubprocessNode) mo).getSubprocess();
				}

				if (editor() instanceof NodeItemEditorPlugin)
				{
					// We are in the nodeeditor, we need not the process object,
					// but the item behind it
					if (referencedItem != null)
					{
						// Get the reference object for name uniqueness checks
						originalObject = ModelConnector.getInstance().getItemByQualifier(referencedItem.getQualifier(), false);

						// We will edit the referenced item directly
						mo = referencedItem;
					}
				}
				else
				{
					if (mo instanceof MultiSocketNode)
					{
						MultiSocketNode node = (MultiSocketNode) mo;

						// If we edit an activity node, create a configuration bean if not already there
						// in order to be able to edit the settings in the property browser.
						// The bean will be removed if it contains the default values only when saving the activity node again
						// (see plugin_propertybrowser_executesave () in the ModelerEventModule)
						if (node.getConfigurationBean() == null)
						{
							// We create a new configuration bean (if defined by the underlying activity)
							if (referencedItem != null)
							{
								node.setConfigurationBean(referencedItem.createConfigurationBean());
							}
						}
					}
				}

				// Make sure the global reference names are up to date or else we may have trouble
				// saving the object (the ModelObjectValidator will try to rebuild the references from the names)
				mo.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

				String title = mo.getName();
				String modelObjectTypeName = mo.getModelObjectTypeName();
				if (modelObjectTypeName != null)
				{
					title = title + " (" + modelObjectTypeName + ")";
				}

				// Send the object to the OE
				modeler.fireEvent(new PropertyBrowserSetEvent(modeler, mo, originalObject, false, mo.getDescription(), title, null, false, true));

				// Show object description in info panel
				modeler.fireEvent(new JaspiraEvent(modeler, "plugin.infopanel.setinfotext", mo));
			}

			if (figure instanceof Colorizable)
			{
				// Make the color chooser display the color of the selected object if it has a custom color
				Colorizable colorizable = (Colorizable) figure;
				Color figureColor = colorizable.getFillColor();
				Color defaultColor = colorizable.getDefaultFillColor();
				if (figureColor != null && ! figureColor.equals(defaultColor))
				{
					modeler.fireEvent("colorchooser.setcolor", figureColor);
				}
			}
		}
		else
		{
			// More than one object selected, clear OE
			modeler.fireEvent(new PropertyBrowserSetEvent(modeler));
			modeler.fireEvent(new JaspiraEvent(modeler, "plugin.infopanel.clearinfotext"));
		}

		// Update the cut/copy/paste button status
		modeler.fireEvent("global.clipboard.updatestatus");
	}

	/**
	 * Gets an enumeration of the currently active handles.
	 * @return An enumeration of {@link Handle} objects
	 */
	private Enumeration selectionHandles()
	{
		if (activeHandles == null)
		{
			activeHandles = new Vector();

			FigureEnumeration k = selectionElements();
			while (k.hasMoreElements())
			{
				Figure figure = k.nextFigure();
				Enumeration kk = figure.handles().elements();

				while (kk.hasMoreElements())
				{
					activeHandles.addElement(kk.nextElement());
				}
			}
		}

		return activeHandles.elements();
	}

	/**
	 * Finds a handle at the given coordinates.
	 * @param x Document coordinates
	 * @param y Document coordinates
	 * @return The hit handle or null if no handle is found
	 */
	public Handle findHandle(final int x, final int y)
	{
		Handle handle;

		Enumeration k = selectionHandles();

		while (k.hasMoreElements())
		{
			handle = (Handle) k.nextElement();

			if (handle.containsPoint(x, y))
				return handle;
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Keyboard and popup handling
	//////////////////////////////////////////////////

	/**
	 * Handles key down events.
	 * All other keys are passed to the currently active tool.
	 * @see CH.ifa.draw.standard.StandardDrawingView#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(final KeyEvent e)
	{
		int code = e.getKeyCode();
		DrawingEditorPlugin modeler = (DrawingEditorPlugin) editor();

		if (code == KeyEvent.VK_ENTER && ! InputState.isCtrlDown() && ! InputState.isAltDown())
		{
			// Purge the event
			e.consume();

			if (selection.size() == 1)
			{
				Figure figure = (Figure) selection.get(0);
				displayPopupMenu(figure, null);
			}

			return;
		}

		if (code == KeyEvent.VK_ENTER && InputState.isAltDown())
		{
			if (selection.size() == 1)
			{
				Figure figure = (Figure) selection.get(0);
				if (figure instanceof ProcessElementContainer && ! (figure instanceof ProcessDrawing))
				{
					// we directly set the tool inside the editor
					modeler.getToolSupport().displayInPlaceEditor((ProcessElementContainer) figure);
				}
			}

			return;
		}

		if (code == KeyEvent.VK_DELETE)
		{
			// Purge the event
			e.consume();

			modeler.fireEvent(new JaspiraActionEvent(modeler, "global.clipboard.delete", Plugin.LEVEL_APPLICATION));

			return;
		}

		// Pass the key to the current tool
		tool().keyDown(e, code);
	}

	/**
	 * Handles key up events.
	 * All keys are passed to the currently active tool.
	 * @see CH.ifa.draw.standard.StandardDrawingView#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(final KeyEvent e)
	{
		// Pass the key to the current tool
		((ModelerToolSupport) tool()).keyUp(e, e.getKeyCode());
	}

	/**
	 * Displays the popup menu for the given figure.
	 *
	 * @param figure Figure to display the menu for
	 * @param me Mouse event that caused the popup display or null if the popup has been initiated by a key
	 */
	public void displayPopupMenu(final Figure figure, final MouseEvent me)
	{
		Transferable[] transferables = null;

		if (figure != null && figure != drawing())
		{
			if (figure instanceof ProcessElementContainer)
			{
				// Obtain the underlying process element (usually a ModelObject)
				Object pe = ((ProcessElementContainer) figure).getReferredProcessElement();

				transferables = new Transferable[]
				{
					new BasicTransferable(pe), new BasicTransferable(figure)
				};
			}
			else
			{
				// Broadcast a popup interaction event to construct the popup menu
				transferables = new Transferable[]
				{
					new BasicTransferable(figure)
				};
			}
		}

		if (transferables != null)
		{
			DrawingEditorPlugin modeler = (DrawingEditorPlugin) editor();

			// Create an interaction event that transports the objects we refer to
			InteractionEvent iae = new InteractionEvent(modeler, InteractionEvent.POPUP, new MultiTransferable(transferables));

			// Fire the event to the other plugins
			modeler.fireEvent(iae);

			// Create a popup menu from what the Jaspira actions the other plugins have added
			JaspiraPopupMenu menu = iae.createPopupMenu();

			if (menu != null)
			{
				// Position and display the menu
				Point location = null;
				if (me != null)
				{
					location = new Point(me.getX(), me.getY());
				}
				else if (figure != null)
				{
					location = figure.center();
					location = applyScale(location, false);
				}

				adjustOffsets(getParent(), location);

				menu.setLocation(location);
				menu.setInvoker(this);
				menu.setVisible(true);

				if (me != null && figure != null)
				{
					figure.invalidate();
				}
			}
		}
	}

	/**
	 * Sums up iteratively all x and y offsets of all
	 * parent compontents, translating viewport positions,
	 * until the top parent component is reached.
	 *
	 * @param comp Component, which's location will be added to the point
	 * @param offsetPoint Point to add the component coordinates to
	 */
	private void adjustOffsets(final Component comp, final Point offsetPoint)
	{
		if (comp == null)
			// Recursion break;
			return;

		if (comp instanceof JViewport)
		{
			// a viewport gives our component a virtual offset we have to translate
			// it another way than normal components
			Point pos = ((JViewport) comp).getViewPosition();
			offsetPoint.translate(- pos.x, - pos.y);
		}
		else
		{
			Point compLocation = comp.getLocation();
			offsetPoint.translate(compLocation.x, compLocation.y);
		}

		// Recurse down to parent
		adjustOffsets(comp.getParent(), offsetPoint);
	}

	//////////////////////////////////////////////////
	// @@ Breakout support
	//////////////////////////////////////////////////

	/**
	 * Adds breakout support for the specified key.
	 *
	 * @param actionName Arbitrary name of the breakout support action
	 * @param keyCode Key code that will initiate the breakout mode
	 */
	protected void addBreakoutSupport(final String actionName, final int keyCode)
	{
		// Breakout action map
		ActionMap am = getActionMap();
		am.put(actionName, new BreakOutOnAction(keyCode));

		// Input map entry for key pressed
		InputMap in = getInputMap(WHEN_FOCUSED);
		in.put(KeyStroke.getKeyStroke(keyCode, 0, false), actionName);
	}

	/**
	 * Toolbox breakout initiation action.
	 */
	private class BreakOutOnAction extends AbstractAction
	{
		/** Key code used to initiate the breakout mode */
		int keyCode;

		/**
		 * Constructor.
		 *
		 * @param keyCode Key code used to initiate the breakout mode
		 */
		public BreakOutOnAction(final int keyCode)
		{
			this.keyCode = keyCode;
		}

		public void actionPerformed(final ActionEvent e)
		{
			boolean breakoutModeActive = SwingUtil.getGlassPane(WorkspaceDrawingView.this).isVisible();

			if (mouseInView || breakoutModeActive)
			{
				if (! breakoutModeActive)
				{
					BreakoutEvent event = new BreakoutEvent((DrawingEditorPlugin) editor(), keyCode);
					((DrawingEditorPlugin) editor()).fireEvent(event);

					BreakoutProvider bop = event.getProvider();
					if (bop != null)
					{
						Component glassPane = SwingUtil.getGlassPane(WorkspaceDrawingView.this);
						if (glassPane instanceof DragDropPane)
						{
							// Get the last mouse point (view coordinates)
							Point lastPoint = ((ModelerToolSupport) tool()).getLastPoint();
							if (lastPoint.x == 0 && lastPoint.y == 0)
							{
								// This may occur after a new process has been opened using the keyboard
								// and the mouse hasn't been moved so far.
								// Simply center the bop on the view
								JScrollPane scrollPane = SwingUtil.getScrollPaneAncestor(WorkspaceDrawingView.this);
								Rectangle viewRect = SwingUtil.convertBoundsToGlassCoords(scrollPane.getViewport());
								lastPoint.x = viewRect.x + viewRect.width / 2;
								lastPoint.y = viewRect.y + viewRect.height / 2;
							}

							Point bobPosition = SwingUtilities.convertPoint(WorkspaceDrawingView.this, lastPoint, SwingUtil
								.getGlassPane(WorkspaceDrawingView.this));

							((DragDropPane) glassPane).startBreakOutMode(bop, bobPosition);
						}
					}
				}
				else
				{
					// Toggle: Deactivate the box
					Component glassPane = SwingUtil.getGlassPane(WorkspaceDrawingView.this);
					if (glassPane instanceof DragDropPane)
					{
						((DragDropPane) glassPane).endBreakOutMode();
					}
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Cursor movement support
	//////////////////////////////////////////////////

	/**
	 * Add support for selection and workspace movement by cursor keys.
	 */
	private void addCursorKeySupport()
	{
		// Somehow Drawing view cursor keys don't work, the non-modified keys are passed directly to the JScrollPane, the action doesn't get called, so we comment it out
		addCursorKeySupport(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK, - 1, 0);
		addCursorKeySupport(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK, 1, 0);
		addCursorKeySupport(KeyEvent.VK_UP, InputEvent.CTRL_MASK, 0, - 1);
		addCursorKeySupport(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK, 0, 1);
		addCursorKeySupport(KeyEvent.VK_LEFT, 0, - 10, 0);
		addCursorKeySupport(KeyEvent.VK_RIGHT, 0, 10, 0);
		addCursorKeySupport(KeyEvent.VK_UP, 0, 0, - 10);
		addCursorKeySupport(KeyEvent.VK_DOWN, 0, 0, 10);
	}

	/**
	 * Add support for selection and workspace movement by cursor keys.
	 *
	 * @param keyCode Key code used to initiate the breakout mode
	 * @param modifiers Key modifiers
	 * @param xOffset Number of pixels to move horizontal
	 * @param yOffset Number of pixels to move vertical
	 */
	private void addCursorKeySupport(final int keyCode, final int modifiers, final int xOffset, final int yOffset)
	{
		KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers, false);
		String actionName = keyStroke.toString() + "_Action";

		// Breakout action map
		ActionMap am = getActionMap();
		am.put(actionName, new CursorAction(xOffset, yOffset));

		// Input map entry for key pressed
		InputMap in = getInputMap(WHEN_FOCUSED);
		in.put(KeyStroke.getKeyStroke(keyCode, modifiers, false), actionName);
	}

	/**
	 * Toolbox breakout initiation action.
	 */
	private class CursorAction extends AbstractAction
	{
		/** Number of pixels to move horizontal */
		int xOffset;

		/** Number of pixels to move vertical */
		int yOffset;

		/**
		 * Constructor.
		 *
		 * @param xOffset Number of pixels to move horizontal
		 * @param yOffset Number of pixels to move vertical
		 */
		public CursorAction(final int xOffset, final int yOffset)
		{
			this.xOffset = xOffset;
			this.yOffset = yOffset;
		}

		public void actionPerformed(final ActionEvent e)
		{
			DrawingEditorPlugin modeler = (DrawingEditorPlugin) editor();
			if (selectionCount() > 0)
			{
				// Move selection
				modeler.startUndo("Move element");

				boolean first = true;
				for (Iterator it = selection.iterator(); it.hasNext();)
				{
					Figure f = (Figure) it.next();

					f.moveBy(xOffset, yOffset);
					if (first)
					{
						scrollIntoView(f, true);
						first = false;
					}
				}

				modeler.endUndo();
				checkDamage();
			}
			else
			{
				// Move scroll pane
				((Trackable) modeler).moveTrackerBy(xOffset * 20, yOffset * 20);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Member Access
	//////////////////////////////////////////////////

	/**
	 * Gets the shadow layouter.
	 * @nowarn
	 */
	public ShadowLayouter getShadowLayouter()
	{
		return shadowLayouter;
	}

	/**
	 * Sets the shadow layouter.
	 * @nowarn
	 */
	public void setShadowLayouter(final ShadowLayouter shadowLayouter)
	{
		if (this.shadowLayouter != null)
		{
			this.shadowLayouter.releaseShadowLayouter();
		}

		this.shadowLayouter = shadowLayouter;

		redraw();
	}

	/**
	 * Gets the figure currently under the cursor or null.
	 * @nowarn
	 */
	public Figure getFigureUnderCursor()
	{
		return figureUnderCursor;
	}

	/**
	 * Sets the figure currently under the cursor or null.
	 * @nowarn
	 */
	public void setFigureUnderCursor(final Figure figureUnderCursor)
	{
		this.figureUnderCursor = figureUnderCursor;
	}

	/**
	 * Gets the grid visibility.
	 * @nowarn
	 */
	public boolean isGridDisplayed()
	{
		return gridDisplayed;
	}

	/**
	 * Sets the grid visibility.
	 * @nowarn
	 */
	public void setGridDisplayed(final boolean gridDisplayed)
	{
		boolean oldGridDisplayed = this.gridDisplayed;
		this.gridDisplayed = gridDisplayed;

		if (oldGridDisplayed != this.gridDisplayed)
		{
			redraw();
		}
	}

	/**
	 * Gets the grid layout type.
	 * @nowarn
	 */
	public int getGridType()
	{
		return gridType;
	}

	/**
	 * Sets the grid layout type.
	 * @nowarn
	 */
	public void setGridType(final int gridType)
	{
		int oldGridType = this.gridType;
		this.gridType = gridType;

		if (gridDisplayed && oldGridType != this.gridType)
		{
			redraw();
		}
	}

	/**
	 * Gets the grid spacing.
	 * @nowarn
	 */
	public int getGridSpacing()
	{
		return gridSpacing;
	}

	/**
	 * Sets the grid spacing.
	 * @nowarn
	 */
	public void setGridSpacing(final int gridSpacing)
	{
		int oldGridSpacing = this.gridSpacing;
		this.gridSpacing = gridSpacing;

		if (gridDisplayed && oldGridSpacing != this.gridSpacing)
		{
			redraw();
		}
	}

	/**
	 * Sets the size offset for workspace view enlargement.
	 * @nowarn
	 */
	public void setSizeOffset(final int sizeOffset)
	{
		this.sizeOffset = sizeOffset;
	}
}
