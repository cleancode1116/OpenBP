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
package org.openbp.client.remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.openbp.common.ReflectUtil;
import org.openbp.core.remote.ClientConnectionInfo;

/**
 * Remote service factory.
 * This factory returns instances of all services of the openBP server that can be invoked by a client.
 * This class is a singleton.
 */
public final class RemoteServiceFactory
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static RemoteServiceFactory singletonInstance;

	/** Dynamic proxy invocation handler */
	private RemoteInvocationHandler handler;

	/** Remote connector client side */
	private RemoteConnectorClient connectorClient;

	/** Table of service proxies (maps interface names to proxy instances) */
	private Map serviceProxies;

	/** Flag if we have connected to the server */
	private boolean connected;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized RemoteServiceFactory getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new RemoteServiceFactory();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private RemoteServiceFactory()
	{
		connectorClient = new RemoteConnectorClient();
		handler = new RemoteInvocationHandler(connectorClient);
		serviceProxies = new HashMap();
	}

	/**
	 * Sets the connection info to the OpenBP server.
	 * @nowarn
	 */
	public void setConnectionInfo(String host, int port)
	{
		ClientConnectionInfo clientConnectionInfo = new ClientConnectionInfo();
		clientConnectionInfo.setRmiServerHost(host);
		clientConnectionInfo.setRmiServerPort(port);
		setConnectionInfo(clientConnectionInfo);
	}

	/**
	 * Sets the connection info to the OpenBP server.
	 * @nowarn
	 */
	public void setConnectionInfo(ClientConnectionInfo connectionInfo)
	{
		connectorClient.setConnectionInfo(connectionInfo);
	}

	//////////////////////////////////////////////////
	// @@ Service creation methods
	//////////////////////////////////////////////////

	/**
	 * Gets an instance of the sepcified service class, either from the cache or as dynamic proxy.
	 *
	 * @param cls Service interface name
	 * @return The service instance
	 */
	public Object getService(Class cls)
	{
		if (!connected)
		{
			connectorClient.connectToServer();
		}

		Object service = serviceProxies.get(cls.getName());

		if (service == null)
		{
			service = Proxy.newProxyInstance(getClass().getClassLoader(), new Class [] { cls }, handler);
			serviceProxies.put(cls.getName(), service);
		}

		return service;
	}

	/**
	 * Invocation handler class for the dynamic proxy that is used for remote method call.
	 */
	private static class RemoteInvocationHandler
		implements InvocationHandler
	{
		/** Connector client */
		private RemoteConnectorClient connectorClient;

		/**
		 * Constructor.
		 *
		 * @param connectorClient Client part of the connector to call
		 */
		public RemoteInvocationHandler(RemoteConnectorClient connectorClient)
		{
			this.connectorClient = connectorClient;
		}

		/**
		 * InvocationHandler implementation method.
		 *
		 * @param proxy The dynamic proxy object
		 * @param method Method to call
		 * @param args Method arguments
		 * @return method return value
		 * @throws Throwable On error
		 */
		public Object invoke(Object proxy, Method method, Object [] args)
			throws Throwable
		{
			String interfaceHandler = proxy.getClass().getInterfaces() [0].getName();
			String methodSignature = ReflectUtil.getUnqualifiedMethodSignature(method);
			return connectorClient.invokeMethod(interfaceHandler, methodSignature, args);
		}
	}
}
