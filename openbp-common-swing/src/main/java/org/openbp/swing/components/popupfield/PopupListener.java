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
package org.openbp.swing.components.popupfield;

import java.util.EventListener;

/**
 * Listener interface for popup listeners.
 * Popup events occurr when the popup menu of a {@link JSelectionField} is opened or closed.
 *
 * @author Heiko Erhardt
 */
public interface PopupListener
	extends EventListener
{
	/**
	 * Called if the state of the popup has changed.
	 *
	 * @param e Event containing more information
	 */
	public void popupStateChanged(PopupEvent e);
}
