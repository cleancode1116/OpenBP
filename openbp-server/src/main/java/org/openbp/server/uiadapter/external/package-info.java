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

/**
 * User interface engine implementation for external UI renderers.
 *
 * An external UI renderer maintains control over the user interface life cycle.
 * It will call the OpenBP engine to handle some event that occured in the user interface.
 * The process will run until it terminates with an end node or a visual is being executed.\n
 *
 * In the latter case, the UI engine will not do any rendering work, it merely saves
 * the name of the visual to a process variable of the session context ("") and returns null
 * in order to interrupt the process that is executing the visual.
 * Control will then be returned to the caller. The caller may retrieve the visual name
 * from the context and continue the process when the next event occurs.
 */
package org.openbp.server.uiadapter.external;
