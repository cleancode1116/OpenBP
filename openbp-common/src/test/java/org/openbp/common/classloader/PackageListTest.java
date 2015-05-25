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
package org.openbp.common.classloader;

import junit.framework.TestCase;

/**
 * Package list test case.
 */
public class PackageListTest extends TestCase
{
	public PackageListTest(String arg0)
	{
		super(arg0);
	}

	public void testPackageList()
	{
		PackageList standardPackages = new PackageList();
		PackageList nonStandardPackages = new PackageList();

		standardPackages.addPackage("java.*");
		standardPackages.addPackage("javax.*");
		standardPackages.addPackage("sun.*");
		standardPackages.addPackage("com.sun.*");

		assertTrue(standardPackages.containsClass("java.lang.String"));
		assertFalse(nonStandardPackages.containsClass("java.lang.String"));

		assertTrue(standardPackages.containsPackage("java.lang"));
		assertFalse(nonStandardPackages.containsPackage("java.lang"));

		assertTrue(standardPackages.containsPackage("javax.servlet.jsp"));
		assertFalse(nonStandardPackages.containsPackage("javax.servlet.jsp"));

		assertTrue(standardPackages.containsClass("javax.servlet.jsp.JspWriter"));
		assertFalse(nonStandardPackages.containsClass("javax.servlet.jsp.JspWriter"));
	}
}
