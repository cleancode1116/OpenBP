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
package org.openbp.server.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openbp.common.CommonRegistry;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.setting.SettingUtil;
import org.openbp.common.util.ToStringHelper;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.common.util.observer.EventObserver;
import org.openbp.common.util.observer.EventObserverMgr;
import org.openbp.core.CoreConstants;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.server.ServerConstants;
import org.openbp.server.engine.EngineEvent;
import org.openbp.server.engine.EngineTraceEvent;
import org.openbp.server.engine.EngineUtil;
import org.openbp.server.persistence.PersistentObjectBase;

/**
 * Standard implementation of the TokenContext interface.
 *
 * NOTE: If you make changes to this class, be sure to perform the also in CayenneTokenContextImpl, which is an exact copy of this class thanks to Java's inability to support multiple inheritance.
 *
 * @author Heiko Erhardt
 */
public class TokenContextImpl extends PersistentObjectBase
	implements TokenContext, Serializable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	//
	// Context hierarchy and workflow
	//

	/** Parent context */
	private TokenContext parentContext;

	/**
	 * Set of child contexts (contains {@link TokenContext} objects)
	 */
	private transient Set childContextSet;

	/**
	 * Node parameters (maps node-qualified parameter names (Strings) to
	 * parameter values (Objects)
	 */
	// Note that this is transient, parameter set will be saved as byte array
	private transient Map paramValues;

	/**
	 * Serialized context data. This byte array represents the context data that
	 * has been deserialized from persistent storage. In order to update this
	 * data, call {@link #updateSerialziedContextData} before saving the token context to
	 * persistent storage. we don't want to deserialize the context data
	 * right away after loading the token context object, so we just read
	 * the context data and deserialize it as soon as we need to access the
	 * content of the context. After deserialization, this member will be
	 * cleared.
	 */
	private transient byte[] serializedContextData;

	/** Runtime attribute table */
	protected transient Map runtimeAttributes;

	/** Model manager */
	private transient ModelMgr modelMgr;

	//
	// Process execution
	//

	/**
	 * Base model under which the processes using this context executes. The
	 * executing model will be used for item lookups.
	 */
	protected transient Model executingModel;

	/** Current socket (entry or exit) of the node that is executing */
	private transient NodeSocket currentSocket;

	/**
	 * Sub process call stack. The call stack contains the position in the
	 * calling process (the input socket of the calling sub process node,
	 * {@link NodeSocket} objects).
	 */
	private CallStack callStack;

	/** Lifecycle state */
	private int lifecycleState;

	/** Lifecycle request */
	private int lifecycleRequest;

	/** Priority */
	private int priority;

	/** Type of queue for the current node */
	private String queueType;

	/**
	 * Name of the cluster node that currently processes this context.
	 * Valid only for token contexts that have the lifecycle state SELECTED or RUNNING.
	 */
	protected String nodeId;

	/** User who owns this token context (optional) */
	protected String userId;

	/** Id of the debugger that is currently tracing this context (optional) */
	protected String debuggerId;

	/** Progress info object */
	private ProgressInfo progressInfo;

	/** Engine observer manager that is local to this token */
	private transient EventObserverMgr observerMgr;

	/** Flag that determines if accessing an undefined process variable should cause an exception */
	private static Boolean strictProcessVariableHandling;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public TokenContextImpl()
	{
		setLifecycleState(LifecycleState.CREATED);
		setLifecycleRequest(LifecycleRequest.NONE);
		childContextSet = new HashSet();
	}

	/**
	 * Destroys context, i\.\e. removes it from its parent context.
	 */
	public void destroy()
	{
		TokenContext parent = getParentContext();
		if (parent != null)
		{
			parent.removeChildContext(this);
			setParentContext(null);
		}
	}

	/**
	 * Returns a string represenation of this object.
	 * 
	 * @return Debug string containing the most important properties of this object
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "id", "currentSocket.qualifier", "lifecycleStateStr", "lifecycleRequestStr", "parentContext.id",
			"executingModel.qualifier");
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/*
	 * @seem TokenContext.getParentContext
	 */
	public TokenContext getParentContext()
	{
		return parentContext;
	}

	/*
	 * @seem TokenContext.setParentContext
	 */
	public void setParentContext(final TokenContext parentContext)
	{
		this.parentContext = parentContext;
	}

	/*
	 * @seem TokenContext.addChildContext
	 */
	public void addChildContext(final TokenContext childContext)
	{
		childContextSet.add(childContext);
		childContext.setParentContext(this);
	}

	/*
	 * @seem TokenContext.removeChildContext
	 */
	public void removeChildContext(final TokenContext childContext)
	{
		if (childContext != null)
		{
			childContextSet.remove(childContext);
		}
	}

	/*
	 * @seem TokenContext.hasChildContext
	 */
	public boolean hasChildContext()
	{
		return childContextSet.size() != 0;
	}

	/*
	 * @seem TokenContext.getChildContexts
	 */
	public Iterator getChildContexts()
	{
		if (childContextSet == null)
			return EmptyIterator.getInstance();
		return childContextSet.iterator();
	}

	/*
	 * @seem TokenContext.getExecutingModel
	 */
	public Model getExecutingModel()
	{
		return executingModel;
	}

	/*
	 * @seem TokenContext.setExecutingModel
	 */
	public void setExecutingModel(final Model executingModel)
	{
		this.executingModel = executingModel;
	}

	/*
	 * @seem TokenContext.getCurrentSocket
	 */
	public NodeSocket getCurrentSocket()
	{
		return currentSocket;
	}

	/*
	 * @seem TokenContext.setCurrentSocket
	 */
	public void setCurrentSocket(final NodeSocket currentSocket)
	{
		this.currentSocket = currentSocket;
		if (currentSocket != null)
		{
			String queueType = currentSocket.getNode().getQueueType();
			setQueueType(queueType);
		}
		else
		{
			setQueueType(null);
		}
	}

	/*
	 * @seem TokenContext.getCallStack
	 */
	public CallStack getCallStack()
	{
		if (callStack == null)
		{
			callStack = new CallStackImpl(this);
		}
		return callStack;
	}

	/*
	 * @seem TokenContext.setCallStack
	 */
	public void setCallStack(final CallStack callStack)
	{
		this.callStack = callStack;
	}

	/*
	 * @seem TokenContext.getLifecycleState
	 */
	public int getLifecycleState()
	{
		return lifecycleState;
	}

	/*
	 * @seem TokenContext.setLifecycleState
	 */
	public void setLifecycleState(final int lifecycleState)
	{
		this.lifecycleState = lifecycleState;
	}

	public String getLifecycleStateStr()
	{
		return LifecycleState.toString(getLifecycleState());
	}

	/*
	 * @seem TokenContext.getLifecycleRequest
	 */
	public int getLifecycleRequest()
	{
		return lifecycleRequest;
	}

	/*
	 * @seem TokenContext.setLifecycleRequest
	 */
	public void setLifecycleRequest(final int lifecycleRequest)
	{
		this.lifecycleRequest = lifecycleRequest;

		synchronized (this)
		{
			notifyAll();
		}
	}

	public String getLifecycleRequestStr()
	{
		return LifecycleRequest.toString(getLifecycleRequest());
	}

	/*
	 * @seem TokenContext.waitLifecycleRequest
	 */
	public void waitLifecycleRequest(final int lifecycleRequest)
	{
		for (;;)
		{
			if (getLifecycleRequest() == lifecycleRequest)
				break;

			synchronized (this)
			{
				try
				{
					wait();
				}
				catch (InterruptedException e)
				{
				}
			}
		}
	}

	/*
	 * @seem TokenContext.getPriority
	 */
	public int getPriority()
	{
		return priority;
	}

	/*
	 * @seem TokenContext.getPriority
	 */
	public void setPriority(final int priority)
	{
		this.priority = priority;
	}

	/*
	 * @seem TokenContext.getQueueType
	 */
	public String getQueueType()
	{
		return queueType;
	}

	/*
	 * @seem TokenContext.setQueueType
	 */
	public void setQueueType(final String queueType)
	{
		this.queueType = queueType;
	}

	/*
	 * @seem TokenContext.getNodeId
	 */
	public String getNodeId()
	{
		return nodeId;
	}

	/*
	 * @seem TokenContext.getNodeId
	 */
	public void setNodeId(final String nodeId)
	{
		this.nodeId = nodeId;
	}

	/*
	 * @seem TokenContext.getUserId
	 */
	public String getUserId()
	{
		return userId;
	}

	/*
	 * @seem TokenContext.setUserId
	 */
	public void setUserId(final String userId)
	{
		this.userId = userId;
	}

	/*
	 * @seem TokenContext.getDebuggerId
	 */
	public String getDebuggerId()
	{
		return debuggerId;
	}

	/*
	 * @seem TokenContext.setDebuggerId
	 */
	public void setDebuggerId(final String debuggerId)
	{
		this.debuggerId = debuggerId;
	}

	/*
	 * @seem TokenContext.getProgressInfo
	 */
	public ProgressInfo getProgressInfo()
	{
		ensureProgressInfo();
		return progressInfo;
	}

	/*
	 * @seem TokenContext.setProgressInfo
	 */
	public void setProgressInfo(final ProgressInfo progressInfo)
	{
		this.progressInfo = progressInfo;
	}

	/*
	 * @seem TokenContext.getProgressCount
	 */
	public int getProgressCount()
	{
		ensureProgressInfo();
		return progressInfo.getProgressCount();
	}

	/*
	 * @seem TokenContext.setProgressCount
	 */
	public void setProgressCount(final int progressCount)
	{
		ensureProgressInfo();
		progressInfo.setProgressCount(progressCount);
	}

	/*
	 * @seem TokenContext.getProgressTotal
	 */
	public int getProgressTotal()
	{
		ensureProgressInfo();
		return progressInfo.getProgressTotal();
	}

	/*
	 * @seem TokenContext.setProgressTotal
	 */
	public void setProgressTotal(final int progressTotal)
	{
		ensureProgressInfo();
		progressInfo.setProgressTotal(progressTotal);
	}

	/*
	 * @seem TokenContext.getProgressText
	 */
	public String getProgressText()
	{
		ensureProgressInfo();
		return progressInfo.getProgressText();
	}

	/*
	 * @seem TokenContext.setProgressText
	 */
	public void setProgressText(final String progressText)
	{
		ensureProgressInfo();
		progressInfo.setProgressText(progressText);
	}

	private void ensureProgressInfo()
	{
		if (progressInfo == null)
		{
			progressInfo = new ProgressInfo();
		}
	}

	/**
	 * Gets the serialized context data.
	 * @nowarn
	 */
	public byte[] getContextData()
	{
		return serializedContextData;
	}

	/**
	 * Sets the serialized context data.
	 * @nowarn
	 */
	public void setContextData(final byte[] serializedContextData)
	{
		// Prepare for lazy deserialization
		this.serializedContextData = serializedContextData;
	}

	//////////////////////////////////////////////////
	// @@ Socket parameter access
	//////////////////////////////////////////////////

	/*
	 * @seem TokenContext.hasParamValue
	 */
	public boolean hasParamValue(final String qualParamName)
	{
		return getParamValues().containsKey(qualParamName);
	}

	/*
	 * @seem TokenContext.getParamValue
	 */
	public Object getParamValue(final String qualParamName)
	{
		TokenContextValue tcv = (TokenContextValue) getParamValues().get(qualParamName);
		if (tcv != null)
			return tcv.getValue();
		return null;
	}

	/*
	 * @seem TokenContext.setParamValue
	 */
	public void setParamValue(final String qualParamName, final Object value)
	{
		TokenContextValue tcv = obtainParamValue(qualParamName, true);
		tcv.setValue(value);
	}

	/*
	 * @seem TokenContext.removeParamValue
	 */
	public void removeParamValue(final String qualParamName)
	{
		getParamValues().remove(qualParamName);
	}

	/*
	 * @seem TokenContext.clearParamValues
	 */
	public void clearParamValues()
	{
		getParamValues().clear();
	}

	/*
	 * @seem TokenContext.getParamValues
	 */
	public Map getParamValues()
	{
		if (paramValues == null)
		{
			paramValues = Collections.synchronizedMap(new HashMap());
		}
		return paramValues;
	}

	protected TokenContextValue obtainParamValue(final String variableName, final boolean isPersistent)
	{
		TokenContextValue tcv = (TokenContextValue) getParamValues().get(variableName);
		if (tcv == null)
		{
			tcv = new TokenContextValue();
			tcv.setPersistentVariable(isPersistent);
			getParamValues().put(variableName, tcv);
		}
		return tcv;
	}

	//////////////////////////////////////////////////
	// @@ Process variables
	//////////////////////////////////////////////////

	/**
	 * Creates a new persistent process variable.
	 * Does nothing if the process variable already exists in this token or one of its parent tokens.
	 * Persistent process variables always have 'context' scope.
	 *
	 * @param variableName Name of the new process variable
	 * @param isPersistent true to create a persistent process variable, false for a transient one
	 */
	public void createProcessVariable(final String variableName, final boolean isPersistent)
	{
		obtainParamValue(CoreConstants.PROCESS_VARIABLE_INDICATOR + variableName, isPersistent);
	}

	/*
	 * @seem TokenContext.hasProcessVariableValue
	 */
	public boolean hasProcessVariableValue(final String variableName)
	{
		if (hasParamValue(CoreConstants.PROCESS_VARIABLE_INDICATOR + variableName))
			return true;
		TokenContext parentContext = getParentContext();
		if (parentContext != null)
			return parentContext.hasProcessVariableValue(variableName);
		return false;
	}

	/*
	 * @seem TokenContext.getProcessVariableValue
	 */
	public Object getProcessVariableValue(final String variableName)
	{
		TokenContextValue tcv = getProcessVariable(variableName, false);
		if (tcv != null)
		{
			return tcv.getValue();
		}
		return null;
	}

	/*
	 * @seem TokenContext.setProcessVariableValue
	 */
	public void setProcessVariableValue(final String variableName, final Object value)
	{
		TokenContextValue tcv = getProcessVariable(variableName, true);
		if (tcv != null)
		{
			tcv.setValue(value);
		}
	}

	/*
	 * @seem TokenContext.removeProcessVariableValue
	 */
	public void removeProcessVariableValue(final String variableName)
	{
		// Remove it from the token context
		removeParamValue(CoreConstants.PROCESS_VARIABLE_INDICATOR + variableName);
	}

	/**
	 * Gets the names of all process variables that are not null.
	 *
	 * @return An iterator of strings
	 */
	public Iterator getProcessVariableNames()
	{
		ArrayList ret = new ArrayList();
		for (Iterator it = getParamValues().keySet().iterator(); it.hasNext();)
		{
			String name = (String) it.next();
			if (TokenContextUtil.isProcessVariableIdentifier(name))
			{
				ret.add(name.substring(1));
			}
		}
		return ret.iterator();
	}

	protected TokenContextValue getProcessVariable(final String variableName, boolean mustExist)
	{
		for (TokenContext context = this; context != null; context = context.getParentContext())
		{
			TokenContextValue tcv = (TokenContextValue) context.getParamValues().get(CoreConstants.PROCESS_VARIABLE_INDICATOR + variableName);
			if (tcv != null)
				return tcv;
		}

		// Check if accessing an undefined process variable should cause an exception
		if (! mustExist)
		{
			if (strictProcessVariableHandling == null)
			{
				// Determine timeout from the settings
				boolean b = SettingUtil.getBooleanSetting(ServerConstants.SYSPROP_PROCESSVARIABLEHANDLING_STRICT, false);
				strictProcessVariableHandling = new Boolean(b);
			}
			if (strictProcessVariableHandling.booleanValue())
			{
				mustExist = true;
			}
		}
		if (mustExist)
		{
			String msg = LogUtil.error(getClass(), "Trying to access undefined process variable $0. [{1}]", variableName, this);
			throw new EngineException("UndefinedProcessVariable", msg);
		}

		// Unknown process variable will cause result null in relaxed mode
		return null;
	}

	//////////////////////////////////////////////////
	// @@ RuntimeAttributeContainer implementation
	//////////////////////////////////////////////////

	/*
	 * @seem TokenContext.getRuntimeAttributes
	 */
	public Map getRuntimeAttributes()
	{
		return runtimeAttributes;
	}

	/*
	 * @seem TokenContext.getRuntimeAttribute
	 */
	public Object getRuntimeAttribute(final String key)
	{
		if (runtimeAttributes != null)
			return runtimeAttributes.get(key);
		return null;
	}

	/*
	 * @seem TokenContext.setRuntimeAttribute
	 */
	public void setRuntimeAttribute(final String key, final Object value)
	{
		if (runtimeAttributes == null)
		{
			runtimeAttributes = new Hashtable();
		}
		runtimeAttributes.put(key, value);
	}

	/*
	 * @seem TokenContext.removeRuntimeAttribute
	 */
	public void removeRuntimeAttribute(final String key)
	{
		if (runtimeAttributes != null)
		{
			runtimeAttributes.remove(key);
			if (runtimeAttributes.isEmpty())
			{
				runtimeAttributes = null;
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Engine observation
	//////////////////////////////////////////////////

	/**
	 * Registers an observer.
	 *
	 * @param observer The observer; The observer's observeEvent method will receive events of the type
	 * {@link EngineEvent} or {@link EngineTraceEvent}
	 * @param eventTypes Lit of event types the observer wants to be notified of
	 * or null for all event types
	 */
	public void registerObserver(final EventObserver observer, final String[] eventTypes)
	{
		obtainObserverMgr().registerObserver(observer, eventTypes);
	}

	/**
	 * Unregisters an observer.
	 *
	 * @param observer The observer
	 */
	public void unregisterObserver(final EventObserver observer)
	{
		obtainObserverMgr().unregisterObserver(observer);
	}

	/**
	 * Suspends broadcasting of engine events.
	 *
	 * @return The previous suspend status
	 */
	public boolean suspendEngineEvents()
	{
		return obtainObserverMgr().suspendObserverEvents();
	}

	/**
	 * Resumes broadcasting of engine events.
	 */
	public void resumeEngineEvents()
	{
		obtainObserverMgr().resumeObserverEvents();
	}

	/**
	 * Checks if there are active engine event observers registered.
	 *
	 * @param eventType Type of event in question
	 * @return true if there is at least one observer
	 */
	public boolean hasActiveObservers(final String eventType)
	{
		return obtainObserverMgr().hasActiveObservers(eventType);
	}

	/**
	 * Notifies all registered observers about a engine event (for internal use only).
	 *
	 * @param event Engine event to dispatch
	 */
	public void fireEngineEvent(final EngineEvent event)
	{
		obtainObserverMgr().fireEvent(event);
	}

	protected EventObserverMgr obtainObserverMgr()
	{
		if (observerMgr == null)
		{
			setObserverMgr(new EventObserverMgr());
		}
		return observerMgr;
	}

	/**
	 * Gets the engine observer manager that is local to this token.
	 * @nowarn
	 */
	public EventObserverMgr getObserverMgr()
	{
		return observerMgr;
	}

	/**
	 * Sets the engine observer manager that is local to this token.
	 * @nowarn
	 */
	public void setObserverMgr(EventObserverMgr observerMgr)
	{
		this.observerMgr = observerMgr;

		if (observerMgr != null)
		{
			EngineUtil.prepareEngineObserverMgr(observerMgr);
		}
	}

	//////////////////////////////////////////////////
	// @@ PersistentObject support
	//////////////////////////////////////////////////

	/*
	 * @seem PersistentObject.onLoad
	 */
	public void onLoad()
	{
		applySerialziedContextData();
	}

	/*
	 * @seem PersistentObject.beforeSave
	 */
	public void beforeSave()
	{
		updateSerialziedContextData();
	}

	//////////////////////////////////////////////////
	// @@ O/R mapper support
	//////////////////////////////////////////////////

	// TODO Fix 2 Make the executingModel an optional value throughout the system
	/**
	 * Gets the executing model qualifier.
	 * @nowarn
	 */
	public String getExecutingModelQualifier()
	{
		if (executingModel != null)
			return executingModel.getQualifier().toString();
		return null;
	}

	/**
	 * Sets the executing model qualifier.
	 * @nowarn
	 */
	public void setExecutingModelQualifier(final String executingModelQualifier)
	{
		if (executingModelQualifier != null)
		{
			ModelQualifier qualifier = new ModelQualifier(executingModelQualifier);
			executingModel = getModelMgr().getModelByQualifier(qualifier);
		}
		else
		{
			executingModel = null;
		}
	}

	/**
	 * Gets the current socket qualifier.
	 * @nowarn
	 */
	public String getCurrentSocketQualifier()
	{
		NodeSocket currentSocket = getCurrentSocket();
		if (currentSocket != null)
			return currentSocket.getQualifier().toString();
		return null;
	}

	/**
	 * Sets the current socket qualifier.
	 * @nowarn
	 */
	public void setCurrentSocketQualifier(final String currentSocketQualifier)
	{
		if (currentSocketQualifier != null)
		{
			ModelQualifier qualifier = new  ModelQualifier(currentSocketQualifier);
			qualifier.setItemType(ItemTypes.PROCESS);
			setCurrentSocket(EngineUtil.determineNodeSocketFromQualifier(qualifier, getModelMgr()));
		}
		else
		{
			setCurrentSocket(null);
		}
	}

	/**
	 * Gets the set of child contexts.
	 * @nowarn
	 */
	public Set getChildContextSet()
	{
		return childContextSet;
	}

	/**
	 * Sets the set of child contexts.
	 * @nowarn
	 */
	public void setChildContextSet(final Set childContextSet)
	{
		this.childContextSet = childContextSet;
	}

	/**
	 * Ensures that the context is deserialized after being read from persistent
	 * storage.
	 */
	protected void applySerialziedContextData()
	{
		byte[] data = getContextData();
		if (data != null)
		{
			TokenContextUtil.fromByteArray(this, data);
		}
	}

	/**
	 * Updates the serialzied context data with actual context values.
	 */
	protected void updateSerialziedContextData()
	{
		setContextData(TokenContextUtil.toByteArray(this));
	}

	/**
	 * Gets the model manager.
	 * @nowarn
	 */
	public ModelMgr getModelMgr()
	{
		if (modelMgr == null)
		{
			modelMgr = (ModelMgr) CommonRegistry.lookup(ModelMgr.class);
		}
		return modelMgr;
	}
}
