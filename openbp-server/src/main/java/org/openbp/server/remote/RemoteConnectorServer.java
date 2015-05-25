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
package org.openbp.server.remote;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.openbp.common.ReflectUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.registry.MappingRegistry;
import org.openbp.common.setting.SettingUtil;
import org.openbp.core.CoreConstants;
import org.openbp.core.engine.EngineException;
import org.openbp.core.remote.ClientConnectionInfo;
import org.openbp.core.remote.RemoteConnector;
import org.openbp.server.ServerConstants;

/**
 * Implementation of the remote connector server interface.
 * Performs the lookup of interface through the service registry of the process server.
 */
public class RemoteConnectorServer extends UnicastRemoteObject
	implements RemoteConnector
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Client connection info */
	private ClientConnectionInfo connectionInfo;

	/** Service registry */
	private MappingRegistry serviceRegistry;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @throws RemoteException On error
	 */
	public RemoteConnectorServer()
		throws RemoteException
	{
	}

	/**
	 * Binds the class to the RMI registry.
	 *
	 * @throws RemoteException On error
	 */
	public void bindToRegistry()
		throws RemoteException
	{
		try
		{
			if (connectionInfo == null)
				return;

			// Create an RMI registry
			if (connectionInfo.getRmiServerPort() == 0)
				throw new EngineException("Connection.Properties", "No RMI port specified for service registry.");

			// Create an RMI registry
			int rmiServerPort = connectionInfo.getRmiServerPort();

			LogUtil.info(getClass(), "Binding to RMI registry using binding name $0, port {1}.", CoreConstants.RMI_BINDING_NAME, Integer.valueOf(rmiServerPort));

			Registry registry = null;
			try
			{
				// This fails, if OpenBP has already been started & stopped in the same VM instance before.
				registry = LocateRegistry.createRegistry(rmiServerPort);
			}
			catch (RemoteException e)
			{
				if (e instanceof java.rmi.server.ExportException)
				{
					// registry probably already exists
					registry = LocateRegistry.getRegistry(rmiServerPort);
				}
				else
					throw e;
			}

			// Bind the service to the RMI registry
			registry.rebind(CoreConstants.RMI_BINDING_NAME, this);
		}
		catch (RemoteException e)
		{
			String msg = LogUtil.error(getClass(), "Error binding remote interface.", e);
			String bindingErrorHandler = SettingUtil.getStringSetting(ServerConstants.SYSPROP_RMIBINDINGERRORHANDLING);
			if ("ignore".equals(bindingErrorHandler))
			{
			}
			else if ("output".equals(bindingErrorHandler))
			{
				System.err.println(msg);
			}
			else
			{
				throw new EngineException("Initialization", msg, e);
			}
		}
	}

	/**
	 * Unbinds the class from the RMI registry.
	 *
	 * @throws RemoteException On error
	 */
	public void unbindFromRegistry()
		throws RemoteException
	{
		try
		{
			// Create an RMI registry
			if (connectionInfo == null || connectionInfo.getRmiServerPort() == 0)
				return;

			// Create an RMI registry
			int rmiServerPort = connectionInfo.getRmiServerPort();
			Registry registry = LocateRegistry.getRegistry(rmiServerPort);

			// Unbind the service from the RMI registry
			registry.unbind(CoreConstants.RMI_BINDING_NAME);
		}
		catch (NotBoundException e)
		{
			// Ignore
		}
		catch (Exception e)
		{
			String msg = LogUtil.error(getClass(), "Error unbinding remote interface.", e);
			String bindingErrorHandler = SettingUtil.getStringSetting(ServerConstants.SYSPROP_RMIBINDINGERRORHANDLING);
			if ("ignore".equals(bindingErrorHandler))
			{
			}
			else if ("output".equals(bindingErrorHandler))
			{
				System.err.println(msg);
			}
			else
			{
				throw new EngineException("Shutdown", msg, e);
			}
		}
	}

	/**
	 * Gets the client connection info.
	 * @nowarn
	 */
	public ClientConnectionInfo getConnectionInfo()
	{
		return connectionInfo;
	}

	/**
	 * Sets the client connection info.
	 * @nowarn
	 */
	public void setConnectionInfo(ClientConnectionInfo connectionInfo)
	{
		this.connectionInfo = connectionInfo;
	}

	/**
	 * Gets the service registry.
	 * @nowarn
	 */
	public MappingRegistry getServiceRegistry()
	{
		return serviceRegistry;
	}

	/**
	 * Sets the service registry.
	 * @nowarn
	 */
	public void setServiceRegistry(MappingRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}

	//////////////////////////////////////////////////
	// @@ RemoteConnector implementation
	//////////////////////////////////////////////////

	/**
	 * Invokes a method of a remote service.
	 *
	 * @param interfaceName Fully qualified name of the interface that contains the method
	 * @param methodSignature Method signature as generated by {@link ReflectUtil#getUnqualifiedMethodSignature}
	 * @param args Method arguments or null
	 * @return Return value of the remote method or null for void methods
	 * @throws RemoteException On error
	 */
	public Object invokeMethod(String interfaceName, String methodSignature, Object[] args)
		throws RemoteException
	{
		// TODO Optimize 6 Cache Method objects

		// Retrieve an implementation for the given interface
		Object implementation = serviceRegistry.lookup(interfaceName);
		if (implementation == null)
		{
			String msg = LogUtil.error(getClass(), "No implementation found for interface $0.", interfaceName);
			throw new EngineException("Initialization", msg);
		}

		try
		{
			Method method = ReflectUtil.findByUnqualifiedMethodSignature(implementation.getClass(), methodSignature);
			return method.invoke(implementation, args);
		}
		catch (Throwable e)
		{
			if (e instanceof RemoteException)
				throw (RemoteException) e;
			if (e instanceof InvocationTargetException)
				e = ((InvocationTargetException) e).getTargetException();
			String msg = LogUtil.error(getClass(), "Error invoking remote method $0.", interfaceName + "." + methodSignature, e);
			throw new RemoteException(msg, e);
		}
	}
}
