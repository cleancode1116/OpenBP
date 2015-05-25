/*
 * Created on 28.08.2008
 *
 * Copyright (c) 2005 Giesecke & Devrient GmbH.
 * All rights reserved. Use is subject to licence terms.
 */
package org.openbp.server.engine;

import org.openbp.server.context.TokenContext;

/**
 * Error handler interface for the engine runner.
 * Instances of this interface can be used to handle errors that occur when executing processes by the engine runner.
 * Attach to the {@link EngineRunner} using the {@link EngineRunner#setEngineRunnerExceptionHandler} method.
 */
public interface EngineRunnerExceptionHandler
{
	/**
	 * Handles an exception that occured during the process execution.
	 *
	 * @param context Token context
	 * @param throwable Exception or Error object
	 * @return true if the handler has handled the error, false for the default handling (= logging of the problem)
	 */
	public boolean handleException(TokenContext context, Throwable throwable);
}
