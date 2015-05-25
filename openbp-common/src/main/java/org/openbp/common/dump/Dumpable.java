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
package org.openbp.common.dump;

import java.io.Writer;

/**
 * Determines that an object can dump itself to an output writer.
 * This is used for debugging and logging purposes in conjunction with the
 * {@link Dumper} class.<br>
 * Any i/o exceptions that might occur must be caught by the dump methods.
 *
 * @author Heiko Erhardt
 */
public interface Dumpable
{
	//////////////////////////////////////////////////
	// @@ Interface methods
	//////////////////////////////////////////////////

	/**
	 * Dumps the object to an output.
	 *
	 * @param writer Writer object to use for output generation
	 */
	public void dump(Writer writer);

	/**
	 * Dumps the object to an output.
	 *
	 * @param writer Writer object to use for output generation
	 * @param indent Indentation level (0 = no indentation)
	 */
	public void dump(Writer writer, int indent);
}
