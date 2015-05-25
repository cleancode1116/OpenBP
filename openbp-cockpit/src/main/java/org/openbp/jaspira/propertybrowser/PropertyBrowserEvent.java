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
package org.openbp.jaspira.propertybrowser;

import java.awt.event.KeyEvent;

import org.openbp.jaspira.propertybrowser.editor.PropertyEditor;
import org.openbp.jaspira.propertybrowser.nodes.AbstractNode;

/**
 * Editing event.
 * Fired whenever an event during editing of a property editor occurs
 * that may require interaction with the component that hosts the editor.
 *
 * @author Heiko Erhardt
 */
public class PropertyBrowserEvent
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Editing stopped due to user interaction */
	public static final int UNDEFINED = 0;

	/** Key was pressed */
	public static final int KEY = 1;

	/** Gained editing component focus */
	public static final int SELECTION_CHANGED = 2;

	/** Gained editing component focus */
	public static final int FOCUS_GAINED = 3;

	/** Lost editing component focus */
	public static final int FOCUS_LOST = 4;

	/** A property has been changed */
	public static final int PROPERTY_CHANGED = 5;

	/** A property has been updated */
	public static final int PROPERTY_UPDATED = 6;

	/** An element has been created */
	public static final int ELEMENT_CREATED = 7;

	/** An element has been copied */
	public static final int ELEMENT_COPIED = 8;

	/** An element has been pasted */
	public static final int ELEMENT_PASTED = 9;

	/** An element has been deleted */
	public static final int ELEMENT_DELETED = 10;

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Property browser */
	public PropertyBrowser propertyBrowser;

	/** Property editor */
	public PropertyEditor propertyEditor;

	/** Node */
	public AbstractNode node;

	/** Event code (see the constants of this class) */
	public int eventType;

	/**
	 * Key event associated with the event.
	 * If a key code is not applicable or the event was caused by e. g. a mouse interaction,
	 * the kexEvent will be null.
	 */
	public KeyEvent keyEvent;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param eventType Event code (see the constants of this class)
	 * @param propertyBrowser Property browser
	 * @param node Node
	 */
	public PropertyBrowserEvent(int eventType, PropertyBrowser propertyBrowser, AbstractNode node)
	{
		this.eventType = eventType;
		this.propertyBrowser = propertyBrowser;
		this.node = node;
	}

	/**
	 * Constructor.
	 *
	 * @param eventType Event code (see the constants of this class)
	 * @param propertyEditor Property editor
	 */
	public PropertyBrowserEvent(int eventType, PropertyEditor propertyEditor)
	{
		this.eventType = eventType;
		this.propertyEditor = propertyEditor;
		this.propertyBrowser = propertyEditor.getPropertyBrowser();
		this.node = (AbstractNode) propertyEditor.getOwner();
	}

	/**
	 * Constructor.
	 *
	 * @param eventType Event code (see the constants of this class)
	 * @param propertyEditor Property editor
	 * @param keyEvent Key event that caused the event
	 */
	public PropertyBrowserEvent(int eventType, PropertyEditor propertyEditor, KeyEvent keyEvent)
	{
		this(eventType, propertyEditor);
		this.keyEvent = keyEvent;
	}
}
