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
package org.openbp.server.context.serializer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import org.openbp.server.context.TokenContext;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Context object serializer class that supports XML serialization via the XStream library.
 *
 * @author Heiko Erhardt
 */
public class XStreamContextObjectSerializer
	implements ContextObjectSerializer
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** XStream instance */
	private XStream xStream;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public XStreamContextObjectSerializer()
	{
		initialize();
	}

	//////////////////////////////////////////////////
	// @@ ContextObjectSerializer implementation
	//////////////////////////////////////////////////

	/**
	 * @return Always true, XStream can handle most classes
	 * @see org.openbp.server.context.serializer.ContextObjectSerializer#acceptsContextObject(Object object, TokenContext context)
	 */
	public boolean acceptsContextObject(Object object, TokenContext context)
	{
		// TODONOW Turned off for demo
		/*
		 initialize ();
		 return true;
		 */
		return false;
	}

	/**
	 * @see org.openbp.server.context.serializer.ContextObjectSerializer#writeContextObject(Object object, ObjectOutputStream out, TokenContext context, String key)
	 */
	public void writeContextObject(Object object, ObjectOutputStream out, TokenContext context, String key)
		throws IOException
	{
		OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");

		/* DEBUG
		 String x = xStream.toXML (object);
		 System.out.println (x);
		 */

		xStream.toXML(object, writer);
	}

	/**
	 * @see org.openbp.server.context.serializer.ContextObjectSerializer#readContextObject(ObjectInputStream in, TokenContext context, String key)
	 */
	public Object readContextObject(ObjectInputStream in, TokenContext context, String key)
		throws IOException, ClassNotFoundException
	{
		InputStreamReader reader = new InputStreamReader(in);

		Object object = xStream.fromXML(reader);

		return object;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	private void initialize()
	{
		if (xStream == null)
		{
			xStream = new XStream(new DomDriver());
		}
	}
}
