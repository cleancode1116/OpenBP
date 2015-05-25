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
package org.openbp.common.template.writer;

/**
 * The single method of this interface is used by the {@link TemplateWriter} to query the merge mode
 * if an output file alread exists.
 *
 * @author Heiko Erhardt
 */
public interface MergeModeRequester
{
	/**
	 * Determines the merge mode for the given writer.
	 * Usually displays some dialog or message box to the user
	 *
	 * @param writer The writer
	 * @return The return code determines the merge action to take:<br>
	 * {@link TemplateWriter#MODE_UNDEFINED}:
	 * Cancels the process<br>
	 * {@link TemplateWriter#MODE_OVERWRITE}:
	 * Overwrite the output file<br>
	 * {@link TemplateWriter#MODE_MERGE}: <br>
	 * Merge contents with the output file
	 */
	public int determineMergeMode(TemplateWriter writer);
}
