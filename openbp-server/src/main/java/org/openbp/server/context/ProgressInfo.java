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
package org.openbp.server.context;

import java.io.Serializable;

/**
 * Value object that holds activity progress information.
 *
 * @author Heiko Erhardt
 */
public class ProgressInfo
	implements Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;

	/** Current progress count */
	private int progressCount;

	/** Progress total count */
	private int progressTotal;

	/** Progress text */
	private String progressText;

	/**
	 * Default constructor.
	 */
	public ProgressInfo()
	{
	}

	/**
	 * Creates a clone of this object.
	 * @return The base method return value
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		return super.clone();
	}

	/**
	 * Increases the current progress count by 1.
	 */
	public void step()
	{
		++progressCount;
	}

	/**
	 * Resets all fields of this value object.
	 */
	public void reset()
	{
		progressCount = progressTotal = 0;
		progressText = null;
	}

	/**
	 * Gets the current progress count.
	 * @nowarn
	 */
	public int getProgressCount()
	{
		return progressCount;
	}

	/**
	 * Sets the current progress count.
	 * @nowarn
	 */
	public void setProgressCount(int progressCount)
	{
		this.progressCount = progressCount;
	}

	/**
	 * Gets the progress total count.
	 * @nowarn
	 */
	public int getProgressTotal()
	{
		return progressTotal;
	}

	/**
	 * Sets the progress total count.
	 * @nowarn
	 */
	public void setProgressTotal(int progressTotal)
	{
		this.progressTotal = progressTotal;
	}

	/**
	 * Gets the progress text.
	 * @nowarn
	 */
	public String getProgressText()
	{
		return progressText;
	}

	/**
	 * Sets the progress text.
	 * @nowarn
	 */
	public void setProgressText(String progressText)
	{
		this.progressText = progressText;
	}
}
