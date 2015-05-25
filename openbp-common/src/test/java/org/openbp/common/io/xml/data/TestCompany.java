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
package org.openbp.common.io.xml.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.util.iterator.EmptyIterator;

/**
 * Test class for the XML driver.
 *
 * @author Heiko Erhardt
 */
public class TestCompany
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Name */
	protected String name;

	/** City */
	protected String city;

	/** Phone */
	protected String phone;

	/** NrEmployees */
	protected int nrEmployees;

	/** Revenue */
	protected double revenue;

	/** Main product */
	protected TestProduct mainProduct;

	/** Product list */
	private List productList;

	/** Special product */
	protected TestProductExtended specialProduct;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	public TestCompany()
	{
	}

	//////////////////////////////////////////////////
	// @@ Attributes
	//////////////////////////////////////////////////

	/**
	 * Gets the name.
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 * @nowarn
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the city.
	 * @nowarn
	 */
	public String getCity()
	{
		return city;
	}

	/**
	 * Sets the city.
	 * @nowarn
	 */
	public void setCity(String city)
	{
		this.city = city;
	}

	/**
	 * Gets the phone.
	 * @nowarn
	 */
	public String getPhone()
	{
		return phone;
	}

	/**
	 * Sets the phone.
	 * @nowarn
	 */
	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	/**
	 * Gets the nrEmployees.
	 * @nowarn
	 */
	public int getNrEmployees()
	{
		return nrEmployees;
	}

	/**
	 * Sets the nrEmployees.
	 * @nowarn
	 */
	public void setNrEmployees(int nrEmployees)
	{
		this.nrEmployees = nrEmployees;
	}

	/**
	 * Sets the nrEmployees.
	 * @nowarn
	 */
	public void setNrEmployees(Integer nrEmployees)
	{
		this.nrEmployees = nrEmployees.intValue();
	}

	/**
	 * Gets the revenue.
	 * @nowarn
	 */
	public double getRevenue()
	{
		return revenue;
	}

	/**
	 * Sets the revenue.
	 * @nowarn
	 */
	public void setRevenue(double revenue)
	{
		this.revenue = revenue;
	}

	/**
	 * Gets the main product.
	 * @nowarn
	 */
	public TestProduct getMainProduct()
	{
		return mainProduct;
	}

	/**
	 * Sets the main product.
	 * @nowarn
	 */
	public void setMainProduct(TestProduct mainProduct)
	{
		this.mainProduct = mainProduct;
	}

	/**
	 * Gets the product list.
	 * @nowarn
	 */
	public List getProductList()
	{
		return productList;
	}

	/**
	 * Gets the product list.
	 * @return An iterator of {@link TestProduct} objects
	 */
	public Iterator getProducts()
	{
		if (productList == null)
			return EmptyIterator.getInstance();
		return productList.iterator();
	}

	/**
	 * Adds a product to the product list.
	 * @nowarn
	 */
	public void addProduct(ITestProduct product)
	{
		if (productList == null)
			productList = new ArrayList();
		productList.add(product);
	}

	/**
	 * Gets the special product.
	 * @nowarn
	 */
	public TestProductExtended getSpecialProduct()
	{
		return specialProduct;
	}

	/**
	 * Sets the special product.
	 * @nowarn
	 */
	public void setSpecialProduct(TestProductExtended specialProduct)
	{
		this.specialProduct = specialProduct;
	}
}
