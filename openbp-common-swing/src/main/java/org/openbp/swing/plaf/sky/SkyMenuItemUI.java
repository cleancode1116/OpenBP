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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.MenuItemUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.openbp.swing.AdvancedAccelerator;

/**
 * UI for a MenuItem.
 *
 * NOTE!
 * This class equals BasicMenuItemUI. It isn't possible to override the
 * BasicMenuItemUI to redefine the accelerator string. - So this class has to
 * clean up!!
 *
 * @author Jens Ferchland
 */
public class SkyMenuItemUI extends MenuItemUI
{
	protected JMenuItem menuItem = null;

	protected Color selectionBackground;

	protected Color selectionForeground;

	protected Color disabledForeground;

	protected Color acceleratorForeground;

	protected Color acceleratorSelectionForeground;

	private String acceleratorDelimiter;

	protected int defaultTextIconGap;

	protected Font acceleratorFont;

	protected MouseInputListener mouseInputListener;

	protected MenuDragMouseListener menuDragMouseListener;

	protected MenuKeyListener menuKeyListener;

	private PropertyChangeListener propertyChangeListener;

	protected Icon arrowIcon = null;

	protected Icon checkIcon = null;

	protected boolean oldBorderPainted;

	/** Used for accelerator binding, lazily created. */
	InputMap windowInputMap;

	private static final boolean DEBUG = false; // show bad params, misc.

	/* Client Property keys for text and accelerator text widths */
	static final String MAX_TEXT_WIDTH = "maxTextWidth";

	static final String MAX_ACC_WIDTH = "maxAccWidth";

	public static ComponentUI createUI(JComponent c)
	{
		return new SkyMenuItemUI();
	}

	public void installUI(JComponent c)
	{
		menuItem = (JMenuItem) c;

		installDefaults();
		installComponents(menuItem);
		installListeners();
		installKeyboardActions();
	}

	protected void installDefaults()
	{
		String prefix = getPropertyPrefix();

		acceleratorFont = UIManager.getFont("MenuItem.acceleratorFont");

		menuItem.setOpaque(true);
		if (menuItem.getMargin() == null || (menuItem.getMargin() instanceof UIResource))
		{
			menuItem.setMargin(UIManager.getInsets(prefix + ".margin"));
		}

		defaultTextIconGap = 4; // Should be from table

		LookAndFeel.installBorder(menuItem, prefix + ".border");
		oldBorderPainted = menuItem.isBorderPainted();
		menuItem.setBorderPainted(((Boolean) (UIManager.get(prefix + ".borderPainted"))).booleanValue());
		LookAndFeel.installColorsAndFont(menuItem, prefix + ".background", prefix + ".foreground", prefix + ".font");

		// MenuItem specific defaults
		if (selectionBackground == null || selectionBackground instanceof UIResource)
		{
			selectionBackground = UIManager.getColor(prefix + ".selectionBackground");
		}
		if (selectionForeground == null || selectionForeground instanceof UIResource)
		{
			selectionForeground = UIManager.getColor(prefix + ".selectionForeground");
		}
		if (disabledForeground == null || disabledForeground instanceof UIResource)
		{
			disabledForeground = UIManager.getColor(prefix + ".disabledForeground");
		}
		if (acceleratorForeground == null || acceleratorForeground instanceof UIResource)
		{
			acceleratorForeground = UIManager.getColor(prefix + ".acceleratorForeground");
		}
		if (acceleratorSelectionForeground == null || acceleratorSelectionForeground instanceof UIResource)
		{
			acceleratorSelectionForeground = UIManager.getColor(prefix + ".acceleratorSelectionForeground");
		}

		// Get accelerator delimiter
		acceleratorDelimiter = UIManager.getString("MenuItem.acceleratorDelimiter");
		if (acceleratorDelimiter == null)
		{
			acceleratorDelimiter = "+";
		}

		// Icons
		if (arrowIcon == null || arrowIcon instanceof UIResource)
		{
			arrowIcon = UIManager.getIcon(prefix + ".arrowIcon");
		}
		if (checkIcon == null || checkIcon instanceof UIResource)
		{
			checkIcon = UIManager.getIcon(prefix + ".checkIcon");
		}
	}

	protected void installComponents(JMenuItem menuItem)
	{
		BasicHTML.updateRenderer(menuItem, menuItem.getText());
	}

	protected String getPropertyPrefix()
	{
		return "MenuItem";
	}

	protected void installListeners()
	{
		mouseInputListener = createMouseInputListener(menuItem);
		menuDragMouseListener = createMenuDragMouseListener(menuItem);
		menuKeyListener = createMenuKeyListener(menuItem);
		propertyChangeListener = createPropertyChangeListener(menuItem);

		menuItem.addMouseListener(mouseInputListener);
		menuItem.addMouseMotionListener(mouseInputListener);
		menuItem.addMenuDragMouseListener(menuDragMouseListener);
		menuItem.addMenuKeyListener(menuKeyListener);
		menuItem.addPropertyChangeListener(propertyChangeListener);
	}

	protected void installKeyboardActions()
	{
		ActionMap actionMap = getActionMap();

		SwingUtilities.replaceUIActionMap(menuItem, actionMap);
		updateAcceleratorBinding();
	}

	public void uninstallUI(JComponent c)
	{
		menuItem = (JMenuItem) c;
		uninstallDefaults();
		uninstallComponents(menuItem);
		uninstallListeners();
		uninstallKeyboardActions();

		//Remove the textWidth and accWidth values from the parent's Client Properties.
		Container parent = menuItem.getParent();
		if ((parent != null && parent instanceof JComponent) && !(menuItem instanceof JMenu && ((JMenu) menuItem).isTopLevelMenu()))
		{
			JComponent p = (JComponent) parent;
			p.putClientProperty(MAX_ACC_WIDTH, null);
			p.putClientProperty(MAX_TEXT_WIDTH, null);
		}

		menuItem = null;
	}

	protected void uninstallDefaults()
	{
		LookAndFeel.uninstallBorder(menuItem);
		menuItem.setBorderPainted(oldBorderPainted);
		if (menuItem.getMargin() instanceof UIResource)
			menuItem.setMargin(null);
		if (arrowIcon instanceof UIResource)
			arrowIcon = null;
		if (checkIcon instanceof UIResource)
			checkIcon = null;
	}

	protected void uninstallComponents(JMenuItem menuItem)
	{
		BasicHTML.updateRenderer(menuItem, "");
	}

	protected void uninstallListeners()
	{
		menuItem.removeMouseListener(mouseInputListener);
		menuItem.removeMouseMotionListener(mouseInputListener);
		menuItem.removeMenuDragMouseListener(menuDragMouseListener);
		menuItem.removeMenuKeyListener(menuKeyListener);
		menuItem.removePropertyChangeListener(propertyChangeListener);

		mouseInputListener = null;
		menuDragMouseListener = null;
		menuKeyListener = null;
		propertyChangeListener = null;
	}

	protected void uninstallKeyboardActions()
	{
		SwingUtilities.replaceUIActionMap(menuItem, null);
		if (windowInputMap != null)
		{
			SwingUtilities.replaceUIInputMap(menuItem, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
			windowInputMap = null;
		}
	}

	protected MouseInputListener createMouseInputListener(JComponent c)
	{
		return new MouseInputHandler();
	}

	protected MenuDragMouseListener createMenuDragMouseListener(JComponent c)
	{
		return new MenuDragMouseHandler();
	}

	protected MenuKeyListener createMenuKeyListener(JComponent c)
	{
		return new MenuKeyHandler();
	}

	private PropertyChangeListener createPropertyChangeListener(JComponent c)
	{
		return new PropertyChangeHandler();
	}

	ActionMap getActionMap()
	{
		String propertyPrefix = getPropertyPrefix();
		String uiKey = propertyPrefix + ".actionMap";
		ActionMap am = (ActionMap) UIManager.get(uiKey);
		if (am == null)
		{
			am = createActionMap();
			UIManager.getLookAndFeelDefaults().put(uiKey, am);
		}
		return am;
	}

	ActionMap createActionMap()
	{
		return new ActionMapUIResource();
	}

	InputMap createInputMap(int condition)
	{
		if (condition == JComponent.WHEN_IN_FOCUSED_WINDOW)
		{
			return new ComponentInputMapUIResource(menuItem);
		}
		return null;
	}

	void updateAcceleratorBinding()
	{
		KeyStroke accelerator = menuItem.getAccelerator();

		if (windowInputMap != null)
		{
			windowInputMap.clear();
		}
		if (accelerator != null)
		{
			if (windowInputMap == null)
			{
				windowInputMap = createInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				SwingUtilities.replaceUIInputMap(menuItem, JComponent.WHEN_IN_FOCUSED_WINDOW, windowInputMap);
			}
			windowInputMap.put(accelerator, "doClick");
		}
	}

	public Dimension getMinimumSize(JComponent c)
	{
		Dimension d = null;
		View v = (View) c.getClientProperty(BasicHTML.propertyKey);
		if (v != null)
		{
			d = getPreferredSize(c);
			d.width -= v.getPreferredSpan(View.X_AXIS) - v.getMinimumSpan(View.X_AXIS);
		}
		return d;
	}

	public Dimension getPreferredSize(JComponent c)
	{
		return getPreferredMenuItemSize(c, checkIcon, arrowIcon, defaultTextIconGap);
	}

	public Dimension getMaximumSize(JComponent c)
	{
		Dimension d = null;
		View v = (View) c.getClientProperty(BasicHTML.propertyKey);
		if (v != null)
		{
			d = getPreferredSize(c);
			d.width += v.getMaximumSpan(View.X_AXIS) - v.getPreferredSpan(View.X_AXIS);
		}
		return d;
	}

	// these rects are used for painting and preferredsize calculations.
	// they used to be regenerated constantly.  Now they are reused.
	static Rectangle zeroRect = new Rectangle(0, 0, 0, 0);

	static Rectangle iconRect = new Rectangle();

	static Rectangle textRect = new Rectangle();

	static Rectangle acceleratorRect = new Rectangle();

	static Rectangle checkIconRect = new Rectangle();

	static Rectangle arrowIconRect = new Rectangle();

	static Rectangle viewRect = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);

	static Rectangle r = new Rectangle();

	private void resetRects()
	{
		iconRect.setBounds(zeroRect);
		textRect.setBounds(zeroRect);
		acceleratorRect.setBounds(zeroRect);
		checkIconRect.setBounds(zeroRect);
		arrowIconRect.setBounds(zeroRect);
		viewRect.setBounds(0, 0, Short.MAX_VALUE, Short.MAX_VALUE);
		r.setBounds(zeroRect);
	}

	protected Dimension getPreferredMenuItemSize(JComponent c, Icon checkIcon, Icon arrowIcon, int defaultTextIconGap)
	{
		JMenuItem b = (JMenuItem) c;
		Icon icon = b.getIcon();
		String text = b.getText();

		String acceleratorText = getAcceleratorString();

		Font font = b.getFont();
		Toolkit kit = Toolkit.getDefaultToolkit();
		FontMetrics fm = kit.getFontMetrics(font);
		FontMetrics fmAccel = kit.getFontMetrics(acceleratorFont);

		resetRects();

		layoutMenuItem(fm, text, fmAccel, acceleratorText, icon, checkIcon, arrowIcon, b.getVerticalAlignment(), b.getHorizontalAlignment(), b.getVerticalTextPosition(), b.getHorizontalTextPosition(), viewRect, iconRect, textRect, acceleratorRect, checkIconRect, arrowIconRect, text == null ? 0 : defaultTextIconGap, defaultTextIconGap);

		// find the union of the icon and text rects
		r.setBounds(textRect);
		r = SwingUtilities.computeUnion(iconRect.x, iconRect.y, iconRect.width, iconRect.height, r);

		//   r = iconRect.union(textRect);

		// To make the accelerator texts appear in a column, find the widest MenuItem text
		// and the widest accelerator text.

		//Get the parent, which stores the information.
		Container parent = menuItem.getParent();

		//Check the parent, and see that it is not a top-level menu.
		if (parent != null && parent instanceof JComponent && !(menuItem instanceof JMenu && ((JMenu) menuItem).isTopLevelMenu()))
		{
			JComponent p = (JComponent) parent;

			//Get widest text so far from parent, if no one exists null is returned.
			Integer maxTextWidth = (Integer) p.getClientProperty(MAX_TEXT_WIDTH);
			Integer maxAccWidth = (Integer) p.getClientProperty(MAX_ACC_WIDTH);

			int maxTextValue = maxTextWidth != null ? maxTextWidth.intValue() : 0;
			int maxAccValue = maxAccWidth != null ? maxAccWidth.intValue() : 0;

			//Compare the text widths, and adjust the r.width to the widest.
			if (r.width < maxTextValue)
			{
				r.width = maxTextValue;
			}
			else
			{
				p.putClientProperty(MAX_TEXT_WIDTH, Integer.valueOf(r.width));
			}

			//Compare the accelarator widths.
			if (acceleratorRect.width > maxAccValue)
			{
				maxAccValue = acceleratorRect.width;
				p.putClientProperty(MAX_ACC_WIDTH, Integer.valueOf(acceleratorRect.width));
			}

			//Add on the widest accelerator
			r.width += maxAccValue;
			r.width += defaultTextIconGap;
		}

		if (useCheckAndArrow())
		{
			// Add in the checkIcon
			r.width += checkIconRect.width;
			r.width += defaultTextIconGap;

			// Add in the arrowIcon
			r.width += defaultTextIconGap;
			r.width += arrowIconRect.width;
		}

		r.width += 2 * defaultTextIconGap;

		Insets insets = b.getInsets();
		if (insets != null)
		{
			r.width += insets.left + insets.right;
			r.height += insets.top + insets.bottom;
		}

		// if the width is even, bump it up one. This is critical
		// for the focus dash line to draw properly
		if (r.width % 2 == 0)
		{
			r.width++;
		}

		// if the height is even, bump it up one. This is critical
		// for the text to center properly
		if (r.height % 2 == 0)
		{
			r.height++;
		}

		/*
		 if (! (b instanceof JMenu && ((JMenu) b).isTopLevelMenu ()))
		 {
		 // Container parent = menuItem.getParent();
		 JComponent p = (JComponent) parent;

		 System.out.println ("MaxText: "+p.getClientProperty(BasicMenuItemUI.MAX_TEXT_WIDTH));
		 System.out.println ("MaxACC"+p.getClientProperty(BasicMenuItemUI.MAX_ACC_WIDTH));

		 System.out.println ("returning pref.width: " + r.width);
		 System.out.println ("Current getSize: " + b.getSize() + "\n");
		 }
		 */
		return r.getSize();
	}

	/**
	 * We draw the background in paintMenuItem()
	 * so override update (which fills the background of opaque
	 * components by default) to just call paint().
	 * @nowarn
	 */
	public void update(Graphics g, JComponent c)
	{
		paint(g, c);
	}

	public void paint(Graphics g, JComponent c)
	{
		paintMenuItem(g, c, checkIcon, arrowIcon, selectionBackground, selectionForeground, defaultTextIconGap);
	}

	protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon, Color background, Color foreground, int defaultTextIconGap)
	{
		JMenuItem b = (JMenuItem) c;
		ButtonModel model = b.getModel();

		//   Dimension size = b.getSize();
		int menuWidth = b.getWidth();
		int menuHeight = b.getHeight();
		Insets i = c.getInsets();

		resetRects();

		viewRect.setBounds(0, 0, menuWidth, menuHeight);

		viewRect.x += i.left;
		viewRect.y += i.top;
		viewRect.width -= (i.right + viewRect.x);
		viewRect.height -= (i.bottom + viewRect.y);

		Font holdf = g.getFont();
		Font f = c.getFont();
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics(f);
		FontMetrics fmAccel = g.getFontMetrics(acceleratorFont);

		// get Accelerator text
		String acceleratorText = getAcceleratorString();

		// layout the text and icon
		String text = layoutMenuItem(fm, b.getText(), fmAccel, acceleratorText, b.getIcon(), checkIcon, arrowIcon, b.getVerticalAlignment(), b.getHorizontalAlignment(), b.getVerticalTextPosition(), b.getHorizontalTextPosition(), viewRect, iconRect, textRect, acceleratorRect, checkIconRect, arrowIconRect, b.getText() == null ? 0 : defaultTextIconGap, defaultTextIconGap);

		// Paint background
		paintBackground(g, b, background);

		Color holdc = g.getColor();

		// Paint the Check
		if (checkIcon != null)
		{
			if (model.isArmed() || model.isSelected())
			{
				g.setColor(foreground);
			}
			else
			{
				g.setColor(holdc);
			}
			if (useCheckAndArrow() && b.isSelected())
				checkIcon.paintIcon(c, g, checkIconRect.x, checkIconRect.y);
			g.setColor(holdc);
		}

		// Paint the Icon
		if (b.getIcon() != null)
		{
			Icon icon;
			if (!model.isEnabled())
			{
				icon = b.getDisabledIcon();
			}
			else if (model.isPressed() && model.isArmed())
			{
				icon = b.getPressedIcon();
				if (icon == null)
				{
					// Use default icon
					icon = b.getIcon();
				}
			}
			else
			{
				icon = b.getIcon();
			}

			if (icon != null)
				icon.paintIcon(c, g, iconRect.x, iconRect.y);
		}

		// Draw the Text
		if (text != null)
		{
			View v = (View) c.getClientProperty(BasicHTML.propertyKey);
			if (v != null)
			{
				v.paint(g, textRect);
			}
			else
			{
				paintText(g, b, textRect, text);
			}
		}

		// Draw the Accelerator Text
		if (acceleratorText != null && !acceleratorText.equals(""))
		{
			//Get the maxAccWidth from the parent to calculate the offset.
			int accOffset = 0;
			Container parent = menuItem.getParent();
			if (parent != null && parent instanceof JComponent)
			{
				JComponent p = (JComponent) parent;
				Integer maxValueInt = (Integer) p.getClientProperty(MAX_ACC_WIDTH);
				int maxValue = maxValueInt != null ? maxValueInt.intValue() : acceleratorRect.width;

				//Calculate the offset, with which the accelerator texts will be drawn with.
				accOffset = maxValue - acceleratorRect.width;
			}

			g.setFont(acceleratorFont);
			if (!model.isEnabled())
			{
				// *** paint the acceleratorText disabled
				if (disabledForeground != null)
				{
					g.setColor(disabledForeground);
					BasicGraphicsUtils.drawString(g, acceleratorText, 0, acceleratorRect.x - accOffset, acceleratorRect.y + fmAccel.getAscent());
				}
				else
				{
					g.setColor(b.getBackground().brighter());
					BasicGraphicsUtils.drawString(g, acceleratorText, 0, acceleratorRect.x - accOffset, acceleratorRect.y + fmAccel.getAscent());
					g.setColor(b.getBackground().darker());
					BasicGraphicsUtils.drawString(g, acceleratorText, 0, acceleratorRect.x - accOffset - 1, acceleratorRect.y + fmAccel.getAscent() - 1);
				}
			}
			else
			{
				// *** paint the acceleratorText normally
				if (model.isArmed() || (c instanceof JMenu && model.isSelected()))
				{
					g.setColor(acceleratorSelectionForeground);
				}
				else
				{
					g.setColor(acceleratorForeground);
				}
				BasicGraphicsUtils.drawString(g, acceleratorText, 0, acceleratorRect.x - accOffset, acceleratorRect.y + fmAccel.getAscent());
			}
		}

		// Paint the Arrow
		if (arrowIcon != null)
		{
			if (model.isArmed() || (c instanceof JMenu && model.isSelected()))
				g.setColor(foreground);
			if (useCheckAndArrow())
				arrowIcon.paintIcon(c, g, arrowIconRect.x, arrowIconRect.y);
		}
		g.setColor(holdc);
		g.setFont(holdf);
	}

	/**
	 * Draws the background of the menu item.
	 *
	 * @param g the paint graphics
	 * @param menuItem menu item to be painted
	 * @param bgColor selection background color
	 * @since 1.4
	 */
	protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor)
	{
		ButtonModel model = menuItem.getModel();
		Color oldColor = g.getColor();
		int menuWidth = menuItem.getWidth();
		int menuHeight = menuItem.getHeight();

		if (menuItem.isOpaque())
		{
			if (model.isArmed() || (menuItem instanceof JMenu && model.isSelected()))
			{
				g.setColor(bgColor);
				g.fillRect(0, 0, menuWidth, menuHeight);
			}
			else
			{
				g.setColor(menuItem.getBackground());
				g.fillRect(0, 0, menuWidth, menuHeight);
			}
			g.setColor(oldColor);
		}
	}

	/**
	 * Renders the text of the current menu item.
	 * <p>
	 * @param g graphics context
	 * @param menuItem menu item to render
	 * @param textRect bounding rectangle for rendering the text
	 * @param text string to render
	 * @since 1.4
	 */
	protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text)
	{
		ButtonModel model = menuItem.getModel();
		FontMetrics fm = g.getFontMetrics();
		int mnemIndex = menuItem.getDisplayedMnemonicIndex();

		if (!model.isEnabled())
		{
			// *** paint the text disabled
			if (UIManager.get("MenuItem.disabledForeground") instanceof Color)
			{
				g.setColor(UIManager.getColor("MenuItem.disabledForeground"));
				BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemIndex, textRect.x, textRect.y + fm.getAscent());
			}
			else
			{
				g.setColor(menuItem.getBackground().brighter());
				BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemIndex, textRect.x, textRect.y + fm.getAscent());
				g.setColor(menuItem.getBackground().darker());
				BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemIndex, textRect.x - 1, textRect.y + fm.getAscent() - 1);
			}
		}
		else
		{
			// *** paint the text normally
			if (model.isArmed() || (menuItem instanceof JMenu && model.isSelected()))
			{
				g.setColor(selectionForeground); // Uses protected field.
			}
			BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemIndex, textRect.x, textRect.y + fm.getAscent());
		}
	}

	/**
	 * Compute and return the location of the icons origin, the
	 * location of origin of the text baseline, and a possibly clipped
	 * version of the compound labels string.  Locations are computed
	 * relative to the viewRect rectangle.
	 */
	private String layoutMenuItem(FontMetrics fm, String text, FontMetrics fmAccel, String acceleratorText, Icon icon, Icon checkIcon, Icon arrowIcon, int verticalAlignment, int horizontalAlignment, int verticalTextPosition, int horizontalTextPosition, Rectangle viewRect, Rectangle iconRect, Rectangle textRect, Rectangle acceleratorRect, Rectangle checkIconRect, Rectangle arrowIconRect, int textIconGap, int menuItemGap)
	{
		SwingUtilities.layoutCompoundLabel(menuItem, fm, text, icon, verticalAlignment, horizontalAlignment, verticalTextPosition, horizontalTextPosition, viewRect, iconRect, textRect, textIconGap);

		/* Initialize the acceelratorText bounds rectangle textRect.  If a null
		 * or and empty String was specified we substitute "" here
		 * and use 0,0,0,0 for acceleratorTextRect.
		 */
		if ((acceleratorText == null) || acceleratorText.equals(""))
		{
			acceleratorRect.width = acceleratorRect.height = 0;
			acceleratorText = "";
		}
		else
		{
			acceleratorRect.width = SwingUtilities.computeStringWidth(fmAccel, acceleratorText);
			acceleratorRect.height = fmAccel.getHeight();
		}

		/* Initialize the checkIcon bounds rectangle's width & height.
		 */
		if (useCheckAndArrow())
		{
			if (checkIcon != null)
			{
				checkIconRect.width = checkIcon.getIconWidth();
				checkIconRect.height = checkIcon.getIconHeight();
			}
			else
			{
				checkIconRect.width = checkIconRect.height = 0;
			}

			/* Initialize the arrowIcon bounds rectangle width & height.
			 */
			if (arrowIcon != null)
			{
				arrowIconRect.width = arrowIcon.getIconWidth();
				arrowIconRect.height = arrowIcon.getIconHeight();
			}
			else
			{
				arrowIconRect.width = arrowIconRect.height = 0;
			}
		}

		Rectangle labelRect = iconRect.union(textRect);
		if (menuItem.getComponentOrientation().isLeftToRight())
		{
			textRect.x += menuItemGap;
			iconRect.x += menuItemGap;

			// Position the Accelerator text rect
			acceleratorRect.x = viewRect.x + viewRect.width - arrowIconRect.width - menuItemGap - acceleratorRect.width;

			// Position the Check and Arrow Icons
			if (useCheckAndArrow())
			{
				checkIconRect.x = viewRect.x + menuItemGap;
				textRect.x += menuItemGap + checkIconRect.width;
				iconRect.x += menuItemGap + checkIconRect.width;
				arrowIconRect.x = viewRect.x + viewRect.width - menuItemGap - arrowIconRect.width;
			}
		}
		else
		{
			textRect.x -= menuItemGap;
			iconRect.x -= menuItemGap;

			// Position the Accelerator text rect
			acceleratorRect.x = viewRect.x + arrowIconRect.width + menuItemGap;

			// Position the Check and Arrow Icons
			if (useCheckAndArrow())
			{
				checkIconRect.x = viewRect.x + viewRect.width - menuItemGap - checkIconRect.width;
				textRect.x -= menuItemGap + checkIconRect.width;
				iconRect.x -= menuItemGap + checkIconRect.width;
				arrowIconRect.x = viewRect.x + menuItemGap;
			}
		}

		// Align the accelertor text and the check and arrow icons vertically
		// with the center of the label rect.
		acceleratorRect.y = labelRect.y + (labelRect.height / 2) - (acceleratorRect.height / 2);
		if (useCheckAndArrow())
		{
			arrowIconRect.y = labelRect.y + (labelRect.height / 2) - (arrowIconRect.height / 2);
			checkIconRect.y = labelRect.y + (labelRect.height / 2) - (checkIconRect.height / 2);
		}

		/*
		 System.out.println ("Layout: text="+menuItem.getText()+"\n\tv="
		 + viewRect +"\n\tc="+checkIconRect+"\n\ti="
		 + iconRect +"\n\tt="+textRect+"\n\tacc="
		 + acceleratorRect +"\n\ta="+arrowIconRect+"\n");
		 */
		return text;
	}

	/*
	 * Returns false if the component is a JMenu and it is a top
	 * level menu (on the menubar).
	 */
	private boolean useCheckAndArrow()
	{
		boolean b = true;
		if ((menuItem instanceof JMenu) && (((JMenu) menuItem).isTopLevelMenu()))
		{
			b = false;
		}
		return b;
	}

	public MenuElement [] getPath()
	{
		MenuSelectionManager m = MenuSelectionManager.defaultManager();
		MenuElement oldPath[] = m.getSelectedPath();
		MenuElement newPath[];
		int i = oldPath.length;
		if (i == 0)
			return new MenuElement [0];
		Component parent = menuItem.getParent();
		if (oldPath [i - 1].getComponent() == parent)
		{
			// The parent popup menu is the last so far
			newPath = new MenuElement [i + 1];
			System.arraycopy(oldPath, 0, newPath, 0, i);
			newPath [i] = menuItem;
		}
		else
		{
			// A sibling menuitem is the current selection
			//
			//  This probably needs to handle 'exit submenu into
			// a menu item.  Search backwards along the current
			// selection until you find the parent popup menu,
			// then copy up to that and add yourself...
			int j;
			for (j = oldPath.length - 1; j >= 0; j--)
			{
				if (oldPath [j].getComponent() == parent)
					break;
			}
			newPath = new MenuElement [j + 2];
			System.arraycopy(oldPath, 0, newPath, 0, j + 1);
			newPath [j + 1] = menuItem;

			/*
			 System.out.println ("Sibling condition -- ");
			 System.out.println ("Old array : ");
			 printMenuElementArray (oldPath, false);
			 System.out.println ("New array : ");
			 printMenuElementArray (newPath, false);
			 */
		}
		return newPath;
	}

	void printMenuElementArray(MenuElement path[], boolean dumpStack)
	{
		System.out.println("Path is(");
		int i, j;
		for (i = 0, j = path.length; i < j; i++)
		{
			for (int k = 0; k <= i; k++)
				System.out.print("  ");
			MenuElement me = path [i];
			if (me instanceof JMenuItem)
				System.out.println(((JMenuItem) me).getText() + ", ");
			else if (me == null)
				System.out.println("NULL , ");
			else
				System.out.println("" + me + ", ");
		}
		System.out.println(")");

		if (dumpStack == true)
			Thread.dumpStack();
	}

	protected class MouseInputHandler
		implements MouseInputListener
	{
		public void mouseClicked(MouseEvent e)
		{
		}

		public void mousePressed(MouseEvent e)
		{
		}

		public void mouseReleased(MouseEvent e)
		{
			MenuSelectionManager manager = MenuSelectionManager.defaultManager();
			Point p = e.getPoint();
			if (p.x >= 0 && p.x < menuItem.getWidth() && p.y >= 0 && p.y < menuItem.getHeight())
			{
				doClick(manager);
			}
			else
			{
				manager.processMouseEvent(e);
			}
		}

		public void mouseEntered(MouseEvent e)
		{
			MenuSelectionManager manager = MenuSelectionManager.defaultManager();
			int modifiers = e.getModifiers();

			// 4188027: drag enter/exit added in JDK 1.1.7A, JDK1.2
			if ((modifiers & (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0)
			{
				MenuSelectionManager.defaultManager().processMouseEvent(e);
			}
			else
			{
				manager.setSelectedPath(getPath());
			}
		}

		public void mouseExited(MouseEvent e)
		{
			MenuSelectionManager manager = MenuSelectionManager.defaultManager();

			int modifiers = e.getModifiers();

			// 4188027: drag enter/exit added in JDK 1.1.7A, JDK1.2
			if ((modifiers & (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0)
			{
				MenuSelectionManager.defaultManager().processMouseEvent(e);
			}
			else
			{
				MenuElement path[] = manager.getSelectedPath();
				if (path.length > 1)
				{
					MenuElement newPath[] = new MenuElement [path.length - 1];
					int i, c;
					for (i = 0, c = path.length - 1; i < c; i++)
						newPath [i] = path [i];
					manager.setSelectedPath(newPath);
				}
			}
		}

		public void mouseDragged(MouseEvent e)
		{
			MenuSelectionManager.defaultManager().processMouseEvent(e);
		}

		public void mouseMoved(MouseEvent e)
		{
		}
	}

	private class MenuDragMouseHandler
		implements MenuDragMouseListener
	{
		public void menuDragMouseEntered(MenuDragMouseEvent e)
		{
		}

		public void menuDragMouseDragged(MenuDragMouseEvent e)
		{
			MenuSelectionManager manager = e.getMenuSelectionManager();
			MenuElement path[] = e.getPath();
			manager.setSelectedPath(path);
		}

		public void menuDragMouseExited(MenuDragMouseEvent e)
		{
		}

		public void menuDragMouseReleased(MenuDragMouseEvent e)
		{
			MenuSelectionManager manager = e.getMenuSelectionManager();
			Point p = e.getPoint();
			if (p.x >= 0 && p.x < menuItem.getWidth() && p.y >= 0 && p.y < menuItem.getHeight())
			{
				doClick(manager);
			}
			else
			{
				manager.clearSelectedPath();
			}
		}
	}

	private class MenuKeyHandler
		implements MenuKeyListener
	{
		public void menuKeyTyped(MenuKeyEvent e)
		{
			if (DEBUG)
			{
				System.out.println("in BasicMenuItemUI.menuKeyTyped for " + menuItem.getText());
			}
			int key = menuItem.getMnemonic();
			if (key == 0)
				return;
			if (lower(key) == lower(e.getKeyChar()))
			{
				MenuSelectionManager manager = e.getMenuSelectionManager();
				doClick(manager);
				e.consume();
			}
		}

		public void menuKeyPressed(MenuKeyEvent e)
		{
			if (DEBUG)
			{
				System.out.println("in BasicMenuItemUI.menuKeyPressed for " + menuItem.getText());
			}
		}

		public void menuKeyReleased(MenuKeyEvent e)
		{
		}

		private int lower(int ascii)
		{
			if (ascii >= 'A' && ascii <= 'Z')
				return ascii + 'a' - 'A';
			else
				return ascii;
		}
	}

	private class PropertyChangeHandler
		implements PropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent e)
		{
			String name = e.getPropertyName();

			if (name.equals("labelFor") || name.equals("displayedMnemonic") || name.equals("accelerator"))
			{
				updateAcceleratorBinding();
			}
			else if (name.equals("text") || "font".equals(name) || "foreground".equals(name))
			{
				// remove the old html view client property if one
				// existed, and install a new one if the text installed
				// into the JLabel is html source.
				JMenuItem lbl = ((JMenuItem) e.getSource());
				String text = lbl.getText();
				BasicHTML.updateRenderer(lbl, text);
			}
		}
	}

	/**
	 * Call this method when a menu item is to be activated.
	 * This method handles some of the details of menu item activation
	 * such as clearing the selected path and messaging the
	 * JMenuItem's doClick() method.
	 *
	 * @param msm  A MenuSelectionManager. The visual feedback and
	 *             internal bookkeeping tasks are delegated to
	 *             this MenuSelectionManager. If <code>null</code> is
	 *             passed as this argument, the
	 *             <code>MenuSelectionManager.defaultManager</code> is
	 *             used.
	 * @since 1.4
	 */
	protected void doClick(MenuSelectionManager msm)
	{
		// Visual feedback
		if (msm == null)
		{
			msm = MenuSelectionManager.defaultManager();
		}
		msm.clearSelectedPath();
		menuItem.doClick(0);
	}

	/**
	 * @see javax.swing.plaf.metal.MetalToolTipUI#getAcceleratorString()
	 */
	public String getAcceleratorString()
	{
		if (menuItem instanceof AdvancedAccelerator)
		{
			// the menu has an advanced accelerator - take its string!
			return ((AdvancedAccelerator) menuItem).getAcceleratorString();
		}

		// get the accelerator string from the KeyStroke.
		KeyStroke accelerator = menuItem.getAccelerator();
		String acceleratorText = "";
		if (accelerator != null)
		{
			int modifiers = accelerator.getModifiers();
			if (modifiers > 0)
			{
				acceleratorText = KeyEvent.getKeyModifiersText(modifiers);

				//acceleratorText += "-";
				acceleratorText += acceleratorDelimiter;
			}

			int keyCode = accelerator.getKeyCode();
			if (keyCode != 0)
			{
				acceleratorText += KeyEvent.getKeyText(keyCode);
			}
			else
			{
				acceleratorText += accelerator.getKeyChar();
			}
		}
		return acceleratorText;
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	/**
	 * Inner class which paints a simple check arrow.
	 */
	public static class CheckIcon
		implements Icon
	{
		/**
		 * Returns the height of the icon.
		 *
		 * @return int - The fixed height of 8
		 */
		public int getIconHeight()
		{
			return 8;
		}

		/**
		 * Returns the width of the icon.
		 *
		 * @return int - The fixed width of 8
		 */
		public int getIconWidth()
		{
			return 8;
		}

		/**
		 * Paint the icon.
		 *
		 * @param c - The Component where the icon is painted
		 * @param g - The graphics object which is used to paint the icon
		 * @param x - The x position for the left top corner
		 * @param y - The y position for the left top corner
		 */
		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			if (g instanceof Graphics2D)
			{
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(1.5f));
			}

			g.setColor(Color.DARK_GRAY);
			g.drawLine(x, y + 5, x + 3, y + 8);
			g.drawLine(x + 3, y + 8, x + 8, y);
		}
	}
}
