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
package org.openbp.common.util.observer;

/**
 * Generic event observer interface.
 * An observer will be notified whenever a particular event occurs.
 *
 * @author Heiko Erhardt
 */
public interface EventObserver
{
	/**
	 * Template method that will be called whenever an event occurs the observer is interested in.
	 * @param e Event
	 */
	public void observeEvent(ObserverEvent e);
}
