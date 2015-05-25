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


/**
 * A control link connects an exit socket of a node to an entry socket of
 * (presumably another) node.
 * This link resembles the flow of control in a process.
 *
 * @author Heiko Erhardt
 */
public interface ControlLink
	extends ProcessObject
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/**
	 * Transaction control: No transaction control.
	 */
	public static final int TA_NONE = 0;

	/**
	 * Transaction control: Begin transaction.
	 */
	public static final int TA_BEGIN = 1;

	/**
	 * Transaction control: Commit transaction.
	 */
	public static final int TA_COMMIT = 2;

	/**
	 * Transaction control: Commit transaction and begin new one.
	 */
	public static final int TA_COMMIT_BEGIN = 3;

	/**
	 * Transaction control: Rollback transaction.
	 */
	public static final int TA_ROLLBACK = 4;

	/**
	 * Transaction control: Rollback transaction and begin new one.
	 */
	public static final int TA_ROLLBACK_BEGIN = 5;

	/**
	 * Rollback behaviour: Update process variables.
	 * Updates the process variables of the rolled-back context with the current values.
	 */
	public static final int RBV_UPDATE_VARIABLES = RollbackDataBehavior.UPDATE_VARIABLES;

	/**
	 * Rollback behaviour: Add new variables|add-variables.
	 * Only adds process variables that are new to the process variables of the rolled-back context.
	 */
	public static final int RBV_ADD_VARIABLES = RollbackDataBehavior.ADD_VARIABLES;

	/**
	 * Rollback behaviour: Restore process variables.
	 * Restores all persistent process variables of the rolled-back token context. Non-persistent variables will be deleted from the context.
	 */
	public static final int RBV_RESTORE_VARIABLES= RollbackDataBehavior.RESTORE_VARIABLES;

	/**
	 * Rollback behaviour: Maintain current position.
	 * The position remains unchanged.
	 */
	public static final int RBP_MAINTAIN_POSITION = RollbackPositionBehavior.MAINTAIN_POSITION;

	/**
	 * Rollback behaviour: Restore current position.
	 * The position of the token context will be restored to the position after the last commit.
	 */
	public static final int RBP_RESTORE_POSITION = RollbackPositionBehavior.RESTORE_POSITION;

	/**
	 * Return value of {@link ControlLinkImpl#canLink}:
	 * There already is an outgoing link from the source socket.
	 */
	public static final int CANNOT_LINK = 0;

	/**
	 * Return value of {@link ControlLinkImpl#canLink}:
	 * The link can be established.
	 */
	public static final int CAN_LINK = 1;

	/**
	 * Return value of {@link ControlLinkImpl#canLink}:
	 * The link can be established, but needs to be reversed
	 */
	public static final int REVERSE_LINK = (1 << 8);

	//////////////////////////////////////////////////
	// @@ Linking to parameters
	//////////////////////////////////////////////////

	/**
	 * Links the connection to a source and a target socket.
	 *
	 * @param sourceSocket Source node socket (may not be null)
	 * @param targetSocket Target node socket (may not be null)
	 */
	public void link(NodeSocket sourceSocket, NodeSocket targetSocket);

	/**
	 * Unlinks the connection from the source and the target sockets.
	 */
	public void unlink();

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the source node socket name ("node.socket").
	 * @nowarn
	 */
	public String getSourceSocketName();

	/**
	 * Sets the source node socket name ("node.socket").
	 * @nowarn
	 */
	public void setSourceSocketName(String sourceSocketName);

	/**
	 * Gets the target node socket name ("node.socket").
	 * @nowarn
	 */
	public String getTargetSocketName();

	/**
	 * Sets the target node socket name ("node.socket").
	 * @nowarn
	 */
	public void setTargetSocketName(String targetSocketName);

	/**
	 * Gets the source node socket.
	 * @nowarn
	 */
	public NodeSocket getSourceSocket();

	/**
	 * Sets the source node socket.
	 * @nowarn
	 */
	public void setSourceSocket(NodeSocket sourceSocket);

	/**
	 * Gets the target node socket.
	 * @nowarn
	 */
	public NodeSocket getTargetSocket();

	/**
	 * Sets the target node socket.
	 * @nowarn
	 */
	public void setTargetSocket(NodeSocket targetSocket);

	/**
	 * Gets the process the node belongs to.
	 * @nowarn
	 */
	public ProcessItem getProcess();

	/**
	 * Sets the process the node belongs to.
	 * @nowarn
	 */
	public void setProcess(ProcessItem process);

	/**
	 * Gets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getGeometry();

	/**
	 * Sets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setGeometry(String geometry);

	/**
	 * Gets the transaction control.
	 * @return {@link #TA_NONE}/{@link #TA_BEGIN}/{@link #TA_COMMIT}/{@link #TA_COMMIT_BEGIN}/{@link #TA_ROLLBACK}/{@link #TA_ROLLBACK_BEGIN}
	 */
	public int getTransactionControl();

	/**
	 * Sets the transaction control.
	 * @param transactionControl {@link #TA_NONE}/{@link #TA_BEGIN}/{@link #TA_COMMIT}/{@link #TA_COMMIT_BEGIN}/{@link #TA_ROLLBACK}/{@link #TA_ROLLBACK_BEGIN}
	 */
	public void setTransactionControl(int transactionControl);

	/**
	 * Gets the rollback data behavior.
	 * @return {@link ControlLink#RBV_UPDATE_VARIABLES}/{@link ControlLink#RBV_ADD_VARIABLES}/{@link ControlLink#RBV_RESTORE_VARIABLES}
	 */
	public int getRollbackDataBehavior();

	/**
	 * Sets the rollback data behavior.
	 * @param rollbackDataBehavior {@link ControlLink#RBV_UPDATE_VARIABLES}/{@link ControlLink#RBV_ADD_VARIABLES}/{@link ControlLink#RBV_RESTORE_VARIABLES}
	 */
	public void setRollbackDataBehavior(int rollbackDataBehavior);

	/**
	 * Gets the rollback position behavior.
	 * @return {@link ControlLink#RBP_MAINTAIN_POSITION}/{@link ControlLink#RBP_RESTORE_POSITION}
	 */
	public int getRollbackPositionBehavior();

	/**
	 * Sets the rollback position behavior.
	 * @param rollbackPositionBehavior {@link ControlLink#RBP_MAINTAIN_POSITION}/{@link ControlLink#RBP_RESTORE_POSITION}
	 */
	public void setRollbackPositionBehavior(int rollbackPositionBehavior);
}
