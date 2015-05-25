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
package org.openbp.jaspira.event;

/**
 * This listener listens to events managed by the event manager.
 *
 * @author Jens Ferchland
 */
public interface JaspiraEventListener
{
	/**
	 * An event needs to be handled.
	 *
	 * @param e The event
	 * @return
	 *		true	The event was consumed by the handler.<br>
	 *		false	The event should be passed on to other handlers.
	 */
	public boolean eventFired(JaspiraEvent e);

	/**
	 * Returns a priority to manage equal events. If the
	 * return value is small the priority is high. Same
	 * priorities will be managed over the alphabetical
	 * order of the event names (biggest hash value).
	 *
	 * @return The event priority
	 */
	public int getPriority();
}
