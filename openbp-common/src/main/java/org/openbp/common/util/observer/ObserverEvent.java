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
 * Template interface for an observer event.
 *
 * @author Author: Heiko Erhardt
 */
public interface ObserverEvent
{
	/**
	 * Gets the type of the event.
	 *
	 * @return The event type.
	 * Must be one of the event types specified using the {@link EventObserverMgr#setSupportedEventTypes} method.
	 */
	public String getEventType();

	/**
	 * Template method that signalizes if subsequent observers shall be skipped.
	 * @return true to break the observer calling loop for this event
	 */
	public boolean shallSkipSubsequentObservers();
}
