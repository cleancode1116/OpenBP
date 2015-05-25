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
package org.openbp.common.io.xml;

/**
 * Exception that indicates that a dependent classes ('extends' or 'depends'
 * attribute in the mapping file) could not be resolved.
 * The class extends from RuntimeException to prevent having to declare
 * the XClassMappingResolver.resolve method throw an exception, which
 * would make it incompatible to Castor's IDResolver class.<br>
 * It might contain the exception that has occurred inside the loading process.
 *
 * @author Heiko Erhardt
 */
public class XResolvingException extends RuntimeException
{
	/**
	 * Constructor.
	 * @param msg Exception message
	 */
	public XResolvingException(String msg)
	{
		super(msg);
	}

	/**
	 * Constructor.
	 * @param exception Nested exception
	 */
	public XResolvingException(Exception exception)
	{
		super(exception);
	}

	/**
	 * Constructor.
	 * @param msg Exception message
	 * @param exception Nested exception
	 */
	public XResolvingException(String msg, Exception exception)
	{
		super(msg, exception);
	}
}
