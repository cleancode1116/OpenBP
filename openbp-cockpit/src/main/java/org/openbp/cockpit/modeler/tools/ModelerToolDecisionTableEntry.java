package org.openbp.cockpit.modeler.tools;

/**
 * Entry in the tool decision table that determines which tool to use for which situation.
 *
 * @author Heiko Erhardt
 */
public class ModelerToolDecisionTableEntry
{
	/** Tool */
	private ModelerTool tool;

	/** Class of the object (usually figure or handle) this tool is suitable for or null for any */
	private Class objectClass;

	/** Expected keyboard and mouse button input state or 0 for any */
	private int requiredInputState;

	/**
	 * Value constructor.
	 *
	 * @param tool Tool
	 * @param objectClass Class of the object (usually figure or handle) this tool is suitable for or null for any
	 * @param requiredInputState Expected keyboard and mouse button input state or 0 for any
	 */
	public ModelerToolDecisionTableEntry(ModelerTool tool, Class objectClass, int requiredInputState)
	{
		this.tool = tool;
		this.objectClass = objectClass;
		this.requiredInputState = requiredInputState;
	}

	/**
	 * Gets the tool.
	 * @nowarn
	 */
	public ModelerTool getTool()
	{
		return tool;
	}

	/**
	 * Gets the class of the object (usually figure or handle) this tool is suitable for or null for any.
	 * @nowarn
	 */
	public Class getObjectClass()
	{
		return objectClass;
	}

	/**
	 * Gets the expected keyboard and mouse button input state.
	 * @nowarn
	 */
	public int getRequiredInputState()
	{
		return requiredInputState;
	}
}
