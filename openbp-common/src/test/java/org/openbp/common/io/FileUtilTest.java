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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Files utilities test case.
 */
public class FileUtilTest extends TestCase
{
	/**
	 * Constructor for FileUtilTest.
	 * @param arg0 Argument
	 */
	public FileUtilTest(String arg0)
	{
		super(arg0);
	}

	protected List getDirectoryNames()
		throws IOException
	{
		List result = new ArrayList();

		addSubDirectoryNames(File.listRoots() [0], result, 10);

		return result;
	}

	protected void addSubDirectoryNames(File directory, List directoryNames, int limit)
		throws IOException
	{
		File [] files = directory.listFiles();

		for (int i = 0; i < Math.min(limit, files.length); i++)
		{
			if (files [i].isDirectory() && !directoryNames.contains(files [i].getCanonicalPath()))
			{
				directoryNames.add(files [i].getCanonicalPath());
				addSubDirectoryNames(files [i], directoryNames, limit - 1);
			}
		}
	}

	public void testGetRelativePath()
		throws Exception
	{
        // XXX
        if (true) return;

		List dirs = getDirectoryNames();

		for (int i = 0; i < 10 * dirs.size(); i++)
		{
			String dirName1 = (String) dirs.get(i % dirs.size());
			String dirName2 = (String) dirs.get((int) (dirs.size() * Math.random()));

			String relPath = FileUtil.getRelativePath(dirName1, dirName2);

			//System.out.println("From '"+dirName1+"' to '"+dirName2+"': '"+relPath+"'");

			File dir1 = new File(dirName1);
			File dir2 = new File(dir1, relPath);

			//System.out.println("Expected: "+dirName2);
			//System.out.println("Got:      "+dir2.getCanonicalPath ());
			assertEquals(dirName2, dir2.getCanonicalPath());
		}
	}

	public static void main(String [] args)
	{
		String from = "/home/falk/src/models/svcs/RevoMotion/model/RevoMotionEmergencyServer/visual/jsp";
		String to = "/home/falk/src/home/bin/./../server/model/System/visual/jsp";
		String rel = FileUtil.getRelativePath(from, to);
		rel = "/" + rel.replace('\\', '/');
		System.out.println("From:  " + from);
		System.out.println("To:    " + to);
		System.out.println("Rel:   " + rel);
	}
}
