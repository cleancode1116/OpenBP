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
package org.openbp.jaspira.action.keys;

import java.util.HashMap;
import java.util.Map;

import javax.swing.KeyStroke;

/**
 * Tree over keysequences. Provides for a given key-sequences the number of sub-sequences.
 *
 * @author Stephan Moritz
 */
public class KeyTree
{
	/////////////////////////////////////////////////////////////////////////
	// @@ members
	/////////////////////////////////////////////////////////////////////////

	/** Parent tree of this tree or null if root. */
	private KeyTree parent;

	/** Maps keyStrokes to subtrees. */
	private Map entries;

	/** The sequence that lead to this sub tree. */
	private KeySequence sequence;

	/** How often this tree has been registered. */
	private int refCount;

	/////////////////////////////////////////////////////////////////////////
	// @@ Members
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a root tree.
	 */
	public KeyTree()
	{
		this(null, null);
	}

	/**
	 * Creates a new sub tree with the given parent and keystroke.
	 * @param parent Parent tree or null
	 * @param sequence Remaining key sequence
	 */
	public KeyTree(KeyTree parent, KeySequence sequence)
	{
		this.parent = parent;
		this.sequence = sequence;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Member access
	/////////////////////////////////////////////////////////////////////////

	/**
	 * Adds the given sequence to the tree, creating branches as necessary.
	 * @param sequence Sequence to add
	 */
	public void addSequence(KeySequence sequence)
	{
		// Increase reference count for this tree
		++refCount;

		// Get the depth of this tree, i.e. the distance to the root tree
		int depth = parent != null ? this.sequence.length() : 0;
		if (sequence.length() <= depth)
			return;
		KeyStroke ks = sequence.getKeyAt(depth);

		if (entries == null)
		{
			entries = new HashMap();
		}

		KeyTree sub = (KeyTree) entries.get(ks);
		if (sub == null)
		{
			sub = new KeyTree(this, sequence.getSubSequence(depth + 1));
			entries.put(ks, sub);
		}

		sub.addSequence(sequence);
	}

	/**
	 * Removes a sequence from the tree, deleting obsolete branches.
	 * @param sequence Sequence to remove
	 * @return
	 *		true	There are still references to this tree<br>
	 *		false	No references left, the tree can be removed
	 */
	public boolean removeSequence(KeySequence sequence)
	{
		// Decrease reference count for this tree
		if (--refCount <= 0)
		{
			// This was the last, the whole sub tree can be removed
			return false;
		}

		if (sequence.length() == 0)
		{
			// We have reached the end of the sequence (recursion stop)
			return true;
		}

		KeyTree sub = getSubTree(sequence.getKeyAt(0));

		if (sub != null && !sub.removeSequence(sequence.getSequenceTail()))
		{
			// remove the subtree
			entries.remove(sequence.getKeyAt(0));

			if (entries.isEmpty())
			{
				entries = null;
			}
		}

		return true;
	}

	/**
	 * Returns true if this sub tree has any keys that lead further.
	 * @nowarn
	 */
	public boolean hasChildren()
	{
		return entries != null && !entries.isEmpty();
	}

	/**
	 * Returns the number of possible keys fom here.
	 * @nowarn
	 */
	public int getNumberOfChildren()
	{
		return entries != null ? entries.size() : 0;
	}

	/**
	 * Returns the sub tree for the given keystroke.
	 * @param stroke Key stroke to check
	 * @return The tree or null if the key stroke does not have a sub tree associated
	 */
	public KeyTree getSubTree(KeyStroke stroke)
	{
		return entries != null ? (KeyTree) entries.get(stroke) : null;
	}

	/**
	 * Returns all children key strokes.
	 * @return The child keys (can ben empty, but not null)
	 */
	public KeyStroke [] keys()
	{
		KeyStroke [] keys = new KeyStroke [getNumberOfChildren()];

		if (entries != null)
		{
			entries.keySet().toArray(keys);
		}

		return keys;
	}

	/**
	 * Returns all sub trees.
	 * @return The sub trees (can ben empty, but not null)
	 */
	public KeyTree [] subTrees()
	{
		KeyTree [] keys = new KeyTree [getNumberOfChildren()];

		if (entries != null)
		{
			entries.values().toArray(keys);
		}

		return keys;
	}

	public KeySequence getSequence()
	{
		return sequence;
	}

	public String toString()
	{
		return sequence != null ? sequence.toString() : "*RootTree*";
	}
}
