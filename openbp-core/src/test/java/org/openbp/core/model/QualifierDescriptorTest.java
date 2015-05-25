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

import junit.framework.TestCase;

import org.openbp.core.model.item.ItemTypes;

public class QualifierDescriptorTest extends TestCase
{
	public QualifierDescriptorTest()
	{
	}

	public void testQualifiers()
	{
		performTest("/Model/Process:ProcName", "Model", "ProcName", ItemTypes.PROCESS, null);
		performTest("/Model/ProcName", "Model", "ProcName", null, null);
		performTest("Process:ProcName", null, "ProcName", ItemTypes.PROCESS, null);
		performTest("ProcName", null, "ProcName", null, null);
		performTest("/Model/Process:ProcName.SocketName.ParamName", "Model", "ProcName", ItemTypes.PROCESS, "SocketName.ParamName");
		performTest("/Model/ProcName.SocketName.ParamName", "Model", "ProcName", null, "SocketName.ParamName");
		performTest("Process:ProcName.SocketName.ParamName", null, "ProcName", ItemTypes.PROCESS, "SocketName.ParamName");
		performTest("ProcName.SocketName.ParamName", null, "ProcName", null, "SocketName.ParamName");
		performTest(".SocketName.ParamName", null, null, null, "SocketName.ParamName");
		performTest(".SocketName.ParamName", null, null, null, "SocketName.ParamName");
	}

	private void performTest(String s, String expectedModel, String expectedItem, String expectedItemType, String expectedObjectPath)
	{
		ModelQualifier qualifier = new ModelQualifier(s);

		assertEquals(expectedModel, qualifier.getModel());
		assertEquals(expectedItem, qualifier.getItem());
		assertEquals(expectedItemType, qualifier.getItemType());
		assertEquals(expectedObjectPath, qualifier.getObjectPath());

		/*
		String s2 = qd.toString();
		assertEquals(s, s2);
		*/
	}
}
