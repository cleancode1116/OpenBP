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
package org.openbp.common.io;

/**
 * Interface to be used by methods that want to query for a retry.
 *
 * @author Heiko Erhardt
 */
public interface RetryNotifier
{
	/**
	 * This method is called when an error appears and there is the chance of a retry.
	 *
	 * @param e Exception that causes the retry or null
	 * @param fileName Specifiction of the file name the exception refers to or null
	 * @return
	 *		true	Yes, the calling method should retry the procedure.<br>
	 *		false	No retry is desired.
	 */
	public boolean shallRetry(Exception e, String fileName);
}
