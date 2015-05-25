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
package org.openbp.cockpit.plugins.toolbox;

import java.awt.event.KeyEvent;

import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.NodeSocketImpl;
import org.openbp.core.model.item.process.ProcessItemImpl;
import org.openbp.core.model.item.process.ProcessUtil;
import org.openbp.core.model.item.process.TextElementImpl;

/**
 * This is a Standard ToolBox with all standard entrys.
 *
 * @author Jens Ferchland
 */
public class StandardToolBoxPlugin extends ToolBoxPlugin
{
	/**
	 * @see org.openbp.cockpit.plugins.toolbox.ToolBoxPlugin#addStandardToolBoxItems()
	 */
	protected void addStandardToolBoxItems()
	{
		// Add initial node
		addToolBoxItem(ProcessUtil.createStandardInitialNode(), "toolboxitem.initial.tooltip");

		// Add final node
		addToolBoxItem(ProcessUtil.createStandardFinalNode(), "toolboxitem.final.tooltip");

		// Add merge node
		addToolBoxItem(ProcessUtil.createStandardMergeNode(), "toolboxitem.merge.tooltip");

		// Add decision node
		addToolBoxItem(ProcessUtil.createStandardDecisionNode(), "toolboxitem.decision.tooltip");

		// Add wait state node
		addToolBoxItem(ProcessUtil.createStandardWaitStateNode(), "toolboxitem.waitstate.tooltip");

		// Add workflow node
		addToolBoxItem(ProcessUtil.createStandardWorkflowNode(), "toolboxitem.workflow.tooltip");

		// Add workflow final node
		addToolBoxItem(ProcessUtil.createStandardWorkflowEndNode(), "toolboxitem.workflowend.tooltip");

		// Add fork node
		addToolBoxItem(ProcessUtil.createStandardForkNode(), "toolboxitem.fork.tooltip");

		// Add join node
		addToolBoxItem(ProcessUtil.createStandardJoinNode(), "toolboxitem.join.tooltip");

		// Add placeholder template
		addToolBoxItem(ProcessUtil.createStandardPlaceholderNode(), "toolboxitem.placeholder.tooltip");

		// Add activity template
		addToolBoxItem(ProcessUtil.createStandardActivityNode(), "toolboxitem.activity.tooltip");

		// Add visual template
		addToolBoxItem(ProcessUtil.createStandardVisualNode(), "toolboxitem.visual.tooltip");

		// Add process template
		addToolBoxItem(new ProcessItemImpl(), "toolboxitem.process.tooltip");

		/*
		 // Add actor template
		 addToolBoxItem (new ActorItemImpl (), "toolboxitem.actor.tooltip");

		 // Add vertical swimline line template
		 addToolBoxItem (new VLineFigure (null), "VLine");

		 // Add horizontal swimline line template
		 addToolBoxItem (new HLineFigure (null), "HLine");
		 */

		// Add text element template
		addToolBoxItem(new TextElementImpl(), "toolboxitem.textelement.tooltip");

		NodeSocket socket;

		// Add entry socket
		socket = new NodeSocketImpl();
		socket.setEntrySocket(true);
		addToolBoxItem(socket, "toolboxitem.socketentry.tooltip");

		// Add exit socket
		socket = new NodeSocketImpl();
		socket.setEntrySocket(false);
		addToolBoxItem(socket, "toolboxitem.socketexit.tooltip");
	}

	/**
	 * @see org.openbp.cockpit.plugins.toolbox.ToolBoxPlugin#canTitleChange()
	 */
	protected boolean canTitleChange()
	{
		// Disable title change
		return false;
	}

	/**
	 * @see org.openbp.cockpit.plugins.toolbox.ToolBoxPlugin#acceptDrop()
	 */
	protected boolean acceptDrop()
	{
		// Disable drop
		return false;
	}

	/**
	 * @see org.openbp.cockpit.plugins.toolbox.ToolBoxPlugin#acceptFlyWheelKey(int)
	 */
	protected boolean acceptFlyWheelKey(int key)
	{
		// Trigger this toolbox using the space key
		return key == KeyEvent.VK_SPACE;
	}
}
