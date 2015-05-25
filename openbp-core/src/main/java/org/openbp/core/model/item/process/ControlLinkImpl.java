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

import java.util.Iterator;

import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;

/**
 * Standard implementation of a control link.
 *
 * @author Heiko Erhardt
 */
public class ControlLinkImpl extends ProcessObjectImpl
	implements ControlLink
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Transaction control: No transaction control */
	public static final String STR_TA_NONE = "none";

	/** Transaction control: Begin transaction */
	public static final String STR_TA_BEGIN = "begin";

	/** Transaction control: Commit transaction */
	public static final String STR_TA_COMMIT = "commit";

	/** Transaction control: Commit transaction and begin new one*/
	public static final String STR_TA_COMMIT_BEGIN = "commit-begin";

	/** Transaction control: Rollback transaction */
	public static final String STR_TA_ROLLBACK = "rollback";

	/** Transaction control: Rollback transaction and begin new one */
	public static final String STR_TA_ROLLBACK_BEGIN = "rollback-begin";

	/** Transaction control: Default behaviour */
	public static final String STR_TA_DEFAULT = "default";

	/** Rollback behaviour: Update process variables */
	public static final String STR_RBV_UPDATE_VARIABLES = "update-variables";

	/** Rollback behaviour: Add new variables|add-variables */
	public static final String STR_RBV_ADD_VARIABLES = "add-variables";

	/** Rollback behaviour: Restore process variables */
	public static final String STR_RBV_RESTORE_VARIABLES= "restore-variables";

	/** Rollback behaviour: Maintain current position */
	public static final String STR_RBP_MAINTAIN_POSITION = "maintain-position";

	/** Rollback behaviour: Restore current position */
	public static final String STR_RBP_RESTORE_POSITION = "restore-position";

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Source node socket name ("node.socket", may not be null) */
	private String sourceSocketName;

	/** Target node socket name ("node.socket", may not be null) */
	private String targetSocketName;

	/** Geometry information (required by the Modeler) */
	private String geometry;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Source node socket (may not be null) */
	private transient NodeSocket sourceSocket;

	/** Target node socket (may not be null) */
	private transient NodeSocket targetSocket;

	/** Process the link belongs to (may not be null) */
	private transient ProcessItem process;

	/** Transaction control */
	private int transactionControl = TA_NONE;

	/** Rollback data behavior */
	private int rollbackDataBehavior = RBV_UPDATE_VARIABLES;

	/** Rollback position behavior */
	private int rollbackPositionBehavior = RBP_MAINTAIN_POSITION;


	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ControlLinkImpl()
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

		ControlLinkImpl src = (ControlLinkImpl) source;

		sourceSocketName = src.sourceSocketName;
		targetSocketName = src.targetSocketName;
		geometry = src.geometry;

		sourceSocket = src.sourceSocket;
		targetSocket = src.targetSocket;
		process = src.process;
		transactionControl = src.transactionControl;
		rollbackDataBehavior = src.rollbackDataBehavior;
		rollbackPositionBehavior = src.rollbackPositionBehavior;
	}

	/**
	 * Gets the reference to the object.
	 * @return The qualified name
	 */
	public ModelQualifier getQualifier()
	{
		return new ModelQualifier(getProcess(), getName());
	}

	//////////////////////////////////////////////////
	// @@ ModelObject overrides
	//////////////////////////////////////////////////

	/**
	 * Gets text that can be used to display this object.
	 *
	 * @nowarn
	 */
	public String getDisplayText()
	{
		String text = getDisplayName();
		if (text != null)
			return text;

		return "";
	}

	/**
	 * Gets the container object (i. e. the parent) of this object.
	 *
	 * @return The container object or null if this object doesn't have a container.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public ModelObject getContainer()
	{
		return process;
	}

	/**
	 * Gets an iterator of the children of the container this object belongs to.
	 * This can be used to check on name clashes between objects of this type.
	 * By default, the method returns null.
	 *
	 * @return The iterator if this object is part of a collection or a map.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public Iterator getContainerIterator()
	{
		return process.getControlLinks();
	}

	//////////////////////////////////////////////////
	// @@ ProcessObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the process the object belongs to.
	 * @nowarn
	 */
	public ProcessItem getProcess()
	{
		return process;
	}

	/**
	 * Sets the process the object belongs to.
	 * @nowarn
	 */
	public void setProcess(ProcessItem process)
	{
		this.process = process;
	}

	/**
	 * Gets the partially qualified name of the object relative to the process.
	 * @nowarn
	 */
	public String getProcessRelativeName()
	{
		return getName();
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

		if (process != null)
		{
			if ((flag & SYNC_LOCAL_REFNAMES) != 0)
			{
				sourceSocketName = sourceSocket != null ? sourceSocket.getProcessRelativeName() : null;
				targetSocketName = targetSocket != null ? targetSocket.getProcessRelativeName() : null;
			}

			if ((flag & RESOLVE_LOCAL_REFS) != 0)
			{
				// Link the source and target sockets to the node
				if (sourceSocketName == null)
				{
					getModelMgr().getMsgContainer().addMsg(this, "Missing source socket name for control link $0.", new Object [] { getName() });
				}
				else
				{
					sourceSocket = process.getSocketByName(sourceSocketName);
					if (sourceSocket == null)
					{
						getModelMgr().getMsgContainer().addMsg(this, "Node socket $0 not found.", new Object [] { sourceSocketName });
					}
					else
					{
						sourceSocket.addControlLink(this);
					}
				}

				if (targetSocketName == null)
				{
					getModelMgr().getMsgContainer().addMsg(this, "Missing target socket name for control link $0.", new Object [] { getName() });
				}
				else
				{
					targetSocket = process.getSocketByName(targetSocketName);
					if (targetSocket == null)
					{
						getModelMgr().getMsgContainer().addMsg(this, "Node socket $0 not found.", new Object [] { targetSocketName });
					}
					else
					{
						targetSocket.addControlLink(this);
					}
				}

				if (sourceSocket == null || targetSocket == null)
				{
					// Invalid link - remove from the process
					process.removeControlLink(this);
				}
			}
		}
	}

	/**
	 * @copy ModelObject.validate
	 */
	public boolean validate(int flag)
	{
		// Check for an object name first
		boolean success = super.validate(flag);

		if (sourceSocketName == null)
		{
			getModelMgr().getMsgContainer().addMsg(this, "No source node socket name specified.");
			success = false;
		}

		if (targetSocketName == null)
		{
			getModelMgr().getMsgContainer().addMsg(this, "No target node socket name specified.");
			success = false;
		}

		return success;
	}

	//////////////////////////////////////////////////
	// @@ Linking to sockets
	//////////////////////////////////////////////////

	/**
	 * Links the connection to a source and a target socket.
	 *
	 * @param sourceSocket Source node socket (may not be null)
	 * @param targetSocket Target node socket (may not be null)
	 */
	public void link(NodeSocket sourceSocket, NodeSocket targetSocket)
	{
		this.sourceSocket = sourceSocket;
		this.targetSocket = targetSocket;

		// Add the link to the socket's link list
		sourceSocket.addControlLink(this);
		targetSocket.addControlLink(this);
	}

	/**
	 * Unlinks the connection from the source and the target sockets.
	 */
	public void unlink()
	{
		// Remove the link from the socket's link list
		if (sourceSocket != null)
			sourceSocket.removeControlLink(this);
		if (targetSocket != null)
			targetSocket.removeControlLink(this);

		sourceSocket = null;
		targetSocket = null;
	}

	/**
	 * Checks if a link can be established between the given sockets.
	 *
	 * @param sourceSocket Source node socket (may not be null)
	 * @param targetSocket Target node socket (may not be null)
	 * @return The return code determines if the link is possible and is one of the following constants:<br>
	 * {@link ControlLink#CANNOT_LINK}/{@link ControlLink#CAN_LINK}|{@link ControlLink#REVERSE_LINK})
	 */
	public static int canLink(NodeSocket sourceSocket, NodeSocket targetSocket)
	{
		boolean reverse = false;

		// Check if the direction of the link is correct
		boolean isSourceEntry = sourceSocket.isEntrySocket();
		boolean isTargetEntry = targetSocket.isEntrySocket();

		if (isSourceEntry == isTargetEntry)
		{
			// Cannot connect entry to entry or exit to exit
			return CANNOT_LINK;
		}

		if (isSourceEntry)
		{
			// We must reverse the link
			reverse = true;
		}

		if (reverse)
		{
			// Swap the types, so we can check if the reverse link is possible
			NodeSocket tmp = sourceSocket;
			sourceSocket = targetSocket;
			targetSocket = tmp;
		}

		// We can connect from the source socket if there is no control link attached to it
		// or if the node of the socket supports multiple exit links
		if (sourceSocket.hasControlLinks() && !sourceSocket.getNode().isMultiExitLinkNode())
		{
			// No more than one outgoing control link per source socket (except for actor nodes!)
			/*
			 if (! (sourceSocket.getNode () instanceof ActorNode))
			 {
			 return CANNOT_LINK;
			 }
			 */
			return CANNOT_LINK;
		}

		if (reverse)
			return CAN_LINK | REVERSE_LINK;
		return CAN_LINK;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the source node socket name ("node.socket").
	 * @nowarn
	 */
	public String getSourceSocketName()
	{
		return sourceSocketName;
	}

	/**
	 * Sets the source node socket name ("node.socket").
	 * @nowarn
	 */
	public void setSourceSocketName(String sourceSocketName)
	{
		this.sourceSocketName = sourceSocketName;
	}

	/**
	 * Gets the target node socket name ("node.socket").
	 * @nowarn
	 */
	public String getTargetSocketName()
	{
		return targetSocketName;
	}

	/**
	 * Sets the target node socket name ("node.socket").
	 * @nowarn
	 */
	public void setTargetSocketName(String targetSocketName)
	{
		this.targetSocketName = targetSocketName;
	}

	/**
	 * Gets the source node socket.
	 * @nowarn
	 */
	public NodeSocket getSourceSocket()
	{
		return sourceSocket;
	}

	/**
	 * Sets the source node socket.
	 * @nowarn
	 */
	public void setSourceSocket(NodeSocket sourceSocket)
	{
		this.sourceSocket = sourceSocket;
	}

	/**
	 * Gets the target node socket.
	 * @nowarn
	 */
	public NodeSocket getTargetSocket()
	{
		return targetSocket;
	}

	/**
	 * Sets the target node socket.
	 * @nowarn
	 */
	public void setTargetSocket(NodeSocket targetSocket)
	{
		this.targetSocket = targetSocket;
	}

	/**
	 * Gets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getGeometry()
	{
		return geometry;
	}

	/**
	 * Sets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setGeometry(String geometry)
	{
		this.geometry = geometry;
	}

	/**
	 * Gets the transaction control.
	 * @return {@link ControlLink#TA_NONE}/{@link ControlLink#TA_BEGIN}/{@link ControlLink#TA_COMMIT}/{@link ControlLink#TA_COMMIT_BEGIN}/{@link ControlLink#TA_ROLLBACK}/{@link ControlLink#TA_ROLLBACK_BEGIN}
	 */
	public int getTransactionControl()
	{
		return transactionControl;
	}

	/**
	 * Sets the transaction control.
	 * @param transactionControl {@link ControlLink#TA_NONE}/{@link ControlLink#TA_BEGIN}/{@link ControlLink#TA_COMMIT}/{@link ControlLink#TA_COMMIT_BEGIN}/{@link ControlLink#TA_ROLLBACK}/{@link ControlLink#TA_ROLLBACK_BEGIN}
	 */
	public void setTransactionControl(int transactionControl)
	{
		this.transactionControl = transactionControl;
	}

	/**
	 * Gets the transaction control as string.
	 * @nowarn
	 */
	public String getTransactionControlStr()
	{
		String ret = null;
		switch (transactionControl)
		{
		case TA_BEGIN:
			ret = STR_TA_BEGIN;
			break;

		case TA_COMMIT:
			ret = STR_TA_COMMIT;
			break;

		case TA_COMMIT_BEGIN:
			ret = STR_TA_COMMIT_BEGIN;
			break;

		case TA_ROLLBACK:
			ret = STR_TA_ROLLBACK;
			break;

		case TA_ROLLBACK_BEGIN:
			ret = STR_TA_ROLLBACK_BEGIN;
			break;

		default:
		}
		return ret;
	}

	/**
	 * Sets the transaction control as string.
	 * @nowarn
	 */
	public void setTransactionControlStr(String transactionControlStr)
	{
		transactionControl = TA_NONE;

		if (transactionControlStr != null)
		{
			if (transactionControlStr.equals(STR_TA_BEGIN))
				transactionControl = TA_BEGIN;
			else if (transactionControlStr.equals(STR_TA_COMMIT))
				transactionControl = TA_COMMIT;
			else if (transactionControlStr.equals(STR_TA_COMMIT_BEGIN))
				transactionControl = TA_COMMIT_BEGIN;
			else if (transactionControlStr.equals(STR_TA_ROLLBACK))
				transactionControl = TA_ROLLBACK;
			else if (transactionControlStr.equals(STR_TA_ROLLBACK_BEGIN))
				transactionControl = TA_ROLLBACK_BEGIN;
		}
	}

	/**
	 * Gets the rollback data behavior.
	 * @return {@link ControlLink#RBV_UPDATE_VARIABLES}/{@link ControlLink#RBV_ADD_VARIABLES}/{@link ControlLink#RBV_RESTORE_VARIABLES}
	 */
	public int getRollbackDataBehavior()
	{
		return rollbackDataBehavior;
	}

	/**
	 * Sets the rollback data behavior.
	 * @param rollbackDataBehavior {@link ControlLink#RBV_UPDATE_VARIABLES}/{@link ControlLink#RBV_ADD_VARIABLES}/{@link ControlLink#RBV_RESTORE_VARIABLES}
	 */
	public void setRollbackDataBehavior(int rollbackDataBehavior)
	{
		this.rollbackDataBehavior = rollbackDataBehavior;
	}

	/**
	 * Gets the rollback position behavior.
	 * @return {@link ControlLink#RBP_MAINTAIN_POSITION}/{@link ControlLink#RBP_RESTORE_POSITION}
	 */
	public int getRollbackPositionBehavior()
	{
		return rollbackPositionBehavior;
	}

	/**
	 * Sets the rollback position behavior.
	 * @param rollbackPositionBehavior {@link ControlLink#RBP_MAINTAIN_POSITION}/{@link ControlLink#RBP_RESTORE_POSITION}
	 */
	public void setRollbackPositionBehavior(int rollbackPositionBehavior)
	{
		this.rollbackPositionBehavior = rollbackPositionBehavior;
	}

	/**
	 * Gets the rollback data behavior as string.
	 * @nowarn
	 */
	public String getRollbackDataBehaviorStr()
	{
		String ret = null;
		switch (rollbackDataBehavior)
		{
		case RBV_UPDATE_VARIABLES:
			ret = null;
			break;

		case RBV_ADD_VARIABLES:
			ret = STR_RBV_ADD_VARIABLES;
			break;

		case RBV_RESTORE_VARIABLES:
			ret = STR_RBV_RESTORE_VARIABLES;
			break;

		default:
		}
		return ret;
	}

	/**
	 * Sets the rollback data behavior as string.
	 * @nowarn
	 */
	public void setRollbackDataBehaviorStr(String rollbackDataBehaviorStr)
	{
		rollbackDataBehavior = RBV_UPDATE_VARIABLES;

		if (rollbackDataBehaviorStr != null)
		{
			if (rollbackDataBehaviorStr.equals(STR_RBV_ADD_VARIABLES))
				rollbackDataBehavior = RBV_ADD_VARIABLES;
			else if (rollbackDataBehaviorStr.equals(STR_RBV_RESTORE_VARIABLES))
				rollbackDataBehavior = RBV_RESTORE_VARIABLES;
		}
	}

	/**
	 * Gets the rollback position behavior as string.
	 * @nowarn
	 */
	public String getRollbackPositionBehaviorStr()
	{
		String ret = null;
		switch (rollbackPositionBehavior)
		{
		case RBP_MAINTAIN_POSITION:
			ret = null;
			break;

		case RBP_RESTORE_POSITION:
			ret = STR_RBP_RESTORE_POSITION;
			break;

		default:
		}
		return ret;
	}

	/**
	 * Sets the rollback position behavior as string.
	 * @nowarn
	 */
	public void setRollbackPositionBehaviorStr(String rollbackPositionBehaviorStr)
	{
		rollbackPositionBehavior = RBP_MAINTAIN_POSITION;

		if (rollbackPositionBehaviorStr != null)
		{
			if (rollbackPositionBehaviorStr.equals(STR_RBP_RESTORE_POSITION))
				rollbackPositionBehavior = RBP_RESTORE_POSITION;
		}
	}
}
