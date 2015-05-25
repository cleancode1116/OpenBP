package com.mycompany.sample.vacation;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
public class VacationSampleTestCase extends TestCase
{
	/** Process server */
	private static ProcessServer processServer;

	public VacationSampleTestCase()
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
		// Prepare the test object
		// This would make a NICE holiday! :-)
		VacationData data = new VacationData();
		data.setSubmitterName("Peter");
		data.setSubmitterEmail("peter@openbp.org");
		data.setState(VacationData.STATE_ACCEPTED);
		data.setReason("Relaxing...");
		data.setFromDate(new Date(new GregorianCalendar(2011, Calendar.JANUARY, 1).getTimeInMillis()));
		data.setFromDate(new Date(new GregorianCalendar(2011, Calendar.DECEMBER, 31).getTimeInMillis()));

		Application.setArguments(null);

		// Create a process server from the supplied Spring confiugration
		processServer = new ProcessServerFactory().createProcessServer("OpenBP-Server-Sample.spring.xml");
		ProcessFacade processFacade = processServer.getProcessFacade();

		// Create the process context and start the process
		TokenContext token = processFacade.createToken();
		token.setDebuggerId("Deb1");
		Map<String, Object> inputParam = new HashMap<String, Object>();
		inputParam.put("Data", data);
		processFacade.startToken(token, "/VacationRequest/HandleVacationRequest.Start", inputParam);

		// Note: This should be done by some worker thread; for the test case, we do it right here.
		processFacade.executePendingContextsInThisThread();

		Map<String, Object> outputParam = new HashMap<String, Object>(); 
		processFacade.retrieveOutputParameters(token, outputParam);

		String errMsg = (String) outputParam.get("ErrMsg");
		if (errMsg != null)
		{
			System.err.println("Error occured in process: " + errMsg);
			processServer.shutdown(true);
			System.exit(1);
		}

		processServer.shutdown(true);
	}
}
