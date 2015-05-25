/*
 *   Copyright 2008 skynamics AG
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
package org.openbp.server.scheduler;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openbp.common.property.RuntimeAttributeContainer;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.server.context.TokenContext;
import org.quartz.Trigger;

/**
 * Descriptor class that defines a process job to be executed by the scheduler.
 *
 * If you do not supply a token context to the scheduler job using the {@link #setTokenContext} method,
 * the scheduler will create a new token context. You should supply a TokenContext in the following cases:<br>
 * <ul>
 * <li>If you want to track the context execution, you need to create the token in advance in order to save its id for further reference.</li>
 * <li>If you want to adjust the TokenContext priority or other TokenContext internals, you also need to create the token yourself.</li>
 * </ul>
 * In all other cases, you should let the scheduler create the TokenContext for you.
 * Especially when starting a new process with a Cron trigger, you may not supply a TokenContext
 * because one token can be executed only once. A Cron trigger requires to create a new token each time it fires.
 *
 * The ProcessJobDescriptor interface now extends the RuntimeAttribute interface.
 * This provides the ability to store arbitrary objects in a map.
 *
 * @author Heiko Erhardt
 */
public class ProcessJobDescriptor
	implements RuntimeAttributeContainer
{
	/** Start mode: Start process */
	public static final String START_MODE_START = "start";

	/** Start mode: Resume process */
	public static final String START_MODE_RESUME = "resume";

	/** Execution mode: Synchronous, immediate execution */
	public static final String EXECUTION_MODE_SYNCHRONOUS = "synchronous";

	/** Execution mode: Asynchronous execution using a thread from the execution pool thread */
	public static final String EXECUTION_MODE_ASYNCHRONOUS = "asynchronous";

	/** Job name */
	private String jobName;

	/** Job group */
	private String jobGroup;

	/** Token context */
	private TokenContext tokenContext;

	/** Trigger list (contains {@link Trigger} objects) */
	private List triggerList;

	/** Start or resumption position (reference to the start node/socket ("/Process/Node" or ".Socket")
	 * or name of an exit socket of the current node */
	private String positionRef;

	/** Initial start position */
	private String initialPositionRef;

	/** Table of input values to a new process (parameters of the start socket) */
	private Map inputValues;

	/** Runtime attribute table */
	private Map runtimeAttributes;

	/** Start mode ({@link #START_MODE_START}/{@link #START_MODE_RESUME}) */
	private String startMode;

	/** Execution mode ({@link #EXECUTION_MODE_SYNCHRONOUS}/{@link #EXECUTION_MODE_ASYNCHRONOUS}) */
	private String executionMode;

	/** Disabled flag */
	private boolean disabled;

	/**
	 * Default constructor.
	 */
	public ProcessJobDescriptor()
	{
	}

	/**
	 * Gets the job name.
	 * @nowarn
	 */
	public String getJobName()
	{
		return jobName;
	}

	/**
	 * Sets the job name.
	 * @nowarn
	 */
	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}

	/**
	 * Gets the job group.
	 * @nowarn
	 */
	public String getJobGroup()
	{
		return jobGroup;
	}

	/**
	 * Sets the job group.
	 * @nowarn
	 */
	public void setJobGroup(String jobGroup)
	{
		this.jobGroup = jobGroup;
	}

	/**
	 * Gets the token context.
	 * @nowarn
	 */
	public TokenContext getTokenContext()
	{
		return tokenContext;
	}

	/**
	 * Sets the token context.
	 * @nowarn
	 */
	public void setTokenContext(TokenContext tokenContext)
	{
		this.tokenContext = tokenContext;
	}

	/**
	 * Gets the trigger list.
	 * @return An iterator of Trigger objects
	 */
	public Iterator getTriggers()
	{
		if (triggerList == null)
			return EmptyIterator.getInstance();
		return triggerList.iterator();
	}

	/**
	 * Adds a trigger.
	 * @param trigger The trigger to add
	 */
	public void addTrigger(Trigger trigger)
	{
		if (triggerList == null)
			triggerList = new ArrayList();
		triggerList.add(trigger);
	}

	/**
	 * Clears the trigger list.
	 */
	public void clearTriggers()
	{
		triggerList = null;
	}

	/**
	 * Gets the trigger list.
	 * @return A list of Trigger objects
	 */
	public List getTriggerList()
	{
		return triggerList;
	}

	/**
	 * Sets the trigger list.
	 * @param triggerList A list of Trigger objects
	 */
	public void setTriggerList(List triggerList)
	{
		this.triggerList = triggerList;
	}

	/**
	 * Gets the start or resumption position.
	 * @return Reference to the start node/socket ("/Process/Node" or ".Socket") or name of an exit socket of the current node
	 */
	public String getPositionRef()
	{
		return positionRef;
	}

	/**
	 * Gets the start or resumption position.
	 * @param positionRef Reference to the start node/socket ("/Process/Node" or ".Socket") or name of an exit socket of the current node
	 */
	public void setPositionRef(String positionRef)
	{
		this.positionRef = positionRef;
	}

	/**
	 * Gets the initial start position.
	 * @return The start position or null if the job merely resumes an existing token
	 */
	public String getInitialPositionRef()
	{
		return initialPositionRef;
	}

	/**
	 * Sets the initial start position.
	 * @param initialPositionRef The start position or null if the job merely resumes an existing token
	 */
	public void setInitialPositionRef(String initialPositionRef)
	{
		this.initialPositionRef = initialPositionRef;
	}

	/**
	 * Gets the table of input values to a new process (parameters of the start socket).
	 * @nowarn
	 */
	public Map getInputValues()
	{
		return inputValues;
	}

	/**
	 * Sets the table of input values to a new process (parameters of the start socket).
	 * @nowarn
	 */
	public void setInputValues(Map inputValues)
	{
		this.inputValues = inputValues;
	}

	/**
	 * Gets the start mode ({@link #START_MODE_START}/{@link #START_MODE_RESUME}).
	 * @nowarn
	 */
	public String getStartMode()
	{
		return startMode;
	}

	/**
	 * Sets the start mode ({@link #START_MODE_START}/{@link #START_MODE_RESUME}).
	 * @nowarn
	 */
	public void setStartMode(String startMode)
	{
		this.startMode = startMode;
	}

	/**
	 * Gets the execution mode.
	 * @return {@link #EXECUTION_MODE_SYNCHRONOUS}/{@link #EXECUTION_MODE_ASYNCHRONOUS}
	 */
	public String getExecutionMode()
	{
		return executionMode;
	}

	/**
	 * Sets the execution mode
	 * @param executionMode {@link #EXECUTION_MODE_SYNCHRONOUS}/{@link #EXECUTION_MODE_ASYNCHRONOUS}
	 */
	public void setExecutionMode(String executionMode)
	{
		this.executionMode = executionMode;
	}

	/**
	 * Gets the disabled flag.
	 * @nowarn
	 */
	public boolean isDisabled()
	{
		return disabled;
	}

	/**
	 * Sets the disabled flag.
	 * @nowarn
	 */
	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	//////////////////////////////////////////////////
	// @@ RuntimeAttributeContainer implementation
	//////////////////////////////////////////////////

	/*
	 * Gets the runtime attribute map.
	 * @return A map holding string key-value definitions
	 */
	public Map getRuntimeAttributes()
	{
		return runtimeAttributes;
	}

	/*
	 * @seem RuntimeAttribute.getRuntimeAttribute
	 */
	public Object getRuntimeAttribute(final String key)
	{
		if (runtimeAttributes != null)
			return runtimeAttributes.get(key);
		return null;
	}

	/*
	 * @seem RuntimeAttribute.setRuntimeAttribute
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
	 * @seem RuntimeAttribute.removeRuntimeAttribute
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

}
