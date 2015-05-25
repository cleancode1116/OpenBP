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
 * Session and token context object implementations.
 *
 * A session context contains all session-relevant, request-independent data
 * like the executing model, debugging support, trace id handling.\n
 * Session contexts can be interactive (i. e. associated with an HTTP session) or non-interactive
 * (batch contexts).
 *
 * A session context is associated with either a single or multiple token contexts.\n
 * The token context contains request-related data like parameter data, call stack, current position
 * in the process etc.
 *
 * There is a 1:1 relationship between session and token context for batch contexts
 * and for interactive session contexts that have request caching disabled.\n
 * Interactive context with request caching keep a table of all token contexts that refer
 * to a particular request state. A multi-request session context can look up an execution
 * context according to a trace id, which is passed as request parameter. As the application
 * advances, new trace ids are generated for each request, which will enable the session
 * context to retrieve the token context matching a particular request. This important
 * for the automatic 'browser back button' and 'open in new window' handling.
 */
package org.openbp.server.context;
