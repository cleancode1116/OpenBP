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

import org.openbp.jaspira.plugin.Plugin;

/**
 * Event that provides the recepients with the capability to insert a
 * specific answer via the setAnswer mehtod. If this happens, the event counts as consumed.
 *
 * @author Stephan Moritz
 */
public class AskEvent extends JaspiraEvent
{
	/** The answer to the request. */
	private Object answer;

	/**
	 * Constructor.
	 * @param source The plugin that dispatches this event. Must not be null.
	 * Will be converted to lower case.
	 * @param eventName The name of the event
	 */
	public AskEvent(Plugin source, String eventName)
	{
		super(source, eventName);
	}

	/**
	 * Constructor.
	 * @param source The plugin that dispatches this event. Must not be null.
	 * Will be converted to lower case.
	 * @param eventName The name of the event
	 * @param object An additional data object. Can be null.
	 */
	public AskEvent(Plugin source, String eventName, Object object)
	{
		super(source, eventName, object);
	}

	/**
	 * Returns the answer.
	 * @nowarn
	 */
	public Object getAnswer()
	{
		return answer;
	}

	/**
	 * Sets the answer.
	 * @nowarn
	 */
	public void setAnswer(Object answer)
	{
		this.answer = answer;

		this.updateFlags(CONSUMED);
	}
}
