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
package org.openbp.core.model.item.process;

import java.util.List;

import org.openbp.core.MimeTypes;
import org.openbp.core.model.Association;
import org.openbp.core.model.AssociationUtil;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypes;

/**
 * Standard implementation of a process node.
 *
 * @author Heiko Erhardt
 */
public class SubprocessNodeImpl extends MultiSocketNodeImpl
	implements SubprocessNode
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Name of the associated process */
	protected String subprocessName;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Underlying process */
	protected transient ProcessItem subprocess;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SubprocessNodeImpl()
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

		SubprocessNodeImpl src = (SubprocessNodeImpl) source;

		subprocessName = src.subprocessName;
		subprocess = src.subprocess;
	}

	/**
	 * @copy ItemProvider.copyFromItem
	 */
	public void copyFromItem(Item item, int syncFlags)
	{
		ProcessUtil.itemToNode((ProcessItem) item, this, syncFlags, null, false);
	}

	/**
	 * @copy ItemProvider.copyToItem
	 */
	public void copyToItem(Item item, int syncFlags)
	{
		ProcessUtil.nodeToItem(this, (ProcessItem) item, syncFlags, null);
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		return ModelObjectSymbolNames.PROCESS_NODE;
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.maintainReferences
	 */
	public void maintainReferences(int flag)
	{
		super.maintainReferences(flag);

		if (getProcess() != null)
		{
			if ((flag & RESOLVE_GLOBAL_REFS) != 0)
			{
				// Complex type, forward to model manager
				try
				{
					subprocess = (ProcessItem) getProcess().resolveItemRef(subprocessName, ItemTypes.PROCESS);
				}
				catch (ModelException e)
				{
					getModelMgr().getMsgContainer().addMsg(this, "Cannot resolve sub process reference.", new Object[]
					{
						e
					});
				}
			}

			if ((flag & SYNC_GLOBAL_REFNAMES) != 0)
			{
				if (subprocess != null)
				{
					subprocessName = getProcess().determineItemRef(subprocess);
				}
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Associations
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.getAssociations
	 */
	public List getAssociations()
	{
		List associations = null;

		int pos = - 1;
		if (subprocess != null)
		{
			associations = AssociationUtil.addAssociations(associations, - 1, subprocess.getAssociations());

			// Insert after (Editor) menu item, which comes first in the associations of the underlying object
			pos = 1;
		}

		associations = AssociationUtil.addAssociation(associations, pos, "Process", subprocess, this, new String[]
		{
			MimeTypes.ITEM
		}, Association.NORMAL, "There is no sub process associated with this node.");

		return associations;
	}

	//////////////////////////////////////////////////
	// @@ Info text
	//////////////////////////////////////////////////

	/**
	 * Gets text that can be used to display this object.
	 *
	 * @return The display text (should usually not be null)
	 */
	public String getDisplayText()
	{
		String text = getDisplayName();
		if (text == null && subprocess != null)
		{
			// Use the underlying process's display text
			text = subprocess.getDisplayName();
		}
		if (text == null)
		{
			text = getName();
		}
		return text;
	}

	/**
	 * Gets text that describes the object.
	 * This can be the regular description (getDescription method) of the object
	 * or the description of an underlying object.
	 *
	 * @return The description text or null if there is no description
	 */
	public String getDescriptionText()
	{
		String text = super.getDescription();
		if (text == null && subprocess != null)
		{
			// Use the underlying sub process' display text
			text = subprocess.getDescription();
		}
		return text;
	}

	//////////////////////////////////////////////////
	// @@ Property access: Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the associated process.
	 * @nowarn
	 */
	public String getSubprocessName()
	{
		return subprocessName;
	}

	/**
	 * Sets the name of the associated process.
	 * @nowarn
	 */
	public void setSubprocessName(String subprocessName)
	{
		this.subprocessName = subprocessName;
	}

	/**
	 * Gets the underlying process.
	 * @nowarn
	 */
	public ProcessItem getSubprocess()
	{
		return subprocess;
	}

	/**
	 * Sets the underlying process.
	 * @nowarn
	 */
	public void setSubprocess(ProcessItem subprocess)
	{
		this.subprocess = subprocess;
	}
}
