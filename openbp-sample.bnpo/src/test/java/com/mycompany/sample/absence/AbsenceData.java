package com.mycompany.sample.absence;

import java.sql.Date;

public class AbsenceData
{
	public static final int STATE_NEW = 1;
	public static final int STATE_ACCEPTED = 2;
	public static final int STATE_REJECTED = 3;

	private Integer id;
	private String submitterName;
	private String submitterEmail;
	private int state;
	private String reason;
	private Date fromDate;
	private Date toDate;

	public Integer getId()
	{
		return id;
	}
	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getSubmitterName()
	{
		return submitterName;
	}
	public void setSubmitterName(String submitterName)
	{
		this.submitterName = submitterName;
	}

	public String getSubmitterEmail()
	{
		return submitterEmail;
	}
	public void setSubmitterEmail(String submitterEmail)
	{
		this.submitterEmail = submitterEmail;
	}

	public int getState()
	{
		return state;
	}
	public void setState(int state)
	{
		this.state = state;
	}

	public String getReason()
	{
		return reason;
	}
	public void setReason(String reason)
	{
		this.reason = reason;
	}

	public Date getFromDate()
	{
		return fromDate;
	}
	public void setFromDate(Date fromDate)
	{
		this.fromDate = fromDate;
	}

	public Date getToDate()
	{
		return toDate;
	}
	public void setToDate(Date toDate)
	{
		this.toDate = toDate;
	}
}
