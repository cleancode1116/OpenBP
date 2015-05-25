/*
 *   Copyright 2009 skynamics AG
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
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

/**
 * Flaoting text field that provides key listeners.
 *
 * @author Stephan Pauxberger
 */
public class InPlaceEditingTextField
{
	private JTextField editWidget;
	private Container container;

	public InPlaceEditingTextField()
	{
		editWidget = new JTextField();
	}

	/**
	 * Positions the overlay.
	 */
	public void displayOverlay(String text, Container container, Rectangle r)
	{
		container.add(editWidget, 0);
		this.container = container;

		editWidget.setText(text);
		editWidget.setBounds(r.x, r.y, r.width, r.height);
		editWidget.setVisible(true);
		editWidget.selectAll();
		editWidget.requestFocus();
	}

	/**
	 * Removes the overlay.
	 */
	public void endOverlay()
	{
		if (container != null)
		{
			editWidget.setVisible(false);
			container.remove(editWidget);

			Rectangle bounds = editWidget.getBounds();
			container.repaint(bounds.x, bounds.y, bounds.width, bounds.height);

			container.requestFocus();
			container = null;
		}
	}

	/**
	 * Gets the text contents of the overlay.
	 */
	public String getText()
	{
		return editWidget.getText();
	}

	/**
	 * Sets the font.
	 *
	 * @param font New font
	 */
	public void setFont(Font font)
	{
		if (font != null)
		{
			editWidget.setFont(font);
		}
	}

	/**
	 * Gets the preferred size of the overlay.
	 */
	public Dimension getPreferredSize(int cols)
	{
		editWidget.setColumns(cols * 3 / 5);	// Heuristics...
		return editWidget.getPreferredSize();
	}

	/**
	 * Adds an action listener
	 */
	public void addActionListener(ActionListener listener)
	{
		editWidget.addActionListener(listener);
	}

	/**
	 * Remove an action listener
	 */
	public void removeActionListener(ActionListener listener)
	{
		editWidget.removeActionListener(listener);
	}

	/**
	 * Adds a key listener
	 */
	public void addKeyListener(KeyListener listener)
	{
		editWidget.addKeyListener(listener);
	}

	/**
	 * Remove a key listener
	 */
	public void removeKeyListener(KeyListener listener)
	{
		editWidget.removeKeyListener(listener);
	}
}
