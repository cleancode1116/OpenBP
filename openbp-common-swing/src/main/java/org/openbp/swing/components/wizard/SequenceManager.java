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
package org.openbp.swing.components.wizard;

/**
 * Sequence manager.
 *
 * @author Heiko Erhardt
 */
public interface SequenceManager
{
	/**
	 * Gets the name of the first element.
	 * @nowarn
	 */
	public String getFirst();

	/**
	 * Sets the name of the first element.
	 * @nowarn
	 */
	public void setFirst(String first);

	/**
	 * Gets the name of the current element.
	 * @nowarn
	 */
	public String getCurrent();

	/**
	 * Sets the name of the current element.
	 * @nowarn
	 */
	public void setCurrent(String current);

	/**
	 * Gets the name of the element that succeeds the current element.
	 *
	 * @return The next element or null if this element does not have a successor
	 */
	public String getNext();

	/**
	 * Gets the name of the element that preceeds the current element.
	 *
	 * @return The next element or null if this element does not have a prdecessor
	 */
	public String getPrevious();

	/**
	 * Gets the name of the element that succeeds the specified element.
	 *
	 * @param name Name of the element or null for the current element
	 * @return The next element or null if this element does not have a successor
	 */
	public String getNext(String name);

	/**
	 * Gets the name of the element that preceeds the specified element.
	 *
	 * @param name Name of the element or null for the current element
	 * @return The next element or null if this element does not have a prdecessor
	 */
	public String getPrevious(String name);

	/**
	 * Chains the current element with the specified element.
	 *
	 * @param link Name of the successor or null if this element is the last element
	 */
	public void chain(String link);

	/**
	 * Chains the name specified elements.
	 *
	 * @param name Name of the element or null for the current element
	 * @param link Name of the successor or null if this element is the last element
	 */
	public void chain(String name, String link);

	/**
	 * Sets the name of the element that succeeds the current element.
	 *
	 * @param link Name of the successor or null if this element is the last element
	 */
	public void setNext(String link);

	/**
	 * Sets the name of the element that succeeds the specified element.
	 *
	 * @param name Name of the element or null for the current element
	 * @param link Name of the successor or null if this element is the last element
	 */
	public void setNext(String name, String link);

	/**
	 * Sets the name of the element that preceeds the current element.
	 *
	 * @param link Name of the predecessor or null if this element is the first element
	 */
	public void setPrevious(String link);

	/**
	 * Sets the name of the element that preceeds the specified element.
	 *
	 * @param name Name of the element or null for the current element
	 * @param link Name of the predecessor or null if this element is the first element
	 */
	public void setPrevious(String name, String link);

	/**
	 * Removes an element from the sequence manager.
	 * The predecessor and the successor of the element will be linked with each other, if any.
	 *
	 * @param name Name of the element or null for the current element
	 */
	public void remove(String name);

	/**
	 * Clears all sequence manager information.
	 * Keeps the first and current references.
	 */
	public void clearSequence();

	/**
	 * Clears all sequence manager information, including the
	 * first and current references.
	 */
	public void clear();
}
