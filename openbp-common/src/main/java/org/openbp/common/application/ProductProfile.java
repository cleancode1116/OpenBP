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
package org.openbp.common.application;

/**
 * The product profile contains all information about a product.
 *
 * @author Andreas Putz
 */
public interface ProductProfile
{
	/**
	 * Gets the short name of a company for internal use.
	 * @nowarn
	 */
	public String getShortCompanyName();

	/**
	 * Gets the full company name for display in the user interface.
	 * @nowarn
	 */
	public String getFullCompanyName();

	/**
	 * Gets the short name of a product for internal use.
	 * @nowarn
	 */
	public String getShortProductName();

	/**
	 * Gets the full name of a product.
	 * @nowarn
	 */
	public String getFullProductName();

	/**
	 * Gets the product version.
	 * @nowarn
	 */
	public String getVersion();

	/**
	 * Gets the product build number.
	 * @nowarn
	 */
	public String getBuildNumber();
}
