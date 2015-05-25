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

import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.NamingException;

import org.openbp.common.CommonRegistry;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.application.Application;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.setting.SettingUtil;
import org.openbp.server.ProcessFacade;
import org.openbp.server.ProcessServer;
import org.openbp.server.ProcessServerFactory;
import org.openbp.server.context.TokenContext;

/**
 * Receives process execution messages from a JMS server in order to invoke processes asynchronously.
 *
 * @author Heiko Erhardt
 */
public class JMSExecutionController
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Topic name */
	private String topicName;

	/** Subscription name */
	private String subscriptionName;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** JMS topic */
	private Topic topic;

	/** Process facade */
	private ProcessFacade processFacade;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public JMSExecutionController()
	{
		setTopicName(SettingUtil.getStringSetting(JMSConstants.PROP_TOPIC, getTopicName()));
		setSubscriptionName(SettingUtil.getStringSetting(JMSConstants.PROP_SUBSCRIBER, getSubscriptionName()));
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
		JMSManager.getInstance().initialize();

		topic = (Topic) JMSManager.getInstance().lookupFromJNDI(topicName);
	}

	//////////////////////////////////////////////////
	// @@ Message loop
	//////////////////////////////////////////////////

	/**
	 * Listens for messages.
	 */
	public void messageLoop()
	{
		Session session = null;

		try
		{
			// Create the session
			session = JMSManager.getInstance().getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
			JMSManager.getInstance().start();

			// Create the durable subscriber
			TopicSubscriber subscriber = session.createDurableSubscriber(topic, getSubscriptionName());

			for (;;)
			{
				Message message = subscriber.receive();
				if (message instanceof MapMessage)
				{
					try
					{
						MapMessage map = (MapMessage) message;

						// Create a new token.
						// Register the standard name of the debugger so we can trace the process
						TokenContext token = getProcessFacade().createToken();
						token.setDebuggerId("Deb1");

						// Check the map for process arguments
						HashMap inputParams = new HashMap();
						for (Enumeration en = map.getMapNames(); en.hasMoreElements();)
						{
							String key = (String) en.nextElement();
							if (key.startsWith(JMSConstants.MAP_ARG_PREFIX))
							{
								String paramName = key.substring(JMSConstants.MAP_ARG_PREFIX.length());
								Object paramValue = map.getObject(key);
								inputParams.put(paramName, paramValue);
							}
						}

						// E. g. "/CAMSPoC/CardProductionOrder.Start"
						final String startRef = map.getString(JMSConstants.MAP_PROCESS_REF);

						getProcessFacade().startToken(token, startRef, inputParams);
					}
					catch (Throwable t)
					{
						LogUtil.error(getClass(), "Error occured while processing JMS message.", t);
					}
				}
			}
		}
		catch (JMSException e)
		{
			// Silently close the session
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
		}
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

	/**
	 * Gets the subscription name.
	 * @nowarn
	 */
	public String getSubscriptionName()
	{
		return subscriptionName;
	}

	/**
	 * Sets the subscription name.
	 * @nowarn
	 */
	public void setSubscriptionName(String subscriptionName)
	{
		this.subscriptionName = subscriptionName;
	}

	/**
	 * Gets the process facade.
	 * @nowarn
	 */
	public ProcessFacade getProcessFacade()
	{
		return processFacade;
	}

	/**
	 * Sets the process facade.
	 * @nowarn
	 */
	public void setProcessFacade(ProcessFacade processFacade)
	{
		this.processFacade = processFacade;
	}

	//////////////////////////////////////////////////
	// @@ Main method for simple tests
	//////////////////////////////////////////////////

	/**
	 * Main entry point to the OpenBP server.
	 * @param args Command line arguments
	 */
	public static void main(String [] args)
	{
		try
		{
			Application.setArguments(args);

			// Initialize our JMS connection
			JMSManager.getInstance().initialize();

			// Put the requester in the common registry so the activities have access to it and may send JMS execution messages
			CommonRegistry.register(new JMSExecutionRequester());

			// Start up the server
			final ProcessServer processServer = new ProcessServerFactory().createProcessServer();

			// Launch the token execution thread
			new Thread(new Runnable()
					   {
						   public void run()
						   {
							   processServer.getProcessFacade().executePendingContextsInDifferentThread();
						   }
						}
						).start();

			// Create the JMS execution controller
			JMSExecutionController controller = new JMSExecutionController();
			controller.setProcessFacade(processServer.getProcessFacade());
			controller.initialize();

			String startedUpMessage = "OpenBP JMS server startup complete.";
			LogUtil.info(JMSExecutionController.class, startedUpMessage);
			System.out.println(startedUpMessage);

			// Run the JMS receiver loop in this thread
			controller.messageLoop();
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
			System.exit(1);
		}
		System.exit(0);
	}
}
