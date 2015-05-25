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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openbp.common.setting.SettingUtil;

/**
 * Class that manages a single JMS connection and some associated objects.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public final class JMSManager
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Factory name */
	private String factoryName = "ConnectionFactory";

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** JNDI context */
	private Context context;

	/** Singleton instance */
	private static JMSManager singletonInstance;

	/** JMS connection factory */
	private ConnectionFactory factory;

	/** JMS connection */
	private Connection connection;

	/** Initialized */
	private boolean initialized;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized JMSManager getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new JMSManager();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private JMSManager()
	{
		setFactoryName(SettingUtil.getStringSetting(JMSConstants.PROP_FACTORY, getFactoryName()));
	}

	/**
	 * Obtains some basic objects from the JNDI context and opens a connection to the JMS server.
	 *
	 * @throws NamingException On error
	 * @throws JMSException If the JMS connection could not be established
	 */
	public void initialize()
		throws NamingException, JMSException
	{
		if (!initialized)
		{
			// Create the JNDI initial context
			context = new InitialContext();

			// Look up the connection factory
			factory = (ConnectionFactory) context.lookup(factoryName);

			// Open the connection
			connection = factory.createConnection();

			initialized = true;
		}
	}

	/**
	 * Closes the connection.
	 */
	public void close()
	{
		// Silently close the connection
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (JMSException e)
			{
				System.err.println("Failed to close connection: " + e);
			}
			connection = null;
		}
	}

	/**
	 * Starts sending or receiving messages.
	 *
	 * @throws JMSException On error
	 */
	public void start()
		throws JMSException
	{
		connection.start();
	}

	/**
	 * Stops sending or receiving messages.
	 *
	 * @throws JMSException On error
	 */
	public void stop()
		throws JMSException
	{
		connection.stop();
	}

	/**
	 * Creates a JMS session.
	 *
	 * @return The new session
	 * @throws JMSException On error
	 */
	public Session createSession()
		throws JMSException
	{
		return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	//////////////////////////////////////////////////
	// @@ Various methods
	//////////////////////////////////////////////////

	/**
	 * Performs a jndi lookup on the given name.
	 *
	 * @param name Name
	 * @return The resulting object or null
	 * @throws NamingException On error
	 */
	public Object lookupFromJNDI(String name)
		throws NamingException
	{
		return context.lookup(name);
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the factory name.
	 * @nowarn
	 */
	public String getFactoryName()
	{
		return factoryName;
	}

	/**
	 * Sets the factory name.
	 * @nowarn
	 */
	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	/**
	 * Gets the jMS connection.
	 * @nowarn
	 */
	public Connection getConnection()
	{
		return connection;
	}
}
