package com.mycompany.sample.absence;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.openbp.common.application.Application;
import org.openbp.server.ProcessFacade;
import org.openbp.server.ProcessServer;
import org.openbp.server.ProcessServerFactory;
import org.openbp.server.context.TokenContext;

/**
 * Test case that ensures restartability of the OpenBP server.
 * Starts and stops the server module twice.
 *
 * @author Heiko Erhardt
 */
public class AbsenceSampleTestCase extends TestCase
{
	/** Process server */
	private static ProcessServer processServer;

	public AbsenceSampleTestCase()
	{
	}

	public void testMain()
	throws Exception
	{
		performTest();
	}

	public void performTest()
		throws Exception
	{
		Application.setArguments(null);

		// Create a process server from the supplied Spring confiugration
		processServer = new ProcessServerFactory().createProcessServer("OpenBP-Server-Hibernate.spring.xml");
		ProcessFacade processFacade = processServer.getProcessFacade();

		// Create the process context and start the process
		TokenContext token = processFacade.createToken();
		token.setDebuggerId("Deb1");
		Map<String, Object> inputParam = new HashMap<String, Object>();
		inputParam.put("Ident", "test");
		processFacade.startToken(token, "/VMSNG/Sell to inventory Technical.Start", inputParam);

		// Note: This should be done by some worker thread; for the test case, we do it right here.
		processFacade.executePendingContextsInThisThread();

		processServer.shutdown(true);
	}
}
