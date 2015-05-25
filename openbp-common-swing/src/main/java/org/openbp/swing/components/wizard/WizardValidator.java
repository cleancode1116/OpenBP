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
package org.openbp.swing.components.wizard;

/**
 * The wizard validator is an interface that a wizard page can implement
 * in order to provide custom control about the forward/backward/finish button enablement.
 *
 * @author Heiko Erhardt
 */
public interface WizardValidator
{
	/**
	 * Determines if we can advance to the next page.
	 * Default: false.
	 * @nowarn
	 */
	public boolean canMoveForward();

	/**
	 * Determines if we can return to the previous page.
	 * Default: true.
	 * @nowarn
	 */
	public boolean canMoveBackward();

	/**
	 * Determines if we can finish the wizard dialog at this point.
	 * Default: false.
	 * @nowarn
	 */
	public boolean canFinish();

	/**
	 * Determines if we can cancel the wizard dialog at this point.
	 * Default: false.
	 * @nowarn
	 */
	public boolean canCancel();
}
