package org.openbp.model.testcase.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.openbp.common.CommonUtil;
import org.openbp.common.io.IOUtil;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;

// {{*Custom imports*
// }}*Custom imports*

/**
 * Activity1.
 * Implementation of the Activity1 activity handler.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'Param'
 * 
 * Output sockets/parameter:
 *   Socket 'Out'
 *     Parameter 'Result'
 */
public class Activity1
	// {{*Custom extends*
	// }}*Custom extends*
	// {{*Custom interfaces*
	implements Handler
	// }}*Custom interfaces*
{
	/** Parameter Param */
	private static final String PARAM_PARAM = "Param";

	/** Parameter Result */
	private static final String PARAM_RESULT = "Result";

	// {{*Custom constants*
	// }}*Custom constants*

	// {{*Custom members*
	// Note: If you define member variables, consider the fact that the same handler instance may be executed
	// by multiple threads in parallel, so you have to make sure that your implementation is thread safe.
	// In general, member variables should be defined for global-like data only.
	// }}*Custom members*

	/**
	 * Executes the handler.
	 *
	 * @param hc Handler context that contains execution parameters
	 * @return true if the handler handled the event, false to apply the default handling to the event
	 * @throws Exception Any exception that may occur during execution of the handler will be
	 * propagated to an exception handler if defined or abort the process execution otherwise.
	 */
	public boolean execute(HandlerContext hc)
		throws Exception
	{
		// {{*Handler implementation*
		String param = (String) hc.getParam(PARAM_PARAM);

		String resourceName = "org/openbp/model/testcase/activity/activity1/Activity1Test.txt";
		ClassLoader cl = getClass().getClassLoader();

		// Test ClassLoader.getResourceAsStream(String name)
		String s1 = loadFromInputStream(cl.getResourceAsStream(resourceName));

		// System.out.println("Resource 1 = '" + s1 + "'");

		// Test ClassLoader.getResourceAsStream(String name)
		URL url = cl.getResource(resourceName);
		String s2 = loadFromInputStream(url.openStream());

		// System.out.println("Resource 2 = '" + s2 + "'");

		if (! CommonUtil.equalsNull(s1, s2))
		{
			throw new RuntimeException("Resource lookup by getResourceAsStream does not equal lookup by getResource ('" + s1 + "' vs. '" + s2 + "' in activity '" + getClass().getName() + "'.");
		}

		HashMap result = new HashMap();
		result.put("Implementation", s1);
		result.put("Param", param);
		result.put("ExecutingModel", hc.getTokenContext().getExecutingModel().getQualifier().toString());
		hc.setResult(PARAM_RESULT, result);
		return true;
		// }}*Handler implementation*
	}

	private String loadFromInputStream(InputStream is)
		throws IOException
	{
		String s = IOUtil.readTextFile(is);
		return s;
	}

	// {{*Custom methods*
	// }}*Custom methods*
}

