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
package org.openbp.swing.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.openbp.common.ExceptionUtil;
import org.openbp.swing.SwingUtil;

/**
 * Split pane that supports an arbitary number of components.
 *
 * @author Stephan Moritz
 */
public class JMultiSplitPane extends JPanel
	implements ComponentListener
{
	/////////////////////////////////////////////////////////////////////////
	// @@ Constants
	/////////////////////////////////////////////////////////////////////////

	/** Split direction - Horizontal. */
	public static final int HORIZONTAL_SPLIT = JSplitPane.HORIZONTAL_SPLIT;

	/** Split direction - Vertical. */
	public static final int VERTICAL_SPLIT = JSplitPane.VERTICAL_SPLIT;

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/** Orientation of this multi split pane */
	private int orientation = HORIZONTAL_SPLIT;

	/** List containing the nested JSplitPanes */
	private List panes = new ArrayList();

	/** Array containing the proportional sizes for all children */
	private double [] proportions;

	/** Set to true during self layouting */
	private boolean changing;

	/////////////////////////////////////////////////////////////////////////
	// @@ Construction
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public JMultiSplitPane()
	{
		super();

		setLayout(new BorderLayout());

		addComponentListener(this);

		setFocusable(false);
	}

	/**
	 * Constructor.
	 *
	 * @param orientation Orientation of the split pane (HORIZONTAL_SPLIT/VERTICAL_SPLIT)
	 */
	public JMultiSplitPane(int orientation)
	{
		this();

		this.orientation = orientation;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Add/remove
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a child component at the given index to the multi split pane.
	 *
	 * @param comp Component to add
	 * @param index Index or -1 to add at the end
	 */
	public void addClient(Component comp, int index)
	{
		if (comp instanceof JMultiSplitPane && ((JMultiSplitPane) comp).getOrientation() == orientation)
		{
			// we add a JMultiSPlitPane with the same orientation.
			// to avoid clustering, we add the contents only.
			insertAllClients(((JMultiSplitPane) comp).getClients(), index);
			return;
		}

		int elements = getClientCount();

		if (index == elements)
		{
			index = -1;
		}
		else if (index > elements)
		{
			throw new IllegalArgumentException();
		}

		if (elements == 0)
		{
			// This is the first component to be entered
			add(comp);
		}
		else if (index == 0)
		{
			// We add it to the beginning
			JSplitPane newPane = createSplitPane(orientation, comp, getComponent(0));

			removeAll();
			add(newPane);
			panes.add(0, newPane);
		}
		else if (index == -1)
		{
			// Add to the end of the structure
			if (elements == 1)
			{
				// We have only one element
				JSplitPane newPane = createSplitPane(orientation, getComponent(0), comp);

				removeAll();
				add(newPane);
				panes.add(newPane);
			}
			else
			{
				JSplitPane last = (JSplitPane) panes.get(panes.size() - 1);

				// We take the right content from the last pane and add it to our next pane.
				JSplitPane newPane = createSplitPane(orientation, last.getRightComponent(), comp);

				last.setRightComponent(newPane);

				panes.add(newPane);
			}
		}
		else
		{
			// Somewhere in the middle
			// Pane before the new entry
			JSplitPane last = (JSplitPane) panes.get(index - 1);

			// We take the right content from the last pane and add it to our next pane.
			JSplitPane newPane = createSplitPane(orientation, comp, last.getRightComponent());

			last.setRightComponent(newPane);

			panes.add(index, newPane);
		}

		// Readjust sizes
		proportions = null;
		layoutDividers();
		revalidate();
	}

	/**
	 * Removes the given component from the split pane.
	 *
	 * @param index Index of the component to remvoe
	 */
	public void removeClient(int index)
	{
		if (index < 0)
			return;

		int elements = getClientCount();

		if (index >= elements)
			throw new IllegalArgumentException();

		if (elements == 1)
		{
			// we remove the last element that there is
			removeAll();

			Component parent = getParent();
			while (parent instanceof JSplitPane)
			{
				parent = parent.getParent();
			}

			if (parent instanceof JMultiSplitPane)
			{
				// We are empty	and our ancestor is also a multi split pane;
				// Remove us from the parent
				((JMultiSplitPane) parent).removeClient(this);
			}
		}
		else if (elements == 2)
		{
			// Only two elements left, we need to remove the last splitpane
			// and add the remaining component directly.

			// remaining component is either left (1) or right (0), depending on index
			Component c = (index == 1) ? ((JSplitPane) panes.get(0)).getLeftComponent() : ((JSplitPane) panes.get(0)).getRightComponent();
			removeAll();
			add(c);

			panes.clear();
		}
		else if (index == elements - 1)
		{
			// We remove the last element
			((JSplitPane) panes.get(index - 2)).setRightComponent(((JSplitPane) panes.get(index - 1)).getLeftComponent());

			panes.remove(index - 1);
		}
		else if (index == 0)
		{
			// Remove the first element
			removeAll();

			add(((JSplitPane) panes.get(0)).getRightComponent());

			panes.remove(0);
		}
		else
		{
			// Remove a component sowhere in the middle
			((JSplitPane) panes.get(index - 1)).setRightComponent(((JSplitPane) panes.get(index)).getRightComponent());

			panes.remove(index);
		}

		// Readjust sizes
		proportions = null;
		layoutDividers();
		revalidate();
	}

	/**
	 * Adds a child component to the end of the multi split pane.
	 *
	 * @param comp Component to add
	 */
	public void addClient(Component comp)
	{
		addClient(comp, -1);
	}

	/**
	 * Inserts all components at the given index.
	 *
	 * @param comps Components to add
	 * @param index Index or -1 to add at the end
	 */
	public void insertAllClients(Component [] comps, int index)
	{
		for (int i = comps.length; i > 0; i--)
		{
			addClient(comps [i - 1], index);
		}
	}

	/**
	 * Adds the given component before the current component.
	 *
	 * @param current Component that will be preceeded by the new one
	 * @param add Component to add
	 */
	public void addClientBefore(Component current, Component add)
	{
		addClient(add, getIndexOfClient(current));
	}

	/**
	 * Adds the given component before the current component.
	 *
	 * @param current Component that will be preceeded by the new one
	 * @param add Component to add
	 */
	public void addClientAfter(Component current, Component add)
	{
		addClient(add, getIndexOfClient(current) + 1);
	}

	/**
	 * Removes the given component from the split pane.
	 *
	 * @param comp Component to remove
	 */
	public void removeClient(Component comp)
	{
		removeClient(getIndexOfClient(comp));
	}

	/**
	 * Replaces the component at the given index with a substitute.
	 *
	 * @param index Index of the component
	 * @param substitute New component
	 */
	public void replaceClient(int index, Component substitute)
	{
		double [] oldProportions = new double [proportions.length];
		System.arraycopy(proportions, 0, oldProportions, 0, proportions.length);

		removeClient(index);
		addClient(substitute, index);

		System.arraycopy(oldProportions, 0, proportions, 0, proportions.length);

		layoutDividers();
	}

	/**
	 * Replaces the given component with a substitute.
	 *
	 * @param old Component to replace
	 * @param substitute New component
	 */
	public void replaceClient(Component old, Component substitute)
	{
		replaceClient(getIndexOfClient(old), substitute);
	}

	/**
	 * Return the nth component of this pane.
	 *
	 * @param n Component index
	 * @return The component
	 */
	public Component getClient(int n)
	{
		int elements = getClientCount();

		if (elements == 1)
		{
			return getComponent(0);
		}
		return n < (elements - 1) ? ((JSplitPane) panes.get(n)).getLeftComponent() : ((JSplitPane) panes.get(n - 1)).getRightComponent();
	}

	/**
	 * Get the number of components in the split pane.
	 * @nowarn
	 */
	public int getClientCount()
	{
		int result = panes.size() + 1;

		if (result == 1)
			return getComponentCount();

		return result;
	}

	/**
	 * Gets the components in this multi split pane.
	 * @nowarn
	 */
	public Component [] getClients()
	{
		int elements = getClientCount();
		Component result[] = new Component [elements];

		for (int i = 0; i < elements; i++)
		{
			result [i] = getClient(i);
		}

		return result;
	}

	/**
	 * Gets the index of the given component.
	 *
	 * @param comp Comp
	 * @return The index or -1 if not found
	 */
	public int getIndexOfClient(Component comp)
	{
		Component [] comps = getClients();
		for (int i = 0; i < comps.length; i++)
		{
			if (comps [i] == comp)
				return i;
		}

		return -1;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Sizes
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Sets the sizes of the clients in proportion to the total size
	 * of the pane. The last component gets the remainder.
	 *
	 * @param proportions Proportions (values between 0..1)<br>
	 * The size of this array must match the number of the components - 1
	 */
	public void setClientProportions(double [] proportions)
	{
		this.proportions = proportions;

		layoutDividers();
	}

	/**
	 * Returns the sizes of the first n-1 components relative to the overall size.
	 *
	 * @return Proportions (values between 0..1)<br>
	 * The size of this array matches the number of the components - 1
	 */
	public double [] getClientProportions()
	{
		return proportions;
	}

	/**
	 * Makes all components have the same size.
	 */
	public void createDefaultProportions()
	{
		int elements = getClientCount();

		proportions = new double [elements];

		if (elements > 0)
		{
			double delta = 1d / elements;

			for (int i = 0; i < elements; ++i)
			{
				proportions [i] = delta;
			}

			// Last one is filler
			proportions [elements - 1] = 1.0d - ((elements - 1) * delta);
		}
	}

	/**
	 * Does a resize based on the currently set values for divider proportions.
	 */
	protected void layoutDividers()
	{
		// Check for cyclic calls
		if (changing)
			return;

		// Convert the proportions into sizes.
		int total = orientation == HORIZONTAL_SPLIT ? getSize().width : getSize().height;
		if (total == 0)
			return;

		changing = true;

		int nClients = getClientCount();
		if (proportions == null || proportions.length != nClients)
		{
			createDefaultProportions();
		}

		int nPanes = panes.size();
		for (int i = 0; i < nPanes; i++)
		{
			int pos = (int) (total * proportions [i] - 2 * i);
			((JSplitPane) panes.get(i)).setDividerLocation(pos);
		}

		changing = false;
	}

	/**
	 * Returns the orientation.
	 * @return int
	 */
	public int getOrientation()
	{
		return orientation;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Helpers
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a split pane for usage as sub split pane of the multi split pane.
	 *
	 * @param orientation Orientation of the pane
	 * @param c1 First component to add to the pane
	 * @param c2 Second component to add to the pane
	 * @return The new pane
	 */
	private JSplitPane createSplitPane(int orientation, Component c1, Component c2)
	{
		JSplitPane pane = new JSplitPane(orientation, c1, c2);
		pane.setBorder(null);

		pane.setTransferHandler(getTransferHandler());

		// pane.addPropertyChangeListener (JSplitPane.DIVIDER_LOCATION_PROPERTY, this);

		return pane;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ ComponentListener implementation
	/////////////////////////////////////////////////////////////////////////

	public void componentHidden(ComponentEvent e)
	{
	}

	public void componentMoved(ComponentEvent e)
	{
	}

	public void componentResized(ComponentEvent e)
	{
		layoutDividers();
	}

	public void componentShown(ComponentEvent e)
	{
		layoutDividers();
	}

	/**
	 * Returns a string representation of this multi split pane.
	 * This consits of the orientation followed by the actual contents.
	 * @nowarn
	 */
	public String toString()
	{
		StringBuffer output = new StringBuffer("JMultiSplitPane ");
		output.append(orientation == VERTICAL_SPLIT ? "(vert)" : "(hor)");

		output.append(" [");

		Component [] compos = getClients();

		for (int i = 0; i < compos.length; i++)
		{
			output.append(compos [i].toString());

			output.append(", ");
		}

		output.append("]");

		return output.toString();
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Testing/Main
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Main method for test.
	 * @nowarn
	 */
	public static void main(String [] args)
	{
		JFrame frame = new JFrame();

		try
		{
			UIManager.setLookAndFeel("org.openbp.swing.plaf.sky.SkyLookAndFeel");
			// redraw components
			if (frame != null)
				SwingUtilities.updateComponentTreeUI(frame);
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
		}
		JMultiSplitPane multi = new JMultiSplitPane();
		JMultiSplitPane sulti = new JMultiSplitPane(JSplitPane.VERTICAL_SPLIT);

		// multi.addClient (new JLabel ("Anfang"));
		// multi.addClient (new JLabel ("2"));

		for (int i = 0; i < 5; i++)
		{
			multi.addClient(new JScrollPane(new JLabel("Label " + i)));
			sulti.addClient(new JScrollPane(new JLabel("SLabel " + i)));
		}

		multi.addClient(new JScrollPane(new JLabel("Mitte")), 3);

		multi.addClient(sulti, 3);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(multi);

		frame.pack();
		SwingUtil.show(frame);
	}
}
