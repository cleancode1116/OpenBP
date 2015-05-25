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
package org.openbp.server.jms;

import java.util.Iterator;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.NamingException;

import org.openbp.common.logger.LogUtil;
import org.openbp.common.setting.SettingUtil;

/**
 * Issues process execution messages to a JMS server in otder to invoke processes asynchronously.
 *
 * @author Heiko Erhardt
 */
public class JMSExecutionRequester
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Topic name */
	private String topicName;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** JMS session */
	private Session session;

	/** Sender */
	private MessageProducer sender;

	/** Initialized */
	private boolean initialized;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public JMSExecutionRequester()
	{
		setTopicName(SettingUtil.getStringSetting(JMSConstants.PROP_TOPIC, getTopicName()));
	}

	/**
	 * Finalizer.
	 * Gracefully closes any open connections.
	 */
	protected void finalize()
	{
		if (session != null)
		{
			try
			{
				session.close();
			}
			catch (JMSException ex)
			{
				LogUtil.error(getClass(), "Failed to close JMS connection.", ex);
			}
			session = null;
		}

		JMSManager.getInstance().close();
	}

	/**
	 * Reads topic and subscriber name from the properties and 
	 * opens a connection to the JMS server.
	 *
	 * @throws NamingException On invalid JNDI name
	 * @throws JMSException On error
	 */
	public void initialize()
		throws NamingException, JMSException
	{
		if (!initialized)
		{
			JMSManager.getInstance().initialize();

			// Look up the destination
			Destination destination = (Destination) JMSManager.getInstance().lookupFromJNDI(topicName);

			// Create the session
			session = JMSManager.getInstance().createSession();

			// Create the sender
			sender = session.createProducer(destination);
		}
	}

	//////////////////////////////////////////////////
	// @@ Execution request
	//////////////////////////////////////////////////

	/**
	 * Requests the execution of the specified process via JMS.
	 *
	 * @param startRef Process reference
	 * @param parameters Map of parameters or null
	 * @throws NamingException On invalid JNDI name
	 * @throws JMSException On error
	 */
	public void requestExecution(String startRef, Map parameters)
		throws NamingException, JMSException
	{
		initialize();

		MapMessage msg = session.createMapMessage();

		msg.setString(JMSConstants.MAP_PROCESS_REF, startRef);

		if (parameters != null)
		{
			for (Iterator it = parameters.entrySet().iterator(); it.hasNext();)
			{
				Map.Entry entry = (Map.Entry) it.next();
				msg.setObject(JMSConstants.MAP_ARG_PREFIX + entry.getKey(), entry.getValue());
			}
		}

		sender.send(msg);
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the topic name.
	 * @nowarn
	 */
	public String getTopicName()
	{
		return topicName;
	}

	/**
	 * Sets the topic name.
	 * @nowarn
	 */
	public void setTopicName(String topicName)
	{
		this.topicName = topicName;
	}
}
