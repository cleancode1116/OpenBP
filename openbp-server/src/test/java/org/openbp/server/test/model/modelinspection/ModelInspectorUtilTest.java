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
package org.openbp.server.test.model.modelinspection;

import java.util.List;

import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.InitialNode;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.process.WaitStateNode;
import org.openbp.core.model.modelinspection.ModelInspectorUtil;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.server.test.base.TestCaseBase;

/**
 * Test case for the ModelInspectorUtil class.
 */
public class ModelInspectorUtilTest extends TestCaseBase
{
	public static final String PROCESSREF = "/TestCase/ModelInspectorUtilTest";

	public ModelInspectorUtilTest()
	{
	}

	public void performTest()
		throws Exception
	{
		ModelMgr modelMgr = getProcessServer().getModelMgr();
		ModelQualifier qualifier = new ModelQualifier(PROCESSREF);
		qualifier.setItemType(ItemTypes.PROCESS);
		ProcessItem process = (ProcessItem) modelMgr.getItemByQualifier(qualifier, true);
	
		Node startNode = process.getNodeByName("Start");
		assertTrue(startNode instanceof InitialNode);
		NodeSocket socket = (NodeSocket) startNode.getDefaultEntrySocket();
		
		List socketDescriptorList = ModelInspectorUtil.determinePossibleExits(socket, WaitStateNode.class, false);
		assertEquals(4, socketDescriptorList.size());

		Node waitStateNode = process.getNodeByName("WaitState");
		assertTrue(waitStateNode instanceof WaitStateNode);
		socket = (NodeSocket) waitStateNode.getDefaultEntrySocket();
		
		socketDescriptorList = ModelInspectorUtil.determinePossibleExits(socket, WaitStateNode.class, true);
		assertEquals(2, socketDescriptorList.size());
	}
}
