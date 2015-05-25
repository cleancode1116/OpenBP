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
package org.openbp.server.persistence.cayenne;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.PersistenceState;
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
import org.openbp.server.context.CallStack;
import org.openbp.server.context.CallStackImpl;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.LifecycleState;
import org.openbp.server.context.ProgressInfo;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.context.TokenContextValue;
import org.openbp.server.engine.EngineEvent;
import org.openbp.server.engine.EngineTraceEvent;
import org.openbp.server.engine.EngineUtil;

/**
 * Cayenne-enabled implementation of the TokenContext interface.
 * Objects to be persisted by Cayenne must implement the Cayenne DataObject interface,
 * which involves operations that may change from Cayenne version to Cayenne version.
 * So this class extends the CayenneDataObject class (why the hell does Java not support multiple inheritance?).
 * The functionality of the class itself is achieved by copying the code from the TokenContextImpl class :-(.
 *
 * @author Heiko Erhardt
 */
public class CayenneTokenContextImpl extends CayenneObjectBase
	implements TokenContext, Serializable
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/**
	 * Node parameters (maps node-qualified parameter names (Strings) to
	 * parameter values (Objects)
	 */
	// Note that this is transient, parameter set will be saved as byte array
	private transient Map<String, Object> paramValues;

	/** Runtime attribute table */
	protected transient Map<String, Object> runtimeAttributes;

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

	/** Engine observer manager */
	private EventObserverMgr observerMgr;

	/** Flag that determines if accessing an undefined process variable should cause an exception */
	private static Boolean strictProcessVariableHandling;

	//
	// Fields to store transient the persistent data.
	// To avoid hitting the database outside readValuesFromCayenne and
	// writeValuesFromCayenne. Accessing the database outside this
	// methods may lead to inconsistent data.
	//
  	/** the parent context */
	protected transient TokenContext parentContext;

		/** the list of child contexts */	
	protected transient List<TokenContext> childContextList;
	
	/** new child contexts since the last call to readValuesFromCayenne and writeValuesToCayenne */
	protected transient List<TokenContext> addedContextList = new ArrayList<TokenContext>();
	
	/** deleted child contexts since the last call to readValuesFromCayenne and writeValuesToCayenne */
	protected transient List<TokenContext> removedContextList = new ArrayList<TokenContext>();

		/** current as string (database storage) */
	protected transient String currentSocketString;

	 	/** current lifecycle state */
	protected transient int lifecycleState;

		/** current lifecycle request */
	protected transient int lifecycleRequest;

		/** priority of the process */
	protected transient int priority;

		/** type of queue */
	protected transient String queueType;

		/** id of node this process is running on */
	protected transient String nodeId;

		/** id of user owning this process */
	protected transient String userId;

		/** id of process debugger */
	protected transient String debuggerId;

		/** partial progess */
	protected transient int progressCount;
		
		/** total progess */
	protected transient int progressTotal;

		/** progress message */
	protected transient String progressText;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public CayenneTokenContextImpl()
	{
		this.lifecycleState = LifecycleState.CREATED;
		this.lifecycleRequest = LifecycleRequest.NONE;
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
			parentContext = null;
		}
	}

	/**
	 * Returns a string represenation of this object.
	 * 
	 * @return Debug string containing the most important properties of this object
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "id", "currentSocket.qualifier", "lifecycleStateStr",
			"lifecycleRequestStr", "parentContext.id", "executingModel.qualifier");
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
		markAsModified();
	}

	/*
	 * @seem TokenContext.addChildContext
	 */
	public void addChildContext(final TokenContext childContext)
	{
		if (!removedContextList.remove(childContext))
			addedContextList.add(childContext);
		childContextList.add(childContext);
		childContext.setParentContext(this);
		markAsModified();
	}

	/*
	 * @seem TokenContext.removeChildContext
	 */
	public void removeChildContext(final TokenContext childContext)
	{
		if (childContext != null)
		{
			if (!addedContextList.remove(childContext))
				removedContextList.add(childContext);
			childContextList.remove(childContext);
			childContext.setParentContext(null);
			markAsModified();
		}
	}

	/*
	 * @seem TokenContext.hasChildContext
	 */
	public boolean hasChildContext()
	{
		if (childContextList == null)
			return false;
		return childContextList.size() > 0;
	}

	/*
	 * @seem TokenContext.getChildContexts
	 */
	@SuppressWarnings("unchecked")
	public Iterator<TokenContext> getChildContexts()
	{
		if (childContextList == null)
			return EmptyIterator.getInstance();
		return childContextList.iterator();
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
		markAsModified();
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
		markAsModified();
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
		markAsModified();
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
		markAsModified();
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
		markAsModified();

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
		markAsModified();
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
		markAsModified();
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
		this.nodeId = nodeId;;
		markAsModified();
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
		markAsModified();
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
		markAsModified();
	}

	/*
	 * @seem TokenContext.getProgressInfo
	 */
	public ProgressInfo getProgressInfo()
	{
		ProgressInfo progressInfo = new ProgressInfo();
		progressInfo.setProgressCount(toInt(progressCount));
		progressInfo.setProgressTotal(toInt(progressTotal));
		progressInfo.setProgressText(progressText);
		return progressInfo;
	}

	/*
	 * @seem TokenContext.setProgressInfo
	 */
	public void setProgressInfo(final ProgressInfo progressInfo)
	{
		if (progressInfo != null)
		{
			progressCount = progressInfo.getProgressCount();
			progressTotal = progressInfo.getProgressTotal();
			progressText = progressInfo.getProgressText();
		}
		else
		{
			progressCount = 0;
			progressTotal = 0;
			progressText = null;
		}
		markAsModified();
	}

	/**
	 * Gets the serialized context data.
	 * @nowarn
	 */
	public byte[] getContextData()
	{
		byte[] serializedContextData = (byte[]) readProperty("data");
		return serializedContextData;
	}

	/**
	 * Sets the serialized context data.
	 * @nowarn
	 */
	public void setContextData(final byte[] serializedContextData)
	{
		writeChangedProperty("data", serializedContextData);
		markAsModified();
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
		markAsModified();
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
	public Map<String, Object> getParamValues()
	{
		if (paramValues == null)
		{
			paramValues = Collections.synchronizedMap(new HashMap<String, Object>());
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
			markAsModified();
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
			markAsModified();
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
	public Iterator<String> getProcessVariableNames()
	{
		ArrayList<String> ret = new ArrayList<String>();
		for (Iterator<String> it = getParamValues().keySet().iterator(); it.hasNext();)
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
			String msg = LogUtil.error(getClass(), "Trying to access undefined process variable $0.", variableName);
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
	public Map<String, Object> getRuntimeAttributes()
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
			runtimeAttributes = new Hashtable<String, Object>();
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
	 * {@link EngineEvent} or {@link EngineTraceEvent}.
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
	// @@ O/R mapper support
	//////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	protected void readValuesFromCayenne()
	{
		String tempExecutingModelQualifier = (String) readProperty("executingModel");
		if (tempExecutingModelQualifier != null)
		{
			ModelQualifier qualifier = new ModelQualifier(tempExecutingModelQualifier);
			executingModel = getModelMgr().getModelByQualifier(qualifier);
		}
		else
		{
			executingModel = null;
		}

		String tempCurrentSocketQualifier = (String) readProperty("currentSocket");
		if (tempCurrentSocketQualifier != null)
		{
			ModelQualifier qualifier = new ModelQualifier(tempCurrentSocketQualifier);
			qualifier.setItemType(ItemTypes.PROCESS);
			setCurrentSocket(EngineUtil.determineNodeSocketFromQualifier(qualifier, getModelMgr()));
		}

		byte[] serializedContextData = (byte[]) readProperty("data");
		if (serializedContextData != null)
		{
			TokenContextUtil.fromByteArray(this, serializedContextData);
		}
		parentContext = (TokenContext) readProperty("parentContext");
		childContextList = (List<TokenContext>) readProperty("childContextList");
		lifecycleState = toInt(readProperty("lifecycleState"));
		lifecycleRequest = toInt(readProperty("lifecycleRequest"));
		priority = toInt(readProperty("priority"));
		queueType = (String) readProperty("queueType");
		nodeId = (String) readProperty("nodeId");
 		userId = (String) readProperty("userId");
		debuggerId = (String) readProperty("debuggerId");
		progressCount = toInt(readProperty("progressCount"));
		progressTotal = toInt(readProperty("progressTotal"));
		progressText = (String) readProperty("progressText");
	}

	protected void writeValuesToCayenne()
	{
		// TODO Fix version checking with Cayenne
		setVersion(Integer.valueOf(1));

		byte[] serializedContextData = TokenContextUtil.toByteArray(this);
		writeChangedProperty("data", serializedContextData);

		String socketQualifier = null;
		if (getCurrentSocket() != null)
		{
			socketQualifier = getCurrentSocket().getQualifier().toString();
		}
		writeChangedProperty("currentSocket", socketQualifier);

		String modelQualifier = null;
		if (getExecutingModel() != null)
		{
			modelQualifier = getExecutingModel().getQualifier().toString();
		}
		writeChangedProperty("executingModel", modelQualifier);

		writeChangedProperty("parentContext", parentContext);
		for(TokenContext tc: addedContextList)
		{
			addToManyTarget("childContextList", (CayenneTokenContextImpl) tc, true);
		}
		addedContextList.clear();
		for(TokenContext tc: removedContextList)
		{
			removeToManyTarget("childContextList", (CayenneTokenContextImpl) tc, true);
		}
		removedContextList.clear();
		writeChangedProperty("lifecycleState", lifecycleState);
		writeChangedProperty("lifecycleRequest", lifecycleRequest);
		writeChangedProperty("priority", priority);
		writeChangedProperty("queuetype", queueType);
		writeChangedProperty("nodeId", nodeId);
		writeChangedProperty("userId", userId);
		writeChangedProperty("debuggerId", debuggerId);
		writeChangedProperty("progressCount", progressCount);
		writeChangedProperty("progressTotal", progressTotal);
		writeChangedProperty("progressText", progressText);
	}

	/**
	 * Marks the token context as modified.
	 * Used internally only.
	 */
	protected void markAsModified()
	{
		int state = getPersistenceState();
		if (state == PersistenceState.COMMITTED)
			setPersistenceState(PersistenceState.MODIFIED);
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

	/*
	 * @see org.apache.cayenne.CayenneDataObject#setPersistenceState(int)
	 */
	@Override
	public void setPersistenceState(final int persistenceState)
	{
		super.setPersistenceState(persistenceState);
		if(persistenceState == PersistenceState.HOLLOW)
		{
			addedContextList.clear();
			removedContextList.clear();
		}
	}
}
