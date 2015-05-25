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
package org.openbp.swing.plaf.sky;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * The Divider for the SplitPane. This Divider is with a Mousover effect
 * and contains the normal funktions like ontouchopening ...
 *
 * @author Jens Ferchland
 */
public class SkySplitPaneDivider extends BasicSplitPaneDivider
{
	private boolean mouseOver = false;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Creates a new <code>SkySplitPaneDivider</code> instance.
	 *
	 * @param ui a <code>BasicSplitPaneUI</code> value
	 */
	public SkySplitPaneDivider(BasicSplitPaneUI ui)
	{
		super(ui);
		setBackground(UIManager.getColor("SplitPaneDivider.background"));
		setForeground(UIManager.getColor("SplitPaneDivider.foreground"));
		this.addMouseListener(new MouseAdapter()
		{
			public void mouseEntered(MouseEvent e)
			{
				setMouseOver(true);
			}

			public void mouseExited(MouseEvent e)
			{
				setMouseOver(false);
			}
		});
	}

	/**
	 * Overwrites the paint method of Component to
	 * generate the line in the middle of the divider.
	 *
	 * @param g a <code>Graphics</code> value
	 */
	public void paint(Graphics g)
	{
		// paint the onetouchbuttons if needed.
		super.paint(g);

		/*
		 int offset = 0;

		 // is it vertical?
		 if (getHeight () > getWidth ())
		 {
		 if (getHeight () > 2 * OFFSETSIZE + MINSIZE)
		 offset = OFFSETSIZE;

		 g.setColor (isMouseOver () ? mouseOverColor : getForeground ());
		 g.drawLine (2, 2 + offset, 2, getHeight () - 4 - (2 * offset));
		 g.setColor (isMouseOver () ? getForeground () : Color.black);
		 g.drawLine (3, 2 + offset, 3, getHeight () - 4 - (2 * offset));
		 }
		 else
		 {
		 if (getWidth () > 2 * OFFSETSIZE + MINSIZE)
		 offset = OFFSETSIZE;

		 g.setColor (isMouseOver () ? mouseOverColor : getForeground ());
		 g.drawLine (2 + offset, 2, getWidth () - 4 - (2 * offset), 2);
		 g.setColor (isMouseOver () ? getForeground () : Color.black);
		 g.drawLine (2 + offset, 3, getWidth () - 4 - (2 * offset), 3);
		 }
		 */
	}

	/**
	 * Repaints the Divider if the mouse is over.
	 *
	 * @param mouseOver a <code>boolean</code> value
	 */
	protected void setMouseOver(boolean mouseOver)
	{
		this.mouseOver = mouseOver;
		repaint();
	}

	/**
	 * Is the mouse over the divider?
	 *
	 * @return a <code>boolean</code> value
	 */
	public boolean isMouseOver()
	{
		return mouseOver;
	}
}
