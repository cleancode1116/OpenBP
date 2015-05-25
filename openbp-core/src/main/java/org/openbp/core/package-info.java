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

/**
 * Classes and sub packages commonly used by both server and client.
 * These are definitions of model objects as well as general patterns, utilities etc.
 * Among these objects are all standard model items.
 *
 * @intro
 * This reference describes the interfaces, classes and methods of the OpenBP API.
 *
 * Note that the API is subject to change in future versions.\n
 * Since OpenBP is not in a version 1.0 state yet, incompatible API changes are likely to happen.
 *
 * <b>Package structure</b>
 *
 * The OpenBP API is structured in several layers that provide a clear separation of concerns and dependancies.
 *
 * The org.openbp.<b><i>common</i></b> package and its sub packages contain basic functionalities and
 * utilities that is actually independant from the OpenBP server.
 *
 * The org.openbp.<b><i>core</i></b> package and its sub packages is used by both the OpenBP server
 * and clients (e. g. the OpenBP Cockpit and process Modeler). It contains most interfaces and definitions for OpenBP
 * objects such as components and services.
 *
 * The org.openbp.<b><i>server</i></b> package and its sub packages form the actual OpenBP Server,
 * i. e. the process engine that executes the processes.
 *
 * The org.openbp.<b><i>scheduler</i></b> package contains an advanced scheduler that may be used to
 * schedule OpenBP processes as well as arbitrary tasks. The scheduler supports time-, interval- and
 * load-based task scheduling and is highly configurable and customizable.
 *
 * The org.openbp.<b><i>model.system</i></b> package and its sub packages contain the classes of the System model.
 *
 * Most implementation classes of the OpenBP server implement a corresponding interface.
 * In your application software, you should always use the interfaces rather than the concrete classes.
 *
 * <b>Invoking an OpenBP process</b>
 *
 * The easiest way to run OpenBP processes in your application, is to use the methods of the org.openbp.server.ProcessFacade
 * interface. You may retrieve an instance of the ProcessFacade by instantiating the OpenBP server.
 * The process facade will provide methods to start a process, pass parameters to it, resume the process
 * after a wait state node has been encountered and retrieve output parameters from the process.
 *
 * @code
	// Instantiate the process server
	{@link ProcessServer} processServer = new {@link ProcessServerFactory}().createProcessServer();
	{@link ProcessFacade} processFacade = processServer.getProcessFacade();

	// Fill the parameter map
	Map inputParams = new HashMap();
	...

	// Create a token and start the supplied process of the given model at the specified node
	{@link TokenContext} token = processFacade.createToken();
	processFacade.startToken(token, "/model/process.node", inputParams);

	// Make the process engine execute all pending contexts and wait until there
	// is all contexts have been executed.s have been executed.s have been executed.
	processFacade.executePendingContextsInThisThread();

	// We come here after the process has ended or hit a wait state

	// Resume the process at the given wait state exit socket
	processFacade.resumeToken(token, "resumeExitSocketName", null);

	// Continue all processes again
	processFacade.executePendingContextsInThisThread();

	// Collect the output parameters
	Map outputParams = new HashMap();
	processFacade.retrieveOutputParameters(token, outputParams);

 @code
 *
 * For a better understanding, also have a look at the {@link CommandLineSample} application and the various OpenBP test cases.
 *
 * <b>Implementation of custom handlers</b>
 *
 * The <b><i>org.openbp.server.handler</i></b> package and its sub packages contain many interfaces and classes
 * that you may use to customize your OpenBP processes on the code level.\n
 * I. e. the {@link Handler} interface defines how custom event handlers must look like.
 * You may generate handler implementations using the wizard of the OpenBP Cockpit.\n
 * The methods of the {@link HandlerContext} class provide access to process state and data from within the handler code.\n
 * For some hints on how to implement handlers, have a look at the source code of the activity handlers of the System model.
 */
package org.openbp.core;
