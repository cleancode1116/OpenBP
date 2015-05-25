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
package org.openbp.server.model.modelmgr.hibernate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;

import org.hibernate.Hibernate;
import org.openbp.core.model.item.Item;
import org.openbp.server.model.modelmgr.DbModelJarFile;

/**
 * Descriptor object that is used to persist a {@link Item} object from/to the database.
 *
 * @author Heiko Erhardt
 */
public class HibernateDbModelJarFile extends DbModelJarFile
{
	/** Byte code of the jar file */
	private byte[] byteCode;

	/**
	 * Default constructor.
	 */
	public HibernateDbModelJarFile()
	{
	}

	/**
	 * Gets the byte code of the jar file.
	 * @nowarn
	 */
	public byte[] getByteCode()
	{
		return byteCode;
	}

	/**
	 * Sets the byte code of the jar file.
	 * @nowarn
	 */
	public void setByteCode(byte[] byteCode)
	{
		this.byteCode = byteCode;
	}

	@SuppressWarnings("unused")
	private void setByteCodeBlob(Blob blob)
	{
		this.byteCode = toByteArray(blob);
	}

	@SuppressWarnings("unused")
	private Blob getByteCodeBlob()
	{
		return fromByteArray(this.byteCode);
	}

	private static Blob fromByteArray(byte[] bytes)
	{
		return Hibernate.createBlob(bytes);
	}

	private static byte[] toByteArray(Blob blob)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			byte buf[] = new byte[4000];
			int dataSize;
			InputStream is = blob.getBinaryStream();

			try
			{
				while((dataSize = is.read(buf)) != -1)
				{
					baos.write(buf, 0, dataSize);
				}
			}
			finally
			{
				if(is != null)
				{
					is.close();
				}
			}
			return baos.toByteArray();
		}
		catch (Exception e)
		{
			// Never happens
		}
		return null;
	}
}
