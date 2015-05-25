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
package org.openbp.server.test;

import java.util.Arrays;

import org.openbp.common.application.ProductProfile;
import org.openbp.common.string.base64.Base64;
import org.openbp.server.context.TokenContext;
import org.openbp.server.test.base.HistoricContextData;
import org.openbp.server.test.base.SimpleDatabaseTestCaseBase;

/**
 * Workflow test that suspends and resumes a workflow that contains a complex parameter that is managed by Hibernate.
 *
 * @author Heiko Erhardt
 */
public class SubProcessTest extends SimpleDatabaseTestCaseBase
{
	public SubProcessTest()
	{
		setStartRef("/TestCase/SubProcessTest.Start");
	}

	public void performTest()
		throws Exception
	{
		// First, start the process anew and directly resume it using the newly generated
		// token context data in order to make sure that the process as such still works.
		Object tokenId = startToken();
		printToken(tokenId);
		resumeToken(tokenId);

		// Now, for each recorded historic token context data, start the process and
		// patch the historic context data into the token context before resuming the process
		for (int i = 0; i < historicContextData.length; ++i)
		{
			try
			{
				tokenId = startToken();
				TokenContext token = getProcessFacade().getTokenById(tokenId);

				String base64ContextData = historicContextData[i].getBase64ContextData();
				byte[] contextData = Base64.decode(base64ContextData);
				token.setContextData(contextData);
				token.onLoad();

				resumeToken(tokenId);
			}
			catch (Exception e)
			{
				throw new Exception("Error while testing compatiblity to version '" + historicContextData[i].getVersion() + "'.", e);
			}
		}
	}

	private Object startToken()
	{
		TokenContext token = createToken();

		getProcessFacade().startToken(token, getStartRef(), null);
		Object tokenId = token.getId();

		getProcessFacade().executePendingContextsInThisThread();

		return tokenId;
	}

	private void resumeToken(Object tokenId)
	{
		TokenContext token = getProcessFacade().getTokenById(tokenId);
		getProcessFacade().resumeToken(token, "Resumption", null);

		getProcessFacade().executePendingContextsInThisThread();
	}

	private void printToken(Object tokenId)
	{
		TokenContext token = getProcessFacade().getTokenById(tokenId);

		token.beforeSave();
		byte[] data = token.getContextData();

        String base64Data = Base64.encodeBytes(data, Base64.DONT_BREAK_LINES);

        byte[] encodedData = Base64.decode(base64Data);
        
		if (! Arrays.equals(data, encodedData))
		{
			throw new RuntimeException("Base64 encoding error.");
		}

		ProductProfile profile = getProcessServer().getProductProfile();
		String versionStr = profile.getVersion() + "-" + profile.getBuildNumber();
		System.out.println("\t\tnew HistoricContextData(\"" + versionStr + "\", \"" + base64Data + "\"),");
	}

	private HistoricContextData[] historicContextData =
	{
		new HistoricContextData("0.9.8-9044", "rO0ABXNyACdvcmcub3BlbmJwLnNlcnZlci5jb250ZXh0LkNhbGxTdGFja0ltcGwIaF4Br/BbHgIAAUwACnN0YWNrSXRlbXN0ABJMamF2YS91dGlsL1ZlY3Rvcjt4cHNyABBqYXZhLnV0aWwuVmVjdG9y2Zd9W4A7rwEDAANJABFjYXBhY2l0eUluY3JlbWVudEkADGVsZW1lbnRDb3VudFsAC2VsZW1lbnREYXRhdAATW0xqYXZhL2xhbmcvT2JqZWN0O3hwAAAAAAAAAAF1cgATW0xqYXZhLmxhbmcuT2JqZWN0O5DOWJ8QcylsAgAAeHAAAAAKc3IAMW9yZy5vcGVuYnAuc2VydmVyLmNvbnRleHQuQ2FsbFN0YWNrSW1wbCRTdGFja0l0ZW1rZbBSMv6R9QMAAkkABWNhdXNlSQAEdHlwZXhwAAAAAAAAAAF0AC4vVGVzdENhc2UvU3ViUHJvY2Vzc1Rlc3QuU3ViUHJvY2Vzc1Rlc3RTdWIxLklueHBwcHBwcHBwcHh0AApfTnVtYmVyT3V0dABNb3JnLm9wZW5icC5zZXJ2ZXIuY29udGV4dC5zZXJpYWxpemVyLkphdmFTZXJpYWxpemF0aW9uQ29udGV4dE9iamVjdFNlcmlhbGl6ZXJzcgARamF2YS5sYW5nLkludGVnZXIS4qCk94GHOAIAAUkABXZhbHVleHIAEGphdmEubGFuZy5OdW1iZXKGrJUdC5TgiwIAAHhwAAAAZHQAB19QVk1haW5xAH4ADHNxAH4ADQAAAGR0AAZfUFZTdWJxAH4ADHNxAH4ADQAAAGVwcHBwcHBwcHBwcA=="),
		new HistoricContextData("0.9.8-9046", "rO0ABXNyAA5qYXZhLmxhbmcuTG9uZzuL5JDMjyPfAgABSgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHAF4Iog6OFOHHNyACdvcmcub3BlbmJwLnNlcnZlci5jb250ZXh0LkNhbGxTdGFja0ltcGwIaF4Br/BbHgIAAUwACnN0YWNrSXRlbXN0ABJMamF2YS91dGlsL1ZlY3Rvcjt4cHNyABBqYXZhLnV0aWwuVmVjdG9y2Zd9W4A7rwEDAANJABFjYXBhY2l0eUluY3JlbWVudEkADGVsZW1lbnRDb3VudFsAC2VsZW1lbnREYXRhdAATW0xqYXZhL2xhbmcvT2JqZWN0O3hwAAAAAAAAAAF1cgATW0xqYXZhLmxhbmcuT2JqZWN0O5DOWJ8QcylsAgAAeHAAAAAKc3IAMW9yZy5vcGVuYnAuc2VydmVyLmNvbnRleHQuQ2FsbFN0YWNrSW1wbCRTdGFja0l0ZW1rZbBSMv6R9QMAAkkABWNhdXNlSQAEdHlwZXhwAAAAAAAAAAF0AC4vVGVzdENhc2UvU3ViUHJvY2Vzc1Rlc3QuU3ViUHJvY2Vzc1Rlc3RTdWIxLklueHBwcHBwcHBwcHh0AApfTnVtYmVyT3V0dABNb3JnLm9wZW5icC5zZXJ2ZXIuY29udGV4dC5zZXJpYWxpemVyLkphdmFTZXJpYWxpemF0aW9uQ29udGV4dE9iamVjdFNlcmlhbGl6ZXJzcgARamF2YS5sYW5nLkludGVnZXIS4qCk94GHOAIAAUkABXZhbHVleHEAfgABAAAAZHQABl9QVlN1YnEAfgAPc3EAfgAQAAAAZXQAB19QVk1haW5xAH4AD3NxAH4AEAAAAGRwcHBwcHBwcHBwcA=="),
	};
}
