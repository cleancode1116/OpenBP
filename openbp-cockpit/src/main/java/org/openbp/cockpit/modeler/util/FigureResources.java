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
package org.openbp.cockpit.modeler.util;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.openbp.cockpit.modeler.ModelerColors;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.generic.XCircleFigure;
import org.openbp.cockpit.modeler.figures.generic.XEllipseFigure;
import org.openbp.cockpit.modeler.figures.generic.XFigure;
import org.openbp.cockpit.modeler.figures.generic.XRectangleFigure;
import org.openbp.cockpit.modeler.figures.generic.XRoundRectangleFigure;
import org.openbp.cockpit.modeler.figures.generic.XTriangleFigure;

import CH.ifa.draw.framework.Figure;

/**
 * Standard figure resources.
 *
 * @author Heiko Erhardt
 */
public class FigureResources
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** 1 pixel stroke */
	public static final Stroke standardStroke1 = new BasicStroke(1.0f);

	/** 2 pixel stroke */
	public static final Stroke standardStroke2 = new BasicStroke(2.0f);

	/** 3 pixel stroke */
	public static final Stroke standardStroke3 = new BasicStroke(3.0f);

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private FigureResources()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	//////////////////////////////////////////////////
	// @@ Overlay decoration support
	//////////////////////////////////////////////////

	private static final XFigure [] acceptOverlayFigures = new XFigure [] { new XCircleFigure(), new XEllipseFigure(), new XRoundRectangleFigure(), new XRectangleFigure(), new XTriangleFigure(), };

	private static final XFigure [] expressionOverlayFigures = new XFigure [] { new XCircleFigure(), new XEllipseFigure(), new XRoundRectangleFigure(), new XRectangleFigure(), new XTriangleFigure(), };

	private static final XFigure [] breakpointOverlayFigures = new XFigure [] { new XCircleFigure(), new XEllipseFigure(), new XRoundRectangleFigure(), new XRectangleFigure(), new XTriangleFigure(), };

	static
	{
		// Initialize the overlays with a semi-transparent green rectangle
		for (int i = 0; i < acceptOverlayFigures.length; ++i)
		{
			XFigure figure = acceptOverlayFigures [i];

			figure.setFrameColor(ModelerColors.CONNECTION_ACCEPT);
			figure.setFillColor(ModelerColors.CONNECTION_ACCEPT);
			figure.setFillColor2(null);
		}

		// Initialize the overlays with a semi-transparent green rectangle
		for (int i = 0; i < expressionOverlayFigures.length; ++i)
		{
			XFigure figure = expressionOverlayFigures [i];

			figure.setFrameColor(null);
			figure.setFillColor(ModelerColors.EXPRESSION_OVERLAY);
			figure.setFillColor2(null);
		}

		// Initialize the overlays with a semi-transparent green rectangle
		for (int i = 0; i < breakpointOverlayFigures.length; ++i)
		{
			XFigure figure = breakpointOverlayFigures [i];

			figure.setFrameColor(null);
			figure.setFillColor(ModelerColors.BREAKPOINT_OVERLAY);
			figure.setFillColor2(null);
		}
	}

	/**
	 * This methods is used by decorators that wish to overlay a presentation figure with
	 * a semi-transparent green color to indicate acceptable locations for object dragging.
	 *
	 * @param owner Owner object to be decorated (i. e. a node, a socket, a parameter etc.)
	 * @return An overlay figure that matches the size and shape of the presentation figure of
	 * the supplied owner object, provided that<br>
	 * - the owner is a {@link VisualElement} which has a {@link VisualElement#getPresentationFigure} or<br>
	 * - the owner is a Figure itself<br>
	 * and<br>
	 * - the presentation figure is supported by this method<br>
	 * or null otherwise.
	 */
	public static Figure getAcceptOverlay(Object owner)
	{
		return getOverlay(owner, acceptOverlayFigures);
	}

	/**
	 * This methods is used by decorators that wish to overlay a presentation figure with
	 * a semi-transparent blue color to indicate an expression inside the figure.
	 *
	 * @param owner Owner object to be decorated (i. e. a node, a socket, a parameter etc.)
	 * @return An overlay figure that matches the size and shape of the presentation figure of
	 * the supplied owner object, provided that<br>
	 * - the owner is a {@link VisualElement} which has a {@link VisualElement#getPresentationFigure} or<br>
	 * - the owner is a Figure itself<br>
	 * and<br>
	 * - the presentation figure is supported by this method<br>
	 * or null otherwise.
	 */
	public static Figure getExpressionOverlay(Object owner)
	{
		return getOverlay(owner, expressionOverlayFigures);
	}

	public static Figure getBreakpointOverlay(Object owner)
	{
		return getOverlay(owner, breakpointOverlayFigures);
	}

	/**
	 * Sub function for overlay figures.
	 *
	 * @param owner Owner object to be decorated (i. e. a node, a socket, a parameter etc.)
	 * @param overlayFigures Array of figures that contain the overlay
	 * @return An overlay figure that matches the size and shape of the presentation figure of
	 * the supplied owner object, provided that<br>
	 * - the owner is a {@link VisualElement} which has a {@link VisualElement#getPresentationFigure} or<br>
	 * - the owner is a Figure itself<br>
	 * and<br>
	 * - the presentation figure is supported by this method<br>
	 * or null otherwise.
	 */
	private static Figure getOverlay(Object owner, XFigure [] overlayFigures)
	{
		Figure presentationFigure = null;

		// Determine the presentation figure of the owner
		if (owner instanceof VisualElement)
			presentationFigure = ((VisualElement) owner).getPresentationFigure();
		else if (owner instanceof Figure)
			presentationFigure = (Figure) owner;

		if (presentationFigure != null)
		{
			// Check if we support this type of figure
			Class presentationClass = presentationFigure.getClass();
			for (int i = 0; i < overlayFigures.length; ++i)
			{
				XFigure figure = overlayFigures [i];

				if (figure.getClass().isAssignableFrom(presentationClass))
				{
					// Yes, we do. Adjust the overlay figure position and orientation accordingly and return it
					figure.displayBox(presentationFigure.displayBox());
					if (presentationFigure instanceof XFigure)
					{
						figure.setOrientation(((XFigure) presentationFigure).getOrientation());
					}

					if (presentationFigure instanceof XRoundRectangleFigure)
					{
						XRoundRectangleFigure sourceRR = (XRoundRectangleFigure) presentationFigure;
						XRoundRectangleFigure targetRR = (XRoundRectangleFigure) figure;

						targetRR.setArcWidth(sourceRR.getArcWidth());
						targetRR.setArcHeight(sourceRR.getArcHeight());
					}

					return figure;
				}
			}
		}

		// Not supported
		return null;
	}
}
