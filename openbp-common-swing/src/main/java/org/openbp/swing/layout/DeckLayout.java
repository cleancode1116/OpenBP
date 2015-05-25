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
package org.openbp.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Deck layout.
 * DeckLayout treats each component in the container as a card.
 * Only one card is visible at a time, and the container acts
 * like a deck of cards.
 * The ordering of cards is determined by the container's own
 * internal ordering of its component objects. DeckLayout
 * defines a set of methods that allow an application to flip
 * through the cards sequentially, or to show a specified card.
 * The addLayoutComponent method can be used to associate a
 * string identifier with a given card for faster random access.
 *
 * @author Heiko Erhardt
 */
public class DeckLayout
	implements LayoutManager2, Serializable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/**
	 * Table of components.
	 * Maps a component name to the component (Component).
	 */
	private Hashtable comps = new Hashtable();

	/** Wrap around when flipping through the card stack */
	private boolean wrap = false;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public DeckLayout()
	{
	}

	/**
	 * Gets the wrap around when flipping through the card stack.
	 * @nowarn
	 */
	public boolean isWrap()
	{
		return wrap;
	}

	/**
	 * Sets the wrap around when flipping through the card stack.
	 * @nowarn
	 */
	public void setWrap(boolean wrap)
	{
		this.wrap = wrap;
	}

	//////////////////////////////////////////////////
	// @@ LayoutManager2 implementation
	//////////////////////////////////////////////////

	/**
	 * Adds the specified component to this deck layout's internal
	 * table, by name. The object specified by constraints must be
	 * a string. The deck layout stores this string as a key-value
	 * pair that can be used for random access to a particular card.
	 * By calling the show method, an application can display the
	 * component with the specified name.
	 *
	 * @param c The component to be added
	 * @param constraints A name that identifies the component
	 */
	public void addLayoutComponent(Component c, Object constraints)
	{
		if (constraints instanceof String || constraints == null)
		{
			addLayoutComponent((String) constraints, c);
		}
		else
		{
			throw new IllegalArgumentException("cannot add to layout: constraint must be a string");
		}
	}

	/**
	 * Not used any more.
	 * @deprecated replaced by addLayoutComponent(Component, Object)
	 * @nowarn
	 */
	public void addLayoutComponent(String name, Component c)
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Component must have a name");
		}
		if (comps.size() > 0)
		{
			setActive(c, false);
		}
		comps.put(name, c);
	}

	/**
	 * Removes the specified component from the layout.
	 *
	 * @param c The component to be removed
	 */
	public void removeLayoutComponent(Component c)
	{
		Enumeration en = comps.keys();
		while (en.hasMoreElements())
		{
			String key = (String) en.nextElement();
			if (comps.get(key) == c)
			{
				comps.remove(key);
				return;
			}
		}
	}

	/**
	 * Calculates the preferred size for the specified page.
	 *
	 * @param parent The name of the parent container
	 * @return minimum dimensions required to lay out the components
	 */
	public Dimension preferredLayoutSize(Container parent)
	{
		Insets insets = parent.getInsets();
		int ncomponents = parent.getComponentCount();
		int w = 0;
		int h = 0;

		for (int i = 0; i < ncomponents; i++)
		{
			Component c = parent.getComponent(i);
			Dimension d = c.getPreferredSize();
			if (d.width > w)
			{
				w = d.width;
			}
			if (d.height > h)
			{
				h = d.height;
			}
		}
		return new Dimension(insets.left + insets.right + w, insets.top + insets.bottom + h);
	}

	/**
	 * Calculates the minimum size for the specified page.
	 *
	 * @param parent The name of the parent container
	 * @return minimum dimensions required to lay out the components
	 */
	public Dimension minimumLayoutSize(Container parent)
	{
		Insets insets = parent.getInsets();
		int ncomponents = parent.getComponentCount();
		int w = 0;
		int h = 0;

		for (int i = 0; i < ncomponents; i++)
		{
			Component c = parent.getComponent(i);
			Dimension d = c.getMinimumSize();
			if (d.width > w)
			{
				w = d.width;
			}
			if (d.height > h)
			{
				h = d.height;
			}
		}
		return new Dimension(insets.left + insets.right + w, insets.top + insets.bottom + h);
	}

	/**
	 * Returns the maximum dimensions for this layout given
	 * the component in the specified target container.
	 *
	 * @param target The component which needs to be laid out
	 * @return The layout size
	 */
	public Dimension maximumLayoutSize(Container target)
	{
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Lays out the specified container using this deck layout.
	 * Each component in the parent container is reshaped to be
	 * the same size as the container, minus insets, horizontal
	 * and vertical gaps.
	 *
	 * @param parent Container that owns the layout
	 */
	public void layoutContainer(Container parent)
	{
		Insets insets = parent.getInsets();
		int ncomponents = parent.getComponentCount();
		for (int i = 0; i < ncomponents; i++)
		{
			Component c = parent.getComponent(i);
			if (c.isVisible())
			{
				c.setBounds(insets.left, insets.top, parent.getSize().width - (insets.left + insets.right), parent.getSize().height - (insets.top + insets.bottom));
			}
		}
	}

	/**
	 * Returns the alignment along the x axis. This specifies how
	 * the component would like to be aligned relative to other
	 * components. The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 *
	 * @param parent Container that owns the layout
	 * @return The alignment
	 */
	public float getLayoutAlignmentX(Container parent)
	{
		return 0.5f;
	}

	/**
	 * Returns the alignment along the y axis. This specifies how
	 * the component would like to be aligned relative to other
	 * components. The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 *
	 * @param parent Container that owns the layout
	 * @return The alignment
	 */
	public float getLayoutAlignmentY(Container parent)
	{
		return 0.5f;
	}

	/**
	 * Invalidates the layout, indicating that if the layout
	 * manager has cached information it should be discarded.
	 *
	 * @param target Container that owns the layout
	 */
	public void invalidateLayout(Container target)
	{
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Make sure that the Container really has this layout installed,
	 * to avoid serious problems.
	 *
	 * @param parent Container that owns the layout
	 */
	private void checkLayout(Container parent)
	{
		if (parent.getLayout() != this)
		{
			throw new IllegalArgumentException("wrong parent for CardLayout");
		}
	}

	/**
	 * Enable or disable the specified component and all its children.
	 * This makes focus traversal function properly. The side effect
	 * is that all children are enabled or disabled and specific
	 * contexts are not maintained. You can get around this by
	 * intercepting setEnabled in your component to restore state
	 * if this is important in your context.
	 *
	 * @param c Component to make active
	 * @param enabled
	 *		true	Activate the component<br>
	 *		false	Deactivate the component
	 */
	private void setActive(Component c, boolean enabled)
	{
		c.setVisible(enabled);
		c.setEnabled(enabled);
		if (c instanceof Container)
		{
			Container container = (Container) c;
			int count = container.getComponentCount();
			for (int i = 0; i < count; i++)
			{
				setActive(container.getComponent(i), enabled);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Component navigation
	//////////////////////////////////////////////////

	/**
	 * Flips to the first card of the container.
	 *
	 * @param parent Container that owns the layout
	 */
	public void first(Container parent)
	{
		synchronized (parent.getTreeLock())
		{
			checkLayout(parent);
			int ncomponents = parent.getComponentCount();
			for (int i = 0; i < ncomponents; i++)
			{
				Component c = parent.getComponent(i);
				if (c.isVisible())
				{
					setActive(c, false);
					c = parent.getComponent(0);
					setActive(c, true);
					parent.validate();
					return;
				}
			}
		}
	}

	/**
	 * Flips to the next card of the specified container. If the
	 * currently visible card is the last one, this method flips
	 * to the first card in the layout.
	 *
	 * @param parent Container that owns the layout
	 * @return Index of the selected component
	 */
	public int next(Container parent)
	{
		synchronized (parent.getTreeLock())
		{
			checkLayout(parent);
			int ncomponents = parent.getComponentCount();
			for (int i = 0; i < ncomponents; i++)
			{
				Component c = parent.getComponent(i);
				if (c.isVisible())
				{
					if (i + 1 >= ncomponents && !wrap)
						return i;
					int index = (i + 1 < ncomponents) ? i + 1 : 0;
					setActive(c, false);
					c = parent.getComponent(index);
					setActive(c, true);
					parent.validate();
					return index;
				}
			}
			return -1;
		}
	}

	/**
	 * Flips to the previous card of the specified container. If the
	 * currently visible card is the first one, this method flips to
	 * the last card in the layout.
	 *
	 * @param parent Container that owns the layout
	 * @return Index of the selected component
	 */
	public int previous(Container parent)
	{
		synchronized (parent.getTreeLock())
		{
			checkLayout(parent);
			int ncomponents = parent.getComponentCount();
			for (int i = 0; i < ncomponents; i++)
			{
				Component c = parent.getComponent(i);
				if (c.isVisible())
				{
					if (i <= 0 && !wrap)
						return i;
					setActive(c, false);
					int index = (i > 0) ? i - 1 : ncomponents - 1;
					c = parent.getComponent(index);
					setActive(c, true);
					parent.validate();
					return index;
				}
			}
			return -1;
		}
	}

	/**
	 * Flips to the last card of the container.
	 *
	 * @param parent Container that owns the layout
	 */
	public void last(Container parent)
	{
		synchronized (parent.getTreeLock())
		{
			checkLayout(parent);
			int ncomponents = parent.getComponentCount();
			for (int i = 0; i < ncomponents; i++)
			{
				Component c = parent.getComponent(i);
				if (c.isVisible())
				{
					setActive(c, false);
					c = parent.getComponent(ncomponents - 1);
					setActive(c, true);
					parent.validate();
					return;
				}
			}
		}
	}

	/**
	 * Flips to the component that was added to this layout using
	 * the specified name. If no such component exists, nothing happens.
	 *
	 * @param parent Container that owns the layout
	 * @param name The component name
	 */
	public void show(Container parent, String name)
	{
		synchronized (parent.getTreeLock())
		{
			checkLayout(parent);
			Component next = (Component) comps.get(name);
			if ((next != null) && !next.isVisible())
			{
				int ncomponents = parent.getComponentCount();
				for (int i = 0; i < ncomponents; i++)
				{
					Component c = parent.getComponent(i);
					if (c.isVisible())
					{
						setActive(c, false);
						break;
					}
				}
				setActive(next, true);
				parent.validate();
			}
		}
	}

	/**
	 * Flips to the component at the numbered position. If no such
	 * component exists, nothing happens.
	 *
	 * @param parent Container that owns the layout
	 * @param index The index (between 0 and component count - 1)
	 */
	public void show(Container parent, int index)
	{
		synchronized (parent.getTreeLock())
		{
			checkLayout(parent);
			if (index < 0 || index > parent.getComponentCount() - 1)
				return;
			int ncomponents = parent.getComponentCount();
			for (int i = 0; i < ncomponents; i++)
			{
				Component c = parent.getComponent(i);
				if (c.isVisible())
				{
					setActive(c, false);
					c = parent.getComponent(index);
					setActive(c, true);
					parent.validate();
					return;
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Component access
	//////////////////////////////////////////////////

	/**
	 * Gets a component by its name.
	 *
	 * @param name Name of the component
	 * @return The component or null if no such component exists
	 */
	public Component getComponentByName(String name)
	{
		return (Component) comps.get(name);
	}
}
