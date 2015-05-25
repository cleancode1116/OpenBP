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
package org.openbp.cockpit.modeler.drawing.shadowlayout;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.Timer;

import CH.ifa.draw.framework.DrawingChangeEvent;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

/**
 * @author Stephan Moritz
 */
public class RealLightSourceShadowLayouter
	implements ActionListener, ShadowLayouter
{
	private double lightX;

	private double lightY;

	private double factor;

	private DrawingView view;

	private AffineTransform to = new AffineTransform();

	private Timer motor;

	private long time;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public RealLightSourceShadowLayouter(DrawingView view, double factor)
	{
		this.view = view;
		this.factor = factor;

		actionPerformed(null);

		motor = new Timer(100, this);
		motor.start();
	}

	public void drawShadows(FigureEnumeration en, Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		AffineTransform trans = g2.getTransform();
		g2.transform(to);

		while (en.hasMoreElements())
		{
			Figure next = en.nextFigure();
			next.draw(g);
		}

		g2.setTransform(trans);
	}

	public Rectangle transformRectangle(Rectangle r)
	{
		Point2D c1 = new Point2D.Double(r.getMinX(), r.getMinY());
		Point2D c2 = new Point2D.Double(r.getMaxX(), r.getMaxY());

		c1 = to.transform(c1, null);

		Point p = new Point((int) c1.getX(), (int) c1.getY());

		Rectangle rec = new Rectangle(p);
		rec.add(to.transform(c2, null));

		return rec;
	}

	// implementation of java.awt.event.ActionListener interface

	/**
	 * @param param1 <description>
	 */
	public void actionPerformed(ActionEvent param1)
	{
		time++;
		if (time > 1440)
		{
			time -= 1440;
		}

		/*
		 Date d = new Date ();

		 time = (d.getHours () * 60) + d.getMinutes ();
		 */
		double angle = time * 2d * Math.PI / 1440d - Math.PI / 2d;

		lightX = Math.cos(angle) * view.getSize().getHeight() + view.getSize().getWidth() / 2d;

		lightY = Math.sin(angle) * view.getSize().getHeight();

		to.translate(lightX, lightY);
		to.scale(factor, factor);
		to.translate(-lightX, -lightY);

		view.drawingInvalidated(new DrawingChangeEvent(view.drawing(), new Rectangle(-10000, -10000, 20000, 20000)));
		view.repairDamage();
	}

	public void releaseShadowLayouter()
	{
		motor.stop();
		motor = null;
	}
}
