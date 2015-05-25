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
 * The splitter layout holds several components, separated by splitter bars.
 * An intelligent resize algorithm determines the size of the remaining components
 * when one of the splitter bars is moved.
 * For each component, its minimum and maximum sizes will be considered when giving
 * the user a visual feedback when moving the splitter bar, e. g. the splitter bar
 * won't move to a position that contradicts the constraints of the components.
 */
package org.openbp.swing.layout.splitter;
