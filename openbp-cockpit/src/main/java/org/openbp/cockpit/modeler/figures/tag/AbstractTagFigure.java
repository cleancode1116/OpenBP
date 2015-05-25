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
package org.openbp.cockpit.modeler.figures.tag;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.openbp.cockpit.modeler.ViewModeMgr;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.BasicFigure;
import org.openbp.cockpit.modeler.figures.generic.ChildFigure;
import org.openbp.cockpit.modeler.figures.generic.CircleConstants;
import org.openbp.cockpit.modeler.figures.generic.Expandable;
import org.openbp.cockpit.modeler.figures.generic.Orientation;
import org.openbp.cockpit.modeler.figures.generic.Quarter;
import org.openbp.cockpit.modeler.figures.generic.ShadowDropper;
import org.openbp.cockpit.modeler.figures.generic.UpdatableFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigureDescriptor;
import org.openbp.cockpit.modeler.figures.generic.XRoundRectangleFigure;
import org.openbp.cockpit.modeler.figures.layouter.HorizontalLayouter;
import org.openbp.cockpit.modeler.figures.layouter.LayoutableTag;
import org.openbp.cockpit.modeler.figures.layouter.TagLayouter;
import org.openbp.cockpit.modeler.figures.process.FigureTypes;
import org.openbp.cockpit.modeler.util.FigureUtil;
import org.openbp.common.CollectionUtil;
import org.openbp.common.util.ToStringHelper;
import org.openbp.jaspira.decoration.DecorationMgr;
import org.openbp.jaspira.gui.interaction.DropClientUtil;
import org.openbp.jaspira.gui.interaction.InteractionClient;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeListener;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.standard.CompositeFigure;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

/**
 * An AbstractTagFigure represents a single tag. It is graphically represented by
 * its presentation figure and can contain any amount of subFigures.
 * The subFigures are layouted by using an Layouter.
 *
 * Contents can be divided using SpacerFigures and SeparatorFigures, the former is
 * also used to create an offset to the center.
 *
 * Each Tag cotains an origin, around which rotating is resolved, a current angle,
 * its layouter, its presentationFigure and its contents.
 *
 * Contents of the are represented by TagContent objects which contain the contained
 * figure as well as its content type.
 *
 * The content state of the tag is actually the definition which content types are visible,
 * all applicable stati being binary ORed to form the content state.
 *
 * @author Stephan Moritz
 */
public abstract class AbstractTagFigure extends CompositeFigure
	implements LayoutableTag, FigureChangeListener, ShadowDropper, Expandable, ChildFigure, VisualElement, UpdatableFigure
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Decoration key for the content type (used with Integer objects) */
	public static final String DECO_TAGCONTENTTYPE = "Tag.ContentType";

	/** Icon content */
	public static final int CONTENT_ICON = (1 << 0);

	/** Static text content or similar */
	public static final int CONTENT_TEXT = (1 << 1);

	/** Content that refers to flow of control */
	public static final int CONTENT_FLOW = (1 << 2);

	/** Content that refers to the flow of data */
	public static final int CONTENT_DATA = (1 << 3);

	/** Content that refers to the flow of data */
	public static final int CONTENT_USER1 = (1 << 4);

	/** Content that refers to the flow of data */
	public static final int CONTENT_USER2 = (1 << 5);

	/** Flag for interaction-related content */
	public static final int CONTENT_INTERACTION = CONTENT_DATA | CONTENT_FLOW;

	/** Flag for static content */
	public static final int CONTENT_STATIC = CONTENT_ICON | CONTENT_TEXT;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Containing figure */
	protected VisualElement parent;

	/** Presentation-figure (the tag itself) */
	protected XFigure presentationFigure;

	/** Represents the figure's shadow */
	protected Figure shadowFigure;

	/** Actual content of the tag (contains TagContent objects) */
	protected List content;

	/** The layouter is responsible for laying out the tag content */
	protected TagLayouter layouter;

	/** Angle of the tag */
	protected double angle;

	/**
	 * The center from which the tag emerges in world coordinates.
	 * Usually the center of the parent figure.
	 */
	protected Point origin;

	/** Content state of the tag */
	protected int contentState;

	/** Current (decorated) content state of the tag */
	protected int currentContentState;

	/** Visual status as defined by {@link VisualElement} */
	protected int visualStatus = VISUAL_VISIBLE;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param parent Parent
	 * @param modelObject Model object this tag represents or null
	 */
	public AbstractTagFigure(VisualElement parent, Object modelObject)
	{
		super();

		// The direct parent figure
		this.parent = parent;
		origin = parent.center();

		if (getDrawing().isDisplayAll())
		{
			contentState = CONTENT_STATIC | CONTENT_INTERACTION;
		}
		else
		{
			contentState = CONTENT_STATIC | ViewModeMgr.getInstance().getTagState(this);
		}

		content = new ArrayList();

		initPresentationFigure();
		initTagLayouter();
		initContent(modelObject);

		applyContentState();
	}

	/**
	 * Initializes the presentation figure and its shadow.
	 */
	protected void initPresentationFigure()
	{
		presentationFigure = createPresentationFigure();
		presentationFigure.setParent(this);
		initShadow();
	}

	/**
	 * Creates the presentation figure of this tag.
	 * By default, this is an {@link XRoundRectangleFigure}
	 * @return The presentation figure
	 */
	protected XFigure createPresentationFigure()
	{
		XFigureDescriptor desc = getDrawing().getProcessSkin().getSymbolDescriptor(FigureTypes.SYMBOLTYPE_TAG);
		return desc.createFigure();
	}

	/**
	 * Creates the shadow figure of the tag's presentation figure.
	 */
	protected void initShadow()
	{
		shadowFigure = FigureUtil.createShadowFigure(presentationFigure);
	}

	/**
	 * Creates the layouter for this tag.
	 * By default, this is a {@link HorizontalLayouter}
	 */
	protected void initTagLayouter()
	{
		layouter = new HorizontalLayouter(this);
	}

	/**
	 * Creates the content of this tag.
	 * By default, this method does nothing.
	 *
	 * @param modelObject Model object this tag represents or null
	 */
	protected void initContent(Object modelObject)
	{
	}

	//////////////////////////////////////////////////
	// @@ Content handling
	//////////////////////////////////////////////////

	public List getContent()
	{
		return content;
	}

	/**
	 * Adds a figure with a given content type to the content list of this tag.
	 * You should call {@link #applyContentState} to apply the content to the list of displayed figures
	 * according to the current content state of the tag.
	 *
	 * @param newFigure The figure to add
	 * @param type The content type of the figure to add
	 */
	public void addContent(Figure newFigure, int type)
	{
		TagContent tagContent = new TagContent(newFigure, type);
		content.add(tagContent);
	}

	/**
	 * Adds a figure with a given content type to the content list of this tag at the given position.
	 * You should call {@link #applyContentState} to apply the content to the list of displayed figures
	 * according to the current content state of the tag.
	 *
	 * @param newFigure The figure to add
	 * @param index Position to insert the new figure at (0 = at the beginning)
	 * @param type The content type of the figure to add
	 */
	public void addContentAt(Figure newFigure, int index, int type)
	{
		TagContent tagContent = new TagContent(newFigure, type);
		content.add(index, tagContent);
	}

	/**
	 * Moves content from old to new index.
	 * @nowarn
	 */
	public void moveContent(int oldIndex, int newIndex)
	{
		content.add(newIndex, content.remove(oldIndex));

		applyContentState();
	}

	/**
	 * Gets the number of content objects (visible or not) of this tag.
	 * @nowarn
	 */
	public int getNumberOfContents()
	{
		return content.size();
	}

	/**
	 * Gets the content figure (visible or not) at the specified position
	 * @nowarn
	 */
	public Figure getContentFigureAt(int pos)
	{
		return ((TagContent) content.get(pos)).getFigure();
	}

	//////////////////////////////////////////////////
	// @@ content state management and layout
	//////////////////////////////////////////////////

	/**
	 * Rebuilds the tag content of the decorated content state differs from the current content state.
	 */
	public void checkDecoratedContentState()
	{
		int decoratedContentState = determineDecoratedContentState();
		if (decoratedContentState != currentContentState)
		{
			applyContentState();
		}
	}

	/**
	 * Rebuilds the child figure list of the tag according to the (decorated) tag content state
	 * from the content list of the tag.
	 */
	public void applyContentState()
	{
		willChange();

		// We rebuild the list from the button up, so first we need to clear it
		removeAll();

		int decoratedContentState = determineDecoratedContentState();

		// Save the state as current state
		currentContentState = decoratedContentState;

		// We put in every content-element that matches the current content state
		for (Iterator it = content.iterator(); it.hasNext();)
		{
			TagContent content = (TagContent) it.next();
			if ((decoratedContentState & content.getContentType()) != 0)
			{
				add(content.figure);
			}
		}

		layoutTag();
	}

	/**
	 * Sets and applies the tag content state.
	 * @nowarn
	 */
	public void setContentState(int contentState)
	{
		this.contentState = contentState;
		applyContentState();
	}

	/**
	 * Gets the tag content state.
	 * @nowarn
	 */
	public int getContentState()
	{
		return contentState;
	}

	/**
	 * Gets the tag content state after decoration.
	 * The returned content state will be retrieved using the {@link DecorationMgr}
	 * The {@link #DECO_TAGCONTENTTYPE} key will be used and the Integer value of the content state
	 * as returned by {@link #getContentState} will be passed to the registered decorators as value.
	 * @nowarn
	 */
	public int determineDecoratedContentState()
	{
		Integer stateObject = Integer.valueOf(getContentState());
		stateObject = (Integer) DecorationMgr.decorate(this, AbstractTagFigure.DECO_TAGCONTENTTYPE, stateObject);
		int state = stateObject.intValue();
		return state;
	}

	/**
	 * Checks if the orientation of the tag is vertical or horizontal.
	 * @nowarn
	 */
	public boolean isVerticalOrientation()
	{
		return layouter != null ? layouter.isVerticalLayouter() : true;
	}

	//////////////////////////////////////////////////
	// @@ Figure overrides
	//////////////////////////////////////////////////

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#basicDisplayBox(Point origin, Point corner)
	 */
	public void basicDisplayBox(Point origin, Point corner)
	{
		setOrigin(new Point(origin.x + (corner.x - origin.x) / 2, origin.y + (corner.y - origin.y) / 2));
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#basicMoveBy(int dx, int dy)
	 */
	protected void basicMoveBy(int dx, int dy)
	{
		origin.setLocation(origin.getX() + dx, origin.getY() + dy);

		super.basicMoveBy(dx, dy);

		presentationFigure.moveBy(dx, dy);
		if (shadowFigure != null)
		{
			shadowFigure.moveBy(dx, dy);
		}
	}

	/**
	 * We do not want any handles, so we return an empty vector.
	 * @see CH.ifa.draw.standard.CompositeFigure#handles()
	 */
	public Vector handles()
	{
		return CollectionUtil.EMPTY_VECTOR;
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#connectorAt(int x, int y)
	 */
	public Connector connectorAt(int x, int y)
	{
		return null;
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#displayBox()
	 */
	public Rectangle displayBox()
	{
		Rectangle box = new Rectangle(presentationFigure.displayBox());

		// Include the display boxes of our composite children
		try
		{
			for (Iterator it = fFigures.iterator(); it.hasNext();)
			{
				box = box.union(((Figure) it.next()).displayBox());
			}
		}
		catch (ConcurrentModificationException e)
		{
			// Ignore
		}

		return box;
	}

	/**
	 * Saves the transformation, draws the tag and restores the transformation.
	 * @see CH.ifa.draw.standard.CompositeFigure#draw(Graphics g)
	 */
	public void draw(Graphics g)
	{
		if (isVisible())
		{
			Graphics2D g2 = (Graphics2D) g;

			// Draw the presentation figure first
			presentationFigure.draw(g2);

			// Draw the children of this composite figure
			super.draw(g2);

			// Finally the overlay if the tag is decorated
			Figure overlay = (Figure) DecorationMgr.decorate(this, BasicFigure.DECO_OVERLAY, null);
			if (overlay != null)
			{
				// Make the overlay the same size as the presentation figure
				overlay.draw(g);
			}
		}
	}

	/**
	 * @see CH.ifa.draw.standard.AbstractFigure#changed ()
	 */
	public void changed()
	{
		super.changed();

		// Update the paint in case of a gradient when changing sizes
		presentationFigure.changed();
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#invalidate()
	 */
	public void invalidate()
	{
		if (getParent() != null)
			getParent().invalidate();
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#containsPoint(int x, int y)
	 */
	public boolean containsPoint(int x, int y)
	{
		return presentationFigure.containsPoint(x, y);
	}

	public boolean canConnect()
	{
		return false;
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#findFigure(int x, int y)
	 */
	public Figure findFigure(int x, int y)
	{
		for (ListIterator lit = content.listIterator(content.size()); lit.hasPrevious();)
		{
			TagContent tc = (TagContent) lit.previous();

			// Make sure we return only figures we can interact with
			if (((tc.getContentType() & CONTENT_INTERACTION) != 0) && tc.getFigure().containsPoint(x, y))
				return tc.getFigure();
		}
		return containsPoint(x, y) ? this : null;
	}

	/**
	 * @see CH.ifa.draw.standard.CompositeFigure#findFigureInside(int x, int y)
	 */
	public Figure findFigureInside(int x, int y)
	{
		for (ListIterator lit = content.listIterator(content.size()); lit.hasPrevious();)
		{
			TagContent tc = (TagContent) lit.previous();

			// Make sure we return only figures we can interact with
			if (((tc.getContentType() & CONTENT_INTERACTION) != 0) && tc.getFigure().containsPoint(x, y))
				return tc.getFigure().findFigureInside(x, y);
		}
		return containsPoint(x, y) ? this : null;
	}

	//////////////////////////////////////////////////
	// @@ LayoutableTag implementation
	//////////////////////////////////////////////////

	/**
	 * Layouts the tag.
	 * Invoked everytime something changes the tag (moving, resize, rotate).
	 * Does the following:<br>
	 * - layout/determine the size of the content<br>
	 * - set the origin of the content point in tag coordinates<br>
	 * - determine own size<br>
	 * - determine appropriate size for the presentation figure<br>
	 * - create display boxes for both and transform them to world coordinates
	 * @see org.openbp.cockpit.modeler.figures.layouter.LayoutableTag#layoutTag()
	 */
	public void layoutTag()
	{
		willChange();

		// we make sure that angle is between 0 and 2PI
		angle = CircleConstants.normalizeAngle(angle);

		// Update the layouter's information
		layouter.determineDirection();

		// Get the size of our contents
		Rectangle rect = layouter.calculateSize();

		// Position the display box of the tag relative to its parent figure
		rect = positionDisplayBox(rect);

		// Apply the layout to the presentation figure and the shadow
		presentationFigure.displayBox(rect);
		if (shadowFigure != null)
		{
			shadowFigure.displayBox(rect);
		}

		// Layout the tag content
		layouter.performLayout(rect);

		changed();
	}

	/**
	 * Gets the the layouter is responsible for laying out the tag content.
	 * @nowarn
	 * @see LayoutableTag#getLayouter()
	 */
	public TagLayouter getLayouter()
	{
		return layouter;
	}

	/**
	 * Sets the the layouter is responsible for laying out the tag content.
	 * @nowarn
	 * @see LayoutableTag#setLayouter(TagLayouter layouter)
	 */
	public void setLayouter(TagLayouter layouter)
	{
		this.layouter = layouter;
	}

	/**
	 * Positions the display box of the tag relative to its parent figure.
	 *
	 * @param rect Rectangle object that contains the size of the figure
	 * @return The new display box rectangle of the presentation figure
	 */
	protected Rectangle positionDisplayBox(Rectangle rect)
	{
		return rect;
	}

	/**
	 * Gets the origin.
	 * @nowarn
	 */
	public Point getOrigin()
	{
		return origin;
	}

	/**
	 * Sets the origin.
	 * @nowarn
	 */
	public void setOrigin(Point origin)
	{
		this.origin = new Point(origin);

		layoutTag();
	}

	/**
	 * Gets the rotation angle.
	 * @nowarn
	 */
	public double getAngle()
	{
		return this.angle;
	}

	/**
	 * Sets the rotation angle.
	 * @nowarn
	 */
	public void setAngle(double angle)
	{
		willChange();

		angle = CircleConstants.normalizeAngle(angle);

		basicSetAngle(angle);

		// Inform clients
		moveBy(0, 0);

		layoutTag();
	}

	/**
	 * Gets the figure that forms the center of the rotation (usually the parent).
	 * @nowarn
	 */
	public Figure getCenterFigure()
	{
		Figure centerFigure = parent;
		while (centerFigure != null)
		{
			if (! (centerFigure instanceof ChildFigure))
				break;
			ChildFigure cf = (ChildFigure) centerFigure;
			if (cf.getParent() == null)
				break;
			centerFigure = cf.getParent();
		}
		return centerFigure;
	}

	/**
	 * Sets the angle to the given one.
	 *
	 * @nowarn
	 */
	protected void basicSetAngle(double angle)
	{
		this.angle = angle;
	}

	//////////////////////////////////////////////////
	// @@ Orientation management
	//////////////////////////////////////////////////

	/**
	 * Determines the 2-way orientation of the tag figure.
	 *
	 * @param vertical
	 * true: Return LEFT or RIGHT<br>
	 * false: Return TOP or BOTTOM
	 * @return {@link Orientation#RIGHT}/{@link Orientation#BOTTOM}/{@link Orientation#LEFT}/{@link Orientation#TOP}
	 */
	public Orientation determine2WayOrientation(boolean vertical)
	{
		Rectangle centerBox = getCenterFigureBox();

		// Determine the quarter from the socket's orientation
		Quarter quarter = CircleConstants.determineQuarter(angle, centerBox);

		Orientation orientation;
		if (vertical)
		{
			orientation = quarter == Quarter.NW || quarter == Quarter.NE ? Orientation.TOP : Orientation.BOTTOM;
		}
		else
		{
			orientation = quarter == Quarter.SW || quarter == Quarter.NW ? Orientation.LEFT : Orientation.RIGHT;
		}
		return orientation;
	}

	/**
	 * Returns the display box of the center figure or null.
	 * Considers {@link Expandable} center figures also.
	 *
	 * @return The box or null if there is no center figure
	 */
	public Rectangle getCenterFigureBox()
	{
		Rectangle centerBox = null;
		Figure centerFigure = getCenterFigure();
		if (centerFigure != null)
		{
			if (centerFigure instanceof Expandable)
			{
				// The orientation change may affect the overall figure display box,
				// which may lead to 'flicker' of the orientation as the angle changes.
				// So we use the compact display box, which should be constant, if possible.
				centerBox = ((Expandable) centerFigure).compactDisplayBox();
			}
			else
			{
				centerBox = centerFigure.displayBox();
			}
		}
		return centerBox;
	}

	//////////////////////////////////////////////////
	// @@ Expandable implemenation
	//////////////////////////////////////////////////

	/**
	 * Returns the display box of the presentation figure.
	 * @see org.openbp.cockpit.modeler.figures.generic.Expandable#compactDisplayBox()
	 */
	public Rectangle compactDisplayBox()
	{
		return presentationFigure.displayBox();
	}

	//////////////////////////////////////////////////
	// @@ VisualElement implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#setDrawing(ProcessDrawing)
	 */
	public void setDrawing(ProcessDrawing processDrawing)
	{
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getDrawing()
	 */
	public ProcessDrawing getDrawing()
	{
		return parent.getDrawing();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getParentElement()
	 */
	public VisualElement getParentElement()
	{
		return parent;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getPresentationFigure()
	 */
	public Figure getPresentationFigure()
	{
		return presentationFigure;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#updatePresentationFigure()
	 */
	public void updatePresentationFigure()
	{
		Rectangle db = null;
		if (presentationFigure != null)
		{
			presentationFigure.willChange();

			// Save current size and position
			db = presentationFigure.displayBox();
		}

		// Reinitialize figure
		initPresentationFigure();

		if (db != null)
		{
			// Reassign size and position
			presentationFigure.displayBox(db);
		}

		// Layout the tag
		layoutTag();

		presentationFigure.changed();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#isVisible()
	 */
	public boolean isVisible()
	{
		return (visualStatus & VisualElement.VISUAL_VISIBLE) != 0;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#setVisible(boolean)
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
			visualStatus &= ~ VisualElement.VISUAL_VISIBLE;
		}

		for (Iterator it = content.iterator(); it.hasNext();)
		{
			Figure fig = ((TagContent) it.next()).getFigure();

			if (fig instanceof VisualElement)
			{
				((VisualElement) fig).setVisible(visible);
			}
		}

		changed();
	}

	/**
	 * Gets the visual status as defined by {@link VisualElement}
	 * @nowarn
	 */
	public int getVisualStatus()
	{
		return visualStatus;
	}

	/**
	 * Sets the visual status as defined by {@link VisualElement}
	 * @nowarn
	 */
	public void setVisualStatus(int visualStatus)
	{
		this.visualStatus = visualStatus;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#handleEvent(VisualElementEvent event)
	 */
	public boolean handleEvent(VisualElementEvent event)
	{
		if (event.type == VisualElementEvent.DOUBLE_CLICK)
		{
			Figure f = findFigure(event.x, event.y);

			if (f == this)
			{
				// Do nothing by default
			}
			else if (f instanceof VisualElement)
			{
				return ((VisualElement) f).handleEvent(event);
			}
		}
		return false;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElement(int, int)
	 */
	public VisualElement findVisualElement(int x, int y)
	{
		for (FigureEnumeration fe = this.figures(); fe.hasMoreElements();)
		{
			Figure next = fe.nextFigure();

			if (next instanceof VisualElement && next.containsPoint(x, y))
				return (VisualElement) next;
		}

		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElementInside(int, int)
	 */
	public VisualElement findVisualElementInside(int x, int y)
	{
		if (! containsPoint(x, y))
			return null;

		VisualElement child = this.findVisualElement(x, y);

		return (child != null ? child.findVisualElementInside(x, y) : this);
	}

	//////////////////////////////////////////////////
	// @@ UpdatableFigure implementation
	//////////////////////////////////////////////////

	/**
	 * Causes all UpdatableFigure children to updateFigure.
	 */
	public void updateFigure()
	{
		if (presentationFigure instanceof UpdatableFigure)
		{
			((UpdatableFigure) presentationFigure).updateFigure();
		}

		for (Iterator it = content.iterator(); it.hasNext();)
		{
			Figure next = ((TagContent) it.next()).getFigure();

			if (next instanceof UpdatableFigure)
			{
				((UpdatableFigure) next).updateFigure();
			}
		}

		applyContentState();
	}

	//////////////////////////////////////////////////
	// @@ ChildFigure implementation
	//////////////////////////////////////////////////

	/**
	 * @see ChildFigure#getParent()
	 */
	public Figure getParent()
	{
		return parent;
	}

	/**
	 * @see ChildFigure#setParent(Figure)
	 */
	public void setParent(Figure parent)
	{
		// Never has a parent
	}

	//////////////////////////////////////////////////
	// @@ ShadowDropper implementation
	//////////////////////////////////////////////////

	/**
	 * @see ShadowDropper#getShadow()
	 */
	public Figure getShadow()
	{
		return (isVisible()) ? shadowFigure : null;
	}

	//////////////////////////////////////////////////
	// @@ Colorizable implementation
	//////////////////////////////////////////////////

	public void setFillColor(Color color)
	{
		presentationFigure.setFillColor(color);
	}

	public Color getFillColor()
	{
		return presentationFigure.getFillColor();
	}

	//////////////////////////////////////////////////
	// @@ Storable implementation
	//////////////////////////////////////////////////

	public void read(StorableInput dr)
	{
	}

	public void write(StorableOutput dw)
	{
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	/**
	 * Simple class that combines a figure object with additional information,
	 * i\.e\. about the type of the figure. Used to integrate a figure into a tag.
	 */
	public class TagContent
	{
		//////////////////////////////////////////////////
		// @@ Members
		//////////////////////////////////////////////////

		/** Content figure */
		protected Figure figure;

		/** Type of the content */
		protected int type;

		//////////////////////////////////////////////////
		// @@ Construction
		//////////////////////////////////////////////////

		/**
		 * Constructor.
		 *
		 * @param figure Content figure
		 * @param type One of the CONTENT_* constants of this class
		 */
		public TagContent(Figure figure, int type)
		{
			this.figure = figure;
			this.type = type;
		}

		/**
		 * Returns a string representation of this object.
		 * @nowarn
		 */
		public String toString()
		{
			return ToStringHelper.toString(this, "figure");
		}

		//////////////////////////////////////////////////
		// @@ MemberAccess
		//////////////////////////////////////////////////

		/**
		 * Gets the content figure.
		 * @nowarn
		 */
		public Figure getFigure()
		{
			return figure;
		}

		/**
		 * Sets the content figure.
		 * @nowarn
		 */
		public void setFigure(Figure figure)
		{
			this.figure = figure;
		}

		/**
		 * Gets the type of the content.
		 * @return One of the CONTENT_* constants of this class
		 */
		public int getContentType()
		{
			return type;
		}

		/**
		 * Sets the type of the content.
		 * @param type One of the CONTENT_* constants of this class
		 */
		public void setContentType(int type)
		{
			this.type = type;
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ InteractionClient
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragActionTriggered(Object, Point)
	 */
	public void dragActionTriggered(Object regionId, Point p)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragStarted(Transferable)
	 */
	public void dragStarted(Transferable transferable)
	{
		DropClientUtil.dragStarted(this, transferable);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragEnded(Transferable)
	 */
	public void dragEnded(Transferable transferable)
	{
		DropClientUtil.dragEnded(this, transferable);
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
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getImportersAt(Point)
	 */
	public List getImportersAt(Point p)
	{
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllImportersAt(Point)
	 */
	public List getAllImportersAt(Point p)
	{
		return DropClientUtil.getAllImportersAt(this, p);
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
		return false;
	}
}
