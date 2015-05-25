/*
 *   Copyright 2009 skynamics AG
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility classes for the XClassLoader package.
 *
 * @author Heiko Erhardt
 */
public final class XClassLoaderUtil
{
	/**
	 * Private constructor prevents instantiation.
	 */
	private XClassLoaderUtil()
	{
	}

	/**
	 * Reads the resource from the repository.
	 *
	 * @param file File to read
	 * @return Bytes that define the content of the entry (i. e. the class code)
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	public static byte [] readFileContent(File file)
		throws Exception
	{
		int size = (int) file.length();
		byte [] buffer = new byte [size];

		BufferedInputStream in = null;
		try
		{
			in = new BufferedInputStream(new FileInputStream(file), 4096);

			int pos = 0;
			while (pos < size)
			{
				pos += in.read(buffer, pos, size - pos);
			}
		}
		catch(IOException ex)
		{
			throw new Exception("Error reading file '" + file.getAbsolutePath() + "'.", ex);
		}
		finally
		{
			if (in != null)
			{
				in.close();
			}
		}

		return buffer;
	}

	/**
	 * Reads the zip file from the given input stream, adding all of its contents that are java class files
	 * to the entry table.
	 *
	 * @param cl The class loader
	 * @param in Input stream to the jar file
	 * @param jarFileUrl URL of the Jar file
	 * @throws Exception On any error that occurs while scanning the repositories specified in the class loader configuration
	 */
	public static void scanJarStream(XClassLoader cl, InputStream in, URL jarFileUrl)
		throws Exception
	{
		ZipInputStream zis = null;
		try
		{
			zis = new ZipInputStream(new BufferedInputStream(in));
			ZipEntry ze;

			// Read each entry from the ZipInputStream until no more entry found
			while ((ze = zis.getNextEntry()) != null)
			{
				String resourceName = ze.getName();
				if (cl.getResourceEntry(resourceName) != null)
				{
					// This entry is already defined, ignore
					continue;
				}

				int size = (int) ze.getSize();
				if (size == 0)
					continue;

				ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
				byte[] buffer = new byte[4096];
				int nRead;
				while ((nRead = zis.read(buffer)) > 0)
				{
					bos.write(buffer, 0, nRead);
				}
				byte [] content = bos.toByteArray();

				// Store the resource name of the zip entry in the table
				URL jarEntryUrl = buildJarEntryUrl(jarFileUrl, resourceName);
				ResourceEntry entry = new ResourceEntry(jarEntryUrl, content);
				cl.addResourceEntry(resourceName, entry);

				// For class files, store a mapping of the class name
				addResourceEntryForClass(cl, resourceName, entry);
			}
		}
		finally
		{
			if (zis != null)
			{
				zis.close();
			}
		}
	}

	/**
	 * Produces a jar URL from a given jar file and entry name.
	 *
	 * @param jarFileUrl The jar file
	 * @param entryName Name of the entry, e. g. "java/net/URL.class"
	 * @return A jar URL of the form jar:file:///path/to/jarfile.jar!/resourcename.
	 */
	private static URL buildJarEntryUrl(URL jarFileUrl, String entryName)
		throws MalformedURLException
	{
		// Append jar: protocol.
		StringBuffer result = new StringBuffer("jar:");

		// Append file URL to the archive file.
		result.append(jarFileUrl);

		// Append seperator.
		result.append("!/");

		// Adjust resource name and append it.
		result.append(entryName);

		// Return the result.
		return new URL(result.toString());
	}

	/**
	 * Adds the given resource entry as class mapping of the entry specifies a class file.
	 * Does nothing if the entry does not denote a class file.
	 *
	 * @param cl The class loader
	 * @param resourceName Resource name (relative path name or zip entry name)
	 * @param entry Entry to add
	 */
	public static void addResourceEntryForClass(XClassLoader cl, String resourceName, ResourceEntry entry)
	{
		// For class files, store a mapping of the class name
		int index;
		if ((index = resourceName.indexOf(".class")) >= 0)
		{
			String className = resourceName.substring(0, index);
			className = className.replace('/', '.');
			className = className.replace('\\', '.');
			cl.addResourceEntry(className, entry);
		}
	}
}
