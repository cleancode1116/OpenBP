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
package org.openbp.server;

import java.util.Iterator;
import java.util.List;

import org.openbp.common.CollectionUtil;
import org.openbp.common.CommonRegistry;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.application.Application;
import org.openbp.common.generic.LifecycleSupport;
import org.openbp.common.generic.msgcontainer.StandardMsgContainer;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.registry.MappingRegistry;
import org.openbp.core.CoreModule;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.Model;
import org.openbp.core.remote.ClientConnectionInfo;
import org.openbp.server.context.SessionRegistry;
import org.openbp.server.context.TokenContextService;
import org.openbp.server.context.serializer.ContextObjectSerializerRegistry;
import org.openbp.server.engine.Engine;
import org.openbp.server.engine.EngineRunner;
import org.openbp.server.persistence.PersistenceContextProvider;
import org.openbp.server.remote.RemoteConnectorServer;
import org.openbp.server.scheduler.ProcessScheduler;

/**
 * This class is the main anchor point of an OpenBP server.
 * It performs all server startup and shutdown tasks.
 * Note that you must call the {@link #initialize} method before you can request any services from the server.
 *
 * @author Heiko Erhardt
 */
public class ProcessServer extends CoreModule
{
	//////////////////////////////////////////////////
	// @@ Components configured by Spring
	//////////////////////////////////////////////////

	/** Process facade */
	private ProcessFacade processFacade;

	/** Engine */
	private Engine engine;

	/** Engine runner */
	private EngineRunner engineRunner;

	/** Token context service */
	private TokenContextService tokenContextService;

	/** Session registry */
	private SessionRegistry sessionRegistry;

	/** Process scheduler */
	private ProcessScheduler processScheduler;

	/** Service implementations part 1 */
	private List services;

	/** Service implementations part 2 */
	private List services2;

	/** Service registry */
	private final MappingRegistry serviceRegistry;

	// TODO Cleanup 5 The next 2 members will be obsolete after reference to common registry has been remvoed

	/** Persistence context provider */
	private PersistenceContextProvider persistenceContextProvider;

	/** Context object serializer registry */
	private ContextObjectSerializerRegistry contextObjectSerializerRegistry;

	/** Connection info */
	private ClientConnectionInfo connectionInfo;

	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Initialization flag */
	private boolean initialized;

	/** Server shutdown hook */
	private ServerShutdownHook serverShutdownHook;

	/** Remote connector server */
	private RemoteConnectorServer remoteConnectorServer;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Protected constructor - to be used by the {@link ProcessServerFactory} only.
	 */
	protected ProcessServer()
	{
		serviceRegistry = new MappingRegistry();
	}

	//////////////////////////////////////////////////
	// @@ Initialization
	//////////////////////////////////////////////////

	/**
	 * Initializes the server.
	 * Call this method before requesting any services from the core.
	 *
	 * @throws OpenBPException On severe error
	 */
	public synchronized void initialize()
	{
		if (initialized)
			return;

		determineRootDir();
		Application.registerPropertyResource("OpenBP-Server.properties", 80, false);

		// Note: It is important to keep the order of these statements!

		// Perform the core initialization
		super.initialize();

		CommonRegistry.register(persistenceContextProvider);
		CommonRegistry.register(contextObjectSerializerRegistry);

		// Register implementations of OpenBP service interfaces in the service registry
		initServices(services);

		// Initialize the persistence layer
		initPersistence();

		// Reads all models
		initModels();

		// Initializes advanced of the system
		initServices(services2);

		// Register a shutdown hook that allows correct database shutdown
		registerShutdownHook();

		// Initializes the remote services (if necessary).
		initRemoting();

		initialized = true;
	}

	/**
	 * Registers implementations of the specified OpenBP service interfaces in the common registry.
	 *
	 * @param l List of services to initialize
	 * @throws OpenBPException On error
	 */
	private void initServices(List l)
	{
		if (l != null)
		{
			for (Iterator it = l.iterator(); it.hasNext();)
			{
				Object o = it.next();

				if (o instanceof LifecycleSupport)
				{
					((LifecycleSupport) o).initialize();
				}

				serviceRegistry.register(o);
			}
		}
	}

	/**
	 * Shuts down the specified OpenBP services.
	 *
	 * @param l List of services to initialize
	 * @throws OpenBPException On error
	 */
	private void shutdownServices(List l)
	{
		if (l != null)
		{
			for (Iterator it = l.iterator(); it.hasNext();)
			{
				Object o = it.next();

				if (o instanceof LifecycleSupport)
				{
					((LifecycleSupport) o).shutdown();
				}
			}
		}
	}

	/**
	 * Initializes the persistence layer.
	 */
	protected void initPersistence()
	{
		if (getPersistenceContextProvider() != null)
		{
			getPersistenceContextProvider().initialize();
		}
	}

	/**
	 * Shuts down the persistence layer.
	 */
	protected void shutdownPersistence()
	{
		if (getPersistenceContextProvider() != null)
		{
			getPersistenceContextProvider().shutdown();
		}
	}

	/**
	 * Registers the services and start up the RMI registry.
	 *
	 * @throws OpenBPException On error
	 */
	private void initRemoting()
	{
		try
		{
			// Determine remote connection properties
			if (connectionInfo == null)
			{
				// Load connection info from properties file if not specified in the Spring config file
				connectionInfo = new ClientConnectionInfo();
				connectionInfo.loadFromProperties();
			}

			// Make the remote connector known to the RMI registry
			if (connectionInfo.isEnabled())
			{
				remoteConnectorServer = new RemoteConnectorServer();
				remoteConnectorServer.setServiceRegistry(getServiceRegistry());
				remoteConnectorServer.setConnectionInfo(connectionInfo);
				remoteConnectorServer.bindToRegistry();
			}
		}
		catch (Exception e)
		{
			throw new EngineException("Initialization", "Error initializing services.", e);
		}
	}

	/**
	 * Initializes the model manager.
	 *
	 * @throws OpenBPException On error
	 */
	private void initModels()
	{
		LogUtil.info(getClass(), "Loading models...");

		// Read all models
		getModelMgr().readModels();

		// Initialize the models
		getModelMgr().initializeModels();

		// Print any errors to stderr
		StandardMsgContainer msgContainer = getModelMgr().getMsgContainer();
		String errMsg = msgContainer.toString();
		if (errMsg != null && ! errMsg.equals(""))
		{
			msgContainer.clearMsgs();
			System.err.println(errMsg);
			System.err.println();
		}
	}

	//////////////////////////////////////////////////
	// @@ Shutdown
	//////////////////////////////////////////////////

	/**
	 * Performs shutdown of the process server using a 60 second timeout.
	 *
	 * @param unregisterHook 
	 *		true	Unregister the VM's shutdown hook. Note that this may not be done during VM shutdown! (when invoked due to servlet reaload etc.)<br>
	 *		false	Do not unregister the hook (when invoked due to VM termination)
	 * Calls shutdownModel for each loaded model.
	 */
	public boolean shutdown(boolean unregisterHook)
	{
		return shutdown(60000L, unregisterHook);
	}

	/**
	 * Performs shutdown of the process server.
	 * Before the server shutdown, the engine runner (if present) will be shut down (see {@link EngineRunner#waitForStop}).
	 * Also calls {@link Model#shutdownModel} for each loaded model.
	 * The method will ignore any exception, however, they will be logged.
	 *
	 * @param timeoutMS Timeout in milliseconds for the engine runner shutdown.
	 * If this value is 0, the method will just check if everything has completed, but will not wait for any processes.
	 * If this value is -1, no timeout will apply (i. e. the method will definately wait
	 * until all context executions have finished).
	 * @param unregisterHook 
	 *		true	Unregister the VM's shutdown hook. Note that this may not be done during VM shutdown!
	 *				(when invoked due to servlet reaload etc.)<br>
	 *		false	Do not unregister the hook (when invoked due to VM termination)
	 * @return true if no context is currently executing and there were no exceptions during shutdown,
	 * false if either the engine runner shutdown timeout has elapsed or there have been exceptions during the shutdown.
	 */
	public boolean shutdown(long timeoutMS, boolean unregisterHook)
	{
		boolean ret = true;

		synchronized (this)
		{
			if (initialized)
			{
				LogUtil.info(getClass(), "Shutting down the OpenBP engine...");

				try
				{
					if (getEngineRunner() != null)
					{
						ret = getEngineRunner().waitForStop(timeoutMS);
					}

					if (remoteConnectorServer != null)
					{
						try
						{
							remoteConnectorServer.unbindFromRegistry();
						}
						catch (Exception e)
						{
							// Ignore errors when unbing, we are shutting down anyway
							// We also don't log this one.
							ret = false;
						}
						remoteConnectorServer = null;
					}

					// Shutdown all models
					for (Iterator it = getModelMgr().getModels().iterator(); it.hasNext();)
					{
						Model model = (Model) it.next();
						try
						{
							shutdownModel(model);
						}
						catch (Exception e)
						{
							LogUtil.error(getClass(), "Error during shutdown of model.", model.getQualifier(), e);
							ret = false;
						}
					}

					shutdownServices(services2);

					shutdownPersistence();

					shutdownServices(services);

					super.shutdown(unregisterHook);

					if (unregisterHook)
					{
						unregisterShutdownHook();
					}
				}
				catch (Exception e)
				{
					LogUtil.error(getClass(), "Error during OpenBP engine shutdown.", e);
					ret = false;
				}
				finally
				{
					LogUtil.info(getClass(), "OpenBP engine shutdown complete.");

					initialized = false;
				}
			}
		}

		return ret;
	}

	/**
	 * Shuts down the given model and its sub models.
	 *
	 * @param model Model to shut down or null for all models
	 */
	private void shutdownModel(Model model)
	{
		// Shut down the model; catch any exception

		// TODO Feature 5 Trigger the appropriate model event
		try
		{
			model.shutdownModel();
		}
		catch (Exception e)
		{
			LogUtil.error(getClass(), "Error while shutting down model $0", model.getQualifier(), e);
		}
	}

	/**
	 * Registers a shutdown hook that allows correct database shutdown.
	 */
	private void registerShutdownHook()
	{
		serverShutdownHook = new ServerShutdownHook();
		Runtime.getRuntime().addShutdownHook(serverShutdownHook);
	}

	/**
	 * Registers a shutdown hook that allows correct database shutdown.
	 */
	private void unregisterShutdownHook()
	{
		if (serverShutdownHook != null)
		{
			Runtime.getRuntime().removeShutdownHook(serverShutdownHook);
			serverShutdownHook = null;
		}
	}

	/**
	 * Server shutdown hook.
	 */
	private class ServerShutdownHook extends Thread
	{
		/**
		 * Constructor.
		 */
		public ServerShutdownHook()
		{
			// This thread must not prevent VM shutdown.
			setDaemon(true);
		}

		/**
		 * Called when the virtual machine is about to terminate.
		 * This can be because the exit method has been called or CTRL+C has been pressed.
		 */
		public void run()
		{
			ProcessServer.this.shutdown(false);
		}
	}

	//////////////////////////////////////////////////
	// @@ Components setters/getters
	//////////////////////////////////////////////////

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

	/**
	 * Gets the engine.
	 * @nowarn
	 */
	public Engine getEngine()
	{
		return engine;
	}

	/**
	 * Sets the engine.
	 * @nowarn
	 */
	public void setEngine(Engine engine)
	{
		this.engine = engine;
	}

	/**
	 * Gets the engine runner.
	 * @nowarn
	 */
	public EngineRunner getEngineRunner()
	{
		return engineRunner;
	}

	/**
	 * Sets the engine runner.
	 * @nowarn
	 */
	public void setEngineRunner(EngineRunner engineRunner)
	{
		this.engineRunner = engineRunner;
	}

	/**
	 * Gets the token context service.
	 * @nowarn
	 */
	public TokenContextService getTokenContextService()
	{
		return tokenContextService;
	}

	/**
	 * Sets the token context service.
	 * @nowarn
	 */
	public void setTokenContextService(TokenContextService tokenContextService)
	{
		this.tokenContextService = tokenContextService;
	}

	/**
	 * Gets the session registry.
	 * @nowarn
	 */
	public SessionRegistry getSessionRegistry()
	{
		return sessionRegistry;
	}

	/**
	 * Sets the session registry.
	 * @nowarn
	 */
	public void setSessionRegistry(SessionRegistry sessionRegistry)
	{
		this.sessionRegistry = sessionRegistry;
	}

	/**
	 * Gets the process scheduler.
	 * @nowarn
	 */
	public ProcessScheduler getProcessScheduler()
	{
		return processScheduler;
	}

	/**
	 * Sets the process scheduler.
	 * @nowarn
	 */
	public void setProcessScheduler(ProcessScheduler processScheduler)
	{
		this.processScheduler = processScheduler;
	}

	/**
	 * Gets the service implementations part 1.
	 * @nowarn
	 */
	public List getServices()
	{
		return services;
	}

	/**
	 * Sets the service implementations part 1.
	 * @nowarn
	 */
	public void setServices(List services)
	{
		this.services = services;
	}

	/**
	 * Gets the service implementations part 2.
	 * @nowarn
	 */
	public List getServices2()
	{
		return services2;
	}

	/**
	 * Sets the service implementations part 2.
	 * @nowarn
	 */
	public void setServices2(List services2)
	{
		this.services2 = services2;
	}

	/**
	 * Gets the service of the specified class.
	 *
	 * @param serviceClass Service class
	 * @return The service instance or null
	 */
	public Object getService(Class serviceClass)
	{
		for (Iterator it = CollectionUtil.iterator(services); it.hasNext();)
		{
			Object s = it.next();
			if (serviceClass.isInstance(s))
				return s;
		}
		for (Iterator it2 = CollectionUtil.iterator(services2); it2.hasNext();)
		{
			Object s = it2.next();
			if (serviceClass.isInstance(s))
				return s;
		}
		return null;
	}

	/**
	 * Gets the persistence context provider.
	 * @nowarn
	 */
	public PersistenceContextProvider getPersistenceContextProvider()
	{
		return persistenceContextProvider;
	}

	/**
	 * Sets the persistence context provider.
	 * @nowarn
	 */
	public void setPersistenceContextProvider(PersistenceContextProvider persistenceContextProvider)
	{
		this.persistenceContextProvider = persistenceContextProvider;
	}

	/**
	 * Gets the context object serializer registry.
	 * @nowarn
	 */
	public ContextObjectSerializerRegistry getContextObjectSerializerRegistry()
	{
		return contextObjectSerializerRegistry;
	}

	/**
	 * Sets the context object serializer registry.
	 * @nowarn
	 */
	public void setContextObjectSerializerRegistry(ContextObjectSerializerRegistry contextObjectSerializerRegistry)
	{
		this.contextObjectSerializerRegistry = contextObjectSerializerRegistry;
	}

	/**
	 * Gets the connection info.
	 * @nowarn
	 */
	public ClientConnectionInfo getConnectionInfo()
	{
		return connectionInfo;
	}

	/**
	 * Sets the connection info.
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

	//////////////////////////////////////////////////
	// @@ Main class for simple test server mode
	//////////////////////////////////////////////////

	/**
	 * Main method for simple server test mode.
	 *
	 * @param arguments Command line argument array
	 */
	public static void main(String[] arguments)
	{
		long time = System.currentTimeMillis();

		try
		{
			// Initialize the server
			Application.setArguments(arguments);

			// Start up the server
			ProcessServer processServer = new ProcessServerFactory().createProcessServer();

			// Compute startup time and print startup message
			time = System.currentTimeMillis() - time;
			String startedUpMessage = "OpenBP test server startup complete [" + time + " ms].";
			System.out.println(startedUpMessage);

			processServer.shutdown(true);
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace("Error initializing OpenBP server", e);
			System.exit(1);
		}
	}
}
