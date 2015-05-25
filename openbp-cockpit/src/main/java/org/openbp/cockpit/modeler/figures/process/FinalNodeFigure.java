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
package org.openbp.cockpit.modeler.figures.process;

import org.openbp.cockpit.modeler.util.FigureUtil;
import org.openbp.core.model.item.process.FinalNode;
import org.openbp.core.model.item.process.NodeSocket;

/**
 * Graphical representation of a initial node.
 */
public class FinalNodeFigure extends NodeFigure
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Is jump node */
	private boolean isJumpNode;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public FinalNodeFigure()
	{
	}

	//////////////////////////////////////////////////
	// @@ Initialization
	//////////////////////////////////////////////////

	/**
	 * Does nothing, entry and final nodes do not have a separate display element
	 * for their name, they display it via their tag.
	 * @see org.openbp.cockpit.modeler.figures.process.NodeFigure#initTextFigure(String)
	 */
	protected void initTextFigure(String textPosition)
	{
		// Do nothing, name is displayed in the tag.
	}

	/**
	 * Sets up the sockets of the node.
	 * Entry and final nodes have a single socket.
	 */
	protected void initSockets()
	{
		add(new SocketFigure(this, (NodeSocket) node.getSockets().next()));
	}

	//////////////////////////////////////////////////
	// @@ UpdatableFigure implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.cockpit.modeler.figures.process.NodeFigure#updateFigure()
	 */
	public void updateFigure()
	{
		super.updateFigure();

		boolean hasJumpTarget = ((FinalNode) node).getJumpTarget() != null;
		if (hasJumpTarget != isJumpNode)
		{
			// Update the status
			isJumpNode = hasJumpTarget;

			// Jump nodes are represented by different presentation figures than pure final nodes;
			// reinitialize the presentation figure and update the child figures
			FigureUtil.updateSkin(this);
		}
	}
}
