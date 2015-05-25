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
package org.openbp.guiclient.util;

import java.awt.datatransfer.DataFlavor;

import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.item.process.ActivityNode;
import org.openbp.core.model.item.process.FinalNode;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.core.model.item.process.NodeProvider;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.ProcessObject;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.core.model.item.process.TextElement;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * This class contains constants for data flavors used by OpenBP clients.
 * Data flavors are used for drag and drop or copy/paste operations.
 *
 * @author Heiko Erhardt
 */
public class ClientFlavors
{
	/**
	 * Private constructor prevents instantiation.
	 */
	private ClientFlavors()
	{
	}

	/** OpenBP model qualifier flavor */
	public static final DataFlavor MODEL_QUALIFIER = new DataFlavor(ModelQualifier.class, "OpenBP Model Qualifier");

	/** OpenBP model object flavor */
	public static final DataFlavor MODEL_OBJECT = new DataFlavor(ModelObject.class, "OpenBP Model Object");

	/** OpenBP process node provider flavor */
	public static final DataFlavor NODE_PROVIDER = new DataFlavor(NodeProvider.class, "OpenBP Process Node Provider");

	/** OpenBP model flavor */
	public static final DataFlavor MODEL = new DataFlavor(Model.class, "OpenBP Model");

	/** OpenBP component flavor */
	public static final DataFlavor ITEM = new DataFlavor(Item.class, "OpenBP Component");

	/** OpenBP component flavor */
	public static final DataFlavor ITEM_TYPE_DESCRIPTOR = new DataFlavor(ItemTypeDescriptor.class, "OpenBP Component Type Descriptor");

	/** OpenBP data type component flavor */
	public static final DataFlavor TYPE_ITEM = new DataFlavor(DataTypeItem.class, "OpenBP Data Type Component");

	/** OpenBP data type component flavor (complex types) */
	public static final DataFlavor COMPLEX_TYPE_ITEM = new DataFlavor(ComplexTypeItem.class, "OpenBP Data Type Component");

	/** OpenBP process component flavor */
	public static final DataFlavor PROCESS_ITEM = new DataFlavor(ProcessItem.class, "OpenBP Process Component");

	/** OpenBP process object flavor */
	public static final DataFlavor PROCESS_OBJECT = new DataFlavor(ProcessObject.class, "OpenBP Process Object");

	/** OpenBP process initial node flavor */
	public static final DataFlavor INITIAL_NODE = new DataFlavor(InitialNode.class, "OpenBP Process Initial Node");

	/** OpenBP process final node flavor */
	public static final DataFlavor FINAL_NODE = new DataFlavor(FinalNode.class, "OpenBP Process Final Node");

	/** OpenBP process activity node flavor */
	public static final DataFlavor ACTIVITY_NODE = new DataFlavor(ActivityNode.class, "OpenBP Process Activity Node");

	/** OpenBP process node flavor */
	public static final DataFlavor NODE = new DataFlavor(Node.class, "OpenBP Process Node");

	/** OpenBP process node socket flavor */
	public static final DataFlavor NODE_SOCKET = new DataFlavor(NodeSocket.class, "OpenBP Process Node Socket");

	/** OpenBP text element flavor */
	public static final DataFlavor TEXT_ELEMENT = new DataFlavor(TextElement.class, "OpenBP Text Element");

	/** OpenBP process variable flavor */
	public static final DataFlavor PROCESS_VARIABLE = new DataFlavor(ProcessVariable.class, "OpenBP Process Variable");

	/** OpenBP process node param flavor */
	public static final DataFlavor NODE_PARAM = new DataFlavor(NodeParam.class, "OpenBP Process Node Parameter");

	/**
	 * OpenBP process node socket array flavor.
	 * Note that the transferable doesn't contain the actual array, but rather a {@link ProcessItem} containing
	 * a single node that holds the sockets.
	 */
	public static final DataFlavor NODE_SOCKETS = new DataFlavor(NodeSocket [].class, "OpenBP Process Node Sockets");

	/**
	 * OpenBP process node param array flavor.
	 * Note that the transferable doesn't contain the actual array, but rather a {@link ProcessItem} containing
	 * a single node with a single socket that holds the parameters.
	 */
	public static final DataFlavor NODE_PARAMS = new DataFlavor(NodeParam [].class, "OpenBP Process Node Socket Parameters");
}
