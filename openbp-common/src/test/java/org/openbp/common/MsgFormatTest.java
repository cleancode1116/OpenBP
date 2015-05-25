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
package org.openbp.common;

import junit.framework.TestCase;

/**
 * Message format test case.
 */
public class MsgFormatTest extends TestCase
{
	public MsgFormatTest (String arg0)
	{
		super (arg0);
	}

	public void testFormat ()
	{
		assertEquals ("Could not find 'a' in directory 'b'",
					  MsgFormat.format ("Could not find $0 in directory $1", "a", "b"));

		assertEquals ("Could not find 'a' in directory 'b'.",
					  MsgFormat.format ("Could not find $0 in directory $1.", "a", "b"));

		assertEquals ("Could not find 'a' in directory '{1}'.",
					  MsgFormat.format ("Could not find $0 in directory $1.", "a"));

		assertEquals ("'a''b''c'",
					  MsgFormat.format ("$0$1$2", "a", "b", "c"));

		assertEquals ("'a''b''a'",
					  MsgFormat.format ("$0$1$0", "a", "b"));

		assertEquals ("Look 'a2'\n\r'b2' here",
					  MsgFormat.format ("Look $0\n\r$1 here", "a2", "b2"));

		assertEquals ("Foo $ foo",
					  MsgFormat.format ("Foo $ foo", "a", "b"));

		assertEquals ("",
					  MsgFormat.format ("", "a", "b"));

		assertEquals ("$$$'a'$$$",
					  MsgFormat.format ("$$$$0$$$", "a", "b"));
	}
}
