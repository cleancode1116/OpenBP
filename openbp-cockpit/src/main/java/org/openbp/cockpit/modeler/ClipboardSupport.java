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
package org.openbp.cockpit.modeler;

import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.figures.process.FlowConnection;
import org.openbp.cockpit.modeler.figures.process.MultiSocketNodeFigure;
import org.openbp.cockpit.modeler.figures.process.NodeFigure;
import org.openbp.cockpit.modeler.figures.process.ParamConnection;
import org.openbp.cockpit.modeler.figures.process.ParamFigure;
import org.openbp.cockpit.modeler.figures.process.PlaceholderNodeFigure;
import org.openbp.cockpit.modeler.figures.process.ProcessElementContainer;
import org.openbp.cockpit.modeler.figures.process.SocketFigure;
import org.openbp.cockpit.modeler.figures.process.TextElementFigure;
import org.openbp.cockpit.modeler.util.ModelerUtil;
import org.openbp.common.generic.msgcontainer.StandardMsgContainer;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.process.ActivityNodeImpl;
import org.openbp.core.model.item.process.ControlLink;
import org.openbp.core.model.item.process.DataLink;
import org.openbp.core.model.item.process.MultiSocketNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.NodeSocketImpl;
import org.openbp.core.model.item.process.Param;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessItemImpl;
import org.openbp.core.model.item.process.SingleSocketNode;
import org.openbp.core.model.item.process.TextElement;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.gui.interaction.MultiTransferable;
import org.openbp.jaspira.gui.interaction.SimpleTransferable;
import org.openbp.swing.components.JMsgBox;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

/**
 * Helper class that contains functions for the modeler clipboard support.
 *
 * @author Heiko Erhardt
 */
public class ClipboardSupport
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Workspace view we are associated with */
	private final WorkspaceDrawingView workspaceView;

	/** Resource for messages */
	private final ResourceCollection resourceCollection;

	/** Flag for socket and parameter pasting only */
	private boolean socketsAndParamsOnly;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 *
	 * @param workspaceView Workspace view we are associated with
	 * @param resourceCollection Resource for messages
	 * @param socketsAndParamsOnly
	 * true: Allows for socket and parameter pasting only.<br>
	 * false: Allows for pasting all supported items.
	 */
	public ClipboardSupport(WorkspaceDrawingView workspaceView, ResourceCollection resourceCollection, boolean socketsAndParamsOnly)
	{
		this.workspaceView = workspaceView;
		this.resourceCollection = resourceCollection;
		this.socketsAndParamsOnly = socketsAndParamsOnly;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Checks if a copy action can be performed.
	 *
	 * @return
	 * true: Process nodes, sockets or parameters are selected.<br>
	 * false: No appropriate object is selected.
	 */
	public boolean canCopy()
	{
		for (FigureEnumeration fe = workspaceView.selectionElements(); fe.hasMoreElements();)
		{
			Figure next = fe.nextFigure();

			if (next instanceof NodeFigure || next instanceof TextElementFigure || next instanceof SocketFigure || next instanceof ParamFigure)
				return true;
		}

		return false;
	}

	/**
	 * Checks if a delete action can be performed.
	 *
	 * @return
	 * true: Process nodes, sockets or parameters are selected.<br>
	 * false: No appropriate object is selected.
	 */
	public boolean canDelete()
	{
		for (FigureEnumeration fe = workspaceView.selectionElements(); fe.hasMoreElements();)
		{
			Figure next = fe.nextFigure();

			if (next instanceof NodeFigure)
			{
				if (! socketsAndParamsOnly)
					return true;
			}
			else
			{
				if (next instanceof SocketFigure)
				{
					NodeFigure nodeFigure = (NodeFigure) ((SocketFigure) next).getParent();
					Node node = nodeFigure.getNode();
					if (node instanceof SingleSocketNode)
					{
						// We may not delete the only socket of a node
						return false;
					}
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if a cut action can be performed.
	 *
	 * @return
	 * true: Process nodes, sockets or parameters are selected.<br>
	 * false: No appropriate object is selected.
	 */
	public boolean canCut()
	{
		return canDelete();
	}

	/**
	 * Creates a transferable object that contains the data to be copied from the given modeler view.
	 * The transferable includes all objects that are currently selected. Depending on the type of selected objects,
	 * this may be:<br>
	 * - A set of nodes and the links connecting the nodes<br>
	 * - A set of sockets<br>
	 * - A set of parameters of a socket
	 *
	 * @return The transferable or null if there is no appropriate selection
	 */
	public Transferable getCopyData()
	{
		// Set up a temporary message container for the model connector, so we can regognize if maintainReferences() fails.
		// Not pretty, but it works.
		try
		{
			// Make sure the reference names of the source are up to date
			((ProcessDrawing) workspaceView.drawing()).getProcess().maintainReferences(
				ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

			// The copy flavor will determine what what the copy transferable actually contains.
			// This might be:
			//
			// ClientFlavor.PROCESS_ITEM:
			// Nodes, control/data links & text elements
			// The transferable contains a process object that holds the nodes and links
			//
			// ClientFlavor.NODE_SOCKETS:
			// Sockets
			// The transferable contains a process object that holds a single node that contains the copied sockets
			//
			// ClientFlavor.NODE_PARAMS:
			// Node parameters
			// The transferable contains a process object that holds a single node with a single socket that contains the copied parameters
			//
			DataFlavor copyFlavor = null;

			// We create a dummy process which we put into the clipboard.
			// In order to force all object references to be absolute, we choose the System model
			// as parent model of the dummy process (this is also fine for objects which belong
			// to the System model itself since these object will always be referenced using their
			// local name)
			ProcessItem process = new ProcessItemImpl();
			process.setName("ProcessDummy");
			process.setModel(ModelConnector.getInstance().getModelByQualifier(CoreConstants.SYSTEM_MODEL_QUALIFIER));

			// When copying a node, we perform the following steps:
			// 1. Encode the geometry
			// 2. Clone the object
			// 3. Add the cloned object to the dummy process
			// 4. Generate reference names from the actual object references (e. g. for actions, sub processes, data types etc.)

			// Node that contains the selected sockets
			ActivityNodeImpl socketHolder = null;

			// List of source nodes that have been copied
			ArrayList copiedSourceNodes = new ArrayList();

			// Socket that contains the selected parameters
			NodeSocket paramHolder = null;

			for (FigureEnumeration fe = workspaceView.selectionElements(); fe.hasMoreElements();)
			{
				Figure next = fe.nextFigure();

				if (next instanceof NodeFigure)
				{
					NodeFigure nodeFigure = (NodeFigure) next;
					nodeFigure.encodeGeometry();

					Node node = nodeFigure.getNode();

					// Remember that we have copied this one
					copiedSourceNodes.add(node);

					// Clone the node and add it to the process
					node = (Node) node.clone();
					process.addNode(node);

					copyFlavor = ClientFlavors.PROCESS_ITEM;
				}

				else if (next instanceof TextElementFigure)
				{
					TextElementFigure textElementFigure = (TextElementFigure) next;
					textElementFigure.encodeGeometry();

					TextElement textElement = textElementFigure.getTextElement();

					// Clone the element and add it to the process
					textElement = (TextElement) textElement.clone();
					process.addTextElement(textElement);

					copyFlavor = ClientFlavors.PROCESS_ITEM;
				}

				else if (next instanceof SocketFigure)
				{
					SocketFigure socketFigure = (SocketFigure) next;
					socketFigure.encodeGeometry();

					NodeSocket socket = socketFigure.getNodeSocket();

					if (socketHolder == null)
					{
						socketHolder = new ActivityNodeImpl();
						socketHolder.setName("NodeDummy");
						process.addNode(socketHolder);
					}

					// Clone the socketand add it to the dummy node
					socket = (NodeSocket) socket.clone();
					socketHolder.addSocket(socket);

					copyFlavor = ClientFlavors.NODE_SOCKETS;
				}

				else if (next instanceof ParamFigure)
				{
					ParamFigure paramFigure = (ParamFigure) next;

					NodeParam param = paramFigure.getNodeParam();

					if (paramHolder == null)
					{
						if (socketHolder == null)
						{
							socketHolder = new ActivityNodeImpl();
							socketHolder.setName("NodeDummy");
							process.addNode(socketHolder);
						}

						paramHolder = new NodeSocketImpl();
						paramHolder.setName("SocketDummy");
						socketHolder.addSocket(paramHolder);
					}

					// Clone the socket and add it to the dummy node
					param = (NodeParam) param.clone();
					paramHolder.addParam(param, - 1);

					copyFlavor = ClientFlavors.NODE_PARAMS;
				}
			}

			// When copying a link, we perform the following steps:
			// 1. Encode the geometry
			// 2. Generate reference names from the actual object references
			//    (e. g. node names for control links, parameter names for data links etc.)
			// 3. Clone the object
			// 4. Add the cloned object to the dummy process
			// 5. Re-establish the object references (now based on the elements of the dummy process)
			//    If this fails, the object will be removed from the dummy process again.

			StandardMsgContainer msgContainer = ModelConnector.getInstance().getMsgContainer();
			msgContainer.clearMsgs();

			for (FigureEnumeration fe = workspaceView.selectionElements(); fe.hasMoreElements();)
			{
				Figure next = fe.nextFigure();

				if (next instanceof FlowConnection)
				{
					FlowConnection flowConnection = (FlowConnection) next;

					ControlLink link = flowConnection.getControlLink();

					if (! copiedSourceNodes.contains(link.getSourceSocket().getNode())
						|| ! copiedSourceNodes.contains(link.getTargetSocket().getNode()))
					{
						// Link source or target has not been copied
						continue;
					}

					flowConnection.encodeGeometry();

					link = (ControlLink) link.clone();
					process.addControlLink(link);

					if (! msgContainer.isEmpty())
					{
						// One of the end points of the link is not present in the copied set,
						// so remove the link from the dummy process again.
						process.removeControlLink(link);
						msgContainer.clearMsgs();
					}
				}

				else if (next instanceof ParamConnection)
				{
					ParamConnection paramConnection = (ParamConnection) next;

					DataLink link = paramConnection.getDataLink();

					Param sourceParam = link.getSourceParam();
					Param targetParam = link.getTargetParam();

					if (sourceParam instanceof NodeParam)
					{
						if (! copiedSourceNodes.contains(((NodeParam) sourceParam).getSocket().getNode()))
						{
							// Link source or target has not been copied
							continue;
						}
					}
					else
					{
						// Don't copy process variable links
						continue;
					}

					if (targetParam instanceof NodeParam)
					{
						if (! copiedSourceNodes.contains(((NodeParam) targetParam).getSocket().getNode()))
						{
							// Link source or target has not been copied
							continue;
						}
					}
					else
					{
						// Don't copy process variable links
						continue;
					}

					paramConnection.encodeGeometry();

					link = (DataLink) link.clone();
					process.addDataLink(link);

					if (! msgContainer.isEmpty())
					{
						// One of the end points of the link is not present in the copied set,
						// so remove the link from the dummy process again.
						process.removeDataLink(link);
						msgContainer.clearMsgs();
					}
				}
			}

			// Re-establish inter-object links and links to other items
			// and remove links to figures to make the gc work
			// TODO Fix 4 This seems to produce some maintainReferences error: "Cannot resolve ... in Model /System"
			process.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS | ModelObject.UNLINK_FROM_REPRESENTATION);

			if (copyFlavor != null && (process.getNodes().hasNext() || process.getTextElements().hasNext()))
			{
				Transferable ret = new SimpleTransferable(process, copyFlavor);

				if (copiedSourceNodes.size() == 1)
				{
					// If we have a single node selected, add the model qualifier of the node in addition
					Node node = (Node) copiedSourceNodes.get(0);
					ModelQualifier qualifier = node.getQualifier();

					MultiTransferable mt = new MultiTransferable();
					mt.addTransferable(ret);
					mt.addTransferable(new SimpleTransferable(qualifier, ClientFlavors.MODEL_QUALIFIER));
					ret = mt;
				}

				return ret;
			}
		}
		catch (CloneNotSupportedException e)
		{
			// Doesn't happen
		}
		finally
		{
			ModelConnector.getInstance().getMsgContainer().clearMsgs();
		}

		return null;
	}

	/**
	 * Cuts the current selection and creates a transferable object that contains the data to be copied from the given modeler view.
	 *
	 * @return The transferable or null if there is no appropriate selection
	 */
	public Transferable cut()
	{
		Transferable result = getCopyData();
		delete();
		return result;
	}

	/**
	 * Deletes the current selection.
	 */
	public void delete()
	{
		Figure selection = null;

		// Delete all elements, try to determine which element to select after the delete.
		// E. g. when deleting a node socket, the node it belongs to will be selected.
		boolean hasSelected = false;
		for (FigureEnumeration fe = workspaceView.selectionElements(); fe.hasMoreElements();)
		{
			Figure next = fe.nextFigure();

			if (!hasSelected)
			{
				if (next instanceof ProcessElementContainer)
				{
					selection = ((ProcessElementContainer) next).selectionOnDelete();
				}
				hasSelected = true;
			}
			else
			{
				// There seems to be more than one object selected
				selection = null;
			}

			workspaceView.drawing().remove(next);
		}

		// Rebuild the selection
		if (selection != null)
		{
			workspaceView.singleSelect(selection);
		}
		else
		{
			workspaceView.clearSelection();
		}
		workspaceView.repairDamage();
	}

	/**
	 * Checks whether the actual clipboard contents can be pasted.
	 *
	 * @param content Transferable object held by the clipboard
	 * @return
	 * true: The clipboard contains process nodes, sockets or parameters is selected.<br>
	 * false: The clipboard contains no appropriate objects.
	 */
	public boolean canPaste(Transferable content)
	{
		if (content != null)
		{
			if (content.isDataFlavorSupported(ClientFlavors.PROCESS_ITEM))
				return true;

			if (content.isDataFlavorSupported(ClientFlavors.NODE_SOCKETS))
				return true;

			if (content.isDataFlavorSupported(ClientFlavors.NODE_PARAMS))
				return true;

			if (content.isDataFlavorSupported(ClientFlavors.MODEL_QUALIFIER))
				return true;
		}

		return false;
	}

	/**
	 * Pastes data from the given transferable to the modeler view.
	 *
	 * @param content Content to paste; The transferable object is expected to contain
	 * data that was created by the {@link #getCopyData} method.
	 */
	public void paste(Transferable content)
	{
		StandardMsgContainer msgContainer = ModelConnector.getInstance().getMsgContainer();
		msgContainer.clearMsgs();

		try
		{
			boolean success = false;

			if (! socketsAndParamsOnly)
			{
				if (! success && content.isDataFlavorSupported(ClientFlavors.MODEL_QUALIFIER))
				{
					// The clipboard contains node socket parameters
					ModelQualifier qualifier = (ModelQualifier) content.getTransferData(ClientFlavors.MODEL_QUALIFIER);
					success = pasteQualifier(qualifier);
				}

				if (! success && content.isDataFlavorSupported(ClientFlavors.PROCESS_ITEM))
				{
					// We need to copy the process from the clipboard, since we are about to modify it.
					ProcessItem source = (ProcessItem) content.getTransferData(ClientFlavors.PROCESS_ITEM);
					success = pasteProcess(source);
				}
			}

			if (! success && content.isDataFlavorSupported(ClientFlavors.NODE_SOCKETS))
			{
				// The clipboard contains node sockets
				ProcessItem source = (ProcessItem) content.getTransferData(ClientFlavors.NODE_SOCKETS);
				success = pasteSockets(source);
			}

			if (! success && content.isDataFlavorSupported(ClientFlavors.NODE_PARAMS))
			{
				// The clipboard contains node socket parameters
				ProcessItem source = (ProcessItem) content.getTransferData(ClientFlavors.NODE_PARAMS);
				success = pasteParams(source);
			}

			workspaceView.repairDamage();

			if (! success)
			{
				String msg = resourceCollection.getRequiredString("messages.paste");
				JMsgBox.show(null, msg, JMsgBox.ICON_INFO);
			}
		}
		catch (CloneNotSupportedException e)
		{
			// Shouldn't happen
			e.printStackTrace();
		}
		catch (UnsupportedFlavorException e)
		{
		}
		catch (IOException e)
		{
		}
		finally
		{
			if (! msgContainer.isEmpty())
			{
				System.err.println(msgContainer.toString());
				msgContainer.clearMsgs();
			}
		}
	}

	/**
	 * Pastes nodes and control/data links from the given source process to the modeler view.
	 *
	 * @param source Process that contains the nodes to paste
	 * @nowarn
	 */
	public boolean pasteProcess(ProcessItem source)
		throws CloneNotSupportedException
	{
		// The clipboard contains nodes and control/data links.
		// Whater is selected, we paste everything into the current process.
		workspaceView.clearSelection();

		// We need to copy the process from the clipboard, since we are about to modify it.
		source = (ProcessItem) source.clone();
		source.setModel(ModelConnector.getInstance().getModelByQualifier(CoreConstants.SYSTEM_MODEL_QUALIFIER));

		// Repair any references of the clone
		source.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS);

		// We will copy to our target process
		ProcessDrawing drawing = (ProcessDrawing) workspaceView.drawing();
		ProcessItem target = drawing.getProcess();

		// Make sure the references names of the target are up to date
		target.maintainReferences(ModelObject.SYNC_LOCAL_REFNAMES | ModelObject.SYNC_GLOBAL_REFNAMES);

		// We need to make the names in the source process unique with respect to our target process
		NamedObjectCollectionUtil.createUniqueNames(source.getNodeList(), target.getNodeList());
		NamedObjectCollectionUtil.createUniqueNames(source.getTextElementList(), target.getTextElementList());
		NamedObjectCollectionUtil.createUniqueNames(source.getDataLinkList(), target.getDataLinkList());
		NamedObjectCollectionUtil.createUniqueNames(source.getControlLinkList(), target.getControlLinkList());

		// Update the reference names according to the changed object names
		source.maintainReferences(ModelObject.SYNC_LOCAL_REFNAMES);

		Figure firstFigure = null;

		// Add the nodes to the target process
		for (Iterator it = source.getNodes(); it.hasNext();)
		{
			Node node = (Node) it.next();

			target.addNode(node);

			// Rebuild the references after adding the object to the target
			node.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES);

			NodeFigure nodeFigure = drawing.createNodeFigure(node);
			if (nodeFigure == null)
			{
				target.removeNode(node);
				continue;
			}

			if (firstFigure == null)
				firstFigure = nodeFigure;

			workspaceView.add(nodeFigure);
			workspaceView.addToSelection(nodeFigure);

			// Position the new node within the process drawing so that it doesn't hide an existing figure
			ModelerUtil.preventOverlap(drawing, nodeFigure);
		}

		// Add the text elements to the target process
		for (Iterator it = source.getTextElements(); it.hasNext();)
		{
			TextElement textElement = (TextElement) it.next();

			target.addTextElement(textElement);

			// Rebuild the references after adding the object to the target
			textElement.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES);

			TextElementFigure textElementFigure = drawing.createTextElementFigure(textElement);
			if (textElementFigure == null)
			{
				target.removeTextElement(textElement);
				continue;
			}

			if (firstFigure == null)
				firstFigure = textElementFigure;

			workspaceView.add(textElementFigure);
			workspaceView.addToSelection(textElementFigure);

			// Position the new element within the process drawing so that it doesn't hide an existing figure
			ModelerUtil.preventOverlap(drawing, textElementFigure);
		}

		// Add the control links
		for (Iterator it = source.getControlLinks(); it.hasNext();)
		{
			ControlLink link = (ControlLink) it.next();

			target.addControlLink(link);

			// Determine the reference names after adding the object to the target
			link.maintainReferences(ModelObject.SYNC_LOCAL_REFNAMES);

			FlowConnection flowConnection = drawing.createFlowConnection(link);
			if (flowConnection == null)
			{
				target.removeControlLink(link);
				continue;
			}

			workspaceView.add(flowConnection);
			workspaceView.addToSelection(flowConnection);
		}

		// Add the data links
		for (Iterator it = source.getDataLinks(); it.hasNext();)
		{
			DataLink link = (DataLink) it.next();

			target.addDataLink(link);

			// Determine the reference names after adding the object to the target
			link.maintainReferences(ModelObject.SYNC_LOCAL_REFNAMES);

			ParamConnection paramConnection = drawing.createParamConnection(link);
			if (paramConnection == null)
			{
				target.removeDataLink(link);
				continue;
			}

			workspaceView.add(paramConnection);
			workspaceView.addToSelection(paramConnection);
		}

		if (firstFigure != null)
		{
			// Get the bounding rectangle
			Rectangle rect = new Rectangle(firstFigure.displayBox());

			// Enlarge by 100 pixel to prevent it from hanging in the corners
			rect.grow(50, 50);

			workspaceView.scrollRectToVisible(rect);

			return true;
		}

		return false;
	}

	/**
	 * Pastes sockets from the given source process to the modeler view.
	 *
	 * @param source Process that contains the sockets to paste
	 * @nowarn
	 */
	public boolean pasteSockets(ProcessItem source)
		throws CloneNotSupportedException
	{
		// A single node must be selected in order to have a paste target.
		if (workspaceView.selectionCount() == 1)
		{
			FigureEnumeration fe = workspaceView.selectionElements();
			if (fe.hasMoreElements())
			{
				Figure selection = fe.nextFigure();
				if (selection instanceof MultiSocketNodeFigure)
				{
					// Get the node figure to add the sockets to
					MultiSocketNodeFigure nodeFigure = (MultiSocketNodeFigure) selection;
					MultiSocketNode targetNode = (MultiSocketNode) nodeFigure.getNode();

					// Get the source node that contains the sockets
					ActivityNodeImpl node = (ActivityNodeImpl) source.getNodeByName("NodeDummy");

					workspaceView.clearSelection();

					// Add all sockets to the selected node
					List sockets = node.getSocketList();
					int n = sockets.size();
					for (int i = 0; i < n; ++i)
					{
						NodeSocket socket = (NodeSocket) sockets.get(i);

						socket = (NodeSocket) socket.clone();

						NamedObjectCollectionUtil.createUniqueName(socket, targetNode.getSocketList());

						targetNode.addSocket(socket);
						socket.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS);

						// Add the corresponding socket figure and select it
						// TODO Feature 5: We should take care that an existing socket is not overlayed by the new socket
						SocketFigure socketFigure = nodeFigure.addSocket(socket);
						workspaceView.addToSelection(socketFigure);
					}

					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Pastes parameters from the given source process to the modeler view.
	 *
	 * @param source Process that contains the parameters to paste
	 * @nowarn
	 */
	public boolean pasteParams(ProcessItem source)
		throws CloneNotSupportedException
	{
		// A single socket must be selected in order to have a paste target.
		if (workspaceView.selectionCount() == 1)
		{
			FigureEnumeration fe = workspaceView.selectionElements();
			if (fe.hasMoreElements())
			{
				Figure figure = fe.nextFigure();
				if (figure instanceof SocketFigure)
				{
					// Get the node figure to add the sockets to
					SocketFigure socketFigure = (SocketFigure) figure;
					NodeSocket targetSocket = socketFigure.getNodeSocket();

					// Get the source node that contains the params
					ActivityNodeImpl node = (ActivityNodeImpl) source.getNodeByName("NodeDummy");

					workspaceView.clearSelection();

					// Add all params of the only socket of the source node to the selected node
					NodeSocket socket = (NodeSocket) node.getSocketList().get(0);
					List params = socket.getParamList();
					int n = params.size();
					for (int i = 0; i < n; ++i)
					{
						NodeParam param = (NodeParam) params.get(i);

						param = (NodeParam) param.clone();

						NamedObjectCollectionUtil.createUniqueName(param, targetSocket.getParamList());

						targetSocket.addParam(param, - 1);
						param.maintainReferences(ModelObject.RESOLVE_GLOBAL_REFS | ModelObject.RESOLVE_LOCAL_REFS);

						// Add the corresponding param figure and select it
						ParamFigure paramFigure = socketFigure.addParam(param, - 1);
						workspaceView.addToSelection(paramFigure);
					}

					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Pastes a model qualifier to a placeholder node.
	 *
	 * @param source Model qualifier of the object to reference
	 * @nowarn
	 */
	public boolean pasteQualifier(ModelQualifier source)
	{
		// A single node must be selected in order to have a paste target.
		if (workspaceView.selectionCount() == 1)
		{
			FigureEnumeration fe = workspaceView.selectionElements();
			if (fe.hasMoreElements())
			{
				Figure selection = fe.nextFigure();
				if (selection instanceof PlaceholderNodeFigure)
				{
					// Get the node figure to add the sockets to
					PlaceholderNodeFigure nodeFigure = (PlaceholderNodeFigure) selection;
					nodeFigure.assignPlaceholderReference(source.toString());

					return true;
				}
			}
		}

		return false;
	}
}
