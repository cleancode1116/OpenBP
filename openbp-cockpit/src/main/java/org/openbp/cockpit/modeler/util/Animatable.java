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
package org.openbp.cockpit.modeler.util;

/**
 * Defines an object that can be animated.
 * See the {@link Animator} class for details.
 *
 * @author Stephan Moritz
 */
public interface Animatable
{
	/**
	 * Called when the animation is about to start.
	 */
	public void animationStart();

	/**
	 * Called to perform an animation step.
	 */
	public void animationStep();

	/**
	 * Called after the animation has ended.
	 */
	public void animationEnd();
}
