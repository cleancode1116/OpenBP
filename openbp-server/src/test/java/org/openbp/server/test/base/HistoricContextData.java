package org.openbp.server.test.base;

import org.openbp.server.context.TokenContextUtil;

/**
 * This class holds a deserialized token context data as returned by {@link TokenContextUtil#toByteArray(org.openbp.server.context.TokenContext)}.
 *
 * @author Heiko Erhardt
 */
public class HistoricContextData
{
	/** Version identifier */
	private String version;

	/** Base64-encoded token context data */
	private String base64ContextData;

	/**
	 * Default constructor.
	 */
	public HistoricContextData()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param version Version identifier
	 * @param base64ContextData Base64-encoded token context data
	 */
	public HistoricContextData(String version, String base64ContextData)
	{
		this.version = version;
		this.base64ContextData = base64ContextData;
	}

	/**
	 * Gets the version identifier.
	 * @nowarn
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * Sets the version identifier.
	 * @nowarn
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}

	/**
	 * Gets the base64-encoded token context data.
	 * @nowarn
	 */
	public String getBase64ContextData()
	{
		return base64ContextData;
	}

	/**
	 * Sets the base64-encoded token context data.
	 * @nowarn
	 */
	public void setBase64ContextData(String base64ContextData)
	{
		this.base64ContextData = base64ContextData;
	}
}
