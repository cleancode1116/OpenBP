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

/**
 * Figure type constants.
 *
 * @author Heiko Erhardt
 */
public final class FigureTypes
{
	/** Symbol type: Node/process variable tag */
	public static final String SYMBOLTYPE_PARAM = "Param";

	/** Symbol type: Node tag */
	public static final String SYMBOLTYPE_TAG = "Tag";

	/** Symbol type: Begin transaction figure */
	public static final String SYMBOLTYPE_BEGIN = "Begin";

	/** Symbol type: Commit figure */
	public static final String SYMBOLTYPE_COMMIT = "Commit";

	/** Symbol type: Commit/begin figure */
	public static final String SYMBOLTYPE_COMMIT_BEGIN = "CommitBegin";

	/** Symbol type: Rollback figure */
	public static final String SYMBOLTYPE_ROLLBACK = "Rollback";

	/** Symbol type: Rollback/begin figure */
	public static final String SYMBOLTYPE_ROLLBACK_BEGIN = "RollbackBegin";

	/** Link type: Control link */
	public static final String LINKTYPE_CONTROL = "Control";

	/** Link type: Data link */
	public static final String LINKTYPE_DATA = "Data";

	/** Link type: Horizontal swim lane line */
	public static final String LINKTYPE_HLINE = "HLine";

	/** Link type: Vertical swim lane line */
	public static final String LINKTYPE_VLINE = "VLine";

	/**
	 * Private constructor prevents instantiation.
	 */
	private FigureTypes()
	{
	}
}
