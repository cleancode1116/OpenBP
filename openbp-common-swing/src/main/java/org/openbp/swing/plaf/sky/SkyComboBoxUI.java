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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import javax.swing.CellRendererPane;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.metal.MetalComboBoxUI;

/**
 * The ComboBoxUI - Button and Editor with a SimpleRoundBorder.
 *
 * @author Jens Ferchland
 */
public class SkyComboBoxUI extends MetalComboBoxUI
	implements SkyTheme
{
	/**
	 * Creates a new <code>SkyComboBoxUI</code> instance.
	 */
	public SkyComboBoxUI()
	{
		super();
	}

	/**
	 * Returns the UI - ComboBoxUI is no singelton!
	 *
	 * @param c a <code>JComponent</code> value
	 * @return a <code>ComponentUI</code> value
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return new SkyComboBoxUI();
	}

	/**
	 * Creates the Editor with the correct Border.
	 *
	 * @return a <code>ComboBoxEditor</code> value
	 */
	protected ComboBoxEditor createEditor()
	{
		ComboBoxEditor editor = super.createEditor();
		Component c = editor.getEditorComponent();
		if (c instanceof JComponent)
			((JComponent) c).setBorder(new EditorBorder());
		return editor;
	}

	/**
	 * Creates the Button with the Arrow and the choosen Object if
	 * it isn't editable.
	 *
	 * @return a <code>JButton</code> value
	 */
	protected JButton createArrowButton()
	{
		JButton button = new ComboBoxButton(comboBox, new ComboBoxIcon(), comboBox.isEditable(), currentValuePane);

		button.setBorder(new SimpleBorder(1, 1, 1, 4));

		return button;
	}

	/**
	 * Creates a new PropertyChangeListener for a Combobox.
	 *
	 * @see javax.swing.plaf.metal.MetalComboBoxUI#createPropertyChangeListener()
	 */
	public PropertyChangeListener createPropertyChangeListener()
	{
		return new SkyPropertyChangeListener();
	}

	//////////////////////////////////////////////////
	// @@ inner class
	//////////////////////////////////////////////////

	/**
	 * This border paints simple lines on top, bottom and left.
	 */
	class EditorBorder extends AbstractBorder
	{
		/** insets of this border */
		private Insets insets;

		//////////////////////////////////////////////////
		// @@ Construction
		//////////////////////////////////////////////////

		/**
		 * Creates a new <code>EditorBorder</code> instance.
		 */
		public EditorBorder()
		{
			this.insets = new Insets(1, 1, 1, 0);
		}

		//////////////////////////////////////////////////
		// @@ Painting
		//////////////////////////////////////////////////

		/**
		 * Implements AbstractBorder. Paints the Border
		 * around the Component.
		 *
		 * @param c a <code>Component</code> value
		 * @param g a <code>Graphics</code> value
		 * @param x an <code>int</code> value
		 * @param y an <code>int</code> value
		 * @param width an <code>int</code> value
		 * @param height an <code>int</code> value
		 */
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
		{
			g.setColor(SkyTheme.COLOR_BORDER);
			g.drawLine(x, y, x + width - 1, y);
			g.drawLine(x, y, x, y + height - 1);
			g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
		}

		/**
		 * Implements AbstractBorder. Returns the Insets of the
		 * Border
		 *
		 * @param c a <code>Component</code> value
		 * @return an <code>Insets</code> value
		 */
		public Insets getBorderInsets(Component c)
		{
			return insets;
		}
	}

	/**
	 * This utility class draws the horizontal bars which indicate a ComboBox.
	 * The arrow is smaller than the MetalCombobox arrow.
	 */
	public class ComboBoxIcon
		implements Icon, Serializable
	{
		//////////////////////////////////////////////////
		// @@ Implementation of Icon
		//////////////////////////////////////////////////

		/**
		 * Paints horizontal bars.
		 * @nowarn
		 */
		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			JComponent component = (JComponent) c;
			int iconWidth = getIconWidth();

			g.translate(x, y);

			g.setColor(component.isEnabled() ? SkyTheme.COLOR_BACKGROUND_DARK_DARK : SkyTheme.COLOR_BACKGROUND_DARK);
			g.drawLine(0, 0, iconWidth - 1, 0);
			g.drawLine(1, 1, 1 + (iconWidth - 3), 1);
			g.drawLine(2, 2, 2 + (iconWidth - 5), 2);
			g.drawLine(3, 3, 3 + (iconWidth - 7), 3);

			g.translate(-x, -y);
		}

		/**
		 * Created a stub to satisfy the interface.
		 * @nowarn
		 */
		public int getIconWidth()
		{
			return 8;
		}

		/**
		 * Created a stub to satisfy the interface.
		 * @nowarn
		 */
		public int getIconHeight()
		{
			return 4;
		}
	}

	/**
	 * This inner class is marked &quot;public&quot; due to a compiler bug.
	 * This class should be treated as a &quot;protected&quot; inner class.
	 * Instantiate it only within subclasses of <FooUI>.
	 */
	public class SkyPropertyChangeListener extends BasicComboBoxUI.PropertyChangeHandler
	{
		public void propertyChange(PropertyChangeEvent e)
		{
			super.propertyChange(e);
		}
	}

	/**
	 * Button with an arrow.
	 */
	public class ComboBoxButton extends JButton
	{
		//////////////////////////////////////////////////
		// @@ Members
		//////////////////////////////////////////////////

		protected JComboBox comboBox;

		protected CellRendererPane rendererPane;

		protected Icon comboIcon;

		protected boolean iconOnly = false;

		//////////////////////////////////////////////////
		// @@ Construction
		//////////////////////////////////////////////////

		/**
		 * Default Constructor - only friendly access!
		 */
		ComboBoxButton()
		{
			super();
			DefaultButtonModel model = new DefaultButtonModel()
			{
				public void setArmed(boolean armed)
				{
					super.setArmed(isPressed() ? true : armed);
				}
			};

			setModel(model);

			// Set the background and foreground to the combobox colors.
			setBackground(UIManager.getColor("ComboBox.background"));
			setForeground(UIManager.getColor("ComboBox.foreground"));

			setMargin(new Insets(0, 0, 0, 0));
		}

		/**
		 * Creates a new ComboBoxButton.
		 *
		 * @param cb combobox of this button
		 * @param i icon for this button
		 * @param pane cell renderer for this button
		 */
		public ComboBoxButton(JComboBox cb, Icon i, CellRendererPane pane)
		{
			this();
			comboBox = cb;
			comboIcon = i;
			rendererPane = pane;
			setEnabled(comboBox.isEnabled());
		}

		/**
		 * Creates a new ComboBoxButton
		 *
		 * @param cb combobox of this button
		 * @param i icon of this button
		 * @param onlyIcon true: button is has an icon only: false button has text and icon
		 * @param pane cell renderer for the text
		 */
		public ComboBoxButton(JComboBox cb, Icon i, boolean onlyIcon, CellRendererPane pane)
		{
			this(cb, i, pane);
			iconOnly = onlyIcon;
		}

		//////////////////////////////////////////////////
		// @@ Memberaccess
		//////////////////////////////////////////////////

		/**
		 * Returns the combobox of this button.
		 * @nowarn
		 */
		public final JComboBox getComboBox()
		{
			return comboBox;
		}

		/**
		 * Sets the combobox of this button.
		 *
		 * @param cb the new combobox
		 */
		public final void setComboBox(JComboBox cb)
		{
			comboBox = cb;
		}

		/**
		 * Returns the icon of the button.
		 *
		 * @nowarn
		 */
		public final Icon getComboIcon()
		{
			return comboIcon;
		}

		/**
		 * Sets the icon of this button.
		 * @nowarn
		 */
		public final void setComboIcon(Icon i)
		{
			comboIcon = i;
		}

		/**
		 * Returns true if the button has only a icon.
		 * @return boolean True - the button has only a icon, false the button has text and icon
		 */
		public final boolean isIconOnly()
		{
			return iconOnly;
		}

		/**
		 * Set the button mode.
		 * @nowarn
		 */
		public final void setIconOnly(boolean isIconOnly)
		{
			iconOnly = isIconOnly;
		}

		//////////////////////////////////////////////////
		// @@ Painting
		//////////////////////////////////////////////////

		/**
		 * Paints the component.
		 * @nowarn
		 */
		public void paintComponent(Graphics g)
		{
			boolean leftToRight = comboBox.getComponentOrientation().isLeftToRight();

			Insets insets = getInsets();

			int width = getWidth() - (insets.left + insets.right);
			int height = getHeight() - (insets.top + insets.bottom);

			if (height <= 0 || width <= 0)
			{
				return;
			}

			int left = insets.left;
			int top = insets.top;
			int right = left + (width - 1);
			int bottom = top + (height - 1);

			int iconWidth = 0;
			int iconLeft = (leftToRight) ? right : left;

			// Paint the icon
			if (comboIcon != null)
			{
				iconWidth = comboIcon.getIconWidth();
				int iconHeight = comboIcon.getIconHeight();
				int iconTop = 0;

				if (iconOnly)
				{
					iconLeft = (getWidth() / 2) - (iconWidth / 2);
					iconTop = (getHeight() / 2) - (iconHeight / 2);
				}
				else
				{
					if (leftToRight)
					{
						iconLeft = (left + (width - 1)) - iconWidth;
					}
					else
					{
						iconLeft = left;
					}
					iconTop = (top + ((bottom - top) / 2)) - (iconHeight / 2);
				}

				g.setColor(comboBox.getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());

				comboIcon.paintIcon(this, g, iconLeft, iconTop);
			}

			// Let the renderer paint
			if (!iconOnly && comboBox != null)
			{
				ListCellRenderer renderer = comboBox.getRenderer();
				Component c;
				boolean renderPressed = getModel().isPressed();
				c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, renderPressed, false);
				c.setFont(rendererPane.getFont());

				c.setForeground(comboBox.getForeground());
				c.setBackground(comboBox.getBackground());

				int cWidth = width - (insets.right + iconWidth);

				boolean shouldValidate = c instanceof JPanel ? true : false;

				if (leftToRight)
				{
					rendererPane.paintComponent(g, c, this, left, top, cWidth, height, shouldValidate);
				}
				else
				{
					rendererPane.paintComponent(g, c, this, left + iconWidth, top, cWidth, height, shouldValidate);
				}
			}
		}
	}
}
