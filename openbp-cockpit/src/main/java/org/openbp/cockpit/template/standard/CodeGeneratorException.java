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
package org.openbp.cockpit.template.standard;

/**
 * Exception that indicates a semantic errror in the execution flow of a method.
 * This will cause the source code generation of the method to be cancelled.
 *
 * @author Heiko Erhardt
 */
public class CodeGeneratorException extends RuntimeException
{
	/**
	 * Default constructor.
	 */
	public CodeGeneratorException()
	{
	}
}
