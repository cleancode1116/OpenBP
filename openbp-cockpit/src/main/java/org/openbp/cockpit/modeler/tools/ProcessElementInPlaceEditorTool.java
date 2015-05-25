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
package org.openbp.cockpit.modeler.tools;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import org.openbp.cockpit.modeler.ModelerGraphics;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;
import org.openbp.common.string.StringUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.process.ProcessObject;
import org.openbp.swing.components.JMsgBox;

import CH.ifa.draw.framework.Figure;

/**
 * The text tool enables in-place-editing of figures that hold a display element.
 * In contrast to the JHotDraw TextTool, it is not activated by a double-click.
 *
 * @author Stephan Pauxberger
 */
public class ProcessElementInPlaceEditorTool extends ModelerTool
{
	/** Process element we are editing */
	private ProcessElementContainer pec;

	/** Text overlay for in-place text editing */
	private InPlaceEditingTextField textOverlay;

	public ProcessElementInPlaceEditorTool(ModelerToolSupport toolSupport)
	{
		super(toolSupport);

		textOverlay = new InPlaceEditingTextField();
		textOverlay.setFont(ModelerGraphics.getStandardTextFont());
		textOverlay.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_ENTER)
				{
					deactivate();
				}
				else if (keyCode == KeyEvent.VK_ESCAPE)
				{
					cancelEdit();
					deactivate();
				}
			}
		});
	}

	public void setAffectedObject(Object affectedObject)
	{
		if (affectedObject != getAffectedObject())
		{
			cancelEdit();
		}
		super.setAffectedObject(affectedObject);

		this.pec = (ProcessElementContainer) affectedObject;
	}

	public void activate()
	{
		super.activate();
		getView().setCursor(ModelerGraphics.standardCursor);
		beginEdit();
	}

	public void deactivate()
	{
		if (endEdit())
		{
			super.deactivate();
		}
	}

	public void mouseUp(MouseEvent e, int x, int y)
	{
		VisualElement clickedFigure = ((ProcessDrawing) getDrawing()).findVisualElementInside(x, y);
		if (clickedFigure instanceof ProcessElementContainer && ! (clickedFigure instanceof ProcessDrawing) && e.getClickCount() == 1)
		{
			if (saveEdit())
			{
				// Toggle
				if (clickedFigure == pec)
				{
					deactivate();
				}
				else
				{
					pec = (ProcessElementContainer) clickedFigure;
					getView().singleSelect(clickedFigure);
					beginEdit();
				}
			}
		}
		else
		{
			deactivate();
		}
	}

	public void beginEdit()
	{
		if (pec == null)
			return;

		String text = pec.getReferredProcessElement().getName();
		int nChars = text != null ? text.length() : 1;
		Dimension textDim = textOverlay.getPreferredSize(nChars);

		Figure f = pec.getPresentationFigure();
		if (f == null)
			f = pec;
		Rectangle figureRect = f.displayBox();
		figureRect = getView().applyScale(figureRect, false);
		int figureWidth = figureRect.width;
		int textWidth = textDim.width;

		int w = figureWidth - 10;
		if (textWidth > w)
			w = textWidth;
		else if (w > 120)
			w = 120;
		if (w < 60)
			w = 60;
		int h = textDim.height;
		int x = figureRect.x;
		if (figureWidth <= 200)
		{
			// Small figure, display to the right of figure
			x += figureWidth + 5;
		}
		else
		{
			// Large figure, center in figure
			x += figureWidth / 2 - w / 2;
		}
		int y = figureRect.y + figureRect.height / 2 - h / 2;
		Rectangle textRect = new Rectangle(x, y, w, h);

		// Rectangle docTextRect = figureRect;
		Rectangle docTextRect = getView().applyScale(textRect, true);
		docTextRect.width += 10;
		getView().scrollRectToVisible(docTextRect);

		textOverlay.displayOverlay(text, (Container)getView(), textRect);

		getEditor().startUndo("Edit Text");
	}

	protected boolean saveEdit()
	{
		if (pec != null)
		{
			ProcessObject po = pec.getReferredProcessElement();

			String text = textOverlay.getText();
			text = StringUtil.trimNull(text);

			String msg = null;

			if (text == null)
			{
				msg = "An object name must not be empty.";
			}
			else if (!ModelQualifier.isValidIdentifier(text))
			{
				msg = "An object name must not contain one of the characters '.', '/', ':', ';'.";
			}
			else if (((ProcessDrawing) getDrawing()).getProcess().getNodeByName(text) != null)
			{
				for (Iterator it = po.getContainerIterator(); it.hasNext();)
				{
					ModelObject mo = (ModelObject) it.next();
					if (mo.getName().equals(text) && mo != po)
					{
						msg = "An element having this name already exists.";
						break;
					}
				}
			}

			if (msg != null)
			{
				JMsgBox.show(null, msg, JMsgBox.ICON_INFO);
				return false;
			}

			po.setName(text);
			pec.updateFigure();

			getView().singleSelect(pec);
		}
		return true;
	}

	protected boolean endEdit()
	{
		if (saveEdit())
		{
			textOverlay.endOverlay();
			pec = null;
			return true;
		}
		return false;
	}

	protected void cancelEdit()
	{
		getEditor().cancelUndo();
		textOverlay.endOverlay();
		pec = null;
	}
}
