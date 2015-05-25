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

import org.openbp.common.dump.Dumper;
import org.openbp.common.generic.description.DescriptionObjectImpl;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.io.xml.data.TestCompany;
import org.openbp.common.io.xml.data.TestProduct;
import org.openbp.common.io.xml.data.TestProductExtended;

/**
 * Test class for the XML driver.
 *
 * @author Heiko Erhardt
 */
public final class TestXMLDriver
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	private TestXMLDriver()
	{
	}

	//////////////////////////////////////////////////
	// @@ Main method
	//////////////////////////////////////////////////

	/** XML file name */
	public static final String FILENAME = "c:/Temp/TestCompany.xml";

	/**
	 * Main method for test.
	 * Serializes test objects to the file "c:/Temp/TestCompany.xml" and reads them back again.
	 * @param args Command line arguments
	 */
	public static void main(String [] args)
	{
		TestCompany tc = new TestCompany();
		tc.setName("Company1");
		tc.setCity("München");
		tc.setPhone(null);
		tc.setNrEmployees(8);
		tc.setRevenue(1000000.0);

		TestProduct tp;
		TestProductExtended tpx;

		tp = new TestProduct();
		tp.setName("ProdA");
		tp.setDisplayName("Product A");
		tp.setVersion("1.0");
		tc.setMainProduct(tp);

		tp = new TestProduct();
		tp.setName("ProdB");
		tp.setDisplayName("Product B");
		tp.setVersion("1.1");
		tc.addProduct(tp);

		tpx = new TestProductExtended();
		tpx.setName("ProdC");
		tpx.setDisplayName("Product C");
		tpx.setDescription("This is a really large comment about the Product C,\nNew line\nblabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, blabla, end");
		tpx.setVersion("0.1");
		tpx.setPrice(20000.0);
		tc.addProduct(tpx);

		tp = new TestProduct();
		tp.setName("ProdD");
		tp.setName("Product D");
		tp.setVersion("1.1");
		tc.addProduct(tp);

		tp = new TestProduct();
		tp.setName("ProdE");
		tp.setName("Product E");
		tp.setVersion("1.1");
		tc.addProduct(tp);

		tp = new TestProduct();
		tp.setName("ProdF");
		tp.setName("Product F");
		tp.setVersion("1.1");
		tc.addProduct(tp);

		tpx = new TestProductExtended();
		tp.setName("ProdG");
		tp.setName("Product G");
		tpx.setVersion("0.2");
		tpx.setPrice(50000.0);
		tc.setSpecialProduct(tpx);

		System.out.println("***** Serializing *****");
		Dumper dumper = new Dumper();
		dumper.dump(tc);

		XMLDriver driver = XMLDriver.getInstance();
		try
		{
			driver.loadMapping(DescriptionObjectImpl.class);
			driver.loadMapping(DisplayObjectImpl.class);
			driver.loadMapping(TestProduct.class);
			driver.loadMapping(TestProductExtended.class);
			driver.loadMapping(TestCompany.class);
		}
		catch (XMLDriverException pe)
		{
			System.err.println("Error loading mapping: ");
			System.err.println(pe);
			System.exit(1);
		}

		// String encoding = null;
		// String encoding = "ASCII";
		// String encoding = "UTF-8";
		// String encoding = "UTF-16";
		// String encoding = "ISO-8859-1";
		// driver.setEncoding (encoding);

		// Serialize
		try
		{
			driver.serialize(tc, FILENAME);
		}
		catch (XMLDriverException pe)
		{
			System.err.println("Error serializing test class: ");
			System.err.println(pe);
			System.exit(1);
		}

		// Deserialize
		try
		{
			TestCompany tcNew = (TestCompany) driver.deserializeFile(TestCompany.class, FILENAME);

			System.out.println("***** Deserializing *****");
			dumper = new Dumper();
			dumper.dump(tcNew);
		}
		catch (XMLDriverException pe)
		{
			System.err.println("Error deserializing test class: ");
			System.err.println(pe);
			System.exit(1);
		}

		System.exit(0);
	}
}
