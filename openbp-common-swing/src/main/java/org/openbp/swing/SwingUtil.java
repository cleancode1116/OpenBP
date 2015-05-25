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
package org.openbp.swing;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

/**
 * This class contains various static utility methods.
 */
public class SwingUtil
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Name of the resource component for general client resources */
	public static final String RESOURCE_COMMON = "common";

	/**
	 * Private constructor prevents instantiation.
	 */
	private SwingUtil()
	{
	}

	//////////////////////////////////////////////////
	// @@ Application control
	//////////////////////////////////////////////////

	/**
	 * Displays the main frame of an application in the center of the screen.
	 *
	 * A main method of a primitive test program would consist of just one line:
	 * @code 3
	 * SwingUtil.displayMainFrame (new JFrameSubClass ());
	 * @code
	 * The method will pack the frame before it is being displayed.
	 *
	 * @param mainWindow Main application window (usually a JFrame or JDialog)
	 * @param provideExitHandler
	 *		true	Automatically provides a WindowClosing handler that exits the application.<br>
	 *		false	Does not provide an exit handler.
	 */
	public static void startApplication(Window mainWindow, boolean provideExitHandler)
	{
		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension mainSize = mainWindow.getSize();
		if (mainSize.height > screenSize.height)
			mainSize.height = screenSize.height;
		if (mainSize.width > screenSize.width)
			mainSize.width = screenSize.width;
		mainWindow.setLocation((screenSize.width - mainSize.width) / 2, (screenSize.height - mainSize.height) / 2);

		if (provideExitHandler)
		{
			mainWindow.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					System.exit(0);
				}
			});
		}

		// Display the frame
		mainWindow.setVisible(true);
	}

	/**
	 * Processes any events pending in the event queue.
	 * The method will yield the current thread if there are any events left
	 * to process in the system event queue. This will give the event dispatcher thread
	 * the opportunity to process the pending events.<br>
	 * However, it is not guaranteed that the event queue will be empty when the
	 * method returns.
	 */
	public static void processPendingEvents()
	{
		if (Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent() != null)
			Thread.yield();
	}

	//////////////////////////////////////////////////
	// @@ Focus control
	//////////////////////////////////////////////////

	/**
	 * Tries to focus the given component.
	 * In difference to Component.requestFocus (), the method tries to find a descendant component that is focusable.
	 * Note that a true result does merely indicate that a focusable component has been found,
	 * not the actual passing of the focus.
	 *
	 * @param comp The component to receive the focus
	 * @return
	 *		true	if an egligable component has been found<br>
	 *		false	Otherwise
	 */
	public static boolean focusComponent(Component comp)
	{
		if (comp == null)
		{
			return false;
		}

		if (comp.isFocusable() && !(comp instanceof JScrollPane))
		{
			comp.requestFocus();
			return true;
		}

		if (comp instanceof Container)
		{
			Component [] comps = ((Container) comp).getComponents();

			for (int i = 0; i < comps.length; i++)
			{
				if (focusComponent(comps [i]))
				{
					return true;
				}
			}
		}

		return false;
	}

	//////////////////////////////////////////////////
	// @@ Glass pane
	//////////////////////////////////////////////////

	/**
	 * Converts the given, component local coordinates into coordinates relative
	 * to an objects glassPane. Throws an IllegalArgumentException if comp has no
	 * RootPaneContainer-ancestor or is null.
	 *
	 * @param p Coordinates to convert or null for (0/0)
	 * @param comp Component the coordinates are relative to
	 * @return The converted coordinates
	 */
	public static Point convertToGlassCoords(Point p, Component comp)
	{
		if (p == null)
			p = new Point();
		if (comp instanceof Scalable)
		{
			p = applyScale(p, false, (Scalable) comp);
		}
		return SwingUtilities.convertPoint(comp, p, getGlassPane(comp));
	}

	/**
	 * Converts the given, component local coordinates into coordinates relative
	 * to an objects glassPane. Throws an IllegalArgumentException if comp has no
	 * RootPaneContainer-ancestor or is null.
	 *
	 * @param p Coordinates to convert or null for (0/0)
	 * @param comp Component the coordinates should be relative to
	 * @return The converted coordinates
	 */
	public static Point convertFromGlassCoords(Point p, Component comp)
	{
		if (p == null)
			p = new Point();
		p = SwingUtilities.convertPoint(getGlassPane(comp), p, comp);
		if (comp instanceof Scalable)
		{
			p = applyScale(p, true, (Scalable) comp);
		}
		return p;
	}

	/**
	 * Converts the component local rectangle into coordinates relative
	 * to an objects glassPane. Throws an IllegalArgumentException if comp has no
	 * RootPaneContainer-ancestor or is null.
	 *
	 * @param r Coordinates to convert
	 * @param comp Component the coordinates are relative to
	 * @return The converted coordinates
	 */
	public static Rectangle convertRectToGlassCoords(Rectangle r, Component comp)
	{
		return SwingUtilities.convertRectangle(comp, r, getGlassPane(comp));
	}

	/**
	 * Converts the given, component local rectangle into coordinates relative
	 * to an objects glassPane. Throws an IllegalArgumentException if comp has no
	 * RootPaneContainer-ancestor or is null.
	 *
	 * @param comp Component
	 * @return The converted coordinates
	 */
	public static Rectangle convertBoundsToGlassCoords(Component comp)
	{
		return SwingUtilities.convertRectangle(comp, SwingUtilities.getLocalBounds(comp), getGlassPane(comp));
	}

	/**
	 * Returns the GlassPane that belongs to a given Component. Throws
	 * an IllegalArgumentException if comp has no RootPaneContainer-ancestor
	 * or is null.
	 *
	 * @param comp Component
	 * @return The glass pane
	 */
	public static Component getGlassPane(Component comp)
	{
		JRootPane root = SwingUtilities.getRootPane(comp);

		if (root == null)
		{
			throw new IllegalArgumentException("Component must be descendant of RootPaneContainer");
		}

		return root.getGlassPane();
	}

	//////////////////////////////////////////////////
	// @@ Scalable support
	//////////////////////////////////////////////////

	/**
	 * Applies the current scale factor of the view to a rectangle.
	 *
	 * @param r rectangle to be scaled
	 * @param scaleToDoc
	 *		true	Assumes that the coordinates are component coordinates that should be translated to document coordinates.
	 *		false	Assumes that the coordinates are document coordinates that should be translated to component coordinates.
	 * @param view Scalable view that provides the scaling factor
	 * @return The scaled rectangle
	 */
	public static Rectangle applyScale(Rectangle r, boolean scaleToDoc, Scalable view)
	{
		Rectangle result = new Rectangle();
		result.x = applyScale(r.x, scaleToDoc, view);
		result.y = applyScale(r.y, scaleToDoc, view);
		result.width = applyScale(r.width, scaleToDoc, view);
		result.height = applyScale(r.height, scaleToDoc, view);
		return result;
	}

	/**
	 * Applies the current scale factor of the view to a point.
	 *
	 * @param p Point to be scaled
	 * @param scaleToDoc
	 *		true	Assumes that the coordinates are component coordinates that should be translated to document coordinates.
	 *		false	Assumes that the coordinates are document coordinates that should be translated to component coordinates.
	 * @param view Scalable view that provides the scaling factor
	 * @return The scaled point
	 */
	public static Point applyScale(Point p, boolean scaleToDoc, Scalable view)
	{
		Point result = new Point();
		result.x = applyScale(p.x, scaleToDoc, view);
		result.y = applyScale(p.y, scaleToDoc, view);
		return result;
	}

	/**
	 * Applies the current scale factor of the view to a coordinate.
	 *
	 * @param coordinate X or Y coordinate to be scaled
	 * @param scaleToDoc
	 *		true	Assumes that the coordinates are component coordinates that should be translated to document coordinates.
	 *		false	Assumes that the coordinates are document coordinates that should be translated to component coordinates.
	 * @param view Scalable view that provides the scaling factor
	 * @return The resulting coordinate
	 */
	public static int applyScale(int coordinate, boolean scaleToDoc, Scalable view)
	{
		double scaleFactor = view.getScaleFactor();
		if (scaleToDoc)
		{
			// Scale component coordinates to document coordinates
			coordinate = (int) (coordinate / scaleFactor + 0.5);
		}
		else
		{
			// Scale document coordinates to component coordinates
			coordinate = (int) (coordinate * scaleFactor + 0.5);
		}

		return coordinate;
	}

	//////////////////////////////////////////////////
	// @@ Mouse and cursor support
	//////////////////////////////////////////////////

	/** Saves the current visible status of the glass pane visible for {@link #waitCursorOn} */
	private static boolean glassPaneVisible;

	/** Saves the current cursor status of the glass pane visible for {@link #waitCursorOn} */
	private static Cursor glassPaneCursor;

	/**
	 * Turns the waits cursor on.
	 * The wait cursor is implemented by showing the glass pane and setting its cursor
	 * to the system wait cursor. The current status of the glass pane is saved and
	 * can be restored using {@link #waitCursorOff}. Subsequent calls of waitCursorOn do not
	 * overwrite the saved state, waitCursorOff will reset the glass pane to the first
	 * saved state.
	 *
	 * @param comp Component that can be used to determine the glass pane
	 */
	public static void waitCursorOn(Component comp)
	{
		Component gp = getGlassPane(comp);

		// Save the current status of the glass pane if not already done
		if (glassPaneCursor == null)
		{
			glassPaneVisible = gp.isVisible();
			glassPaneCursor = gp.getCursor();
		}

		// Display the glass pane and set the wait cursor
		gp.setVisible(true);
		gp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	/**
	 * Turns the waits cursor off.
	 * This will reset the changes to the glass pane caused by a call to {@link #waitCursorOn}.
	 * If there is not information present about the previous state of the glass pane
	 * (i. e. because waitCursorOff has already been called), the method will do nothing.
	 *
	 * @param comp Component that can be used to determine the glass pane
	 */
	public static void waitCursorOff(Component comp)
	{
		// Reset the status of the glass pane if reset info present
		if (glassPaneCursor != null)
		{
			Component gp = getGlassPane(comp);

			gp.setVisible(glassPaneVisible);
			gp.setCursor(glassPaneCursor);

			glassPaneVisible = false;
			glassPaneCursor = null;
		}
	}

	/**
	 * Forces a mouse movement by 1 pixel to the right and back that will generate mouse movement events.
	 * This is used in the first line to update mouse position-dependant cursor shapes programatically.
	 *
	 * @param pos Current mouse positon in screen coordinates
	 */
	public static void forceMouseMove(Point pos)
	{
		try
		{
			Robot robot = new Robot();
			robot.setAutoDelay(0);
			robot.mouseMove(pos.x + 1, pos.y);
			robot.mouseMove(pos.x, pos.y);
		}
		catch (AWTException e)
		{
			// Ignored on systems that don't support Robot
		}
	}

	//////////////////////////////////////////////////
	// @@ Listener utilities
	//////////////////////////////////////////////////

	/**
	 * Checks if the specified listener is already registered as listener of the specified type
	 * in a listener list.
	 * @param listenerList Listener list to search
	 * @param type Type of the listener to be added
	 * @param listener Listener to be added
	 * @return
	 *		true	The listener is already registered.<br>
	 *		false	No such listener has been found.
	 */
	public static boolean containsListener(EventListenerList listenerList, Class type, EventListener listener)
	{
		if (listenerList != null)
		{
			// Guaranteed to return a non-null array
			Object [] listeners = listenerList.getListenerList();

			// Search for the listener
			for (int i = listeners.length - 2; i >= 0; i -= 2)
			{
				if (listeners [i] == type && listeners [i + 1] == listener)
					return true;
			}
		}

		return false;
	}

	//////////////////////////////////////////////////
	// @@ Multi line string support
	//////////////////////////////////////////////////

	/** Flag for {@link #drawMultilineString(Graphics, String, int, Rectangle, boolean)}: Indicates that the text should be left justified. */
	public static final int LEFT = (1 << 0);

	/** Flag for {@link #drawMultilineString(Graphics, String, int, Rectangle, boolean)}: Indicates that the text should be centered. */
	public static final int CENTER = (1 << 1);

	/** Flag for {@link #drawMultilineString(Graphics, String, int, Rectangle, boolean)}: Indicates that the text should be right justified. */
	public static final int RIGHT = (1 << 2);

	/** Flag for {@link #drawMultilineString(Graphics, String, int, Rectangle, boolean)}: Indicates that the text should be displayed at the top of the drawing area . */
	public static final int TOP = (1 << 3);

	/** Flag for {@link #drawMultilineString(Graphics, String, int, Rectangle, boolean)}: Indicates that the text should be displayed at the middle of the drawing area . */
	public static final int MIDDLE = (1 << 4);

	/** Flag for {@link #drawMultilineString(Graphics, String, int, Rectangle, boolean)}: Indicates that the text should be displayed at the bottom of the drawing area . */
	public static final int BOTTOM = (1 << 5);

	/**
	 * Draws a multi line text and/or calculates the dimensions of the text
	 * when drawn into the given Graphics object.
	 *
	 * @param g Graphics context to paint into
	 * @param s Text to draw
	 * @param alignment Text alignment ({@link #LEFT}/{@link #CENTER}/{@link #RIGHT} | {@link #TOP}/{@link #MIDDLE}/{@link #BOTTOM})
	 * @param r Rectangle containing the drawing region
	 * @param print
	 *		true	Draws the text<br>
	 *		false	Calculates the text dimensions only
	 *
	 * If r.width &gt; 0 then the text lines are wrapped to this width.<br>
	 * If r.width is 0 or print is false, the method writes the width of the longest line back into r.width.<br>
	 * Note: The height of the text is always written back into r.height!
	 */
	public static void drawMultilineString(Graphics g, String s, int alignment, Rectangle r, boolean print)
	{
		drawMultilineString(g, null, s, alignment, r, print);
	}

	/**
	 * Calculates the dimensions of the text.
	 *
	 * @param fm Font metrics of the current font
	 * @param s Text to draw
	 * @param alignment Text alignment ({@link #LEFT}/{@link #CENTER}/{@link #RIGHT} | {@link #TOP}/{@link #MIDDLE}/{@link #BOTTOM})
	 * @param r Rectangle containing the drawing region
	 *
	 * The method writes the width of the longest line back into r.width.<br>
	 * The height of the text is written back into r.height.
	 */
	public static void computeMultilineStringBounds(FontMetrics fm, String s, int alignment, Rectangle r)
	{
		drawMultilineString(null, fm, s, alignment, r, false);
	}

	/**
	 * Draws a multi line text and/or calculates the dimensions of the text
	 * when drawn into the given Graphics object.
	 *
	 * @param g Graphics context to paint into (can be null if fm is supplied and print is false)
	 * @param fm Font metrics of the current font or null to retrieve the metrics from the graphics context
	 * @param s Text to draw
	 * @param alignment Text alignment ({@link #LEFT}/{@link #CENTER}/{@link #RIGHT} | {@link #TOP}/{@link #MIDDLE}/{@link #BOTTOM})
	 * @param r Rectangle containing the drawing region
	 * @param print
	 *		true	Draws the text<br>
	 *		false	Calculates the text dimensions only
	 *
	 * If r.width &gt; 0 then the text lines are wrapped to this width.<br>
	 * If r.width is 0 or print is false, the method writes the width of the longest line back into r.width.<br>
	 * Note: The height of the text is always written back into r.height!
	 */
	private static void drawMultilineString(Graphics g, FontMetrics fm, String s, int alignment, Rectangle r, boolean print)
	{
		if (s == null)
			return;

		String word = "";
		String line = "";
		int maxWidth = 0;
		int height = 0;
		boolean wordCompleted;
		boolean lineCompleted;
		int n = s.length();

		if (fm == null)
			fm = g.getFontMetrics();

		if (r.height != 0 && (alignment & (MIDDLE | BOTTOM)) != 0)
		{
			// we have a height specification for the drawing rectangle and we shall print
			// the text in the middle of the rectangle or at the bottom.
			// we need to now the vertical extent of the text, compute it.
			int totalHeight = 0;
			for (int i = 0; i < n; i++)
			{
				char c = s.charAt(i);

				if (c == '\n')
				{
					wordCompleted = true;
					lineCompleted = true;
				}
				else
				{
					if (c == '\t')
					{
						word += "        ";
					}
					else
					{
						word += c;
					}
					lineCompleted = (i == n - 1);
					wordCompleted = lineCompleted || Character.isSpaceChar(c);
				}

				if (wordCompleted)
				{
					if (r.width > 0)
					{
						if (fm.stringWidth(line + word) > r.width)
						{
							totalHeight += fm.getHeight();
							line = word;
						}
						else
						{
							line += word;
						}
					}
					else
					{
						line += word;
					}

					word = "";
				}

				if (lineCompleted)
				{
					totalHeight += fm.getHeight();
					line = "";
				}
			}

			// Compute the offset for the first line to print
			if ((alignment & MIDDLE) != 0)
			{
				height = (r.height - totalHeight) / 2;
			}
			else
			{
				height = r.height - totalHeight;
			}
		}

		for (int i = 0; i < n; i++)
		{
			char c = s.charAt(i);

			if (c == '\n')
			{
				wordCompleted = true;
				lineCompleted = true;
			}
			else
			{
				if (c == '\t')
				{
					word += "        ";
				}
				else
				{
					word += c;
				}
				lineCompleted = (i == n - 1);
				wordCompleted = lineCompleted || Character.isSpaceChar(c);
			}

			if (wordCompleted)
			{
				if (r.width > 0)
				{
					if (fm.stringWidth(line + word) > r.width)
					{
						if (fm.stringWidth(line) > maxWidth)
						{
							maxWidth = fm.stringWidth(line);
						}
						height += fm.getHeight();

						if (print)
						{
							drawString(g, line, alignment, new Rectangle(r.x, r.y + height - fm.getDescent(), r.width, 0));
						}
						line = word;
					}
					else
					{
						line += word;
					}
				}
				else
				{
					line += word;
				}

				word = "";
			}

			if (lineCompleted)
			{
				if (fm.stringWidth(line) > maxWidth)
				{
					maxWidth = fm.stringWidth(line);
				}
				height += fm.getHeight();

				if (print)
				{
					drawString(g, line, alignment, new Rectangle(r.x, r.y + height - fm.getDescent(), r.width, 0));
				}

				line = "";
			}
		}

		if (r.width == 0 || !print)
		{
			r.width = maxWidth;
		}
		r.height = height;
	}

	/**
	 * Draws a string into a Graphics object.
	 *
	 * @param g Graphics context to paint into
	 * @param s Text to draw
	 * @param alignment Text alignment ({@link #LEFT}/{@link #CENTER}/{@link #RIGHT})
	 * @param r Bounding rectangle (width has to be greater 0 when the alignment is CENTER or RIGHT)
	 */
	public static void drawString(Graphics g, String s, int alignment, Rectangle r)
	{
		if (s == null)
			return;

		int x = r.x;
		if ((alignment & CENTER) != 0)
		{
			// Center text in line
			x += (r.width - g.getFontMetrics().stringWidth(s)) / 2;
		}
		else if ((alignment & RIGHT) != 0)
		{
			// Right-align text
			x += r.width - g.getFontMetrics().stringWidth(s);
		}
		g.drawString(s, x, r.y);
	}

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Shows a modal dialog.
	 *
	 * @param dlg Dialog to display
	 */
	public static void show(JDialog dlg)
	{
		dlg.setModal(true);
		dlg.setVisible(true);
	}

	/**
	 * Shows a frame.
	 *
	 * @param frame Dialog to display
	 */
	public static void show(JFrame frame)
	{
		frame.setVisible(true);
	}

	/**
	 * Shows a window.
	 *
	 * @param window Dialog to display
	 */
	public static void show(JWindow window)
	{
		window.setVisible(true);
	}

	/**
	 * Gets the root pane container of a component.
	 *
	 * @param c The component
	 * @return The container or null
	 */
	public static RootPaneContainer getRootPaneContainer(Component c)
	{
		// Find the root pane container of this component
		for (; c != null; c = c.getParent())
		{
			if (c instanceof RootPaneContainer)
			{
				return (RootPaneContainer) c;
			}
		}
		return null;
	}

	/**
	 * Gets the scroll pane that wraps a given component.
	 *
	 * @param c Given component
	 * @return The scroll pane or null if the component is not wrapped in a scroll pane
	 */
	public static JScrollPane getScrollPaneAncestor(Component c)
	{
		for (c = c.getParent(); c != null; c = c.getParent())
		{
			if (c instanceof JScrollPane)
				return (JScrollPane) c;
		}
		return null;
	}

	/**
	 * Gets the dialog that contains the given component.
	 *
	 * @param c The component
	 * @return The dialog or null the component is not part of a dialog
	 */
	public static Dialog getDialog(Component c)
	{
		// Find the root pane container of this component
		for (; c != null; c = c.getParent())
		{
			if (c instanceof Dialog)
			{
				return (Dialog) c;
			}
		}
		return null;
	}

	/**
	 * Inflates the rectangle by the specified x/y value.
	 *
	 * @param r Rectangle to enlarge
	 * @param x Horizontal value; if negative, the rectangle will be deflated
	 * @param y Vertical value; if negative, the rectangle will be deflated
	 */
	public static void inflateRectangle(Rectangle r, int x, int y)
	{
		r.x -= x;
		r.y -= y;
		r.width += 2 * x;
		r.height += 2 * y;
	}
}
