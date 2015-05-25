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
package org.openbp.cockpit.modeler.figures.process;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.VisualElementEvent;
import org.openbp.cockpit.modeler.figures.generic.XFontSizeHandle;
import org.openbp.cockpit.modeler.figures.generic.XTextFigure;
import org.openbp.cockpit.modeler.util.ModelerFlavors;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.model.item.process.ProcessObject;
import org.openbp.core.model.item.process.TextElement;
import org.openbp.jaspira.gui.interaction.Importer;
import org.openbp.jaspira.gui.interaction.ViewDropRegion;
import org.openbp.swing.SwingUtil;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.standard.AbstractLocator;

/**
 * The text element figure represents a simple text rectangle.
 * The size of the figure is computed automatically based on the text and font settings.
 *
 * @author Stephan Moritz
 */
public class TextElementFigure extends XTextFigure
	implements ProcessElementContainer
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Region id for color region */
	public static final String REGION_COLOR = "color";

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Text element */
	private TextElement textElement;

	/** The visual status as defined in org.openbp.cockpit.modeler.figures.VisualElement */
	private int visualStatus = VISUAL_VISIBLE;

	/** Process drawing we belong to */
	private ProcessDrawing drawing;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public TextElementFigure()
	{
		super();
	}

	/**
	 * Connects the figure to a process object.
	 *
	 * @param textElement Text element
	 * @param drawing Process drawing that owns the figure
	 */
	public void connect(TextElement textElement, ProcessDrawing drawing)
	{
		// Text figure that adjusts its size according to the text
		setAutoSize(true);

		this.textElement = textElement;
		this.drawing = drawing;

		String text = textElement.getDescription();
		if (text == null)
			text = "Text";
		setText(text);

		decodeGeometry();
	}

	/**
	 * Gets the text element.
	 * @nowarn
	 */
	public TextElement getTextElement()
	{
		return textElement;
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "textElement");
	}

	//////////////////////////////////////////////////
	// @@ Figure overrides
	//////////////////////////////////////////////////

	/**
	 * We display a single font size handle in the top left corner only.
	 * @see CH.ifa.draw.standard.AbstractFigure#handles()
	 */
	public Vector handles()
	{
		Vector handles = new Vector();
		handles.addElement(new XFontSizeHandle(this, new SizeHandleLocator(this)));
		return handles;
	}

	/**
	 * @see CH.ifa.draw.framework.Figure#release()
	 */
	public void release()
	{
		super.release();

		// Remove the element from the process
		getDrawing().getProcess().removeTextElement(textElement);
	}

	//////////////////////////////////////////////////
	// @@ Geometry serialization support
	//////////////////////////////////////////////////

	/**
	 * Decodes the enclosed geometry information of the asssociated process element.
	 */
	public void decodeGeometry()
	{
		parseGeometry(textElement.getGeometry());
	}

	/**
	 * Encodes the geometry information of this object into the associated process element.
	 */
	public void encodeGeometry()
	{
		textElement.setGeometry(createGeometry());
	}

	//////////////////////////////////////////////////
	// @@ ProcessElementContainer implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getProcessElement()
	 */
	public ProcessObject getProcessElement()
	{
		return textElement;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#getReferredProcessElement()
	 */
	public ProcessObject getReferredProcessElement()
	{
		return getProcessElement();
	}

	/**
	 * No object should be selected on deletion.
	 * @see org.openbp.cockpit.modeler.figures.process.ProcessElementContainer#selectionOnDelete()
	 */
	public Figure selectionOnDelete()
	{
		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElement(int, int)
	 */
	public ProcessElementContainer findProcessElementContainer(int x, int y)
	{
		if (containsPoint(x, y))
			return this;

		return null;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElementInside(int, int)
	 */
	public ProcessElementContainer findProcessElementContainerInside(int x, int y)
	{
		return findProcessElementContainer(x, y);
	}

	//////////////////////////////////////////////////
	// @@ VisualElement implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#setDrawing(ProcessDrawing)
	 */
	public void setDrawing(ProcessDrawing drawing)
	{
		this.drawing = drawing;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getDrawing()
	 */
	public ProcessDrawing getDrawing()
	{
		return drawing;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#getParentElement()
	 */
	public VisualElement getParentElement()
	{
		return getDrawing();
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

		changed();
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#handleEvent(VisualElementEvent event)
	 */
	public boolean handleEvent(VisualElementEvent event)
	{
		return false;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElement(int, int)
	 */
	public VisualElement findVisualElement(int x, int y)
	{
		return this;
	}

	/**
	 * @see org.openbp.cockpit.modeler.figures.VisualElement#findVisualElementInside(int, int)
	 */
	public VisualElement findVisualElementInside(int x, int y)
	{
		return this;
	}

	//////////////////////////////////////////////////
	// @@ UpdatableFigure implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.generic.UpdatableFigure#updateFigure()
	 */
	public void updateFigure()
	{
		String newText = textElement.getDescription();

		if (newText == null)
			newText = "Text";
		setText(newText);
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
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragStarted(Transferable)
	 */
	public void dragStarted(Transferable transferable)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllDropRegions(List, Transferable, MouseEvent)
	 */
	public List getAllDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		return getDropRegions(flavors, data, mouseEvent);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		if (flavors.contains(ModelerFlavors.COLOR))
		{
			// Use the whole figure as target
			WorkspaceDrawingView view = getDrawing().getView();
			Rectangle r = view.applyScale(displayBox(), false);
			return Collections.singletonList(new ViewDropRegion(REGION_COLOR, this, r, view));
		}

		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getImportersAt(Point)
	 */
	public List getImportersAt(Point p)
	{
		WorkspaceDrawingView view = getDrawing().getView();

		Point docPoint = SwingUtil.convertFromGlassCoords(p, view);
		if (containsPoint(docPoint.x, docPoint.y))
			return Collections.singletonList(new Importer(REGION_COLOR, this, new DataFlavor[]
			{
				ModelerFlavors.COLOR
			}));

		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllImportersAt(Point)
	 */
	public List getAllImportersAt(Point p)
	{
		return getImportersAt(p);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getSubClients()
	 */
	public List getSubClients()
	{
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#importData(Object, Transferable, Point)
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		try
		{
			if (regionId.equals(REGION_COLOR))
			{
				// Set the color
				Color color = (Color) data.getTransferData(ModelerFlavors.COLOR);

				getDrawing().getEditor().startUndo("Set Color");

				setFillColor(color);
				invalidate();

				getDrawing().getEditor().endUndo();

				return true;
			}
		}
		catch (UnsupportedFlavorException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
		return false;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Inner classes
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Locator that returns a postion 5 pixels from the upper left of the display box.
	 */
	private class SizeHandleLocator extends AbstractLocator
	{
		/** Owner figure */
		private final Figure owner;

		/**
		 * Constructor
		 *
		 * @param owner Owner figure
		 */
		public SizeHandleLocator(Figure owner)
		{
			super();
			this.owner = owner;
		}

		/**
		 * Returns the position.
		 *
		 * @param figure Is ignored
		 */
		public Point locate(Figure figure)
		{
			Rectangle r = owner.displayBox();

			return new Point(r.x - HANDLE_DISTANCE, r.y - HANDLE_DISTANCE);
		}
	}
}
