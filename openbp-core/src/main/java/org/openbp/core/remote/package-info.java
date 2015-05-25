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
 * This package deals with services of the OpenBP server to the outside and sessions of clients (e. g. the cockpit) with the OpenBP server.
 *
 * A client may invoke a service at the server using an RMI connection.
 * Services are managed by an instance of the ServiceRegistry that can be used to lookup the service.
 * For particular documentation on how to use services at the client side, see the org.openbp.guiclient.remote package.
 *
 * Some services can be called without the need for client identification, other require an authentication
 * of the client and a ClientSession to be maintained between client and server.\n
 * A client session is created by the ClientSessionService based on ClientLoginInfo
 * and is usually passed as arguments to methods of other services.
 * A session has an expiration time after which it will be considered invalid. In this case,
 * service methods that require a session will throw an InvalidSessionException.
 *
 * For a closer understanding of service implementations, also see the org.openbp.server.remote package.
 */
package org.openbp.core.remote;
