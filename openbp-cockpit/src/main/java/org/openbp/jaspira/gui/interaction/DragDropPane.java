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
package org.openbp.jaspira.gui.interaction;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.FocusManager;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.text.JTextComponent;

import org.openbp.common.icon.MultiIcon;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.jaspira.JaspiraConstants;

/**
 * A drag and drop pane is a component that is to be used as glass pane for a window.
 * It is used to handle drag and drop events and propagate them to {@link InteractionClient} s via {@link DragAwareRegion} s.
 * It also provides support for break out box (bob) menus.
 * A bob is a circular overlay window that presents items that can be imported into the underlying
 * drop client. The user may select the item by simply clicking on it. A bob menu is always initiated
 * by a hot key.
 *
 * @author Stephan Moritz
 */
public class DragDropPane extends JComponent
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Accept cursor. Indicates that a drop is possible at the current position. */
	public static CursorPrototype acceptCursorPrototype;

	/** Reject cursor. Indicates that a drop is not possible at the current position. */
	public static CursorPrototype rejectCursorPrototype;

	/** Size of a break out box entry */
	private static final int BOB_ENTRY_SIZE = 60;

	/** Size of the break out box item circle */
	private static final int BOB_CIRCLE_SIZE = 32;

	/** Key for the escape action in the action map */
	private static final String ESCAPE = "escape";

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** List of currently registered drop clients (contains {@link InteractionClient} objects) */
	private Set dropClients;

	/** Drawing composite used for drawing on the glass pane */
	private Composite composite;

	/** List of all drag-aware regions currently active */
	private List currentRegions;

	/** The currently active region of a dnd action */
	private DragAwareRegion currentRegion;

	/** The currently transported data of a dnd action */
	private Transferable currentTransferable;

	/** The supported flavors of the current drag action */
	private List currentFlavors;

	/** The currently active drag originator of a dnd action */
	private DragOrigin currentDragOrigin;

	/** Icon denoting the currently dragged drag item */
	private MultiIcon currentDragIcon;

	/** Current accept cursor */
	private Cursor currentAcceptCursor;

	/** Current reject cursor */
	private Cursor currentRejectCursor;

	/** Mouse event that triggered the drag action */
	private MouseEvent mouseEvent;

	/** The last point used during drag or bobmode */
	private Point lastLocation;

	/** Is the bob shown? */
	private boolean breakoutshown;

	/** all regions of our breakoutmenu */
	private BreakoutBoxEntry[] bobEntries;

	/** Mouse listener for bob mode */
	private MouseInputListener bobMouseListener;

	/** Mouse wheel listener for bob mode */
	private MouseWheelListener bobMouseWheelListener;

	/** Point where the bob is being displayed */
	private Point bobPosition;

	/** Point where we drop the selection of the bob */
	private Point bobDropPoint;

	/** Radius of the bob */
	private int bobRadius;

	/** Angle between two entries */
	private double angleDelta;

	/** Angle offset */
	private double angleOffset;

	/** Title of the recently selected entry */
	private String recentlySelectedEntryTitle;

	private int animationPos = -1;
	private double animationAngle;
	private Timer animationTimer; 

	private static int[] animationControl = new int[] { 1, 2, 4, 7, 12, 20, 12, 7, 4, 2, 1 };
	private static int animationLevels;
	static
	{
		animationLevels = 0;
		for (int i = 0; i < animationControl.length; ++i)
		{
			animationLevels += animationControl[i];
		}
	}
	private static long animationInterval = 50;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	static
	{
		// Create cursor prototypes
		ResourceCollection res = ResourceCollectionMgr.getDefaultInstance().getResource(JaspiraConstants.RESOURCE_JASPIRA, DragDropPane.class);

		acceptCursorPrototype = new BasicCursorPrototype(null, ((ImageIcon) res.getRequiredObject("cursor.dragaccept")).getImage(), new Point());

		rejectCursorPrototype = new BasicCursorPrototype(null, ((ImageIcon) res.getRequiredObject("cursor.dragreject")).getImage(), new Point());
	}

	/**
	 * Creates a new drag drop pane and installs it as a glass pane for the
	 * givent container.
	 * Does nothing if a dnd pane has already been installed
	 * @param container Container to install the pane for
	 * @return The installed dnd pane
	 */
	public static DragDropPane installDragDropPane(DropPaneContainer container)
	{
		Component glassPane = container.getGlassPane();
		if (glassPane instanceof DragDropPane)
			return (DragDropPane) glassPane;

		DragDropPane pane = new DragDropPane();
		container.setDragDropPane(pane);
		return pane;
	}

	/**
	 * Constructor.
	 * We do not want a DragPane to be installed into open space.
	 * Rather use: install ().
	 */
	private DragDropPane()
	{
		super();

		// We need it to be invisible
		setOpaque(false);

		// Make the dnd pane slightly opaque when displayed
		setTransparency(0.2f);

		dropClients = new HashSet();
		currentRegions = new ArrayList();

		// The pane may provide tool tips for regions
		ToolTipManager.sharedInstance().registerComponent(this);

		// Register an action to close the breakout box when escape is being pressed.
		registerBobCloseAction();

		lastLocation = new Point();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Painting
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Draws the Pane (i\.e\. the active DropRegions)
	 * @see java.awt.Component#paint(Graphics)
	 */
	public void paint(Graphics g)
	{
		if (!isVisible())
			return;

		if (currentFlavors != null)
		{
			// We are in drag/Drop mode
			Graphics2D g2 = (Graphics2D) g;

			// We save the old Composite object of the context
			// Set our composite (transparency)
			Composite oldComposite = g2.getComposite();
			g2.setComposite(composite);

			for (Iterator it = currentRegions.iterator(); it.hasNext();)
			{
				((DragAwareRegion) it.next()).draw(g2);
			}

			if (currentRegion != null && currentRegion.getOverlay() != null)
			{
				g2.setColor(Color.BLACK);

				g2.draw(currentRegion.getOverlay());
			}

			// Reset the composite
			g2.setComposite(oldComposite);
		}
		else if (bobEntries != null)
		{
			// We are in bob mode
			Graphics2D g2 = (Graphics2D) g;

			// Set our Composite (transparency)
			Composite oldComposite = g2.getComposite();
			g2.setComposite(composite);

			// Bob is always blue
			g2.setColor(Color.BLUE);

			// Draw the outer bob circle
			g2.fillOval(bobPosition.x - bobRadius, bobPosition.y - bobRadius, bobRadius * 2, bobRadius * 2);

			// Reset the composite
			g2.setComposite(oldComposite);

			// Draw the inner bob circle
			g2.drawOval(bobPosition.x - 5, bobPosition.y - 5, 10, 10);

			// Draw the bob entries
			for (int i = 0; i < bobEntries.length; i++)
			{
				bobEntries[i].draw(g);
			}
		}
	}

	/**
	 * Rebuilds the DragAwareRegions of the pane.
	 */
	public void regionsInvalidated()
	{
		repaintRegions();

		currentRegions.clear();

		if (currentFlavors != null)
		{
			for (Iterator it = dropClients.iterator(); it.hasNext();)
			{
				InteractionClient next = (InteractionClient) it.next();
				List regions = next.getAllDropRegions(currentFlavors, currentTransferable, mouseEvent);
				if (regions != null)
				{
					currentRegions.addAll(regions);
				}
			}
		}

		repaintRegions();
	}

	/**
	 * Repaints the current dnd regions.
	 */
	private void repaintRegions()
	{
		Rectangle invalidateRect = new Rectangle();

		for (Iterator it = currentRegions.iterator(); it.hasNext();)
		{
			DragAwareRegion region = (DragAwareRegion) it.next();

			Rectangle r = region.getBounds();
			invalidateRect.add(r);
		}

		repaint(invalidateRect);
	}

	//////////////////////////////////////////////////
	// @@ Dnd support
	//////////////////////////////////////////////////

	/**
	 * Initiates a dragging action.
	 * Determines the flavors supported by the transferable and notifies the drop clients.
	 * @param transferable Transferable to be dragged
	 * @param dragOrigin Originator of the dragging action
	 * @param mouseEvent Mouse event that triggered the drag action
	 */
	public void startDrag(Transferable transferable, DragOrigin dragOrigin, MouseEvent mouseEvent)
	{
		currentTransferable = transferable;
		currentDragOrigin = dragOrigin;
		currentFlavors = Arrays.asList(currentTransferable.getTransferDataFlavors());
		this.mouseEvent = mouseEvent;

		// Display the pane
		setVisible(true);

		// Create a drag cursors
		// Use the dragged item for prototype construction.
		currentDragIcon = currentDragOrigin.getDragImage();
		currentAcceptCursor = acceptCursorPrototype.createCursor(currentDragIcon);
		currentRejectCursor = rejectCursorPrototype.createCursor(currentDragIcon);

		// Initially set the reject cursor
		setCursor(currentRejectCursor);

		// Notify clients of started dragging
		for (Iterator it = dropClients.iterator(); it.hasNext();)
		{
			((InteractionClient) it.next()).dragStarted(currentTransferable);
		}

		regionsInvalidated();
	}

	/**
	 * Cancels any currently ongoing drag action.
	 */
	public void cancelDrag()
	{
		if (currentTransferable != null && currentDragOrigin != null)
		{
			currentDragOrigin.dropCanceled(currentTransferable);
		}
		endDrag();
	}

	/**
	 * Called when the drag ends. Clears all regions and notifies the drop clients.
	 */
	public void endDrag()
	{
		Transferable oldTransferable = currentTransferable;

		currentDragOrigin = null;
		currentRegion = null;
		currentRegions.clear();
		currentFlavors = null;
		currentTransferable = null;
		mouseEvent = null;

		currentDragIcon = null;
		currentAcceptCursor = null;
		currentRejectCursor = null;

		// Notify clients of ended dragging
		if (oldTransferable != null)
		{
			for (Iterator it = dropClients.iterator(); it.hasNext();)
			{
				((InteractionClient) it.next()).dragEnded(oldTransferable);
			}
		}

		setCursor(Cursor.getDefaultCursor());

		setVisible(false);
	}

	//////////////////////////////////////////////////
	// @@ BOB support
	//////////////////////////////////////////////////

	/**
	 * Starts the bob mode using the given provider.
	 *
	 * @param provider Provider for bob entries
	 * @param position Center of the bob in screen coordinates
	 */
	public void startBreakOutMode(final BreakoutProvider provider, Point position)
	{
		if (FocusManager.getCurrentManager().getFocusOwner() instanceof JTextComponent)
		{
			// Umpf, quick hack to prevent keys entered in text fields from initiating the bob mode
			return;
		}

		bobMouseListener = new MouseInputAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (breakoutshown)
				{
					if (bobEntries != null)
					{
						for (int i = 0; i < bobEntries.length; i++)
						{
							if (bobEntries[i].reactsOn(e.getX(), e.getY()))
							{
								BreakoutBoxEntry bobEntry = bobEntries[i];

								endBreakOutMode();

								bobEntry.importData(bobDropPoint);

								recentlySelectedEntryTitle = bobEntry.getToolTipText();
								return;
							}
						}
					}

					endBreakOutMode();
					return;
				}
			}

			/**
			 * @see javax.swing.event.MouseInputAdapter#mouseMoved(java.awt.event.MouseEvent)
			 */
			public void mouseMoved(MouseEvent e)
			{
				// We store the new position
				lastLocation = e.getPoint();
			}
		};
		addMouseListener(bobMouseListener);
		addMouseMotionListener(bobMouseListener);

		bobMouseWheelListener = new MouseWheelListener()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				int notches = e.getWheelRotation();

				rotateBobEntries(notches);
			}
		};
		addMouseWheelListener(bobMouseWheelListener);

		bobPosition = new Point(position);
		bobDropPoint = new Point(position);
		lastLocation = new Point(bobDropPoint);

		buildBreakOutMenu(provider);
		showBreakOutMenu();
		setVisible(true);
		breakoutshown = true;
	}

	/**
	 * Ends the bob mode
	 */
	public void endBreakOutMode()
	{
		if (breakoutshown)
		{
			// Invalidate the bob area
			int r = bobRadius + BOB_ENTRY_SIZE;
			Rectangle invalidateRect = new Rectangle(bobPosition.x - r, bobPosition.y - r, 2 * r, 2 * r);
			repaint(invalidateRect);

			bobEntries = null;
			breakoutshown = false;
		}

		removeMouseListener(bobMouseListener);
		removeMouseMotionListener(bobMouseListener);
		bobMouseListener = null;

		removeMouseWheelListener(bobMouseWheelListener);
		bobMouseWheelListener = null;

		Point pos = getLocationOnScreen();
		pos.translate(lastLocation.x, lastLocation.y);
		setVisible(false);
	}

	private void buildBreakOutMenu(final BreakoutProvider provider)
	{
		// Determine the importers that might be displayed in the bob
		List importers = new ArrayList();
		for (Iterator it = dropClients.iterator(); it.hasNext();)
		{
			InteractionClient client = (InteractionClient) it.next();

			List clientImporters = client.getAllImportersAt(bobDropPoint);
			if (clientImporters != null)
			{
				importers.addAll(clientImporters);
			}
		}

		// Retrieve all valid BreakOutEntries for the given coordinates and location
		// and arrange them on a circle.
		bobEntries = provider.createBreakOutEntries(importers);

		bobRadius = Math.max((int) Math.round(bobEntries.length * BOB_ENTRY_SIZE / (2d * Math.PI)), 30);
		bobPosition.translate(-bobRadius, 0);

		angleOffset = 0d;
		angleDelta = 2d * Math.PI / bobEntries.length;

		if (recentlySelectedEntryTitle != null)
		{
			for (int i = 0; i < bobEntries.length; ++i)
			{
				if (recentlySelectedEntryTitle.equals(bobEntries[i].getToolTipText()))
				{
					angleOffset += i * angleDelta;
				}
			}
		}
	}

	private void showBreakOutMenu()
	{
		angleOffset = normalizeAngle(angleOffset);

		Rectangle invalidateRect = new Rectangle();
		int n = bobEntries.length;
		for (int i = 0; i < n; i++)
		{
			Rectangle rect = new Rectangle(bobPosition.x, bobPosition.y, BOB_CIRCLE_SIZE, BOB_CIRCLE_SIZE);

			double alpha = angleOffset + angleDelta * (n - i) + Math.PI / 2;
			rect.x += (int) Math.round(Math.sin(alpha) * bobRadius) - (BOB_CIRCLE_SIZE / 2);
			rect.y += (int) Math.round(Math.cos(alpha) * bobRadius) - (BOB_CIRCLE_SIZE / 2);

			bobEntries[i].setLocationOnGlassPanel(rect);

			Rectangle invalid = new Rectangle(rect);
			invalid.grow(10, 10);
			invalidateRect.add(invalid);
		}

		// Ensure that the bob box is entirely visible on the screen
		int xDiff = 0;
		int yDiff = 0;
		Rectangle screenRect = getBounds();
		if (invalidateRect.x < screenRect.x)
			xDiff = screenRect.x - invalidateRect.x;
		else if (invalidateRect.x + invalidateRect.width > screenRect.x + screenRect.width)
			xDiff = (screenRect.x + screenRect.width) - (invalidateRect.x + invalidateRect.width);
		if (invalidateRect.y < screenRect.y)
			yDiff = screenRect.y - invalidateRect.y;
		else if (invalidateRect.y + invalidateRect.height > screenRect.y + screenRect.height)
			yDiff = (screenRect.y + screenRect.height) - (invalidateRect.y + invalidateRect.height);

		if (xDiff != 0 || yDiff != 0)
		{
			// Doesn't fit on screen, move into screen bounds
			bobPosition.translate(xDiff, yDiff);
			invalidateRect.translate(xDiff, yDiff);

			for (int i = 0; i < n; i++)
			{
				Rectangle rect = bobEntries[i].getLocationOnGlassPanel();
				rect.translate(xDiff, yDiff);
				bobEntries[i].setLocationOnGlassPanel(rect);
			}
		}

		repaint(invalidateRect);
	}

	private void rotateBobEntries(int delta)
	{
		double angleToMove = -delta * angleDelta;

		// Finish the last animation
		if (animationTimer != null)
		{
			endAnimation();
			angleToMove += getRemainingAnimationAngle();
		}

		int n = bobEntries.length;
		if (n == 0)
			return;
		delta %= n;
		if (delta == 0)
			return;

		// Computer and start animation
		animationPos = 0;
		animationAngle = angleToMove / animationLevels;

		animationTimer = new Timer("Breakout menu animation timer");
		animationTimer.schedule(new TimerTask()
		{
			public void run()
			{
				if (! advanceAnimation())
				{
					endAnimation();
					angleOffset += getRemainingAnimationAngle();
					showBreakOutMenu();
				}
			}
		}, 0, animationInterval);
	}

	private void endAnimation()
	{
		if (animationTimer != null)
		{
			animationTimer.cancel();
			animationTimer = null;
		}
	}

	private double getRemainingAnimationAngle()
	{
		double remainingAngle = 0d;
		if (animationPos >= 0 && animationPos < animationControl.length)
		{
			int n = 0;
			for (int i = animationPos; i < animationControl.length; ++i)
			{
				n += animationControl[i];
			}
			remainingAngle = n * animationAngle;
		}
		return remainingAngle;
	}

	private boolean advanceAnimation()
	{
		if (animationPos < 0)
			return false;

		angleOffset += animationControl[animationPos] * animationAngle;
		showBreakOutMenu();

		if (++animationPos < animationControl.length)
		{
			return true;
		}
		return false;
	}

	/**
	 * Registers an action to close the bob when escape is being pressed.
	 */
	private void registerBobCloseAction()
	{
		getActionMap().put(ESCAPE, new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (breakoutshown && isVisible())
				{
					endBreakOutMode();
				}
			}
		});

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), ESCAPE);
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ DragInitiator connection
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Handler for 'mouse dragged' events.
	 * Passes the event to the current region in dnd mode and updates the dnd cursor.
	 * @param p Current mouse position
	 */
	public void mouseDragged(Point p)
	{
		lastLocation = p;

		DragAwareRegion newRegion = getRegionAt(p);

		if (newRegion != currentRegion)
		{
			// We are over another region
			if (currentRegion != null)
			{
				currentRegion.dragExit();
			}

			currentRegion = newRegion;
			if (currentRegion != null)
			{
				currentRegion.dragEnter();

				CursorPrototype cursor = currentRegion.getCursor();
				if (cursor != null)
				{
					setCursor(cursor.createCursor(currentDragIcon));
				}
				else if (currentRegion != null && currentRegion.canImport())
				{
					setCursor(currentAcceptCursor);
				}
				else
				{
					setCursor(currentRejectCursor);
				}
			}
			else
			{
				setCursor(currentRejectCursor);
			}
		}
	}

	/**
	 * Handler for 'mouse released' events.
	 * Notifies the current drag originator of the drop and ends dnd mode.
	 * @param p Current mouse position in glass coordinates
	 */
	public void mouseReleased(Point p)
	{
		try
		{
			// Put this in a try-finally block to prevent never-ending drag mode on exceptions
			if (currentDragOrigin != null)
			{
				if (currentRegion != null && currentTransferable != null)
				{
					currentDragOrigin.dropAccepted(currentTransferable);

					if (currentRegion.importData(currentTransferable, p))
					{
						currentDragOrigin.dropPerformed(currentTransferable);
					}
					else
					{
						currentDragOrigin.dropCanceled(currentTransferable);
					}
				}
				else
				{
					currentDragOrigin.dropCanceled(currentTransferable);
				}
			}
		}
		finally
		{
			endDrag();
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Tooltip
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the tool tip appropriate for the given region.
	 * @param event Mouse event containing the coordinates that should be used to determine
	 * the hovered bob entry if the bob is active.
	 * @return The tip or null
	 */
	public String getToolTipText(MouseEvent event)
	{
		if (event != null)
		{
			if (currentFlavors != null && currentRegion != null)
			{
				// We are in dnd mode, return the tooltip for the current region
				return currentRegion.getToolTipText();
			}
			else if (bobEntries != null)
			{
				for (int i = 0; i < bobEntries.length; i++)
				{
					if (bobEntries[i].reactsOn(event.getX(), event.getY()))
					{
						return bobEntries[i].getToolTipText();
					}
				}
			}
		}

		return null;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Regions
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the region at the given point.
	 * @param p Point to check
	 * @return The region or null if no region corresponds to this point
	 */
	private DragAwareRegion getRegionAt(Point p)
	{
		for (Iterator it = currentRegions.iterator(); it.hasNext();)
		{
			DragAwareRegion region = (DragAwareRegion) it.next();

			if (region.reactsOn(p.x, p.y))
			{
				// We have our region
				return region;
			}
		}

		// No region found
		return null;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Adding and removal of DropReceivers
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a drop client.
	 * @param client Client to add
	 */
	public void addDropClient(InteractionClient client)
	{
		dropClients.add(client);
	}

	/**
	 * Removes a drop client.
	 * @param client Client to remove
	 */
	public void removeDropClient(InteractionClient client)
	{
		dropClients.remove(client);
	}

	/**
	 * Removes all drop clients.
	 */
	public void clearDropClients()
	{
		dropClients.clear();
	}

	/**
	 * Sets the composite used to draw the glasPaneElements.
	 * This will usually be an instance of AlphaComposite.
	 * Note that it is usually more convient to set the transparency directly via
	 * {@link #setTransparency}, which incidently just creates a new AlphaComposite object.
	 *
	 * @param composite The composite to set
	 * @see java.awt.AlphaComposite
	 */
	public void setComposite(Composite composite)
	{
		this.composite = composite;
	}

	/**
	 * Sets the alpha Component, i\.e\. the transparency of the drop pane.
	 *
	 * @param alpha New alpha value, between 0f (opaque) 1f (fully transparent)
	 */
	public void setTransparency(float alpha)
	{
		setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
	}

	/**
	 * Returns the originator of the current dnd action.
	 * @return The drag originator or null if no dnd action is going on
	 */
	public DragOrigin getDragOrigin()
	{
		return currentDragOrigin;
	}

	/**
	 * Returns the last mouse location.
	 * @return The mouse location in screen coordinates
	 */
	public Point getLastLocation()
	{
		return lastLocation;
	}

	/**
	 * Gets the mouse event that triggered the drag action.
	 * @nowarn
	 */
	public MouseEvent getMouseEvent()
	{
		return mouseEvent;
	}

	private static final double CIRCLE = 2d * Math.PI;

	/**
	 * Normalizes an angle.
	 *
	 * @param angle Angle to normalize
	 * @return The angle in the range [0;2 * PI]
	 */
	private static double normalizeAngle(double angle)
	{
		// Normalize the angle
		while (angle > CIRCLE)
		{
			angle -= CIRCLE;
		}
		while (angle < 0)
		{
			angle += CIRCLE;
		}
		return angle;
	}
}
