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
package org.openbp.core.model.item.process;

/**
 * A workflow end node terminates all workflows of a workflow group.
 *
 * The workflow data of all workflow steps involved in the group will be removed from the workflow database.
 * It should be called after a workflow has been completed.
 *
 * A workflow group includes all workflows that have been started within the current process or between
 * the 'Begin multipart workflow' and 'End multipart workflow' activities.</description>
 *
 * @author Heiko Erhardt
 */
public interface WorkflowEndNode
	extends MultiSocketNode
{
}
