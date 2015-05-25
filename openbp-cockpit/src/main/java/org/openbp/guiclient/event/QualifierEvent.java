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
package org.openbp.guiclient.event;

import org.openbp.core.model.ModelQualifier;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * JaspiraEvent that carries a model qualifier as Object.
 *
 * @author Stephan Moritz
 */
public class QualifierEvent extends JaspiraEvent
{
	/**
	 * Constructor.
	 *
	 * @param source The plugin that dispatches this event. Must not be null.
	 * Will be converted to lower case.
	 * @param eventName The name of the event
	 * @param qualifier Model qualifier to transport
	 */
	public QualifierEvent(Plugin source, String eventName, ModelQualifier qualifier)
	{
		super(source, eventName, qualifier);
	}

	/**
	 * Returns the model qualifier that this event carries.
	 * @nowarn
	 */
	public ModelQualifier getQualifier()
	{
		return (ModelQualifier) getObject();
	}
}
