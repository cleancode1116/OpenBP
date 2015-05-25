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
package org.openbp.core.handler;

/**
 * This marker interface indicates that this handler is thread-safe.
 * This means that the handler will be instantiated once only at application startup.
 * When the handler is being executed, multiple threads will share a common instance of the handler class.
 * Thread-safe handlers must not have member variables that relate to a particular handler execution.
 * Only configuration data that is initialized upon instantiation of the handler is allowed as
 * member data.
 *
 * @author Author: Heiko Erhardt
 */
public interface ThreadSafeHandler
{
}
