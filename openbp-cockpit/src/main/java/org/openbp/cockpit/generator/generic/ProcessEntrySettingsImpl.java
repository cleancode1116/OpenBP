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
package org.openbp.cockpit.generator.generic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.cockpit.generator.GeneratorSettings;
import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.msgcontainer.MsgContainer;
import org.openbp.common.util.CopyUtil;
import org.openbp.common.util.iterator.EmptyIterator;

/**
 * Process entry settings object.
 * This class usually serves as base class for generator settings that contain a list of process entries.
 *
 * @author Heiko Erhardt
 */
public class ProcessEntrySettingsImpl extends GeneratorSettings
	implements ProcessEntrySettings
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Entry list (contains {@link ProcessEntry} objects) */
	private List entryList;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ProcessEntrySettingsImpl()
	{
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		ProcessEntrySettingsImpl src = (ProcessEntrySettingsImpl) source;

		if (copyMode == Copyable.COPY_FIRST_LEVEL || copyMode == Copyable.COPY_DEEP)
		{
			// Create deep clones of collection members
			entryList = (List) CopyUtil.copyCollection(src.entryList, copyMode == Copyable.COPY_DEEP ? CopyUtil.CLONE_VALUES : CopyUtil.CLONE_NONE);
		}
		else
		{
			// Shallow clone
			entryList = src.entryList;
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the entry list.
	 * @return An iterator of {@link ProcessEntry} objects
	 */
	public Iterator getEntries()
	{
		if (entryList == null)
			return EmptyIterator.getInstance();
		return entryList.iterator();
	}

	/**
	 * Adds an entry.
	 * @param entry The entry to add
	 */
	public void addEntry(ProcessEntry entry)
	{
		if (entryList == null)
			entryList = new ArrayList();
		entryList.add(entry);
	}

	/**
	 * Clears the entry list.
	 */
	public void clearEntries()
	{
		entryList = null;
	}

	/**
	 * Gets the entry list.
	 * @return A list of {@link ProcessEntry} objects
	 */
	public List getEntryList()
	{
		return entryList;
	}

	/**
	 * Sets the entry list.
	 * @param entryList A list of {@link ProcessEntry} objects
	 */
	public void setEntryList(List entryList)
	{
		this.entryList = entryList;
	}

	//////////////////////////////////////////////////
	// @@ Serialization support
	//////////////////////////////////////////////////

	/**
	 * This template method is called before the settings object is being serialized.
	 * It can be overridden to implement custom operations.
	 */
	public void beforeSerialization()
	{
		super.beforeSerialization();

		for (Iterator it = getEntries(); it.hasNext();)
		{
			ProcessEntry entry = (ProcessEntry) it.next();

			entry.beforeSerialization(getModel());
		}
	}

	/**
	 * This template method is called after the settings object has been deserialized.
	 * It will add all data members that are not present in the field list to the field list.
	 *
	 * @param msgs Container for error messages
	 */
	public void afterDeserialization(MsgContainer msgs)
	{
		super.afterDeserialization(msgs);

		for (Iterator it = getEntries(); it.hasNext();)
		{
			ProcessEntry entry = (ProcessEntry) it.next();

			entry.afterDeserialization(msgs, getModel());
		}
	}
}
