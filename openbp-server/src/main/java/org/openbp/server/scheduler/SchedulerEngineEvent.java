/*
 * Created on 01.11.2008
 *
 * Copyright (c) 2005 Giesecke & Devrient GmbH.
 * All rights reserved. Use is subject to licence terms.
 * 
 * Author: Heiko Erhardt (heiko.erhardt@gi-de.com)
 */
package org.openbp.server.scheduler;

import org.openbp.server.context.TokenContext;
import org.openbp.server.engine.Engine;
import org.openbp.server.engine.EngineEvent;

/**
 * Engine event object for scheduler events.
 *
 * @author Heiko Erhardt
 */
public class SchedulerEngineEvent extends EngineEvent
{
	/** Event type: A new process job has been started. */
	public static final String START_JOB = "startjob";

	/** Event type: A process job has been resumed. */
	public static final String RESUME_JOB = "resumejob";

	/** Table of all possible values */
	public static final String[] SUPPORTED_EVENT_TYPES =
	{
		START_JOB, RESUME_JOB,
	};

	/**
	 * Returns a list of supported event types.
	 * @nowarn
	 */
	public static String[] getSupportedEventTypes()
	{
		return SUPPORTED_EVENT_TYPES;
	}

	/** Process job descriptor */
	private ProcessJobDescriptor processJobDescriptor;

	/**
	 * Value constructor.
	 *
	 * @param eventType Event type
	 * @param context Token context
	 * @param processJobDescriptor Process job descriptor
	 * @param engine Engine
	 */
	public SchedulerEngineEvent(String eventType, TokenContext context, ProcessJobDescriptor processJobDescriptor, Engine engine)
	{
		super(eventType, context, engine);
		this.processJobDescriptor = processJobDescriptor;
	}

	/**
	 * Gets the process job descriptor.
	 * @nowarn
	 */
	public ProcessJobDescriptor getProcessJobDescriptor()
	{
		return processJobDescriptor;
	}

	/**
	 * Sets the process job descriptor.
	 * @nowarn
	 */
	public void setProcessJobDescriptor(ProcessJobDescriptor processJobDescriptor)
	{
		this.processJobDescriptor = processJobDescriptor;
	}
}
