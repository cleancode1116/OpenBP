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
package org.openbp.cockpit.modeler.figures.generic;

/**
 * This interface denotes a figure object whose appearance depends on external parameters,
 * i\.e\. some kind of model object (called the 'provider').
 * The interface provides a method that synchonizes the figure object with the provider.
 *
 * @author Stephan Moritz
 */
public interface UpdatableFigure
{
	/**
	 * Synchronizes this figure with its provider, i\.e\. the object that is represented by the figure.
	 * Updates the figure according to the current properties of the provider.
	 */
	public void updateFigure();
}
