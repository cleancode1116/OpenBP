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
package org.openbp.core.model;

import java.util.Iterator;
import java.util.List;

import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.PrintNameProvider;
import org.openbp.common.generic.description.DisplayObject;
import org.openbp.common.generic.taggedvalue.TaggedValue;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.modelmgr.ModelMgr;

/**
 * This interface defines methods common to all OpenBP model objects.
 * A model object is an item or a part of an item
 * (e.g. a node, a link, a socket, a data type or data member etc.).
 *
 * @author Heiko Erhardt
 */
public interface ModelObject
	extends DisplayObject, Cloneable, Copyable, PrintNameProvider
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Operation mode of {@link #maintainReferences}: Update reference names of references to local objects from the actual reference */
	public static final int SYNC_LOCAL_REFNAMES = (1 << 0);

	/** Operation mode of {@link #maintainReferences}: Resolve references to local objects from the reference names */
	public static final int RESOLVE_LOCAL_REFS = (1 << 1);

	/** Operation mode of {@link #maintainReferences}: Update reference names of references to external items from the actual reference */
	public static final int SYNC_GLOBAL_REFNAMES = (1 << 2);

	/** Operation mode of {@link #maintainReferences}: Resolve references to external items from the reference names */
	public static final int RESOLVE_GLOBAL_REFS = (1 << 3);

	/** Operation mode of {@link #maintainReferences}: Validate the object */
	public static final int VALIDATE_BASIC = (1 << 5);

	/**
	 * Operation mode of {@link #validate}: Perform checks for completeness of attributes that
	 * are required to use an object an a production environment
	 */
	public static final int VALIDATE_RUNTIME = (1 << 6);

	/** Operation mode of {@link #maintainReferences}: Instantiate item classes */
	public static final int INSTANTIATE_ITEM = (1 << 7);

	/** Operation mode of {@link #maintainReferences}: Remove the link to the graphical representation object */
	public static final int UNLINK_FROM_REPRESENTATION = (1 << 8);

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Gets the qualified name of this model object.
	 * @nowarn
	 */
	public ModelQualifier getQualifier();

	/**
	 * Gets the model the item belongs to.
	 * @nowarn
	 */
	public abstract Model getOwningModel();

	/**
	 * Gets the model manager that loaded this model.
	 * @nowarn
	 */
	public ModelMgr getModelMgr();

	/**
	 * Gets the children (direct subordinates) of this object.
	 * By default, the method returns null.
	 *
	 * @return The list of objects or null if the object does not have children.
	 * Note that the list may contain objects of different type.
	 */
	public List getChildren();

	/**
	 * Gets the container object (i. e. the parent) of this object.
	 *
	 * @return The container object or null if this object doesn't have a container.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public ModelObject getContainer();

	/**
	 * Gets an iterator of the children of the container this object belongs to.
	 * This can be used to check on name clashes between objects of this type.
	 * By default, the method returns null.
	 *
	 * @return The iterator if this object is part of a collection or a map.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public Iterator getContainerIterator();

	/**
	 * Gets the read-only property.
	 * @nowarn
	 */
	public boolean isReadOnly();

	/**
	 * Checks if this object can be modified (i\.e\. if itself and its model is not read-only).
	 * @nowarn
	 */
	public boolean isModifiable();

	//////////////////////////////////////////////////
	// @@ Design time attribute support implementation
	//////////////////////////////////////////////////

	/**
	 * Gets a design time attribute value.
	 *
	 * @param key Name of the attribute
	 * @return The value of the attribute or null if no such attribute exists
	 */
	public Object getDesignTimeAttribute(String key);

	/**
	 * Sets a design time attribute value.
	 *
	 * @param key Name of the attribute
	 * @param value Value of the attribute
	 */
	public void setDesignTimeAttribute(String key, Object value);

	/**
	 * Removes a design time attribute.
	 *
	 * @param key Name of the attribute
	 */
	public void removeDesignTimeAttribute(String key);

	/**
	 * Gets the design time attribute list.
	 * @return A list of {@link TaggedValue} objects or null
	 */
	public List getDesignTimeAttributeList();

	//////////////////////////////////////////////////
	// @@ Tagged values
	//////////////////////////////////////////////////

	/**
	 * Gets the tagged value list.
	 * @return An iterator of {@link TaggedValue} objects
	 */
	public Iterator getTaggedValues();

	/**
	 * Gets a tagged value by its collection index.
	 *
	 * @param index Collection index
	 * @return The tagged value
	 */
	public TaggedValue getTaggedValue(int index);

	/**
	 * Adds a tagged value.
	 * @param taggedValue The tagged value to add
	 */
	public void addTaggedValue(TaggedValue taggedValue);

	/**
	 * Clears the tagged value list.
	 */
	public void clearTaggedValues();

	/**
	 * Gets the tagged value list.
	 * @return A list of {@link TaggedValue} objects
	 */
	public List getTaggedValueList();

	/**
	 * Sets the tagged value list.
	 * @param taggedValueList A list of {@link TaggedValue} objects
	 */
	public void setTaggedValueList(List taggedValueList);

	/**
	 * Gets the value of a tagged value.
	 *
	 * @param name Name of the attribute
	 * @return The value or null if no such attribute exists
	 */
	public String getTaggedValue(String name);

	/**
	 * Gets the temporary reference (used by the modeler).
	 * This value is not copied by the copyFrom method or the clone method.
	 * @nowarn
	 */
	public Object getTmpReference();

	/**
	 * Sets the temporary reference (used by the modeler).
	 * This value is not copied by the copyFrom method or the clone method.
	 * @nowarn
	 */
	public void setTmpReference(Object tmpReference);

	//////////////////////////////////////////////////
	// @@ Info text
	//////////////////////////////////////////////////

	/**
	 * Gets informational text about the object.
	 * The text can be used to e. g. display a tool tip that describes the object.
	 *
	 * @return An array of strings that make up the information text.<br>
	 * Each array element corresponds to a paragraph that should be displayed.
	 * A paragraph may contain newline ('\n') and tab ('\t') characters that
	 * should be interpreted by the user interface.
	 */
	public String[] getInfoText();

	/**
	 * Gets text that describes the object.
	 * This can be the regular description (getDescription method) of the object
	 * or the description of an underlying object.
	 *
	 * @return The description text or null if there is no description
	 */
	public String getDescriptionText();

	/**
	 * Gets the technical name of this type of model object.
	 *
	 * @return The name or null
	 */
	public String getModelObjectTypeName();

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName();

	//////////////////////////////////////////////////
	// @@ Reference processing/validation
	//////////////////////////////////////////////////

	/**
	 * Checks if the object is valid.
	 * The object is invalid if no object name has been specified.
	 * Any errors will be logged to the message container of this object.
	 * @param flag Flag that inidicates which validations to perform.
	 * Some validations may be performed always, some only if a particular flag is set.
	 * For a list of possible bit values, see the constants of this class.
	 * @return
	 *		true	The object is valid.<br>
	 *		false	Errors were found within the object or its sub objects.
	 */
	public boolean validate(int flag);

	/**
	 * Maintains the references from this object to other objects and the hierarchy
	 * links of this object to its parent objects.
	 *
	 * References consist of two parts:<br>
	 * - A name (qualified name) of the referenced object<br>
	 * - A link (reference) to the object
	 *
	 * The method can establish the link according to the reference name or
	 * determine the reference name from an existing link.<br>
	 * The first option should be executed after the object has been loaded
	 * from persistent storage (or received from an RMI connection). In this case,
	 * a validation (see {@link #validate}) will also be performed.<br>
	 * The latter one should be performed before the object is being saved to persistent
	 * storage or sent over an RMI connection.
	 *
	 * The method will also link this object to its parent object.
	 * This should be done after the object has been received over an RMI
	 * connection or to repair the hierarchy links.
	 *
	 * @param flag Flag that determines the operation of the method.
	 * Can be any combination of integer constants of the {@link ModelObject} interface.
	 */
	public void maintainReferences(int flag);

	//////////////////////////////////////////////////
	// @@ Associations
	//////////////////////////////////////////////////

	/**
	 * Gets a list of objects associated with this item.
	 * Associations depend on the item type and can be e. g. source files,
	 * xml files, templates items, visual files, icon items, icon files etc.
	 * Each association will be packed in an {@link Association} object.
	 * The method will return the association as specified in the item definition,
	 * e. g. "icon/Abc16.png" for an icon file or "ViewOrder" for a visual item.
	 *
	 * If the association is a file, use the {@link Item#resolveFileRef} method in order
	 * to get a valid path to the file.<br>
	 * If the association is an item itself, use the {@link Item#resolveItemRef} method in order
	 * to get the item.<br>
	 *
	 * If the item supports a particular association, but no concrete association has
	 * been defined, the association object contains an Exception object.
	 * The exception message will give a hint on how to create this association.
	 *
	 * @return A list of {@link Association} objects or null if the item
	 * does not support file associations.<br>
	 * Note that the associated files or items may not exist yet, however they have been
	 * specified by the user in the item definition.
	 */
	public List getAssociations();
}
